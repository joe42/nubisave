package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.ini4j.Ini;

import com.github.joe42.splitter.backend.Mounter;
import com.github.joe42.splitter.backend.StorageService;
import com.github.joe42.splitter.util.file.IniUtil;
import com.github.joe42.splitter.vtf.FolderEntry;
import com.github.joe42.splitter.vtf.VirtualFile;
import com.github.joe42.splitter.vtf.VirtualFileContainer;
import com.github.joe42.splitter.vtf.VirtualRealFile;

import fuse.FuseException;
import fuse.FuseFtype;
import fuse.FuseStatfs;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;
public class ConfigurableFuseBox extends FuseBox  implements StorageService{

	private VirtualFileContainer virtualFolder;
	private VirtualFile vtSplitterConfig;
	private Mounter mounter;
	public ConfigurableFuseBox(Splitter splitter, Mounter mounter) throws IOException{
		super(splitter);
		this.mounter = mounter;
		virtualFolder = new VirtualFileContainer();
		vtSplitterConfig = new VirtualFile(CONFIG_PATH);
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
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.getattr(path);
		}
		throw new FuseException("No Such Entry")
		.initErrno(FuseException.ENOENT);
	}
	

	public String removeDataFolderPrefix(String path){
        path = path.substring(DATA_DIR.length());
        if( ! path.startsWith("/") ){
            path = "/";
        }
        return path;
	}
	
	public FuseDirEnt[] getdir(String path) throws FuseException {
		/** Additionally to the overwritten method's return value it can return the virtual configuration files*/
		if (path.equals(vtSplitterConfig.getDir())) {
			return virtualFolder.getDir(path);
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.getdir(path);
		}
		if(path.equals("/")){
			FuseDirEnt[] ret = new FuseDirEnt[2];
			FuseDirEnt dirEntry = new FuseDirEnt();
			dirEntry.name = ".";
			dirEntry.mode = FuseFtype.TYPE_DIR;
			ret[0] = dirEntry;
			dirEntry = new FuseDirEnt();
			dirEntry.name = "..";
			dirEntry.mode = FuseFtype.TYPE_DIR;
			ret[1] = dirEntry;
			dirEntry = new FuseDirEnt();
			dirEntry.name = vtSplitterConfig.getName();
			dirEntry.mode = FuseFtype.TYPE_DIR;
			ret[1] = dirEntry;
			dirEntry = new FuseDirEnt();
			dirEntry.name = DATA_DIR_NAME; 
			dirEntry.mode = FuseFtype.TYPE_DIR;
			ret[1] = dirEntry;
			return ret;
		}
		throw new FuseException("Invalid parameter for getdir (ConfigurableSplitter)")
			.initErrno(FuseException.EIO);
	}

	public void write(String path, ByteBuffer buf, long offset)
			throws FuseException {
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
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
			return;
		}
		//Mount backend module:
		String configFileName = new File(path).getName();
		Ini options = IniUtil.getIni(vtf.getText());
		String mountpoint = mounter.mount(configFileName, options); 
		if (mountpoint != null) {
			virtualFolder.remove(path); 
			virtualFolder.add(new VirtualRealFile(path, mountpoint+CONFIG_PATH));
			vtf = virtualFolder.get(path);
			buf.rewind();
			vtf.write(buf, offset);
			return;
		} else {
			throw new FuseException("IO Exception on creating store.")
			.initErrno(FuseException.EIO);
        }
	}

	private void configureSplitter() {
		Ini config = IniUtil.getIni(vtSplitterConfig.getText());
		redundancy = config.fetch("splitter", "redundancy", Integer.class);
	}
	
	public void read(String path, ByteBuffer buf, long offset)
			throws FuseException {
		if ( path.startsWith(DATA_DIR) ) {
			path = removeDataFolderPrefix(path);
			super.read(path, buf, offset);
			return;
		}
		VirtualFile vtf = virtualFolder.get(path);
		vtf.read(buf, offset);
	}
	
	public void release(String path, int flags) throws FuseException {
		if (path.startsWith(DATA_DIR)) {
			path = removeDataFolderPrefix(path);
			super.release(path, flags);
		}
	}
	
	
	
	public void mknod(String path, int mode, int rdev) throws FuseException {
		/** Makes new mountpoints at storages/foldername, where foldername is the filename of path.
		 *  The configuration file for the module is present in the same directory as the Splitter's configuration file.
		 * */
		Boolean makeNewStorage = false;
		makeNewStorage = ! virtualFolder.containsFile(path);
		makeNewStorage &= vtSplitterConfig.getDir().equals(new File(path).getParent());
		if (makeNewStorage) {
	        //TODO: move mount logic to write and only create stub here to be recognized by getattr. Then also allow to mount other "Modules".
			virtualFolder.add(new VirtualFile(path));
            return;
		}
		if(path.equals(DATA_DIR) || virtualFolder.containsFile(path)){
			throw new FuseException("Entity"+path+" already exists.")
				.initErrno(FuseException.EEXIST);
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			super.mknod(path, mode, rdev);
		}
	}
	
	public void truncate(String path, long size) throws FuseException {
			if (virtualFolder.containsFile(path)) {
				VirtualFile vtf = virtualFolder.get(path);
				vtf.truncate();
				return;
			}
			if(path.startsWith(DATA_DIR)){
				path = removeDataFolderPrefix(path);
			}
			super.truncate(path, size);
	}

	public void unlink(String path) throws FuseException {
		//unmount storage module if removing config file
		if(path.startsWith(CONFIG_DIR)){
			VirtualFile vf = virtualFolder.get(path);
			if(vf == null){
				throw new FuseException("Cannot remove "+path)
				.initErrno(FuseException.EEXIST);
			}
			if( ! (vf instanceof VirtualRealFile) ){
				throw new FuseException("Cannot remove "+path)
				.initErrno(FuseException.EACCES);
			}
			/*Bug:
			 * Bugfix in feature #462 Unmounting Service via Virtual File Interface:
Only remove virtual configuration file if the corresponding configuration 
file is removed after at most 10 seconds
						String uniqueServiceName = new File(path).getName();
			if(mounter.unmount(uniqueServiceName)){
				virtualFolder.remove(path);
			}
		}*/
			String uniqueServiceName = new File(path).getName();
			mounter.unmount(uniqueServiceName);
			virtualFolder.remove(path);
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			super.unlink(path);
		}
	}
	
	public FuseStatfs statfs() throws FuseException {
		FuseStatfs ret = super.statfs();
		ret.files += 2 + virtualFolder.getFileNames(vtSplitterConfig.getDir()).size(); //data and config directory + virtual configuration files
		return ret;
	}	
	
	public void rmdir(String path) throws FuseException {
		if(! path.startsWith(DATA_DIR) || path.equals(DATA_DIR)){
			throw new FuseException("Cannot remove "+path)
				.initErrno(FuseException.EACCES);
		} else{
			path = removeDataFolderPrefix(path);
			super.rmdir(path);
		}
	}	
	
	public void rename(String from, String to) throws FuseException {
		//FIX: success depends on targets beeing files or directories
		if(from.startsWith(CONFIG_DIR) && to.startsWith(CONFIG_DIR)){
			VirtualFile vfFrom = virtualFolder.get(from);
			VirtualFile vfTo = virtualFolder.get(to);
			if(vfFrom == null || vfTo == null){
				throw new FuseException("Cannot move "+from+" to "+to)
				.initErrno(FuseException.EEXIST);
			}
			if( ! (vfFrom instanceof VirtualRealFile && vfTo instanceof VirtualRealFile) ){
				throw new FuseException("Cannot move "+from+" to "+to)
				.initErrno(FuseException.EACCES);
			}
			String uniqueServiceNameFrom = new File(from).getName();
			String uniqueServiceNameTo = new File(to).getName();
			mounter.moveData(uniqueServiceNameFrom, uniqueServiceNameTo);
			virtualFolder.remove(from);
			return;
		}
		if(from.equals(to)){
			throw new FuseException("Cannot rename "+from+" to "+to)
				.initErrno(FuseException.EACCES);
		} 
		if(from.startsWith(DATA_DIR) && to.startsWith(DATA_DIR)){
			from = removeDataFolderPrefix(from);
			to = removeDataFolderPrefix(to);
			super.rename(from, to);
			return;
		}
		throw new FuseException("Cannot rename "+from+" to "+to)
			.initErrno(FuseException.EACCES);
	}	
		
		public void mkdir(String path, int mode) throws FuseException {
			if(path.startsWith(DATA_DIR) && ! path.equals(DATA_DIR)){
				path = removeDataFolderPrefix(path);
				super.mkdir(path, mode);
				return;
			}
			throw new FuseException("Cannot mkdir "+path)
				.initErrno(FuseException.EACCES);
	}
}
