package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.List;


/**
 * A storage strategy iterating over a list of storages by consecutive calls to {@link #getFragmentDirectories() getFragmentDirectories()}.
 * Consecutive 
 */
public class RoundRobinStorageStrategy implements StorageStrategy {
	private long round;
	private List<String> potentialStorageDirectories;
	private int redundancy;
	private long filesize;
	
	
	/**
	 * Creates a RoundRobinStorageStrategy object with a list of storages to iterate over and a zero redundancy.
	 * @param potentialStorageDirectories
	 */
	public RoundRobinStorageStrategy(List<String> potentialStorageDirectories){
		this.potentialStorageDirectories = potentialStorageDirectories;
		round = 0;
		redundancy = 50;
	}

	public void setPotentialStorageDirectories(List<String> potentialStorageDirectories){
		this.potentialStorageDirectories = potentialStorageDirectories;
	}
	public List<String> getPotentialStorageDirectories(){
		return potentialStorageDirectories;
	}

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
	 * Returns the next potential storage with each call. If the number n of redundant fragments returned by  {@link #getNrOfRedundantFragments() getNrOfRedundantFragments()} is greater than 0,
	 * the next 1+n storages are returned, each time starting at the last storage returned.
	 * @return a list of directory paths 
	 */
	@Override
	public List<String> getFragmentDirectories() {
		int nrOfFragments = getNrOfRedundantFragments() +1;
		List<String> ret = new ArrayList<String>();
		for(long i=round; i<round+nrOfFragments;i++){
			ret.add(potentialStorageDirectories.get((int) (i%potentialStorageDirectories.size())));
		}
		round++;
		return ret;
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * Returns the number of potential stores minus one multiplied by the redundancy. If  the resulting number has a fractional part, the next higher integer is returned. 
	 * For instance, 9 stores and a redundancy of 50% (x = (9-1)*0.5 = 3.5) result in a return value of 4 redundant stores.  
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
		return 1;// +  getNrOfRedundantFragments();
	}

}
