package com.github.joe42.splitter.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

public class BackendServices {
	private Map<String, BackendService> services;
	
	public BackendServices(){
		this.services = new HashMap<String, BackendService>();
	}
	
	/**
	 * Return true iff o is an instance of BackendServices and maps the same service names to the same BackendService instances.
	 */
	@Override
	public boolean equals(Object o){
		if( ! (o instanceof BackendServices) ){
			return false;
		}
		return ((BackendServices)o).services.equals(services);
	}
	
	/**
	 * Adds a BackendService object
	 * @param serviceName the name of the service
	 * @param service the BackendService object
	 */
	public void add(String serviceName, BackendService service){
		services.put(serviceName, service);
	}
	
	/**
	 * Removes a service by its name
	 * @param serviceName the name of a BackendService
	 */
	public void remove(String serviceName){
		services.remove(serviceName);
	}

	/**
	 * Get a BackendService object 
	 * @param serviceName the name of the BackendService object
	 * @return a BackendService object with the specified name or null if the service is not managed by this BackendServices' instance
	 */
	public BackendService get(String serviceName){
		return services.get(serviceName);
	}
	
	/**
	 * Get a BackendService object 
	 * @param dataDirPath the data directory path of the BackendService object
	 * @return a BackendService object with the specified data directory path or null if the service is not managed by this BackendServices' instance
	 */
	public BackendService getServiceByDataDirPath(String dataDirPath){
		for(BackendService service: services.values()){
			if(service.getDataDirPath().equals(dataDirPath)){
				return service;
			}
		}
		return null;
	}
	
	/**
	 * @return the front end storage services' directory for data access
	 */
	public List<String> getDataDirPaths() {
		List<String> ret = new ArrayList<String>();
		for(BackendService service: getFrontEndStorageServices()){
				ret.add(service.getDataDirPath());
		}
		return ret;
	}

	/**
	 * @return the front end storage services
	 */
	public List<BackendService> getFrontEndStorageServices() {
		List<BackendService> storageServices = new ArrayList<BackendService>();
		for(BackendService service: services.values()){
			if(! service.isBackendModule()){
				storageServices.add(service);
			}
		}
		return storageServices;
	}

	/**
	 * @param storageServiceCombination
	 * @return number of file parts stored in storageServiceCombination for each stored file
	 */
	public static int getNrOfFilePartsOfCombination(
			Set<BackendService> storageServiceCombination) {
		int fileParts = 0;
		for(BackendService storageService: storageServiceCombination){
			fileParts += storageService.getNrOfFilePartsToStore();
		}
		return fileParts;
	}

	/**
	 * Get probability of only the storageServiceCombination being available and no other front end storage
	 * @param storageServiceCombination subset of front end storage services
	 * @return probability for the storageServiceCombination being available exclusively
	 */
	public double getExclusiveAvailabilityOfStorageCombination(Set<BackendService> storageServiceCombination) {
		double availability = 0;
		availability += getAvailabilityOfCombination(storageServiceCombination);
		availability -= getUnavailabilityOfCombination( (Set<BackendService>) CollectionUtils.disjunction(getFrontEndStorageServices(), storageServiceCombination) );
		return availability;
	}

	/**
	 * Get the probability of a combination of stores being unavailable
	 * If the combination of stores cannot be used to reconstruct a stored file, 0 is returned.
	 * @param storageServiceCombination 
	 * @return unavailability of a combination of stores
	 */
	public static double getUnavailabilityOfCombination(Set<BackendService> storageServiceCombination) {
		double unavailabilityOfCombination;
		unavailabilityOfCombination = 0;
		for(BackendService storageService: storageServiceCombination){
			if (unavailabilityOfCombination == 0){
				unavailabilityOfCombination = 1-storageService.getAvailability();
			} else{
				unavailabilityOfCombination *= 1-storageService.getAvailability();
			}
		}
		return unavailabilityOfCombination;
	}

	/**
	 * Get the availability of a combination of stores
	 * If the combination of stores cannot be used to reconstruct a stored file, 0 is returned.
	 * @param storageServiceCombination 
	 * @return availability of a combination of stores
	 */
	public static double getAvailabilityOfCombination(Set<BackendService> storageServiceCombination) {
		double availabilityOfCombination;
		availabilityOfCombination = 0;
		for(BackendService storageService: storageServiceCombination){
			if (availabilityOfCombination == 0){
				availabilityOfCombination = storageService.getAvailability();
			} else{
				availabilityOfCombination *= storageService.getAvailability();
			}
		}
		return availabilityOfCombination;
	}

	public Set<BackendService> getStorageServicesFromStorageDirectories(
			List<String> storageDirectoryCombination) {
		Set<BackendService> storages = new HashSet<BackendService>();
		for(String storageDirectory: storageDirectoryCombination){
			storages.add(getServiceByDataDirPath(storageDirectory));
		}
		return storages;
	}
}
