/**
 *   FUSE-J: Java bindings for FUSE (Filesystem in Userspace by Miklos Szeredi (mszeredi@inf.bme.hu))
 *
 *   Copyright (C) 2003 Peter Levart (peter@select-tech.si)
 *
 *   This program can be distributed under the terms of the GNU LGPL.
 *   See the file COPYING.LIB
 */

package fuse.zipfs;

import fuse.*;
import fuse.compat.Filesystem1;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;
import fuse.zipfs.util.Node;
import fuse.zipfs.util.Tree;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.helper.Serializer;
import jdbm.htree.HTree;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;

import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;




public class ZipFilesystem implements Filesystem1 {
	private static final Log log = LogFactory.getLog(ZipFilesystem.class);

	private static final int blockSize = 512;

	//hard-coded for 3 storages
	private static final int MAX_FILE_FRAGMENTS = 3;
	private static final int MAX_FILE_FRAGMENTS_NEEDED = 3;

	private List<String> fragmentStores = new ArrayList<String>();
	private RecordManager  recman;
	private HTree filemap;
	private HTree dirmap;
	private HTree stores;
	private Hashtable<String, File> tempFiles = new Hashtable<String, File>();

	public ZipFilesystem(File file) throws IOException {
	}

	public ZipFilesystem(String storage_path) throws IOException {
		fragmentStores.add(storage_path + "/storage01");
		fragmentStores.add(storage_path + "/storage02");
		fragmentStores.add(storage_path + "/storage03");
		
		Properties props = new Properties();
		recman = RecordManagerFactory.createRecordManager( "splitter", props );
		// create or load 
		filemap = loadPersistentMap(recman, "filemap");
		dirmap = loadPersistentMap(recman, "dirmap");
				
		try {
			dirmap.put("/", new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new IOException("IO Exception on accessing metadata");
		}
		
		log.info("dirmap size:"+((Entry)dirmap.get("/")).size);
	}
	
	private HTree loadPersistentMap(RecordManager recman, String mapName) throws IOException {
		long recid = recman.getNamedObject( mapName );
		HTree ret;
        if ( recid != 0 ) {
        	ret = HTree.load( recman, recid );
        } else {
        	ret = HTree.createInstance( recman );
            recman.setNamedObject( mapName, ret.getRecid() );
        }
        return ret;
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
		/*if(path.startsWith("/.config###")){
			if(path.equals("/.config###")){
				entry = new FolderEntry();
				stat.mode = FuseFtype.TYPE_DIR | 0755;
			}
			if()
		}*/
		try {
			if(filemap.get(path) != null){
				entry = (Entry) filemap.get(path);
				stat.mode = FuseFtype.TYPE_FILE | 0755;
			} else if(dirmap.get(path) != null){
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
		
		stat.nlink = 1;
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
		FastIterator pathes;
		try {
			if (filemap.get(path) != null)
				throw new FuseException("Not A Directory")
						.initErrno(FuseException.ENOTDIR);
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
					&& fileName.indexOf("/",path.length()-1) == path.length() - 1) {
				FuseDirEnt dirEntry = new FuseDirEnt();
				dirEntry.name = fileName.substring(path.length());
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
					&& dirName.indexOf("/",path.length()-1) == path.length() - 1 && ! dirName.equals(path)) {
				FuseDirEnt dirEntry = new FuseDirEnt();
				dirEntry.name = dirName.substring(path.length());
				dirEntry.mode = FuseFtype.TYPE_DIR;
				dirEntries.add(dirEntry);
			}
		}
		FuseDirEnt[] ret = new FuseDirEnt[dirEntries.size()+2];

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
			ret[i] = dirEntryIter;
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
		try {
			File temp = File.createTempFile(path, ".tmp");
			temp.deleteOnExit();
			tempFiles.put(path, temp);
			filemap.put(path, new FileEntry());
			recman.commit();
		} catch (IOException e) {
			throw new FuseException("IO Exception on accessing metadata")
			.initErrno(FuseException.EIO);
		}
	}

	public void open(String path, int flags) throws FuseException {
		log.info("opened: "+path);
		//ZipEntry entry = getFileZipEntry(path);

		// if (flags == O_WRONLY || flags == O_RDWR)
		// throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void rename(String from, String to) throws FuseException {
		if(from.equals(to))
			return;
		Entry entry = null;
		HTree map = null;
		try {
			if(filemap.get(from) != null){ 
				map = filemap;
			} else if(dirmap.get(from) != null){ 
				map = dirmap;
			}
			if(map == null)
				throw new FuseException("No Such Entry")
				.initErrno(FuseException.ENOENT);
			entry = (Entry) map.get(from);
			map.put(to, entry);
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
			if(dirEntry == null)
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
		return new FuseStatfs ();
	}

	public void symlink(String from, String to) throws FuseException {
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void truncate(String path, long size) throws FuseException {
		// throw new FuseException("Read Only").initErrno(FuseException.EACCES);
	}

	public void unlink(String path) throws FuseException {
		try {
			FileEntry entry = (FileEntry) filemap.get(path);
			int del_cnt = 0;
			for(String fragmentName: entry.fragment_names){
				if(new File(fragmentName).delete()){
					del_cnt++;
				}
			}
			int possibly_existing_filenr = MAX_FILE_FRAGMENTS - del_cnt;
			if( possibly_existing_filenr >= MAX_FILE_FRAGMENTS_NEEDED ){
				throw new FuseException("IO Exception on deleting file")
				.initErrno(FuseException.EACCES);
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
			    if(tempFiles.get(path) == null){
			    	tempFiles.put(path, glueFilesTogether(path));
			    }
			    File temp = tempFiles.get(path);
			    FileChannel wChannel = new FileOutputStream(temp, false).getChannel();
			    wChannel.write(buf, offset);
			} catch (IOException e) {
				throw new FuseException("IO Exception")
				.initErrno(FuseException.EIO);
			}
	}
	
	private void splitFile(String path) throws FuseException{
		if(tempFiles.get(path) == null){
			throw new FuseException("IO Exception - nothing to split")
			.initErrno(FuseException.EIO);
		}//TODO: use same filenames if file exists already
		File temp = tempFiles.get(path);
		FileEntry fileEntry = null;
		try {
			fileEntry = (FileEntry) filemap.get(path);
		} catch (IOException e1) {
			throw new FuseException("IO Exception on accessing metadata")
			.initErrno(FuseException.EIO);
		}
		String fragment_name;
		String uniquePath;
		if(fileEntry.fragment_names.isEmpty()){
			uniquePath = StringUtil.getUniqueAsciiString(path);
			for (int fragment_nr = 0; fragment_nr < MAX_FILE_FRAGMENTS; fragment_nr++) {
				fragment_name = fragmentStores.get(fragment_nr) + uniquePath
				+ '#' + fragment_nr;
				fileEntry.fragment_names.add(fragment_name);
			}
		}
		Digest digestFunc = new SHA256Digest();	
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		String hexString = null;
		InformationDispersalCodec crsidacodec;
		InformationDispersalEncoder encoder;
		try{
			crsidacodec = new CauchyInformationDispersalCodec(MAX_FILE_FRAGMENTS, MAX_FILE_FRAGMENTS_NEEDED, 1);
			encoder = crsidacodec.getEncoder();
		}
		catch (Exception e) {
			throw new FuseException("IDA could not be initialized")
			.initErrno(FuseException.EACCES);
		}
		try{
			byte[] arr = new byte[(int)temp.length()];
			FileInputStream fis = new FileInputStream(temp);
			fis.read(arr);
			digestFunc.update(arr, 0, arr.length);
			digestFunc.doFinal(digestByteArray, 0);
			List<byte[]> result = encoder.process(arr);
			
			for (int fragment_nr = 0; fragment_nr < MAX_FILE_FRAGMENTS; fragment_nr++) {

				log.info("write: "+fragment_nr);
				fragment_name = fileEntry.fragment_names.get(fragment_nr);
				fileEntry.fragment_names.add(fragment_name);
				File fileSegment = new File(fragment_name);
				byte[] b = result.get(fragment_nr);
				digestFunc.reset();	
				digestFunc.update(b, 0, b.length);
				digestFunc.doFinal(digestByteArray, 0);
				
				hexString = new String(Hex.encode(digestByteArray));
				
				OutputStream out = new FileOutputStream(fileSegment);
				out.write(b);				
				out.flush();
				out.close();
			}		
			fileEntry.size = arr.length;
			filemap.put(path, fileEntry);
			recman.commit();
		}catch(Exception e){
			throw new FuseException("IO error: "+e.toString(), e)
			.initErrno(FuseException.EIO);
		}    
	}

	private File glueFilesTogether(String path) throws FuseException {
		File ret = null;
		List<byte[]> receivedFileSegments = new ArrayList<byte[]>();
		Digest digestFunc = new SHA256Digest();
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		InformationDispersalCodec crsidacodec;
		InformationDispersalDecoder decoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(
					MAX_FILE_FRAGMENTS, MAX_FILE_FRAGMENTS_NEEDED, 1);
			decoder = crsidacodec.getDecoder();
		} catch (Exception e) {
			throw new FuseException("IDA could not be initialized")
					.initErrno(FuseException.EACCES);
		}
		String hexString = null;
		InputStream in = null;
		int readBytes;
		try {
			ret = File.createTempFile(path, ".tmp");
			ret.deleteOnExit();
			int validSegment = 0;
			List<String> fragmentNames = ((FileEntry) filemap.get(path)).fragment_names;
			for (String fragment_name : fragmentNames) {
				File file_fragment = new File(fragment_name);
				if (file_fragment.exists() && file_fragment.isFile()) {

					in = new FileInputStream(file_fragment);
					byte[] segmentBuffer = new byte[(int) file_fragment
							.length()];

					readBytes = in.read(segmentBuffer);
					log.info("read file segment: " + file_fragment.getName()
							+ "; " + readBytes + " bytes!");

					digestFunc.reset();

					digestFunc.update(segmentBuffer, 0, segmentBuffer.length);

					digestFunc.doFinal(digestByteArray, 0);

					hexString = new String(Hex.encode(digestByteArray));

					/*
					 * if (!hexString.equals(file_fragment.getName())) {
					 * log.error("this file segment is invalid! " +
					 * file_fragment.getName() + " <> " + hexString); } else {
					 */
					receivedFileSegments.add(segmentBuffer);
					validSegment++;
					if (validSegment >= MAX_FILE_FRAGMENTS_NEEDED) {
						break;
					}
					// }
					in.close();
				}
			}
			byte[] recoveredFile = decoder.process(receivedFileSegments);

			digestFunc.reset();

			digestFunc.update(recoveredFile, 0, recoveredFile.length);

			digestFunc.doFinal(digestByteArray, 0);
		 
			OutputStream out = new FileOutputStream(ret);
			out.write(recoveredFile, 0, recoveredFile.length); 

		} catch (Exception e) {
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
		return ret;
	}
	
	public void  read(String path, ByteBuffer buf, long offset)
			throws FuseException {
		File temp;
		try {
		    if(tempFiles.get(path) == null){
		    	temp = glueFilesTogether(path);
		    	tempFiles.put(path, temp);
		    }
	    	temp = tempFiles.get(path);
		    FileChannel rChannel = new FileInputStream(temp).getChannel();
		    rChannel.read(buf, offset);
		} catch (IOException e) {
			throw new FuseException("IO Exception")
			.initErrno(FuseException.EIO);
		}
		if (log.isDebugEnabled())
			log.debug("read " + buf.position() + "/" + buf.capacity()
					+ " requested bytes");
	}

	public void release(String path, int flags) throws FuseException {
		if(tempFiles.get(path) != null){
			splitFile(path);
			tempFiles.get(path).delete();
			tempFiles.remove(path);
		}
	}

	

	//
	// Java entry point

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Must specify storages base path");
			System.exit(-1);
		}

		String fuseArgs[] = new String[args.length - 1];
		System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);

		try {
			FuseMount.mount(fuseArgs, new ZipFilesystem(args[args.length - 1]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
