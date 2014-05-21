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
	private OptimalRedundancyStategy optimalRedundancy = null;
	private OptimalRedundancyStategy deviationOptimalRedundancy = null;
	private BackendServices services;
	private boolean changeToCurrentStrategy = true;
	private StorageStrategy previousStorageStrategy = null;
	private Set<String> dataDirPaths = new TreeSet<String>();
	public static class StrategyNames {
		public static final String 
				ROUNDROBIN = "RoundRobin", 
				USEALLINPARALLEL = "UseAllInParallel", 
				OPTIMALREDUNDANCY = "OptimalRedundancy", 
				DEVIATIONOPTIMALREDUNDANCY = "DeviationOptimalRedundancy";
	}
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
		if(strategyName.equalsIgnoreCase(StrategyNames.ROUNDROBIN)){
			if(roundRobin == null || ! dataDirPaths.equals(new TreeSet<String>(services.getDataDirPaths()))){
				roundRobin = new RoundRobinStorageStrategy(services);
			}
			roundRobin.setRedundancy(redundancy);
			changeToCurrentStrategy = roundRobin.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = roundRobin;
			ret = roundRobin;
		} else if(strategyName.equalsIgnoreCase(StrategyNames.USEALLINPARALLEL))  { 
			if(useInParallel == null){
				useInParallel = new UseAllInParallelStorageStrategy(services);
			}
			useInParallel.setRedundancy(redundancy);
			changeToCurrentStrategy = useInParallel.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = useInParallel;
			ret = useInParallel;
		} else if(strategyName.equalsIgnoreCase(StrategyNames.OPTIMALREDUNDANCY))  { 
			if(optimalRedundancy == null){
				optimalRedundancy = new OptimalRedundancyStategy(services);
			}
			optimalRedundancy.setRedundancy(redundancy);
			changeToCurrentStrategy = optimalRedundancy.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = optimalRedundancy;
			ret = optimalRedundancy;
		} else { 
			if(deviationOptimalRedundancy == null){
				deviationOptimalRedundancy = new DeviationOptimalRedundancyStategy(services);
			}
			deviationOptimalRedundancy.setRedundancy(redundancy);
			changeToCurrentStrategy = deviationOptimalRedundancy.changeToCurrentStrategy(previousStorageStrategy);
			previousStorageStrategy = deviationOptimalRedundancy;
			ret = deviationOptimalRedundancy;
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
