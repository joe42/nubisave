package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses all potential storage directories. 
 * Storing a file is only successful if all storage operations were successful.
 */
public class UseAllInParallelStorageStrategy  implements StorageStrategy {
	private List<String> potentialStorageDirectories;
	private int redundancy;
	private long filesize;
	
	
	/**
	 * Creates a UseAllInParallelStorageStrategy object using all potentialStorageDirectories in parallel with half the number of #potentialStorageDirectories - 1 as expendable stores.
	 * @param potentialStorageDirectories
	 */
	public UseAllInParallelStorageStrategy(List<String> potentialStorageDirectories){
		this.potentialStorageDirectories = potentialStorageDirectories;
		redundancy = 50;
	}

	public void setPotentialStorageDirectories(List<String> potentialStorageDirectories){
		this.potentialStorageDirectories = potentialStorageDirectories;
	}
	public List<String> getPotentialStorageDirectories(){
		return potentialStorageDirectories;
	}

	/**
	 * Set the redundancy in percent from 0 to 100. 
	 * A value of 100 means that all but one of the stores used to store a file can fail with the file still being recoverable.
	 * A value of 0 means that non of the stores used to store a file may fail. Otherwise the file cannot be recovered.
	 * See {@link #getNrOfRedundantFragments() getNrOfRedundantFragments()} for the exact formula details.
	 * @param redundancy
	 */
	public void setRedundancy(int redundancy){
		this.redundancy = redundancy;
	}
	
	public int getRedundancy(){
		return redundancy;
	}

	public void setFileSize(long filesize){
		this.filesize = filesize;
	}
	
	public long getFileSize(){
		return filesize;
	}
	
	/**
	 * @return false
	 */
	public boolean changeToCurrentStrategy(StorageStrategy other){
		return false;
	}


	/**
	 * Get the directories to store the file fragments to
	 * Returns all potential storages.
	 * @return a list of directory paths 
	 */
	@Override
	public List<String> getFragmentDirectories() {
		return potentialStorageDirectories;
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * Returns the number of potential stores minus one multiplied by the redundancy. If  the resulting number has a fractional part, the next lower integer is returned. 
	 * For instance, 8 stores and a redundancy of 50% (x = (8-1)*0.5 = 3.5) result in a return value of 3 redundant fragments.  
	 * @return the number of redundant fragments 
	 */
	@Override
	public int getNrOfRedundantFragments() {
		int nrOfFileStores =  potentialStorageDirectories.size();
		int nrOfRedundantFragments = (int) ((nrOfFileStores-1) * (redundancy /100f));
		return nrOfRedundantFragments;
	}

	/**
	 * Get the number of file fragments that must be stored successfully.
	 * The number equals one plus the number of redundant fragments returned by {@link #getNrOfRedundantFragments() getNrOfRedundantFragments()}.
	 * @return the number of file fragments that must be stored
	 */
	@Override
	public int getNrOfRequiredSuccessfullyStoredFragments() {
		return 1 +  getNrOfRedundantFragments(); 
	}

}
