package com.github.joe42.splitter.backend;

import java.io.IOException;
import java.util.*;

import org.ini4j.Ini;

import com.github.joe42.splitter.util.file.IniUtil;
/**
 * A storage service that can be mounted and unmounted in the file system.
 * It may represent either a front end storage used to store files in or a back end services, which might be used by one or more front end storages.
 */
public class BackendService implements StorageService{
	private String path, name, mountCommand;
	private String store;
	private Boolean isBackendModule;
	private Integer nrOfFilePartsToStore;
	private Double availability;

	private BackendService(String store, String name){
		/**
		 * Creates a backend service.
		 * @param storages the directory where this services should be mounted as a subfolder
		 * @param name the unique name of the service, which should later be the name of the folder where the service is mounted to
		 */
		this.store = store;
		this.path = store;		
		this.name = name;
		mountCommand = "";
	}
	public BackendService(String store, String name, Ini options){
		/**
		 * Creates a backend service.
		 * @param storages the directory where this services should be mounted as a subfolder
		 * @param name the unique name of the service, which should later be the name of the folder where the service is mounted to
		 * @param options see {@link #configure(String, Ini) configure} for a list of possible options
		 * 					
		 */
		this(store,name);
		configure(name, options);
	}
	
	/**
	 * Configure this storage via an ini file.
	 * The ini file should contain a command in the section mounting with the key command, which is run to mount the storage. 
	 * If the command contains a parameter of the name mountpoint, then it will be substituted by store+"/"+name.
	 * Further the file may contain a parameter section with options, where each word in the command is substituted
	 * by the value of the parameter option of the same name. 
	 * 	
	 * There can be a section splitter containing the following optional options to configure
	 * the use of the file system by splitter: 
	 * The isbackendmodule option decides whether the service 
	 * should be mounted in the special directory StorageService.HIDDEN_DIR_NAME. 
	 * To this end the mountpoint parameter is substituted by the path
	 * this.getStorages()+"/"+StorageService.HIDDEN_DIR_NAME+"/"+uniqueServiceName  instead.
	 * Choose a storage strategy for the splitter by setting the storagestrategy option to roundrobin or useallinparallel. 
	 * The fileparts option is an integer which may be used by a storage strategy to choose how intensely a storage shall be used.
	 * Commonly, fileparts is used to determine how many file parts are distributed to the store after splitting up a file.  
	 * @param name
	 * @param options
	 */
	public void configure(String name, Ini options) {
		isBackendModule = IniUtil.get(options, "splitter", "isbackendmodule", Boolean.class);
		if(isBackendModule == null){
			isBackendModule = false;
		}
		if(isBackendModule){
			path += "/"+HIDDEN_DIR_NAME;	
		}
		path += "/"+name;
		mountCommand = IniUtil.get(options, "mounting", "command");
		//TODO: if mountCommand == null log or exception
		mountCommand = substituteCommandParameters(options);
		nrOfFilePartsToStore = IniUtil.get(options, "splitter", "fileparts", Integer.class);
		if(nrOfFilePartsToStore == null){
			nrOfFilePartsToStore = 0;
		}
		availability = IniUtil.get(options, "splitter", "availability", Double.class);
		if(availability == null){
			availability = 0d;
		}
	}
	
	private String substituteCommandParameters(Ini config){
		/**
		 * @return the mountcommand with each parameter is substituted by the value of the  option of the same name in the parameter section of config
		 */
		String substitutedCommand = "";
		Map<String, String> substitutions = config.get("parameter");
		for(String word: mountCommand.split(" ")){
			if(word.equals("mountpoint")){
				word = path;
			} else if(substitutions != null){
				if( substitutions.containsKey(word) ){
					if(word.startsWith("backendservice")){
						word = store+"/"+HIDDEN_DIR_NAME+"/"+substitutions.get(word);
					} else {
						word = substitutions.get(word);
					}
				}
			}
			substitutedCommand += word+" ";
		}
		return substitutedCommand;
	}

	/**
	 * @return true iff this service is a backend of another preprocessing service
	 */
	public boolean isBackendModule(){
		return isBackendModule;
	}
	
	/** @return the path where this service is mounted */
	public String getPath(){
		return path;
	}
	/** @return the directory where this service is mounted as a subfolder*/
	public String getStore(){
		return store;
	}
	/** @return the command to run to mount this service */
	public String getMountcommand(){
		return mountCommand;
	}
	/** @return the name of this service */
	public String getName(){
		return name;
	}
	/** @return the path where this storage service keeps its data */
	public String getDataDirPath(){
		return path+DATA_DIR;
	}
	/** @return the path where this storage service keeps its configuration files */
	public String getConfigDirPath(){
		return path+CONFIG_DIR;
	}
	/** @return the path of this storage service's configuration file */
	public String getConfigFilePath(){
		return path+CONFIG_PATH;
	}
	/** Executes this.getMountcommand() to mount the service at this.getPath()
	 * This should create a directory this.getPath()+DATA_DIR, where files may be stored 
	 * and a file this.getPath()+CONFIG_PATH advertising the success of mounting the service 
	 * and providing a place to write the configuration file to.
	 * */
	public void mount() throws IOException {
		new ProcessBuilder( "/bin/bash", "-c", mountCommand ).start();
	}
	/** Executes fusermount -uz to unmount the service at this.getPath() and this.getPah()+"/data" */
	public void unmount() throws IOException {
		//TODO: add unmountcommand section to option file
		Runtime rt =  Runtime.getRuntime();
		rt .exec("fusermount -uz "+path);
		rt .exec("fusermount -uz "+path+"/data");
	}
	public int getNrOfFilePartsToStore() {
		return nrOfFilePartsToStore;
	}
	public double getAvailability() {
		return availability;
	}
	
	/**
	 * Return true iff o is an instance of BackendService and its attributes are equal.
	 * The attributes that are compared are:
	 * path, name, mountCommand, store, isBackendModule, nrOfFilePartsToStore, and availability.
	 */
	@Override
	public boolean equals(Object o){
		if( ! (o instanceof BackendServices) ){
			return false;
		}
		boolean ret = true;
		BackendService service = (BackendService) o;
		ret &= path.equals(service.path);
		ret &= name.equals(service.name);
		ret &= mountCommand.equals(service.mountCommand);
		ret &= store.equals(service.store);
		ret &= isBackendModule.equals(service.isBackendModule);
		ret &= nrOfFilePartsToStore.equals(service.nrOfFilePartsToStore);
		ret &= availability.equals(service.availability);
		return ret;
	}
	
	@Override
	public int hashCode(){
		int ret = path.hashCode();
		ret += name.hashCode();
		ret += mountCommand.hashCode();
		ret += isBackendModule.hashCode();
		ret += nrOfFilePartsToStore.hashCode();
		ret += availability.hashCode();
		return ret;
	}
}
