package com.github.joe42.splitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
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

//TODO: add copyright
//Parallel read: done
//Fixed: write appended data; or wrote nonsence.
//<1238
//overwrite existing files

public class FuseBox implements Filesystem1 {
	private static final Logger  log = Logger.getLogger("FuseBox");

	private static final int blockSize = 512;

	private RecordManager recman;
	private HTree filemap;
	private HTree dirmap;
	private RandomAccessTemporaryFileChannels tempFiles = new RandomAccessTemporaryFileChannels();
	private FuseStatfs statfs;
	protected int redundancy;
	private RandomAccessTemporaryFileChannel tempReadChannel;
	private CauchyReedSolomonSplitter splitter;
	private FileFragmentStore fileFragmentStore;

	private int UID;

	private int GID;

	public FuseBox(CauchyReedSolomonSplitter splitter) throws IOException {
		// .config###/
		// aufruf splitter_mount.sh mountordner ordner_mit_storage_ordner
		// "redundancy level in percent 0-100"
		PropertyConfigurator.configure("log4j.properties");
		
		this.splitter = splitter;
		fileFragmentStore = new FileFragmentStore();
		PropertiesUtil props = new PropertiesUtil("../bin/nubi.properties");
		recman = RecordManagerFactory.createRecordManager(props.getProperty("splitter_database_location"), props.getProperties());
		// create or load
		filemap = loadPersistentMap(recman, "filemap");
		dirmap = loadPersistentMap(recman, "dirmap");

		try {
			dirmap.put("/", new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new IOException("IO Exception on accessing metadata");
		}

		log.debug("dirmap size:" + ((Entry) dirmap.get("/")).size);

		UID = getUID();
		GID = getGID();
	}

	private int getUID() {
		String uid = null;
		try {
		    String userName = System.getProperty("user.name");
		    String command = "id -u "+userName;
		    Process child = Runtime.getRuntime().exec(command);

		    // Get the input stream and read from it
		    InputStream in = child.getInputStream();
		    int c;
		    uid = "";
		    while ((c = in.read()) != -1) {
		        uid += ((char)c);
		    }
		    in.close();
		} catch (IOException e) {
		}
		return Integer.parseInt(uid.trim());
	}

	private int getGID() {
		String gid = null;
		try {
		    String userName = System.getProperty("user.name");
		    String command = "id -g "+userName;
		    Process child = Runtime.getRuntime().exec(command);

		    // Get the input stream and read from it
		    InputStream in = child.getInputStream();
		    int c;
		    gid = "";
		    while ((c = in.read()) != -1) {
		        gid += ((char)c);
		    }
		    in.close();
		} catch (IOException e) {
		}
		return Integer.parseInt(gid.trim());
	}

	private HTree loadPersistentMap(RecordManager recman, String mapName)
			throws IOException {
		long recid = recman.getNamedObject(mapName);
		HTree ret;
		if (recid != 0) {
			ret = HTree.load(recman, recid);
		} else {
			ret = HTree.createInstance(recman);
			recman.setNamedObject(mapName, ret.getRecid());
		}
		return ret;
	}

	public void chmod(String path, int mode) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void chown(String path, int uid, int gid) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	// mknod is not called because of unsupportet op exception here
	public FuseStat getattr(String path) throws FuseException {
		FuseStat stat = new FuseStat();
		Entry entry = null;
		/*
		 * if(path.startsWith("/.config###")){ if(path.equals("/.config###")){
		 * entry = new FolderEntry(); stat.mode = FuseFtype.TYPE_DIR | 0755; }
		 * if() }
		 */
		try {
			if (filemap.get(path) != null) {
				entry = (Entry) filemap.get(path);
				stat.mode = FuseFtype.TYPE_FILE | 0644;
			} else if (dirmap.get(path) != null) {
				entry = (Entry) dirmap.get(path);
				stat.mode = FuseFtype.TYPE_DIR | 0755;
			}
		} catch (IOException e) {
			throw new FuseException("IO Exception on reading metadata")
					.initErrno(FuseException.EIO);
		}
		if (entry == null)
			throw new FuseException("No Such Entry")
					.initErrno(FuseException.ENOENT);
		//TODO: tidy up
		stat.nlink = entry.nlink;
		stat.uid = entry.uid;
		stat.gid = entry.gid;
		stat.size = entry.size;
		stat.atime = entry.atime;
		stat.mtime = entry.mtime;
		stat.ctime = entry.ctime;
		stat.blocks = (int) ((stat.size + 511L) / 512L);

		return stat;
	}

	public FuseDirEnt[] getdir(String path) throws FuseException {
		//TODO: tidy up
		FastIterator pathes;
		try {
			if (dirmap.get(path) == null)
				throw new FuseException("No Such Entry")
						.initErrno(FuseException.ENOENT);
			pathes = filemap.keys();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		String fileName, dirName;
		List<FuseDirEnt> dirEntries = new ArrayList<FuseDirEnt>();
		while ((fileName = (String) pathes.next()) != null) {
			if (fileName.startsWith(path)
					&& path.equals( new File(fileName).getParent() )) {
				FuseDirEnt dirEntry = new FuseDirEnt();
				dirEntry.name = new File(fileName).getName();
				dirEntry.mode = FuseFtype.TYPE_FILE;
				dirEntries.add(dirEntry);
			}
		}
		try {
			pathes = dirmap.keys();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		while ((dirName = (String) pathes.next()) != null) {
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
			dirmap.put(path, new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
	}

	public void mknod(String path, int mode, int rdev) throws FuseException {

		FileEntry fileEntry;
		try {
			tempFiles.putNewFileChannel(path);
			fileEntry = new FileEntry(path);
			fileEntry.uid = UID;
			fileEntry.gid = GID;
			filemap.put(path, fileEntry);
			recman.commit();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		if (tempFiles.getFileChannel(path) == null) {
			throw new FuseException("IO Exception - nothing to split")
					.initErrno(FuseException.EIO);
		}
		splitter.splitFile(fileFragmentStore, fileEntry, tempFiles.getFileChannel(path)); //TODO: throw exception if not successful and remove filemap entry //kann man nicht rausnehmen (vielleicht doch; Dropbox 400 Error bei leeren Dateien ist gefixed)
		try {
			recman.commit();
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
		if (from.equals(to)) // only if from is dir and to is file 
			throw new FuseException("Entity"+to+" already exists.")
				.initErrno(FuseException.EEXIST);
		Entry fromEntry = null;
		HTree map = null;
		try {
			if (filemap.get(from) != null) {
				map = filemap;
			} else if (dirmap.get(from) != null) {
				map = dirmap;
			}
			if (map == null)
				throw new FuseException("No Such Entry")
						.initErrno(FuseException.ENOENT);

			if (tempFiles.getFileChannel(from) != null) {
				FileEntry fileEntry;
				try {
					fileEntry = (FileEntry) filemap.get(from);
				} catch (IOException e1) {
					throw new FuseException("IO Exception on accessing metadata")
							.initErrno(FuseException.EIO);
				}
				splitter.splitFile(fileFragmentStore, fileEntry, tempFiles.getFileChannel(from)); //kann man nicht rausnehmen (vielleicht doch; Dropbox 400 Error bei leeren Dateien ist gefixed)
				tempFiles.delete(from);
			}
			fromEntry = (Entry) map.get(from);
			map.put(to, fromEntry);
			fromEntry.path = to;
			fileFragmentStore.moveFragments(from, to);
			map.remove(from);
			recman.commit();
		} catch (IOException e) {
			throw new FuseException("IO Exception on reading metadata")
					.initErrno(FuseException.EIO);
		}
	}

	public void rmdir(String path) throws FuseException {
		Entry dirEntry = null;
		try {
			dirEntry = (FolderEntry) dirmap.get(path);
			if (dirEntry == null)
				throw new FuseException("No Such Entry")
						.initErrno(FuseException.ENOENT);
			new File(path).delete();
			dirmap.remove(path);
			recman.commit();
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
			iter = filemap.keys();
			String path = (String) iter.next();
			while (path != null) {
				files++;
				blocks += (((Entry) filemap.get(path)).size + blockSize - 1)
						/ blockSize;
				path = (String) iter.next();
			}
			iter = dirmap.keys();
			path = (String) iter.next();
			while (path != null) {
				dirs++;
				blocks += (((Entry) dirmap.get(path)).size + blockSize - 1)
						/ blockSize;
				path = (String) iter.next();
			}

		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
		statfs = new FuseStatfs();
		statfs.blocks = blocks;
		statfs.blockSize = blockSize;
		statfs.blocksFree = 0;
		statfs.files = files + dirs;
		statfs.filesFree = 0;
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
			if (filemap.get(path) != null) {
				FileEntry fileEntry;
				try {
					fileEntry = (FileEntry) filemap.get(path);
				} catch (IOException e1) {
					throw new FuseException("IO Exception on accessing metadata")
							.initErrno(FuseException.EIO);
				}
				tempFiles.put(path, splitter.glueFilesTogether(fileFragmentStore, fileEntry));
				try {
					tempFiles.getFileChannel(path).truncate(size);
				} catch (FileNotFoundException e) {
					throw new FuseException("No Such Entry")
							.initErrno(FuseException.ENOENT);
				} catch (IOException e) {
					throw new FuseException("IO Exception on truncating file")
							.initErrno(FuseException.EIO);
				}
			}
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
					.initErrno(FuseException.EIO);
		}
	}

	public void unlink(String path) throws FuseException {
		try {
			for (String fragmentName : fileFragmentStore.getFragments(path)) {
				new File(fragmentName).delete();
			}
			filemap.remove(path);
			recman.commit();
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
			if (tempFiles.getFileChannel(path) == null) {
				System.out.println("glue? ");
				FileEntry entry = (FileEntry) filemap.get(path);
				tempFiles.put(path, splitter.glueFilesTogether(fileFragmentStore, entry));
				System.out.println("glued! ");
			}
			FileChannel wChannel = tempFiles.getFileChannel(path);
			//wChannel.position(offset);
			wChannel.write(buf,offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
					.initErrno(FuseException.EIO);
		}
	}



	

	public void read(String path, ByteBuffer buf, long offset)
			throws FuseException {
		try {
			if (tempReadChannel == null) {
				FileEntry entry = (FileEntry) filemap.get(path);
				tempReadChannel = splitter.glueFilesTogether(fileFragmentStore, entry);
			}
		tempReadChannel.getChannel().read(buf, offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
					.initErrno(FuseException.EIO);
		}
		if (log.isDebugEnabled())
			log.debug("read " + buf.position() + "/" + buf.capacity()
					+ " requested bytes");
	}

	public void release(String path, int flags) throws FuseException {
		if (tempFiles.getFileChannel(path) != null) {
			FileEntry fileEntry;
			try {
				fileEntry = (FileEntry) filemap.get(path);
			} catch (IOException e1) {
				throw new FuseException("IO Exception on accessing metadata")
						.initErrno(FuseException.EIO);
			}
			splitter.splitFile(fileFragmentStore, fileEntry, tempFiles.getFileChannel(path)); //kann man nicht rausnehmen (vielleicht doch; Dropbox 400 Error bei leeren Dateien ist gefixed)
			tempFiles.delete(path);
		}
		if (tempReadChannel != null) {
			tempReadChannel.delete();
			tempReadChannel = null;
		}
	}
}
