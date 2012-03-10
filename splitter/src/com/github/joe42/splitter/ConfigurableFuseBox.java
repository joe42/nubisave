package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.Mounter;
import com.github.joe42.splitter.backend.StorageServicesMgr;
import com.github.joe42.splitter.backend.StorageService;
import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.FileUtil;
import com.github.joe42.splitter.util.file.IniUtil;
import com.github.joe42.splitter.vtf.FolderEntry;
import com.github.joe42.splitter.vtf.VirtualFile;
import com.github.joe42.splitter.vtf.VirtualFileContainer;
import com.github.joe42.splitter.vtf.VirtualRealFile;

import fuse.FuseDirFiller;
import fuse.FuseException;
import fuse.FuseFtype;
import fuse.FuseGetattrSetter;
import fuse.FuseStatfs;
import fuse.FuseStatfsSetter;
import fuse.compat.FuseDirEnt;
import fuse.compat.FuseStat;
public class ConfigurableFuseBox extends FuseBox  implements StorageService{

	private VirtualFileContainer virtualFolder;
	private VirtualFile vtSplitterConfig;
	private StorageServicesMgr storageServiceMgr;
	private static final Logger log = Logger.getLogger("FuseBox");
	
	public ConfigurableFuseBox(CauchyReedSolomonSplitter splitter, StorageServicesMgr storageServiceMgr) throws IOException{
		super(new FilePartFragmentStore(splitter));
		this.storageServiceMgr = storageServiceMgr;
		virtualFolder = new VirtualFileContainer();
		vtSplitterConfig = new VirtualFile(CONFIG_PATH);
		vtSplitterConfig.setText("[splitter]\nredundancy = 0");
		virtualFolder.add(vtSplitterConfig);
	}

	@Override
	public int getattr(String path, FuseGetattrSetter getattrSetter) throws FuseException {
		FuseStat attr;
		if(virtualFolder.containsFile(path)){
			VirtualFile vtf = virtualFolder.get(path);
			attr = vtf.getAttr();
			getattrSetter.set(attr.inode, attr.mode, attr.nlink, attr.uid, attr.gid, 0, attr.size, attr.blocks, attr.atime, attr.mtime, attr.ctime);
			return 0;
		}
		if(virtualFolder.containsDir(path)){
			attr = new FolderEntry().getFuseStat();
			getattrSetter.set(attr.inode, attr.mode, attr.nlink, attr.uid, attr.gid, 0, attr.size, attr.blocks, attr.atime, attr.mtime, attr.ctime);
			return 0;
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.getattr(path, getattrSetter);
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

	@Override
	public int getdir(String path, FuseDirFiller dirFiller) throws FuseException {
		/** Additionally to the overwritten method's return value it can return the virtual configuration files*/
		if (path.equals(vtSplitterConfig.getDir())) {
			for(FuseDirEnt ent: virtualFolder.getDir(path)){
				dirFiller.add(ent.name, 0, ent.mode);
			}
			return 0;
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.getdir(path, dirFiller);
		}
		if(path.equals("/")){

			dirFiller.add(".", 0, FuseFtype.TYPE_DIR | 0755);
			dirFiller.add("..", 0, FuseFtype.TYPE_DIR | 0755);
			dirFiller.add(vtSplitterConfig.getName(), 0, FuseFtype.TYPE_DIR | 0755);
			dirFiller.add(DATA_DIR_NAME, 0, FuseFtype.TYPE_DIR | 0755);
			return 0;
		}
		throw new FuseException("Invalid parameter for getdir (ConfigurableSplitter)")
			.initErrno(FuseException.EIO);
	}

	@Override
	public int write(String path, Object fh, boolean isWritepage, ByteBuffer buf, long offset)
			throws FuseException {
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.write(path, fh, isWritepage, buf, offset);
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
			return 0;
		}
		//Mount backend module:
		String configFileName = new File(path).getName();
		Ini options = IniUtil.getIni(vtf.getText());
		if(storageServiceMgr.isMounted(configFileName)){
			storageServiceMgr.configureService(configFileName, options);
			updateVTSplitterConfigFile();
			return 0;
		}
		String mountpoint = storageServiceMgr.mount(configFileName, options); 
		if (mountpoint != null) {
			virtualFolder.remove(path); 
			virtualFolder.add(new VirtualRealFile(path, mountpoint+CONFIG_PATH));
			vtf = virtualFolder.get(path);
			buf.rewind();
			vtf.write(buf, offset);
			updateVTSplitterConfigFile();
			return 0;
		} else {
			throw new FuseException("IO Exception on creating store.")
			.initErrno(FuseException.EIO);
        }
	}

	private void updateVTSplitterConfigFile() {
		Ini config = IniUtil.getIni(vtSplitterConfig.getText());
		config.put("splitter", "availability", getStorageAvailability());
		vtSplitterConfig.setText(IniUtil.getString(config));
	}

	private void configureSplitter() {
		Ini config = IniUtil.getIni(vtSplitterConfig.getText());
		setRedundancy(config.fetch("splitter", "redundancy", Integer.class));
		setStorageStrategyName(config.fetch("splitter", "storagestrategy", String.class));
		updateVTSplitterConfigFile();
	}

	@Override
	public int read(String path, Object fh, ByteBuffer buf, long offset)
			throws FuseException {
		if ( path.startsWith(DATA_DIR) ) {
			path = removeDataFolderPrefix(path);
			return super.read(path, fh, buf, offset);
		}
		VirtualFile vtf = virtualFolder.get(path);
		vtf.read(buf, offset);
		return 0;
	}

	@Override
	public int fsync(String path, Object fh, boolean isDatasync) throws FuseException {
		if (path.startsWith(DATA_DIR)) {
			path = removeDataFolderPrefix(path);
			return super.flush(path, fh);
		}
		return 0;
	}
	
	@Override
	public int flush(String path, Object fh) throws FuseException {
		if (path.startsWith(DATA_DIR)) {
			path = removeDataFolderPrefix(path);
			return super.flush(path, fh);
		}
		return 0;
	}

	/** Makes new mountpoints at storages/foldername, where foldername is the filename of path.
	 *  The configuration file for the module is present in the same directory as the Splitter's configuration file.
	 * */
	@Override
	public int mknod(String path, int mode, int rdev) throws FuseException {
		Boolean makeNewStorage = false;
		makeNewStorage = ! virtualFolder.containsFile(path);
		makeNewStorage &= vtSplitterConfig.getDir().equals(new File(path).getParent());
		if (makeNewStorage) {
			virtualFolder.add(new VirtualFile(path));
            return 0;
		}
		if(path.equals(DATA_DIR) || virtualFolder.containsFile(path)){
			throw new FuseException("Entity"+path+" already exists.")
				.initErrno(FuseException.EEXIST);
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.mknod(path, mode, rdev);
		}
		return 0;
	}

	@Override
	public int truncate(String path, long size) throws FuseException {
		if (virtualFolder.containsFile(path)) {
			VirtualFile vtf = virtualFolder.get(path);
			vtf.truncate();
			return 0;
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
		}
		return super.truncate(path, size);
	}

	@Override
	public int unlink(String path) throws FuseException {
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
			storageServiceMgr.unmount(uniqueServiceName);
			virtualFolder.remove(path);
		}
		if(path.startsWith(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.unlink(path);
		}
		return 0;
	}

	@Override
	public int statfs(FuseStatfsSetter statfsSetter) throws FuseException {
		FuseStatfs statfs = super.statfs();
		statfs.files += 2 + virtualFolder.getFileNames(vtSplitterConfig.getDir()).size(); //data and config directory + virtual configuration files
		statfsSetter.set(statfs.blockSize, statfs.blocks, statfs.blocksFree, statfs.blocksAvail, statfs.files, statfs.filesFree, statfs.namelen);
		return 0;
	}	

	@Override
	public int rmdir(String path) throws FuseException {
		if(! path.startsWith(DATA_DIR) || path.equals(DATA_DIR)){
			throw new FuseException("Cannot remove "+path)
				.initErrno(FuseException.EACCES);
		} else{
			path = removeDataFolderPrefix(path);
			super.rmdir(path);
		}
		return 0;
	}	

	@Override
	public int rename(String from, String to) throws FuseException {
		if (from.startsWith(CONFIG_DIR) && to.startsWith(CONFIG_DIR)) {
			VirtualFile vfFrom = virtualFolder.get(from);
			VirtualFile vfTo = virtualFolder.get(to);
			if (vfFrom == null || vfTo == null) {
				throw new FuseException("Cannot move " + from + " to " + to)
						.initErrno(FuseException.ENOENT);
			}
			if (!(vfFrom instanceof VirtualRealFile && vfTo instanceof VirtualRealFile)) {
				throw new FuseException("Cannot move " + from + " to " + to)
						.initErrno(FuseException.EACCES);
			}
			moveFragments(from, to);
			return 0;
		}
		if (from.equals(to)) {
			throw new FuseException("Cannot rename " + from + " to " + to)
					.initErrno(FuseException.EACCES);
		}
		if (from.startsWith(DATA_DIR) && to.startsWith(DATA_DIR)) {
			from = removeDataFolderPrefix(from);
			to = removeDataFolderPrefix(to);
			return super.rename(from, to);
		}
		throw new FuseException("Cannot rename " + from + " to " + to)
				.initErrno(FuseException.EACCES);
	}

	private void moveFragments(final String from, final String to) throws FuseException {
		Thread t = new Thread(new FileCopy(from, to));
        t.start();
	}	
	//TODO: disable write access during operation
	private class FileCopy implements Runnable {
		private String source;
		private String destination;
		public FileCopy(String from, String to){
			this.source = from;
			this.destination = to;
		}
		public void run() {
			boolean successful = true;
			String uniqueServiceNameFrom = new File(source).getName();
			String uniqueServiceNameTo = new File(destination).getName();
			BackendService serviceFrom = storageServiceMgr.getServices().get(uniqueServiceNameFrom);
			BackendService serviceTo = storageServiceMgr.getServices().get(uniqueServiceNameTo);
			log.debug("mv "+serviceFrom.getDataDirPath()+"/* "+serviceTo.getDataDirPath());
			File[] filesToMove = new File(serviceFrom.getDataDirPath()).listFiles();
			int nrOfFilesToMove = filesToMove.length;
			int cntMovedFiles = 1;
			for(File srcFileFragment: filesToMove){
				try {
					if( ! getFileFragmentStore().hasFragment(srcFileFragment.getPath()) ){
						continue;
					}
				} catch (IOException e1) {
					successful = false;
					break;
				}
				File destFileFragment = new File(serviceTo.getDataDirPath()+"/"+srcFileFragment.getName());
				try{
					FileUtil.copy(srcFileFragment, destFileFragment);
					getFileFragmentStore().renameFragment(srcFileFragment.getPath(), destFileFragment.getPath());
					srcFileFragment.delete();
				} catch (IOException e) {
					successful = false;
				}
				cntMovedFiles++;
				VirtualFile vtf = virtualFolder.get(source);
				if(vtf == null){ //might be deleted in concurrent operation
					return;
				}
				Ini ini = IniUtil.getIni(vtf.getText());
				ini.put("splitter", "migrationprogress", (int)((cntMovedFiles * 100d) / nrOfFilesToMove));	
				vtf.setText(IniUtil.getString(ini));
			} //end of for
			VirtualFile vtf = virtualFolder.get(source);
			if(vtf == null){ //might be deleted in concurrent operation
				return;
			}
			Ini ini = IniUtil.getIni(vtf.getText());
			ini.put("splitter", "migrationissuccessful", successful);	
			vtf.setText(IniUtil.getString(ini));
		}
	}
	
	@Override
	public int mkdir(String path, int mode) throws FuseException {
		if(path.startsWith(DATA_DIR) && ! path.equals(DATA_DIR)){
			path = removeDataFolderPrefix(path);
			return super.mkdir(path, mode);
		}
		throw new FuseException("Cannot mkdir "+path)
			.initErrno(FuseException.EACCES);
	}
}
