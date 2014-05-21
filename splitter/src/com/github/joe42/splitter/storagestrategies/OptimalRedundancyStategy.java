package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyList;
import org.python.core.PyTuple;
import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.util.math.SetUtil;

/**
 * Uses all potential storage directories.
 */
public class OptimalRedundancyStategy extends UseAllInParallelStorageStrategy {
	List<PyList> availabilitiesCacheLists = new ArrayList<PyList>();
	List<Double> availabilitiesCacheValues = new ArrayList<Double>();

	/**
	 * Creates a OptimalRedundancyStategy object using all potentialStorageDirectories in parallel, distributing 
	 * data according to availability of storage.
	 * @param storageServices
	 */
	public OptimalRedundancyStategy(BackendServices storageServices){
		super(storageServices);
	}
	
	public int getNrOfElements(){
		return 100;
	}

	/**
	 * @return the availability of the file stored in percent according to this storage strategy and the storage services' availability
	 */
	@Override
	public double getStorageAvailability(){
		if(potentialStorageDirectories.size() == 0) {
			return 0;
		}		
		List<BackendService> storageServices = services.getFrontEndStorageServices();
		Map<String,Integer> nrOfElementsMap = getFragmentNameToNrOfElementsMap();

		PyList availabilityList = new PyList();
		for(BackendService s: storageServices){
			//System.out.println(s.getName()+" av: "+s.getAvailability()+" filepts: "+s.getNrOfFilePartsToStore()+" elements: "+nrOfElementsMap.get(s.getDataDirPath()));
			availabilityList.add(new PyTuple(new PyFloat(s.getAvailability()), new PyInteger(s.getNrOfFilePartsToStore()*nrOfElementsMap.get(s.getDataDirPath()))));
		}
		int nrOfRequiredElements = getNrOfElements() - getNrOfRedundantFragments(); 
		//System.out.println("k = "+nrOfRequiredElements);
		//System.out.println("av = "+availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList));
		cacheAvailability(availabilityList, availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList));
		return getCachedAvailability(availabilityList);
	}

	private void cacheAvailability(PyList availabilityList,
			double availability) {
		if( ! availabilitiesCacheLists.contains(availabilityList) ) {
			availabilitiesCacheLists.add(availabilityList);
			availabilitiesCacheValues.add(availability);
		}
	}

	private double getCachedAvailability(PyList availabilityList) {
		return availabilitiesCacheValues.get(availabilitiesCacheLists.indexOf(availabilityList));
	}

	public Map<String,Integer> getFragmentNameToNrOfElementsMap() {
		List<String> fragmentNames = services.getDataDirPaths();
		int n = getNrOfElements();
		//sort input for deterministic output of specialRound, independent of list order
		double proportion_factor;
		Map<String,Integer> ret = new HashMap<String, Integer>();
		for(String fragmentName: fragmentNames) {
			proportion_factor = getProportion(fragmentName);
			//System.out.println("blubb:"+n * proportion_factor );
			ret.put(fragmentName, (int) Math.floor(n * proportion_factor));
		}
		remedyDifferenceToNrOfElements(ret);
		return ret;
	}
	
	/**
	 * Makes sure that the absolute number of elements is equal to getNrOfElements().
	 * @param ret
	 */
	protected void remedyDifferenceToNrOfElements(Map<String, Integer> fragmentsToElements) {
		int n = getNrOfElements();
		int sum = 0;
		for(double value: fragmentsToElements.values()) {
			sum += value;
		}
		int rest = n - sum;
		TreeMap<String,Integer> sortedMap = sortMapByAvailability(fragmentsToElements);
		if(rest > 0) { //add elements to the nodes with highest availability
			for(Map.Entry<String,Integer> entry : sortedMap.entrySet()){
				if(rest == 0) {
					break;
				}
				fragmentsToElements.put(entry.getKey(), entry.getValue()+1);
				rest -= 1;
			}			
		} else if(rest < 0) { //remove elements to the nodes with lowest availability
			for(Map.Entry<String,Integer> entry : sortedMap.descendingMap().entrySet()){
				if(rest == 0) {
					break;
				}
				fragmentsToElements.put(entry.getKey(), entry.getValue()-1);
				rest -= 1;
			}		
		}
	}
	
	/**
	 * Get a sorted copy of fragmentsToElements map.
	 * @param fragmentsToElements A map from fragment path to an integer
	 * @return a copy of the fragmetnsToElements map sorted by the availability of the store, the fragment is stored at
	 */
	protected TreeMap<String, Integer> sortMapByAvailability(Map<String, Integer> fragmentsToElements) {
		class AvailabilityComparator implements Comparator<String> {
			// Note: this comparator imposes orderings that are inconsistent with equals.    
			public int compare(String a, String b) {
				if (getBackendService(a).getAvailability() <= getBackendService(b).getAvailability()) {
					return -1;
				} else {
					return 1;
				} // returning 0 would merge keys
			}
		}
		TreeMap<String, Integer> ret = new TreeMap<String, Integer>(new AvailabilityComparator());
		ret.putAll(fragmentsToElements);
		return ret;
	}
	
	/**Get factor for storing an amount of data proportional to the storage's availability*/
	protected double getProportion(String fragmentName) {
		List<BackendService> storageServices = services.getFrontEndStorageServices();
		BackendService service = getBackendService(fragmentName);
		double availability_sum = 0;
		double ret;
		for(BackendService s: storageServices){
			availability_sum += s.getAvailability();
		}
		ret = service.getAvailability() / availability_sum;

		return ret;
	}
	
	protected BackendService getBackendService(String fragmentName) {
		Set<BackendService> storageServices = new HashSet<BackendService>(services.getFrontEndStorageServices());
		for(BackendService s: storageServices){
			if(fragmentName.startsWith(s.getDataDirPath())){
				return s;
			}
		}
		throw new RuntimeException("The file "+fragmentName+" does not belong to any current BackendService instance.");
	}

	protected int getNrOfFilePartsNeededToReconstructFile() {
		return getNrOfElements()-getNrOfRedundantFragments();
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * @return the number of redundant fragments 
	 */
	@Override
	public int getNrOfRedundantFragments() {
		int maxReplicationFactor = potentialStorageDirectories.size(); //at most one replica per store
		int n = getNrOfElements();
		int k = (int) (n / ( maxReplicationFactor * (redundancy /100d)));
		int m = n - k;
		if(m <= 0){
			m = 0;
		}
		return m;
	}

	@Override
	public double getStorageRedundancy() {
		return 1+1d*getNrOfRedundantFragments()/getNrOfFilePartsNeededToReconstructFile();
	}
	
	@Override
	public Map<String, String> getCodecInfo() {
		TreeMap<String, String> ret = new TreeMap<String, String>();
		ret.put("erasure code elements",new Integer(getNrOfElements()).toString());
		ret.put("redundant erasure code elements",new Integer(getNrOfRedundantFragments()).toString());
		return ret;
	}
}
