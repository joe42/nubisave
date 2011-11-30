package com.github.joe42.splitter.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendServices {
	private Map<String, BackendService> services;
	
	public BackendServices(){
		this.services = new HashMap<String, BackendService>();
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
}
