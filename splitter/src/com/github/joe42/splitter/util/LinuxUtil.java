package com.github.joe42.splitter.util;

import java.io.*;

/**
 * Utility functions for Linux systems.
 */
public class LinuxUtil {
	/**
	 * Return the free bytes of the path's file system. 
	 * @param fileSystemPath the path of the file system
	 * @return the number of free bytes of the file system containing path
	 */
	public static long getFreeBytes(String fileSystemPath){
		long freeBytes;
		try {
			InputStream out = new ProcessBuilder( "/bin/bash", "-c", "scripts/getfilesystemusedbytes.sh "+fileSystemPath ).start().getInputStream();
			InputStreamReader isr = new InputStreamReader(out);
		    BufferedReader br = new BufferedReader(isr);
		    freeBytes = Long.parseLong(br.readLine());
		    br.close();
		} catch (IOException e) {
			return 0;
		}
	    return freeBytes;
	}
	

	/**
	 * Return the used bytes of the path's file system. 
	 * @param fileSystemPath the path of the file system
	 * @return the number of used bytes of the file system containing path
	 */
	public static long getUsedBytes(String fileSystemPath){
		long usedBytes;
		try {
			InputStream out = new ProcessBuilder( "/bin/bash", "-c", "scripts/getfilesystemusedbytes.sh "+fileSystemPath ).start().getInputStream();
			InputStreamReader isr = new InputStreamReader(out);
		    BufferedReader br = new BufferedReader(isr);
		    usedBytes = Long.parseLong(br.readLine());
		    br.close();
		} catch (IOException e) {
			return 0;
		}
	    return usedBytes;
	}
}
