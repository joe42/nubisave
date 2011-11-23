package com.github.joe42.splitter.backend;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.ini4j.Ini;


public class Mounter {
	private String storages;
	private Map<String, BackendService> services;
	/**
	 * For mounting the FUSE modules.
	 * */
	public Mounter(String storages){
		this.storages = storages;
		services = new HashMap<String, BackendService>();
	}
	/**
	 * Get the current map of services
	 * @return a Map object mapping the service's names to the BackendService objects
	 */
	public Map<String, BackendService> getServices(){
		return services;
	}
	
	public String mount(String uniqueServiceName, Ini mountOptions){
		/**
		 * Execute the command given in mountOption's mounting section, which should mount a filesystem. 
		 * The command's parameters of the name mountpoint will be substituted by the path 
		 * this.getStorages()+"/"+uniqueServiceName. This is where the filesystem should be mounted to. 
		 * Further mountOptions may contain a parameter section with options, where each word in the command is substituted 
		 * by the value of the parameter option of the same name. Last there can be a isbackendmodule option in the section splitter.
		 * This option decides weather the service should be mounted in the special directory StorageService.HIDDEN_DIR_NAME. 
		 * To this end the mountpoint parameter is substituted by the path
		 * this.getStorages()+"/"+StorageService.HIDDEN_DIR_NAME+"/"+uniqueServiceName  instead.
		 * 
		 * The execution should result in a filesystem mounted so that its data can be accessed by the subdirectory called
		 * StorageService.DATA_DIR_NAME in the path where it should be mounted and a configuration StorageService.CONFIG_PATH, 
		 * also in the path, where the filesystem should be mounted.
		 * 
		 * Adds a new backend service to services, if mounting is successful.
		 * 
		 * @param uniqueServiceName the name of the subdirectory, where the filesystem should be mounted into
		 * @param mountOptions an ini file with several mount options for the storage service to mount
		 * @return the path to the mountpoint if the file StorageService.CONFIG_PATH exists  within it after at most 10 seconds and null otherwise
		 * */
		BackendService service = new BackendService(storages, uniqueServiceName, mountOptions);
		try {
			System.out.println("Executing: "+service.getMountcommand());
			service.mount();
			if(! isMounted(service.getConfigFilePath())){
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		services.put(uniqueServiceName, service);
		return service.getPath();
	}
	
	public String getStorages(){
		/**@return the folder with this mounter's mounted filesystems*/
		return storages;
	}
	
	public boolean unmount(String uniqueServiceName){
		/** Executes fusermount -uz to unmount the service at this.getPath() and this.getPah()+"/data"
		 * @return true iff the service was unmounted within 10 seconds
		 */
		try {
			BackendService service = services.get(uniqueServiceName);
			if(service == null){
				return false;
			}
			service.unmount();
			if(! isMounted(service.getConfigFilePath())){
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private boolean isMounted(String configFilePath) {
		/** Waits at most 10 seconds until the file configFilePath exists.
		 * This method is used to determine if a filesystem module is mounted successfully.
		 *  @returns: true iff the file exists after at most 10 seconds
		 */
		System.err.println(configFilePath);
		File configFile = new File(configFilePath);
		int timeUsed = 0;
		while(!configFile.exists() && timeUsed < 1000*10){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			timeUsed += 500;
		}
		return configFile.exists();
	}
	public void moveData(String uniqueServiceNameFrom, String uniqueServiceNameTo) {
		BackendService serviceFrom = services.get(uniqueServiceNameFrom);
		BackendService serviceTo = services.get(uniqueServiceNameTo);
		Runtime rt =  Runtime.getRuntime();
		try {
			System.out.println("mv "+serviceFrom.getDataDirPath()+"/* "+serviceTo.getDataDirPath());
			rt.exec("mv "+serviceFrom.getDataDirPath()+"/* "+serviceTo.getDataDirPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
