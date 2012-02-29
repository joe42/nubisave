package com.github.joe42.splitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.github.joe42.splitter.util.LinuxUtil;
import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannels;
import com.github.joe42.splitter.util.file.PropertiesUtil;
import com.github.joe42.splitter.vtf.Entry;
import com.github.joe42.splitter.vtf.FileEntry;
import com.github.joe42.splitter.vtf.FolderEntry;
import com.github.joe42.splitter.vtf.VirtualFileContainer;

import fuse.FuseException;
import fuse.FuseFtype;
import fuse.FuseMount;
import fuse.FuseStatfs;
import fuse.compat.Filesystem1;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;

public class FuseBox implements Filesystem1 {
	private static final Logger  log = Logger.getLogger("FuseBox");

	private static final int blockSize = 512;

	protected FileFragmentStore fileStore;

	private FuseStatfs statfs;
	private FileMetaDataStore metaDataStore;

	private int UID;

	private int GID;

	public FuseBox(CauchyReedSolomonSplitter splitter) throws IOException {
		PropertyConfigurator.configure("log4j.properties");
		
		metaDataStore = new FileMetaDataStore();
		fileStore = new FilePartFragmentStore(splitter);
		UID = LinuxUtil.getUID();
		GID = LinuxUtil.getGID();
	}

	public void chmod(String path, int mode) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void chown(String path, int uid, int gid) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public FuseStat getattr(String path) throws FuseException {
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

		return stat;
	}

	public FuseDirEnt[] getdir(String path) throws FuseException {
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
		List<FuseDirEnt> dirEntries = new ArrayList<FuseDirEnt>();
		while ((fileName = (String) paths.next()) != null) {
			if (fileName.startsWith(path)
					&& path.equals( new File(fileName).getParent() )) {
				FuseDirEnt dirEntry = new FuseDirEnt();
				dirEntry.name = new File(fileName).getName();
				dirEntry.mode = FuseFtype.TYPE_FILE;
				dirEntries.add(dirEntry);
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
				FuseDirEnt dirEntry = new FuseDirEnt();
				dirEntry.name = new File(dirName).getName();
				dirEntry.mode = FuseFtype.TYPE_DIR;
				dirEntries.add(dirEntry);
			}
		}
		FuseDirEnt[] ret = new FuseDirEnt[dirEntries.size() + 2];

		int i = 0;
		FuseDirEnt dirEntry = new FuseDirEnt();
		dirEntry.name = ".";
		dirEntry.mode = FuseFtype.TYPE_DIR;
		ret[i++] = dirEntry;
		dirEntry = new FuseDirEnt();
		dirEntry.name = "..";
		dirEntry.mode = FuseFtype.TYPE_DIR;
		ret[i++] = dirEntry;
		for (FuseDirEnt dirEntryIter : dirEntries) {
			ret[i++] = dirEntryIter;
		}
		return ret;
	}

	public void link(String from, String to) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void mkdir(String path, int mode) throws FuseException {
		try {
			metaDataStore.makeFolderEntry(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
	}

	public void mknod(String path, int mode, int rdev) throws FuseException {

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
	}

	public void open(String path, int flags) throws FuseException {
		log.debug("opened: " + path);
		// ZipEntry entry = getFileZipEntry(path);

		// if (flags == O_WRONLY || flags == O_RDWR)
		// throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void rename(String from, String to) throws FuseException {
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
	}

	public void rmdir(String path) throws FuseException {
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
	}

	public FuseStatfs statfs() throws FuseException {
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
		statfs = new FuseStatfs();
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

	public void symlink(String from, String to) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EOPNOTSUPP);
	}

	public void truncate(String path, long size) throws FuseException {
		try {
			fileStore.flushCache(path);
			fileStore.truncate(path, size);
		} catch (IOException e) {
			throw new FuseException("IO Exception on truncating file")
					.initErrno(FuseException.EIO);
		}
	}

	public void unlink(String path) throws FuseException {
		removeFile(path);
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

	public void utime(String path, int atime, int mtime) throws FuseException {
		// noop
	}

	public String readlink(String path) throws FuseException {
		throw new FuseException("Not a link").initErrno(FuseException.ENOENT);
	}

	public void write(String path, ByteBuffer buf, long offset)
			throws FuseException {
		try {
			fileStore.write(path, buf, offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
					.initErrno(FuseException.EIO);
		}
	}

	public void read(String path, ByteBuffer buf, long offset)
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
	}

	public void release(String path, int flags) throws FuseException {
		try {
			fileStore.flushCache(path);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
			.initErrno(FuseException.EIO);
		}
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
}
