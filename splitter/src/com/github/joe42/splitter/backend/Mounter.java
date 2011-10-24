package com.github.joe42.splitter.backend;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.ini4j.Ini;

public class Mounter {
	/**
	 * For mounting the FUSE modules.
	 * */
	public static boolean mount(String mountpoint, Ini mountOptions){
		/**
		 * Execute the command given in mountOption's mounting section, 
		 * substituting the commands parameters with parameters of the same name in moutOption's parameter section and 
		 * the command's parameter with the name mountpoint with mountpoint.
		 * The execution should results in a FUSE module mounted so that its data can be accessed by folder mountpoint/data and 
		 * a configuration file mountpoint/config/config.
		 * @param mountpoint  the command executed should put the data directory and the config/config file into this folder
		 * @return true iff the file mountpoint/config/config exists  after at most 10 seconds
		 * */
		Runtime rt = Runtime.getRuntime();
		boolean successful;
		boolean isBackendModule = false;
		try{
			isBackendModule = mountOptions.get("splitter", "isbackendmodule", Boolean.class);
		} catch(NullPointerException e){
			System.out.println("Service is no backend module");
		}
		if(isBackendModule){
			mountpoint = mountpoint+"/backend";	
		}
		String command = mountOptions.get("mounting", "command");
		String substitutedCommand = "";
		Map<String, String> substitutions = mountOptions.get("parameter");
		for(String word: command.split(" ")){
			if(word.equals("mountpoint")){
				word = mountpoint;
			} else if(substitutions != null){
				if( substitutions.containsKey(word) ){
					if(word.startsWith("backendservice")){
						word = mountpoint+"/backend/"+substitutions.get(word);
					} else {
						word = substitutions.get(word);
					}
				}
			}
			substitutedCommand += word+" ";
		}
		try {
			System.out.println("Executing: "+substitutedCommand);
			rt.exec(substitutedCommand);
			successful = isMounted(mountpoint+"/config/config");
		} catch (IOException e) {
			e.printStackTrace();
			successful =  false;
		}
		return successful;
	}
	private static boolean isMounted(String configFilePath) {
		/** Waits at most 10 seconds until the file configFilePath exists.
		 * This method is used to determine if the CloudFusion module has been mounted successfully.
		 *  @returns: True iff the file exists after at most 10 seconds
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
}
