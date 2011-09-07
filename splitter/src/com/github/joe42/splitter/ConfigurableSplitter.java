package com.github.joe42.splitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.ini4j.Ini;

import jdbm.helper.FastIterator;

import com.github.joe42.splitter.util.file.IniUtil;
import com.github.joe42.splitter.vtf.*;

import fuse.FuseException;
import fuse.FuseFtype;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;
public class ConfigurableSplitter extends Splitter{

	private  String CONFIG_DIR;
	private VirtualFileContainer virtualFolder;
	private VirtualFile vtSplitterConfig;

	public ConfigurableSplitter(String storages, String configFileDir, String configFileName) throws IOException{
		super(storages, 0);
		init(configFileDir, configFileName);
	}
	public ConfigurableSplitter(String storages) throws IOException {
		super(storages, 0);
		init("/.###config###", "config");
	}

	private void init(String configFileDir, String configFileName) {
		CONFIG_DIR = configFileDir;
		virtualFolder = new VirtualFileContainer();
		vtSplitterConfig = new VirtualFile(configFileDir+"/"+configFileName);
		vtSplitterConfig.setText("[splitter]\nredundancy = 0");
		virtualFolder.add(vtSplitterConfig);
	}
	
	public FuseStat getattr(String path) throws FuseException {
		if(virtualFolder.containsFile(path)){
			VirtualFile vtf = virtualFolder.get(path);
			return vtf.getAttr();
		}
		if(virtualFolder.containsDir(path)){
			return new FolderEntry().getFuseStat();
		}
		return super.getattr(path);
	}
	
	public FuseDirEnt[] getdir(String path) throws FuseException {
		/** Additionally to the overwritten method it returns the virtual configuration files*/
		if (path.equals(CONFIG_DIR)) {
			return virtualFolder.getDir(path);
		}
		return super.getdir(path);
	}

	public void write(String path, ByteBuffer buf, long offset)
			throws FuseException {
		if(! virtualFolder.containsFile(path) ) {
			super.write(path, buf, offset);
			return;
		}
		VirtualFile vtf = virtualFolder.get(path);
		vtf.write(buf, offset);
		boolean parsedSuccessfully = IniUtil.isIni(vtf.getText());
		if( ! parsedSuccessfully ){
			throw new FuseException("Could not parse ini file")
			.initErrno(FuseException.EIO);
		}
		if (vtSplitterConfig.getPath().equals(path)){
			configureSplitter();
		}
	}

	private void configureSplitter() {
		Ini config = IniUtil.getIni(vtSplitterConfig.getText());
		redundancy = config.fetch("splitter", "redundancy", Integer.class);
	}
	
	public void read(String path, ByteBuffer buf, long offset)
		throws FuseException {
			if ( ! virtualFolder.containsFile(path) ) {
				super.read(path, buf, offset);
				return;
			}
			VirtualFile vtf = virtualFolder.get(path);
			vtf.read(buf, offset);
	}
	
	public void release(String path, int flags) throws FuseException {
		if (! virtualFolder.containsFile(path)) {
			super.release(path, flags);
		}
	}
	
	private boolean mountCloudfusionModule(String mountpoint, String configFilePath){
		Runtime rt = Runtime.getRuntime();
		boolean successful;
		try {
			System.out.println("python -m cloudfusion.main "+mountpoint+" /"+configFilePath);
			rt.exec("mkdir -p "+mountpoint);
			rt.exec("mkdir -p .cloudfusion/logs");
			rt.exec("python -m cloudfusion.main "+mountpoint+" /"+configFilePath);
			successful = waitUntilLoaded(mountpoint+"/"+configFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			successful =  false;
		}
		return successful;
	}
	
	private boolean waitUntilLoaded(String configFilePath) {
		/** Waits at most 10 Seconds until the file configFilePath exists.
		 * This method is used to determine if the CloudFusion module has been mounted successfully.
		 *  @returns: True iff the file exists
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
	public void mknod(String path, int mode, int rdev) throws FuseException {
		/** Makes new mountpoints at storages/foldername, where foldername is the filename of path.
		 *  The configuration file for the module is present in the same directory as the Splitter's configuration file.
		 * */
		Boolean makeNewStorage = false;
		makeNewStorage = ! virtualFolder.containsFile(path);
		makeNewStorage &= CONFIG_DIR.equals(new File(path).getParent());
		if (makeNewStorage) {
			String configFileName = new File(path).getName();
			String configFilePath = configFileName;
			//TODO: move mount logic to write and only create stub here to be recognized by getattr. Then also allow to mount other "Modules".
			boolean mounted = mountCloudfusionModule(storages+"/"+configFileName, configFilePath);
			if (mounted) {
					virtualFolder.add(new VirtualRealFile(path, storages+"/"+configFileName+"/"+configFileName));
			} else {
				throw new FuseException("IO Exception on creating CloudFusion store.")
				.initErrno(FuseException.EIO);
            }
            return;
		}
		super.mknod(path, mode, rdev);
	}
	
	public void truncate(String path, long size) throws FuseException {
			if (virtualFolder.containsFile(path)) {
				VirtualFile vtf = virtualFolder.get(path);
				vtf.truncate();
				return;
			}
			super.truncate(path, size);
	}

	public void unlink(String path) throws FuseException {
		/**Don't remove virtual files*/
		if ( !virtualFolder.containsFile(path) ) {
			super.unlink(path);
		}
	}
}
