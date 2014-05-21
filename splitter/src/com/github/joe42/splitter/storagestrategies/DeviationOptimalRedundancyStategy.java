package com.github.joe42.splitter.storagestrategies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;


import org.apache.log4j.Logger;
import org.python.antlr.PythonParser.continue_stmt_return;
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
		Map<String,Integer> nrOfElementsMap = getFragmentNameToNrOfElementsMap();
		int nrOfElements = 0;
		for(Integer elements: nrOfElementsMap.values()){
			System.out.println("elements for store x:"+elements);
			nrOfElements += elements;
		}
		return nrOfElements;
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
			System.out.println(s.getName()+" av: "+s.getAvailability()+" filepts: "+s.getNrOfFilePartsToStore()+" elements: "+nrOfElementsMap.get(s.getDataDirPath()));
			availabilityList.add(new PyTuple(new PyFloat(s.getAvailability()), new PyInteger(s.getNrOfFilePartsToStore()*nrOfElementsMap.get(s.getDataDirPath()))));
		}
		int nrOfRequiredElements = getNrOfElements() - getNrOfRedundantFragments(); 
		System.out.println("k = "+nrOfRequiredElements);
		System.out.println("av = "+availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList));
		return availabilityCalculator.getAvailability(new PyInteger(nrOfRequiredElements), availabilityList);
	}


	public Map<String,Integer> getFragmentNameToNrOfElementsMap() {
		//sort input for deterministic output of specialRound, independent of list order
		Map<String,Integer> ret = new HashMap<String, Integer>();
		List<String> fragmentNames = services.getDataDirPaths();
		for(String fragmentName: fragmentNames) {
			ret.put(fragmentName, 1);
		}
		Map<String,Integer> sortedMap = sortMapByAvailability(ret);
		boolean firstIteration = true;
		double availabilityOfPreviousEntry = 0;
		double differenceToPreviousEntry = 0;
		int elementsToAdd = 0;
		for(Entry<String, Integer> entry: sortedMap.entrySet()) {
			if(firstIteration) {
				availabilityOfPreviousEntry = getBackendService(entry.getKey()).getAvailability();
				firstIteration = false;
				continue;
			}
			differenceToPreviousEntry = getBackendService(entry.getKey()).getAvailability() - availabilityOfPreviousEntry;
			differenceToPreviousEntry = Math.round( differenceToPreviousEntry *10 ) / 10d;
			System.out.println("differenceToPreviousEntry:"+differenceToPreviousEntry);
			System.out.println("availabilityOfPreviousEntry:"+availabilityOfPreviousEntry);
			if(differenceToPreviousEntry >= 0.1) {
				elementsToAdd++;
				availabilityOfPreviousEntry = getBackendService(entry.getKey()).getAvailability();
			}
			ret.put(entry.getKey(), entry.getValue() + elementsToAdd);
		}
		return ret;
	}

	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored. 
	 * @return the number of redundant fragments 
	 */
	@Override
	public int getNrOfRedundantFragments() {
		int MAX_REDUNDANT_ELEMENTS = getNrOfElements()-1;
		int nrOfRedundantFragments = (int) (MAX_REDUNDANT_ELEMENTS * (redundancy /100f));
		System.out.println("redundand fragments:"+nrOfRedundantFragments);
		return nrOfRedundantFragments;
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
		return 1+1d*getNrOfRedundantFragments()/getNrOfFilePartsNeededToReconstructFile();
	}
	
	@Override
	protected int getNrOfFilePartsNeededToReconstructFile() {
		return getNrOfElements()-getNrOfRedundantFragments();
	}
}
