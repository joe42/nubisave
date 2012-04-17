package com.github.joe42.splitter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import jdbm.helper.FastIterator;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.github.joe42.splitter.util.LinuxUtil;
import com.github.joe42.splitter.vtf.Entry;
import com.github.joe42.splitter.vtf.FileEntry;
import com.github.joe42.splitter.vtf.FolderEntry;

import fuse.Filesystem3;
import fuse.FuseDirFiller;
import fuse.FuseException;
import fuse.FuseFtype;
import fuse.FuseGetattrSetter;
import fuse.FuseOpenSetter;
import fuse.FuseSizeSetter;
import fuse.FuseStatfs;
import fuse.FuseStatfsSetter;
import fuse.XattrLister;
import fuse.XattrSupport;
import fuse.compat.FuseStat;

public class FuseBox implements Filesystem3, XattrSupport {
	private static final Logger  log = Logger.getLogger("FuseBox");
	private static final int blockSize = 512;
	protected FileFragmentStore fileStore;
	protected FileMetaDataStore metaDataStore;
	protected int UID;
	protected int GID;

	public FuseBox(FilePartFragmentStore fileStore) throws IOException {
		PropertyConfigurator.configure("log4j.properties");
		
		metaDataStore = new FileMetaDataStore();
		this.fileStore = fileStore;
		UID = LinuxUtil.getUID();
		GID = LinuxUtil.getGID();
	}

	@Override
	public int chmod(String path, int mode) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	@Override
	public int chown(String path, int uid, int gid) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}
	@Override
	public int getattr(String path, FuseGetattrSetter getattrSetter) throws FuseException {
		FuseStat stat = new FuseStat();
		Entry entry = null;
		try {
			entry = metaDataStore.getEntry(path);
			if (entry instanceof FileEntry) {
				stat.mode = FuseFtype.TYPE_FILE | 0644;
				stat.size = fileStore.getSize(path);
			} else {
				stat.mode = FuseFtype.TYPE_DIR | 0755;
				stat.size = 0;
			}
		} catch (IOException e) {
			throw new FuseException("IO Exception on reading metadata")
					.initErrno(FuseException.EIO);
		}
		if (entry == null)
			throw new FuseException("No Such Entry")
					.initErrno(FuseException.ENOENT);
		stat.nlink = entry.nlink;
		stat.uid = entry.uid;
		stat.gid = entry.gid;
		stat.atime = entry.atime;
		stat.mtime = entry.mtime;
		stat.ctime = entry.ctime;
		stat.blocks = (int) ((stat.size + 511L) / 512L);
		getattrSetter.set(stat.inode, stat.mode, stat.nlink, stat.uid, stat.gid, 0, stat.size, stat.blocks, stat.atime, stat.mtime, stat.ctime);
		return 0;
	}

	@Override
	public int getdir(String path, FuseDirFiller dirFiller) throws FuseException {
		FastIterator paths;
		try {
			if (metaDataStore.getFolderEntry(path) == null)
				throw new FuseException("No Such Entry")
						.initErrno(FuseException.ENOENT);
			paths = metaDataStore.getFileEntryPaths();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		String fileName, dirName;
		while ((fileName = (String) paths.next()) != null) {
			if (fileName.startsWith(path)
					&& path.equals( new File(fileName).getParent() )) {
				dirFiller.add(new File(fileName).getName(), 0, FuseFtype.TYPE_FILE | 0644);
			}
		}
		try {
			paths = metaDataStore.getFolderEntryPaths();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		while ((dirName = (String) paths.next()) != null) {
			if (dirName.startsWith(path)
					&& path.equals( new File(dirName).getParent() )
					&& !dirName.equals(path)) {
				dirFiller.add(new File(dirName).getName(), 0, FuseFtype.TYPE_DIR | 0755);
			}
		}
		dirFiller.add(".", 0, FuseFtype.TYPE_DIR | 0755);
		dirFiller.add("..", 0, FuseFtype.TYPE_DIR | 0755);

		return 0;
	}

	@Override
	public int link(String from, String to) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	@Override
	public int mkdir(String path, int mode) throws FuseException {
		try {
			metaDataStore.makeFolderEntry(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		return 0;
	}

	@Override
	public int mknod(String path, int mode, int rdev) throws FuseException {
		FileEntry fileEntry;
		try {
			fileEntry = metaDataStore.makeFileEntry(path);
			fileEntry.uid = UID;
			fileEntry.gid = GID;
			metaDataStore.putFileEntry(path, fileEntry);
			fileStore.mknod(path);
		} catch (IOException e) {
			e.printStackTrace();
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		return rdev;
	}
	
	@Override
	public int open(String path, int flags, FuseOpenSetter openSetter)
			throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int rename(String from, String to) throws FuseException {
		if (from.equals(to)) 
			throw new FuseException("Entity"+to+" already exists.")
				.initErrno(FuseException.EEXIST);
		try {
			fileStore.rename(from, to);
			metaDataStore.rename(from, to);
		} catch (IOException e) {
			throw new FuseException("IO Exception on reading metadata")
					.initErrno(FuseException.EIO);
		}
		return 0;
	}

	@Override
	public int rmdir(String path) throws FuseException {
		Entry dirEntry = null;
		try {
			dirEntry = (FolderEntry) metaDataStore.getFolderEntry(path);
			if (dirEntry == null)
				throw new FuseException("No Such Entry")
						.initErrno(FuseException.ENOENT);
			new File(path).delete();
			metaDataStore.remove(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		return 0;
	}
	
	protected FuseStatfs statfs() throws FuseException {
		int files = 0;
		int dirs = 0;
		int blocks = 0;
		FastIterator iter;
		try {
			iter = metaDataStore.getFileEntryPaths();
			String path = (String) iter.next();
			while (path != null) {
				files++;
				blocks += (fileStore.getSize(path) + blockSize - 1)	/ blockSize;
				path = (String) iter.next();
			}
			iter = metaDataStore.getFolderEntryPaths();
			path = (String) iter.next();
			while (path != null) {
				dirs++;
				blocks += (0 + blockSize - 1)
						/ blockSize;
				path = (String) iter.next();
			}

		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		FuseStatfs statfs = new FuseStatfs();
		long freeBytes = fileStore.getFreeBytes();
		statfs.blocks = (int)((freeBytes+fileStore.getUsedBytes())/blockSize);
		statfs.blocksAvail = (int)(freeBytes/blockSize);
		statfs.blockSize = blockSize;
		statfs.blocksFree = (int)(freeBytes/blockSize);
		statfs.files = files + dirs;
		statfs.filesFree = 1000;
		statfs.namelen = 2048;

		log.debug(files + " files, " + dirs + " directories, " + blocks
				+ " blocks (" + blockSize + " byte/block).");
		return statfs;
	}
	
	@Override
	public int statfs(FuseStatfsSetter statfsSetter) throws FuseException {
		FuseStatfs statfs = statfs();
		log.debug(statfs.files + " files, " + statfs.blocks
				+ " blocks (" + statfs.blockSize + " byte/block).");
		statfsSetter.set(statfs.blockSize, statfs.blocks, statfs.blocksFree, statfs.blocksAvail, statfs.files, statfs.filesFree, statfs.namelen);
		return 0;
	}

	@Override
	public int symlink(String from, String to) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EOPNOTSUPP);
	}

	@Override
	public int truncate(String path, long size) throws FuseException {
		try {
			fileStore.flushCache(path);
			fileStore.truncate(path, size);
		} catch (IOException e) {
			throw new FuseException("IO Exception on truncating file")
					.initErrno(FuseException.EIO);
		}
		return 0;
	}

	@Override
	public int unlink(String path) throws FuseException {
		removeFile(path);
		return 0;
	}

	private void removeFile(String path) throws FuseException {
		try {
			fileStore.remove(path);
			metaDataStore.remove(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception on reading metadata")
					.initErrno(FuseException.EIO);
		}
	}

	@Override
	public int utime(String path, int atime, int mtime) throws FuseException {
		return 0;
	}

	@Override
	public int readlink(String path, CharBuffer link) throws FuseException {
		throw new FuseException("Not a link").initErrno(FuseException.ENOENT);
		//return 0;
	}

	@Override
	public int write(String path, Object fh, boolean isWritepage, ByteBuffer buf, long offset)
			throws FuseException {
		try {
			fileStore.write(path, buf, offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
					.initErrno(FuseException.EIO);
		}
		return 0;
	}
	
	@Override
	public int read(String path, Object fh, ByteBuffer buf, long offset)
			throws FuseException {
		try {
			fileStore.read(path, buf, offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
					.initErrno(FuseException.EIO);
		}		
		if (log.isDebugEnabled())
			log.debug("read " + buf.position() + "/" + buf.capacity()
					+ " requested bytes");
		return 0;
	}
	
	@Override
	public int fsync(String path, Object fh, boolean isDatasync) throws FuseException {
		try {
			fileStore.flushCache(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
			.initErrno(FuseException.EIO);
		}
		return 0;
	}
	
	@Override
	public int flush(String path, Object fh) throws FuseException {
		try {
			fileStore.flushCache(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
			.initErrno(FuseException.EIO);
		}
		return 0;
	}

	protected void setRedundancy(int redundancy) {
		fileStore.setRedundancy(redundancy);
	}
	
	protected int getRedundancy() {
		return fileStore.getRedundancy();
	}

	protected void setStorageStrategyName(String storageStrategyName) {
		fileStore.setStorageStrategyName(storageStrategyName);
	}
	
	protected void getStorageStrategyName() {
		fileStore.getStorageStrategyName();
	}
	
	protected FileFragmentStore getFileFragmentStore(){
		return fileStore;
	}
	
	/**
	 * Get the minimal availability of files stored by the current fuse box.
	 * @return the availability in percent
	 */
	public double getStorageAvailability(){
		return fileStore.getStorageAvailability(); //forward call to the file store
	}
	
	@Override
	public int release(String path, Object fh, int flags) throws FuseException {
		return 0;
	}

	/**
	 * Do cleanup and release resources
	 */
	public void close() {		
	}

	@Override
	public int getxattr(String path, String name, ByteBuffer dst)
			throws FuseException, BufferOverflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getxattrsize(String path, String name, FuseSizeSetter sizeSetter)
			throws FuseException {
		sizeSetter.setSize(0);
		return 0;
	}

	@Override
	public int listxattr(String path, XattrLister lister) throws FuseException {
		//lister.add(xattrName);
		return 0;
	}

	@Override
	public int removexattr(String path, String name) throws FuseException {
		return 0;
	}

	@Override
	public int setxattr(String path, String name, ByteBuffer value, int flags)
			throws FuseException {
		return 0;
	}
}
