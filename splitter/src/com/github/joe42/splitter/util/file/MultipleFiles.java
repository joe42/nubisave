package com.github.joe42.splitter.util.file;

import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.bouncycastle.crypto.Digest;

/**
 * Handle multiple files.
 * This class is used as a return value by MultipleFileHandler instances to indicate which files could be transfered successfully,
 * which files failed to be transfered and which of them were transfered but had a wrong checksum thereafter. 
 */
public class MultipleFiles{
	private Map<String,byte[]> allFilePathsToChecksum;
	private Map<String,byte[]> successfullyTransferedFilePathsToFiles;
	private List<String> failedFilePaths;
	private List<String> wrongChecksumFilePaths;
	private Digest digestFunc;

	/**
	 * Create a new instance with a mapping of file paths to the corresponding checksums of the files and a digest function to generate the checksums from the actual files.
	 * @param filePathsToChecksum
	 */
	MultipleFiles(Map<String,byte[]> filePathsToChecksum, Digest digestFunc){
		allFilePathsToChecksum = filePathsToChecksum;
		successfullyTransferedFilePathsToFiles = new HashMap<String,byte[]>();
		failedFilePaths = new ArrayList<String>();
		wrongChecksumFilePaths = new ArrayList<String>();
		this.digestFunc = digestFunc;
	}

	/**
	 * Create a new instance with a mapping of file paths and a digest function to generate the checksums from the actual files.
	 * Checksums of the files will be generated when they are transfered. Until then they are set to null.
	 * @param filePathsToChecksum
	 */
	MultipleFiles(Set<String> filePaths, Digest digestFunc){
		allFilePathsToChecksum = new HashMap<String, byte[]>();
		for(String filePath: filePaths){
			allFilePathsToChecksum.put(filePath, null);
		}
		successfullyTransferedFilePathsToFiles = new HashMap<String,byte[]>();
		failedFilePaths = new ArrayList<String>();
		wrongChecksumFilePaths = new ArrayList<String>();
		this.digestFunc = digestFunc;
	}

	void addTransferedFile(String filePath, byte[] file){
		byte[] checksum = calculateChecksum(file);
		if( getChecksum(filePath) == null) {
			allFilePathsToChecksum.put(filePath, checksum);
		} 
		if( Arrays.equals(checksum, getChecksum(filePath)) ){
			successfullyTransferedFilePathsToFiles.put(filePath, file);
		} else {
			wrongChecksumFilePaths.add(filePath);
		}
	}
	
	void addFailedFilePath(String filePath){
		failedFilePaths.add(filePath);
	}

	void addWrongChecksumFilePath(String filePath){
		wrongChecksumFilePaths.add(filePath);
	}
	
	private byte[] calculateChecksum(byte[] arr) {
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		digestFunc.reset();
		digestFunc.update(arr, 0, arr.length);
		digestFunc.doFinal(digestByteArray, 0);
		return digestByteArray;
	}

	/**
	 * Get the file paths of the files that were successfully transfered by the invoked method.
	 * @return the file paths of the files that were successfully transfered
	 */
	public Set<String> getSuccessfullyTransferedFilePaths(){
		return successfullyTransferedFilePathsToFiles.keySet();
	}
	/**
	 * Get the files that were successfully transfered by the invoked method in random order.
	 * @return the file paths of the files that were successfully transfered
	 */
	public List<byte[]> getSuccessfullyTransferedFiles() {
		return new ArrayList<byte[]>(successfullyTransferedFilePathsToFiles.values());
	}
	
	/**
	 * Get a file that was successfully transfered by the invoked method.
	 * @param filePath the path of a successfully transfered file
	 * @return the file  with the path filePath that has been successfully transfered
	 */
	public byte[] getSuccessfullyTransferedFile(String filePath){
		return successfullyTransferedFilePathsToFiles.get(filePath);
	}
	
	/**
	 * Get the file paths of the files that were not transfered by the invoked method because the transfer was canceled.
	 * @return the file paths of the files that were not transfered
	 */
	@SuppressWarnings("unchecked")
	public List<String> getUntransferedFilePaths(){
		return new ArrayList<String>(CollectionUtils.subtract( CollectionUtils.subtract(CollectionUtils.subtract(allFilePathsToChecksum.keySet(), successfullyTransferedFilePathsToFiles.keySet()), failedFilePaths), wrongChecksumFilePaths ));
	}
	
	/**
	 * Get the file paths of the files that could not be transfered because of an IO error.
	 * @return the file paths of the files that could not be transfered
	 */
	public List<String> getFailedFilePaths(){
		return failedFilePaths;
	}

	/**
	 * Get the file paths of the files that had a wrong checksum after transferring the file.
	 * This is different to  {@link #getFailedFilePaths() getFailedFilePaths()} in that the files could be transfered without IO error.
	 * @return the file paths with a wrong checksum
	 */
	public List<String> getWrongChecksumFilePaths(){
		return wrongChecksumFilePaths;
	}

	/**
	 * Get a map of file paths to the expected checksum of each file 
	 * @return a map of file paths to their corresponding checksums 
	 */
	public Map<String, byte[]> getFileNamesToChecksum() {
		return allFilePathsToChecksum;
	}
	
	/**
	 * Get the expected checksum of a file with the path filePath 
	 * @param filePath
	 * @return the checksum a file
	 */
	public byte[] getChecksum(String filePath) {
		return allFilePathsToChecksum.get(filePath);
	}

	/**
	 * Get all file paths 
	 * @return all file paths 
	 */
	public Set<String> getFileNames() {
		return allFilePathsToChecksum.keySet();
	}
	
	/**
	 * Get the checksum of each file 
	 * The checksum at position i is the checksum of the file with the path specified in filePaths at the same index position.
	 * The checksum might be null if the corresponding file has not yet been written successfully. 
	 * @return all checksums in the order dictated by filePaths
	 */
	public List<byte[]> getChecksums(List<String> filePaths) {
		List<byte[]> checksums = new ArrayList<byte[]>();
		for(String path: filePaths){
			checksums.add(allFilePathsToChecksum.get(path));
		}
		return checksums;
	}
	
	/**
	 * Get the number of files that failed to be transfered or have a wrong checksum.
	 * @return
	 */
	public int getNrOfUnsuccessfullyTransferedFiles(){
		return wrongChecksumFilePaths.size()+failedFilePaths.size();
	}

	/**
	 * Get the number of the files that were successfully transfered by the invoked method.
	 * @return the number of the files that were successfully transfered
	 */
	public int getNrOfSuccessfullyTransferedFiles(){
		return successfullyTransferedFilePathsToFiles.size();
	}

	/**
	 * Set the checksum for a file
	 * @param filePath
	 * @param file
	 * @return
	 */
	public byte[] setChecksum(String filePath, byte[] file) {
		return allFilePathsToChecksum.put(filePath, calculateChecksum(file));
	}

}
