package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyTuple;


import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;

/**
 * Uses all potential storage directories.
 */
public class DeviationOptimalRedundancyStategy extends OptimalRedundancyStategy {

	/**
	 * Creates a DeviationOptimalRedundancyStategy object using all potentialStorageDirectories in parallel, distributing 
	 * data according to availability deviation of storages.
	 * @param storageServices
	 */
	public DeviationOptimalRedundancyStategy(BackendServices storageServices){
		super(storageServices);
	}
	
	/**
	 * @return Number of elements produced by the cauchy reed solomon codec
	 */
	public int getNrOfElements(){
		return getNrOfElements(getNrOfEquivalenceClasses()); 
	}

	protected int getNrOfElements(int equivalenceClasses){
		Map<String,Integer> nrOfElementsMap = getFragmentNameToNrOfElementsMap(equivalenceClasses);
		int nrOfElements = 0;
		for(Integer elements: nrOfElementsMap.values()){
			nrOfElements += elements;
		}
		return nrOfElements;
	}

	/**
	 * @return the availability of the file stored in percent according to this storage strategy and the storage services' availability
	 */
	@Override
	public double getStorageAvailability(){
		return getStorageAvailability(getNrOfEquivalenceClasses()); 
	}

	protected List<Double> getSortedAvailabilitiesList(){
		List<Double> ret = new ArrayList<Double>();
		List<String> fragmentNames = services.getDataDirPaths();
		for(String fragmentName: fragmentNames) {
			ret.add(getBackendService(fragmentName).getAvailability());
		}
		Collections.sort(ret);
		return ret;
	}
	
	protected Integer getNrOfEquivalenceClasses() { 
		Integer ret = 1;
		List<Double> availabilities = getSortedAvailabilitiesList();
		Double maxDiff = Collections.max(availabilities) - Collections.min(availabilities);
		if(maxDiff == 0) {			
			return ret;
		}
		int maxReplicationFactor = potentialStorageDirectories.size(); //at most one replica per store
		double previousAvailability = getStorageAvailability(1); 
		double previousDistanceToDesiredRedundancyFactor = 999999999;
		for(int d=1; d<15; d++) {
			double step_width = maxDiff / d;
			int classes = 1;
			double limit = Collections.min(availabilities) + step_width;
			for(double av: availabilities){
				if(av > limit){
					limit += step_width; 
					classes++;
				}
			}
			if(getNrOfElements(classes) > 30){
				return ret;
			}
			double newAvailability = getStorageAvailability(classes);
			double desiredRedundancyFactor = maxReplicationFactor * (redundancy /100d);
			double distanceToDesiredRedundancyFactor = Math.abs(desiredRedundancyFactor - getStorageRedundancy(classes));
	        if(previousAvailability < newAvailability && previousDistanceToDesiredRedundancyFactor > distanceToDesiredRedundancyFactor){
	            previousAvailability = newAvailability;
	            previousDistanceToDesiredRedundancyFactor = distanceToDesiredRedundancyFactor;
	            ret = classes;
	        }
		}
		return ret;
	}
	
	protected double getStorageAvailability(int equivalenceClasses) {
		if(potentialStorageDirectories.size() == 0) {
			return 0;
		}		
		List<BackendService> storageServices = services.getFrontEndStorageServices();

		Map<String,Integer> nrOfElementsMap = getFragmentNameToNrOfElementsMap(equivalenceClasses);
		PyList availabilityList = new PyList();
		for(BackendService s: storageServices){
			//System.out.println(s.getName()+" av: "+s.getAvailability()+" filepts: "+s.getNrOfFilePartsToStore()+" elements: "+nrOfElementsMap.get(s.getDataDirPath()));
			availabilityList.add(new PyTuple(new PyFloat(s.getAvailability()), new PyInteger(s.getNrOfFilePartsToStore()*nrOfElementsMap.get(s.getDataDirPath()))));
		}
		int nrOfRequiredElements = getNrOfElements(equivalenceClasses) - getNrOfRedundantFragments(equivalenceClasses);  
		//System.out.println("k = "+nrOfRequiredElements);
		//System.out.println("av = "+availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList));
		return availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList);
	}

	public Map<String,Integer> getFragmentNameToNrOfElementsMap(int equivalenceClasses) { 
		Map<String,Integer> ret = new HashMap<String, Integer>();
		List<String> fragmentNames = services.getDataDirPaths();
		for(String fragmentName: fragmentNames) {
			ret.put(fragmentName, 1);
		}
		Map<String,Integer> sortedMap = sortMapByAvailability(ret);
		List<Double> availabilities = getSortedAvailabilitiesList();
		Double maxDiff = Collections.max(availabilities) - Collections.min(availabilities);
		double step_width = maxDiff / equivalenceClasses;
		double limit = Collections.min(availabilities) + step_width;
		int elementsToAdd = 0;
		for(Entry<String, Integer> entry: sortedMap.entrySet()) {
			if(getBackendService(entry.getKey()).getAvailability() > limit) {
				elementsToAdd++;
				limit += step_width;
			}
			ret.put(entry.getKey(), entry.getValue() + elementsToAdd);
		}
		return ret;
	}
	

	protected int getNrOfRedundantFragments(int equivalenceClasses) {
		int maxReplicationFactor = potentialStorageDirectories.size(); //at most one replica per store
		int n = getNrOfElements(equivalenceClasses);
		int k = (int) (n / ( maxReplicationFactor * (redundancy /100d)));
		int m = n - k;
		if(m <= 0){
			m = 0;
		}
		//System.out.println("n:"+n+" k:"+k+" m:"+m+" maxrepl.fac:"+maxReplicationFactor+" redundancy:"+redundancy+ " red.factor:"+(1.0*n/k));
		
		return m;
	}
	

	protected int getNrOfRequiredSuccessfullyStoredFragments(int equivalenceClasses) {
		return potentialStorageDirectories.size(); 
	}

	protected double getStorageRedundancy(int equivalenceClasses) {
		return 1d*getNrOfElements(equivalenceClasses)/getNrOfFilePartsNeededToReconstructFile(equivalenceClasses); // n/k == 1+m/k
	}
	
	protected int getNrOfFilePartsNeededToReconstructFile(int equivalenceClasses) {
		return getNrOfElements(equivalenceClasses)-getNrOfRedundantFragments(equivalenceClasses);
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * @return the number of redundant fragments 
	 */
	@Override
	public int getNrOfRedundantFragments() {
		return getNrOfRedundantFragments(getNrOfEquivalenceClasses());
	}

	/**
	 * Get the number of file fragments that must be stored successfully.
	 * The number equals the number of all fragments.
	 * @return the number of file fragments that must be stored
	 */
	@Override
	public int getNrOfRequiredSuccessfullyStoredFragments() {
		return potentialStorageDirectories.size(); 
	}

	@Override
	public double getStorageRedundancy() {
		return getStorageRedundancy(getNrOfEquivalenceClasses());
	}
	
	@Override
	protected int getNrOfFilePartsNeededToReconstructFile() {
		return getNrOfFilePartsNeededToReconstructFile(getNrOfEquivalenceClasses());
	}

	@Override
	public Map<String, String> getCodecInfo() {
		TreeMap<String, String> ret = new TreeMap<String, String>();
		ret.put("erasure code elements",new Integer(getNrOfElements()).toString());
		ret.put("redundant erasure code elements",new Integer(getNrOfRedundantFragments()).toString());
		return ret;
	}
}
