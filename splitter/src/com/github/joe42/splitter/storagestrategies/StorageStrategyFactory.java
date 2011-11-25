package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;

/**
 * Responsible for creating, configuring and pooling StorageStrategies.
 */
public class StorageStrategyFactory {
	private RoundRobinStorageStrategy roundRobin = null;
	private BackendServices services;
	private boolean changeToCurrentStrategy = true;
	private StorageStrategy previousStorageStrategy = null;
	
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
		if(strategyName.equals("RoundRobin")){
			if(roundRobin == null){
				roundRobin = new RoundRobinStorageStrategy(services.getDataDirPaths());
			}
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
