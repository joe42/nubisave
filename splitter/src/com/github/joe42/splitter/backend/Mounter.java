package com.github.joe42.splitter.backend;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

/**
 *Mounts and unmounts BackendService file systems.
 */
public class Mounter {

	private String storages;
	private static final Logger  log = Logger.getLogger("Mounter");

	public Mounter(String storages) {
		this.storages = storages;	
	}

	public String getStorages() {
		return storages;
	}

	public void setStorages(String storages) {
		this.storages = storages;
	}

	/** Waits at most 10 seconds until the file configFilePath exists.
	 * This method is used to determine if a filesystem module is mounted successfully.
	 *  @param configFilePath 
	 * @returns: true iff the file exists after at most 10 seconds
	 */
	private boolean isMounted(String configFilePath) {
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
			
	/**
	 * Execute the command given in mountOption's mounting section, which should mount a file system.
	 * 
	 * The execution should result in a file system mounted so that its data can be accessed by the subdirectory called
	 * StorageService.DATA_DIR_NAME in the path where it should be mounted and a configuration StorageService.CONFIG_PATH, 
	 * also in the path, where the file system should be mounted.
	 * 
	 * @param storage BackendService instance to mount
	 * @return the path to the mountpoint if the file StorageService.CONFIG_PATH exists  within it after at most 10 seconds and null otherwise
	 * */
	public boolean mount(BackendService storage){
		try {
			log.info("Executing: "+storage.getMountcommand());
			storage.mount();
			if(! isMounted(storage.getConfigFilePath())){
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** Executes fusermount -uz to unmount the service at this.getPath() and this.getPath()+"/data"
	 * @param uniqueServiceName TODO
	 * @return true iff the service was unmounted within 10 seconds
	 */
	public boolean unmount(BackendService service){
		try {
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

	/**
	 * @param storage
	 * @return true iff storage's configuration file exists
	 */
	public boolean isStorageMounted(BackendService storage) {
		return new File(storage.getConfigFilePath()).exists();
	}
}