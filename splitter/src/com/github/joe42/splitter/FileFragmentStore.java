package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannels;
import com.github.joe42.splitter.vtf.FileEntry;

import fuse.FuseException;

public class FileFragmentStore {
	protected RandomAccessTemporaryFileChannels tempFiles;
	protected RandomAccessTemporaryFileChannel tempReadChannel;
	protected CauchyReedSolomonSplitter splitter;
	private int redundancy;
	protected FileFragmentMetaDataStore fileFragmentMetaDataStore;

	public FileFragmentStore(CauchyReedSolomonSplitter splitter) throws IOException {
		tempFiles = new RandomAccessTemporaryFileChannels();
		this.splitter = splitter;
		this.fileFragmentMetaDataStore = new FileFragmentMetaDataStore();
	}

	public void setRedundancy(int redundancy){
		this.redundancy = redundancy;
	}
	
	public int getRedundancy(){
		return redundancy;
	}

	public void write(String path, ByteBuffer buf, long offset) throws IOException, FuseException {
		if (tempFiles.getFileChannel(path) == null) {
			if( ! fileFragmentMetaDataStore.hasFragments(path) ){
				tempFiles.putNewFileChannel(path);
			} else {
				tempFiles.put(path, splitter.glueFilesTogether(fileFragmentMetaDataStore, path));
			}
		}
		FileChannel wChannel = tempFiles.getFileChannel(path);
		wChannel.write(buf,offset);		
	}
	
	public void mknod(String path){
		tempFiles.putNewFileChannel(path);
	}

	public void read(String path, ByteBuffer buf, long offset) throws FuseException, IOException {
		if (tempReadChannel == null) {
			if( ! fileFragmentMetaDataStore.hasFragments(path) ) {
				return;
			}
			tempReadChannel = splitter.glueFilesTogether(fileFragmentMetaDataStore, path);
		}
		tempReadChannel.getChannel().read(buf, offset);
	}

	/**
	 * Find out if the file associated with path has already been persisted.
	 * @param path path to the file
	 * @return true iff the file has been stored completely
	 */
	public boolean hasFlushed(String path)  throws IOException {
		System.out.println("tempFiles: "+tempFiles);
		System.out.println("tempFiles: "+tempFiles.getFileChannel(path));
		return tempFiles.getFileChannel(path) == null;
	}

	public long getSize(String path) throws IOException {
		return fileFragmentMetaDataStore.getFragmentsSize(path);
	}

	public void remove(String path) throws IOException {
		if( ! fileFragmentMetaDataStore.hasFragments(path) ) {
			return;
		}
		for (String fragmentName : fileFragmentMetaDataStore.getFragments(path)) {
			new File(fragmentName).delete();
		}
		fileFragmentMetaDataStore.remove(path);
	}

	public void flushCache(String path) throws FuseException, IOException {
		if ( ! hasFlushed(path) ) {
			FileChannel temp = tempFiles.getFileChannel(path);
			splitter.splitFile(fileFragmentMetaDataStore, path, temp, redundancy);
		}
		removeCache(path);
	}

	protected void removeCache(String path) {
		tempFiles.delete(path);
		tempReadChannel = null;
	}

	public void truncate(String path, long size) throws FuseException, IOException {
		tempFiles.put(path, splitter.glueFilesTogether(fileFragmentMetaDataStore, path));
		tempFiles.getFileChannel(path).truncate(size);
	}

	public void rename(String from, String to) throws IOException, FuseException {
		remove(to);
		flushCache(from);
		fileFragmentMetaDataStore.moveFragments(from, to);		
	}
	
}