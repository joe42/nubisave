package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
//TODO: check if backendservices changed here, but not with equals because backendservices is a reference that would be changed as well?
/**
 * Responsible for creating, configuring and pooling StorageStrategies.
 */
public class StorageStrategyFactory {
	private RoundRobinStorageStrategy roundRobin = null;
	private UseAllInParallelStorageStrategy useInParallel = null;
	private BackendServices services;
	private boolean changeToCurrentStrategy = true;
	private StorageStrategy previousStorageStrategy = null,  currentStorageStrategy = null;
	private Set<String> dataDirPaths = new TreeSet<String>();
	public StorageStrategyFactory(BackendServices services){
		this.services = services;
	}

	/**
	 * Get a strategy, possibly creating and (re)configuring it.
	 * @param strategyName
	 * @param redundancy
	 * @return
	 */
	public StorageStrategy createStrategy(String strategyName, int redundancy) {
		currentStorageStrategy = null;
		if(strategyName.equals("UseAllInParallel")){
			if(useInParallel == null){
				useInParallel = new UseAllInParallelStorageStrategy(services);
			}
			useInParallel.setRedundancy(redundancy);
			changeToCurrentStrategy = useInParallel.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = useInParallel;
			currentStorageStrategy = useInParallel;
		} else { //RoundRobin as default
			if(roundRobin == null || ! dataDirPaths.equals(new TreeSet<String>(services.getDataDirPaths()))){
				roundRobin = new RoundRobinStorageStrategy(services);
			}
			roundRobin.setRedundancy(redundancy);
			changeToCurrentStrategy = roundRobin.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = roundRobin;
			currentStorageStrategy = roundRobin;
		}
		return currentStorageStrategy;
	}
	
	/**
	 * @return true iff the the splitter should change his already stored files according to this storage strategy's attributes 
	 */
	public boolean changeToCurrentStrategy(){
		return changeToCurrentStrategy ;
	}
	
	/**
	 * Get the minimal availability of files achieved by the current StorageStrategy instance.
	 * The calculation considers the availability of the storages on which the {@link #getFragmentDirectories() fragment directories} are located.
	 * The availability of NubiSave itself does not need to be considered.
	 * @return the availability in percent
	 */
	public double getStorageAvailability(){
		return currentStorageStrategy.getStorageAvailability(); //forward call to the respective storage strategy instance
	}
}
