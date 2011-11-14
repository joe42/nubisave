package com.github.joe42.splitter.backend;

import java.io.IOException;
import java.util.*;

import org.ini4j.Ini;

import com.github.joe42.splitter.util.file.IniUtil;
/**
 * A backend service, which is a storage service that can be mounted and unmounted in the file system.
 */
public class BackendService implements StorageService{
	private String path, name, mountCommand;
	private String store;

	public BackendService(String store, String name){
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
		 * @param options an ini file containing the command in the section mounting with the key command, which is run \
to mount the service. If the command contains a parameter of the name mountpoint, then it will be substituted by store+"/"+name.\
Further it may contain a parameter section with options, where each word in the command is substituted \
by the value of the parameter option of the same name. Last there can be a isbackendmodule option in the section splitter.\
This option decides weather the service should be mounted in the special directory HIDDEN_DIR_NAME. \
To this end the mountpoint parameter is substituted by store+"/"+HIDDEN_DIR_NAME+"/"+name  instead.
		 * 					
		 */
		this(store,name);
		Boolean isBackendModule = IniUtil.get(options, "splitter", "isbackendmodule", Boolean.class);
		if(isBackendModule != null && isBackendModule){
			path += "/"+HIDDEN_DIR_NAME;	
		}
		path += "/"+name;
		mountCommand = IniUtil.get(options, "mounting", "command");
		//TODO: if mountCommand == null log or exception
		mountCommand = substituteCommandParameters(options);
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

	public String getPath(){
		/** @return the path where this service is mounted */
		return path;
	}
	public String getStore(){
		/** @return the directory where this service is mounted as a subfolder*/
		return store;
	}
	public String getMountcommand(){
		/** @return the command to run to mount this service */
		return mountCommand;
	}
	public String getName(){
		/** @return the name of this service */
		return name;
	}
	public String getDataDirPath(){
		/** @return the path where this storage service keeps its data */
		return path+DATA_DIR;
	}
	public String getConfigDirPath(){
		/** @return the path where this storage service keeps its configuration files */
		return path+CONFIG_DIR;
	}
	public String getConfigFilePath(){
		/** @return the path of this storage service's configuration file */
		return path+CONFIG_PATH;
	}
	public void mount() throws IOException {
		/** Executes this.getMountcommand() to mount the service at this.getPath()
		 * This should create a directory this.getPath()+DATA_DIR, where files may be stored 
		 * and a file this.getPath()+CONFIG_PATH advertising the success of mounting the service 
		 * and providing a place to write the configuration file to.
		 * */
		Runtime rt =  Runtime.getRuntime();
		rt.exec(mountCommand);
	}
	public void unmount() throws IOException {
		/** Executes fusermount -uz to unmount the service at this.getPath() and this.getPah()+"/data" */
		//TODO: add unmountcommand section to option file
		Runtime rt =  Runtime.getRuntime();
		rt .exec("fusermount -uz "+path);
		rt .exec("fusermount -uz "+path+"/data");
	}
}
