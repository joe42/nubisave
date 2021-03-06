package com.github.joe42.splitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jdbm.helper.FastIterator;

import org.apache.log4j.Logger;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.util.file.ConcurrentMultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFiles;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannels;
import com.github.joe42.splitter.util.file.SerialMultipleFileHandler;
import com.github.joe42.splitter.vtf.FileEntry;
import com.github.joe42.splitter.util.*;

import fuse.FuseException;

public class FileFragmentStore {
	private static final Logger  log = Logger.getLogger("FileFragmentStore");
	protected RandomAccessTemporaryFileChannels tempFiles;
	protected RandomAccessTemporaryFileChannel tempReadChannel;
	protected Splitter splitter;
	protected FileFragmentMetaDataStore fileFragmentMetaDataStore;

	public FileFragmentStore(Splitter splitter) throws IOException {
		tempFiles = new RandomAccessTemporaryFileChannels();
		this.splitter = splitter;
		this.fileFragmentMetaDataStore = new FileFragmentMetaDataStore();
	}

	public void reloadDatabase() throws IOException{
		fileFragmentMetaDataStore.reloadDatabase();
	}

	public byte[] readMetaData(String path) throws IOException{
		MultipleFileHandler serial_multi_file_handler = new SerialMultipleFileHandler();
		Map<String, byte[]> filePathsToChecksum = new HashMap<String, byte[]>();
		log.debug("Read meta data file");
		for(BackendService storageService: splitter.getBackendServices().getFrontEndStorageServices()){
			if(storageService.getNrOfFilePartsToStore() != 0){
				filePathsToChecksum.put(storageService.getDataDirPath()+path, null);
				log.debug("Try reading meta data file from:"+storageService.getDataDirPath()+path);
			}
		}
		log.debug("successfull parts: "+serial_multi_file_handler.getFilesAsByteArrays(filePathsToChecksum, 1).getNrOfSuccessfullyTransferedFiles());
		return serial_multi_file_handler.getFilesAsByteArrays(filePathsToChecksum, 1).getSuccessfullyTransferedFiles().get(0);
	}
	
	public void writeMetaData(String content, String path) throws IOException{
		log.debug("Write meta data file");
		HashMap<String, byte[]> filePathsToChecksum = new HashMap<String, byte[]>();
		byte[] file = content.getBytes("UTF-8");
		for(BackendService storageService: splitter.getBackendServices().getFrontEndStorageServices()){
			if(storageService.getNrOfFilePartsToStore() != 0){
				filePathsToChecksum.put(storageService.getDataDirPath()+path, file);
				log.debug("Try writing meta data file to:"+storageService.getDataDirPath()+path);
			}
		}
		new ConcurrentMultipleFileHandler().writeFilesAsByteArrays(filePathsToChecksum);
	}

	public void setRedundancy(int redundancy){
		this.splitter.setRedundancy(redundancy);
	}
	
	public int getRedundancy(){
		return this.splitter.getRedundancy();
	}
	
	public void setStorageStrategyName(String storageStrategyName){
		this.splitter.setStorageStrategyName(storageStrategyName);
	}
	
	public String getStorageStrategyName(){
		return this.splitter.getStorageStrategyName();
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
	
	public void mknod(String path) throws IOException, FuseException {
		tempFiles.putNewFileChannel(path);
	}

	public void read(String path, ByteBuffer buf, long offset) throws FuseException, IOException {
		log.debug("buf.limit(): "+buf.limit()+" buf.position(): "+buf.position()+" "+"file size: "+fileFragmentMetaDataStore.getFragmentsSize(path)+" offset: "+offset+" end of read: "+(offset+buf.limit()));
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
		return tempFiles.getFileChannel(path) == null;
	}

	public long getSize(String path) throws IOException {
		return fileFragmentMetaDataStore.getFragmentsSize(path);
	}
	
	public long getFreeBytes() {
		long freeBytes = 0;
		for( String fileSystemPath: splitter.getBackendServices().getDataDirPaths() ){
		    freeBytes += LinuxUtil.getFreeBytes(fileSystemPath);
		}
		return freeBytes;
	}	
	
	public long getUsedBytes() {
		long usedBytes = 0;
		for( String fileSystemPath: splitter.getBackendServices().getDataDirPaths() ){
		    usedBytes += LinuxUtil.getUsedBytes(fileSystemPath);
		}
		return usedBytes;
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
			splitter.splitFile(fileFragmentMetaDataStore, path, temp);
		}
		removeCache(path);
	}

	protected void removeCache(String path) {
		tempFiles.delete(path);
		if(tempReadChannel != null) {
			tempReadChannel.delete();
		} 
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

	/**
	 * Rename a fragment of a file.
	 * This is used to move fragments to different locations, i.e. another file system.
	 * @param from the fragment path to be moved
	 * @param to the destination fragment path
	 * @throws IOException when the operation did not succeed 
	 */
	public void renameFragment(String from, String to) throws IOException {
		fileFragmentMetaDataStore.moveFragment(from, to);		
	}
	
	/** Check if a file fragment exists
	 * @param fragmentName a path of a file fragment
	 * @return true iff the file fragment exists
	 * @throws IOException */
	public boolean hasFragment(String fragmentName) throws IOException{
		return fileFragmentMetaDataStore.hasFragment(fragmentName);		
	}

	/**
	 * Get the minimal availability of files stored by the current file fragment store.
	 * @return the availability in percent
	 */
	public double getStorageAvailability() {
		return splitter.getStorageAvailability(); //forward call to the splitter
	}
}