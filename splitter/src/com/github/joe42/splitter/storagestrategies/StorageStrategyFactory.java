package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;

/**
 * Responsible for creating, configuring and pooling StorageStrategies.
 */
public class StorageStrategyFactory {
	public static enum AvailableStorageStrategies {Roundrobin};
	private RoundRobinStorageStrategy roundRobin = null;
	private BackendServices services;
	private boolean changeToCurrentStrategy = true;
	private StorageStrategy previousStorageStrategy = null;
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
		StorageStrategy ret = null;
		if(false){//strategyName.equals("RoundRobin")
		} else { //RoundRobin as default
			if(roundRobin == null || ! dataDirPaths.equals(new TreeSet<String>(services.getDataDirPaths()))){
				dataDirPaths = new TreeSet<String>(services.getDataDirPaths());
				roundRobin = new RoundRobinStorageStrategy(services.getDataDirPaths());
			}
			if( !new TreeSet<String>(roundRobin.getPotentialStorageDirectories()).equals(new TreeSet<String>(services.getDataDirPaths())) ) {
				roundRobin.setPotentialStorageDirectories(services.getDataDirPaths());
			}
			roundRobin.setRedundancy(redundancy);
			changeToCurrentStrategy = roundRobin.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = roundRobin;
			ret = roundRobin;
		}
		return ret;
	}
	
	/**
	 * @return true iff the the splitter should change his already stored files according to this storage strategy's attributes 
	 */
	public boolean changeToCurrentStrategy(){
		return changeToCurrentStrategy ;
	}
}
