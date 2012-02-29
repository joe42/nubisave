package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.SetUtils;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.util.math.SetUtil;


/**
 * Iterates over a list of storages by consecutive calls to {@link #getFragmentDirectories() getFragmentDirectories()}.
 */
public class RoundRobinStorageStrategy implements StorageStrategy, Observer {
	private long round;
	private int redundancy;
	private long filesize;
	private BackendServices storageServices;
	private ArrayList<String> potentialStorageDirectories;
	private boolean storageServicesHaveChanged;
	
	
	/**
	 * Creates a RoundRobinStorageStrategy object with a list of storages to iterate over and with half the number of #potentialStorageDirectories - 1 as expendable stores.
	 * @param storageServices the storage service used by this storage strategy
	 */
	public RoundRobinStorageStrategy(BackendServices storageServices){
		this.storageServices = storageServices;
		storageServices.addObserver(this);
		storageServicesHaveChanged = true;
		update();
		round = 0;
		redundancy = 50;
	}

	public void setStorageServices(BackendServices storageServices){
		this.storageServices = storageServices;
		storageServicesHaveChanged = true;
	}
	
	public BackendServices getStorageServices(){
		return storageServices;
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
	 * Returns the next potential storage with each call. If the number n of redundant fragments returned by  {@link #getNrOfRedundantFragments() getNrOfRedundantFragments()} is greater than 0,
	 * the next 1+n storages are returned, each time starting at the last storage returned.
	 * @return a list of directory paths 
	 */
	@Override
	public List<String> getFragmentDirectories() {
		int nrOfFragments = getNrOfRedundantFragments() +1;
		List<String> ret = new ArrayList<String>();
		for(long i=(round*nrOfFragments); i<(round*nrOfFragments)+nrOfFragments;i++){
			ret.add(potentialStorageDirectories.get((int) (i%potentialStorageDirectories.size())));
		}
		round++;
		return ret;
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * Returns the number of potential stores minus one multiplied by the redundancy. If  the resulting number has a fractional part, the next lower integer is returned. 
	 * For instance, 8 stores and a redundancy of 50% (x = (8-1)*0.5 = 3.5) result in a return value of 3 redundant fragments.  
	 * @return the number of redundant fragments 
	 */
	@Override
	public int getNrOfRedundantFragments() {
		int nrOfFileStores = potentialStorageDirectories.size();
		int nrOfRedundantFragments = (int) ((nrOfFileStores-1) * (redundancy /100f));
		return nrOfRedundantFragments;
	}

	/**
	 * Get the number of file fragments that must be stored successfully.
	 * Return 1 for higher speed.
	 * @return the number of file fragments that must be stored
	 */
	@Override
	public int getNrOfRequiredSuccessfullyStoredFragments() {
		return 1;
	}
	

	/**
	 * @return the minimal availability of the file stored according to this StorageStrategy instance in percent
	 */
	@Override
	public double getStorageAvailability(){
		double availability = 0;
		double combinationAvailability = 0;
		Set<Set<BackendService>> storageCombinations = getStorageCombinations();
		for(Set<BackendService> storageCombination: storageCombinations ){
			//get availability of file stored in combination (sum of probabilities of all combinations, where at least one store is available in strageCombination)
			combinationAvailability = storageServices.getExclusiveAvailabilityOfStorageCombination(storageCombination);
			if (availability > combinationAvailability){
				availability = combinationAvailability;
			}
		}
		return availability;
	}

	/**
	 * @return a set of storages containing all combinations of storages used by
	 *         this strategy
	 */
	private Set<Set<BackendService>> getStorageCombinations() {
		Set<Set<BackendService>> combinations = new HashSet<Set<BackendService>>();
		Set<BackendService> combination = new HashSet<BackendService>(
				storageServices.getFrontEndStorageServices());
		List<String> storageDirectoryCombination;
		int nrOfFragments = getNrOfRedundantFragments() + 1;
		round = 0;
		while (true) {
			storageDirectoryCombination = new ArrayList<String>();
			for (long i = round; i < round + nrOfFragments; i++) {
				storageDirectoryCombination.add(potentialStorageDirectories
						.get((int) (i % potentialStorageDirectories.size())));
			}
			round += getNrOfRedundantFragments() + 1;
			combination = storageServices
					.getStorageServicesFromStorageDirectories(storageDirectoryCombination);
			if (combinations.contains(combination)) {
				return combinations;
			}
			combinations.add(combination); 
		}
	}
	
	/**
	 * Put the storage strategy into a consistent state. 
	 * Should be called after a storage service has changed.
	 */
	public void update() {
		if(storageServicesHaveChanged){
			storageServicesHaveChanged = false;
			potentialStorageDirectories = new ArrayList<String>();
			for(BackendService storageService: storageServices.getFrontEndStorageServices()){
				if(storageService.getNrOfFilePartsToStore()>0){
					potentialStorageDirectories.add(storageService.getDataDirPath());
				}
			}
		}
	}
	
	/**
	 * Sets a flag, that this storage strategy's services have been changed
	 * {@link #update() update()} must be called afterwards, to put the storage strategy into a consistent state.
	 */
	@Override
	public void update(Observable storageServices, Object arg1) {
		storageServicesHaveChanged = true;
	}
}
