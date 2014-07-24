package com.github.joe42.splitter;

import org.apache.commons.logging.LogFactory;

import com.github.joe42.splitter.backend.StorageServicesMgr;

import fuse.FuseMount;
/**
 * Entry point into the program. The class Main is responsible for setting up the Nubisave file system for initial use.
 */
public class Main {
	/**
	 * Initializes the Nubisave file system and blocks until the file system is unmounted by fusermount -u
	 * @param args Array of parameters. Normally -f -s -o$fuseopts <mountpointfolder> <storagesfolder>. \
	 * Only the last parameter is used by NubiSave. The parameters before are passed on to configure the FUSE file system. \
	 * Parameters are passed by the start script splitter/mount.sh. 
	 */
	public static void main(String[] args) {
		if (args.length < 4) {
			System.out
					.println("Must specify mountpoint folder_with_storage_mountpoints");
			System.exit(-1);
		}

		String fuseArgs[] = new String[args.length-1];
		System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);
		// System.out.println(fuseArgs[0]);
		FuseBox fuseBox = null;
		try {
			StorageServicesMgr storageServiceMgr = new StorageServicesMgr(args[args.length-1]); //takes the last parameter which is the folder with all backends
			//This creates the splitter used to split the data written to Nubisave. The class CachingCauchyReedSolomonSplitter is used as a decorator wrapping around
			//the actual splitter instance, to cache access for speed.
			Splitter splitter = new CachingCauchyReedSolomonSplitter( new OptimalCauchyReedSolomonSplitter(storageServiceMgr.getServices()) );
			//The Fusebox is responsible for file system access like writing/reading a file to NubiSave.
			//It uses the splitter instance to split or glue the files. Look here to see how files are processed.
			 fuseBox = new ConfigurableFuseBox(splitter, storageServiceMgr);  
			FuseMount.mount(fuseArgs, fuseBox, LogFactory.getLog("javafs")); //This is the binding to FUSE, which actually creates the virtual file system 
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(fuseBox != null){
			fuseBox.close();
		}
	}
}
