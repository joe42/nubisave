package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jdbm.RecordManagerFactory;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Hex;
import org.ini4j.Ini;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.backend.Mounter;
import com.github.joe42.splitter.backend.StorageServicesMgr;
import com.github.joe42.splitter.backend.StorageService;
import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.FileUtil;
import com.github.joe42.splitter.util.file.IniUtil;
import com.github.joe42.splitter.util.file.PropertiesUtil;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.vtf.FileEntry;
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


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

/**
 * Subclass of Fusebox responsible for configuring Nubisave during runtime.
 * It will add or remove background storages when special configuration files are written into Nubisave's config directory.
 * Also, by changing Nubisave's config/config file, you can configure the redundancy used by the storage strategy,
 * as well as the storage strategy to use.
 * The config directory's files are virtual. They are perceived by the file system by calling the method getattr, which 
 * claims the virtual file or the directory exists, returning various file attributes like the file size of the virtual file. 
 * Also, the files need to be visible when listing the config directory. This is implemented by overwriting the getdir method, 
 * which returns the config directory as an item of Nubisave's root directory, and the virtual files in the config directory,
 * when listing the config directory itself (i.e. by calling ls nubisave/config or by opening the path with a file manager).
 * The write method is overwritten in order to intercept write operations to the virtual files in the config directory,
 * to trigger the desired action, like reconfiguring the storage strategy, or adding a new storage backend.
 * The rename method is overwritten to allow for a bit of magic when moving (renaming) one of the virtual configuration files
 * to another existing virtual configuration file. The configuration files each represent one of Nubisave's storages.
 * When moving one of those configuration files to another, all of the data in the renamed file are moved to the other,
 * and the renamed configuration file is then removed.
 * The unlink method is called when removing a file in Nubisave. It is overwritten to unmount a storage when removing its configuration file.
 * If the file system operations do not concern the virtual config directory, they are handled by the Superclass FuseBox.     
 */
public class ConfigurableFuseBox extends FuseBox  implements StorageService{

	private VirtualFileContainer virtualFolder;
	/**
	 * Stores the contents of the virtual file config/config of Nubisave's root directory.
	 */
	private VirtualFile vtSplitterConfig;
	private StorageServicesMgr storageServiceMgr;
	private boolean parallel_execution_of_read;
	private static final Logger log = Logger.getLogger("FuseBox");
	
	public ConfigurableFuseBox(Splitter splitter, StorageServicesMgr storageServiceMgr) throws IOException{
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
		double availability = getStorageAvailability();
		config.put("splitter", "availability", getStorageAvailability());
		long MILLISECONDS_IN_YEAR = 31556952000L;
		long diffTime = (long) (MILLISECONDS_IN_YEAR * availability);
		Duration duration = null;
		try {
			duration = DatatypeFactory.newInstance().newDuration(0);
			duration = DatatypeFactory.newInstance().newDuration(diffTime);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

		String unavailabilityDurationPerYear = String.format("%02d days, %02d hours, %02d minutes, and %02d seconds", duration.getDays(), duration.getHours(), duration.getMinutes(), duration.getSeconds());
		config.put("splitter", "unavailability per year", unavailabilityDurationPerYear);
		config.put("splitter", "redundancy factor", getStorageRedundancy());
		Map<String, String> codecInfo = getCodecInfo();
		for(Entry<String, String> info: codecInfo.entrySet()){
			config.put("codec", info.getKey(), info.getValue());
		}

		for (Map.Entry<String, String> entry : getCodecInfo().entrySet()) {
			config.put("splitter", entry.getKey(), entry.getValue());
		}
		System.out.println("redundancy factor"+ getStorageRedundancy());
		vtSplitterConfig.setText(IniUtil.getString(config));
		config.getFile().delete();
	}

	private void configureSplitter() throws FuseException {
		Ini config = IniUtil.getIni(vtSplitterConfig.getText());
		//TODO: check and write version
		try {
			if(config.get("splitter").containsKey("save")){ 
				log.debug("save Splitter's configuration");
				if(config.containsKey("database")){ 
					saveDatabase(config);
				}
				String sessionNumber = config.fetch("splitter", "save", String.class);
				config.remove("splitter", "save");
				fileStore.writeMetaData(IniUtil.getString(config), "/.nubisave_database.meta"+sessionNumber);
				storageServiceMgr.getServices().storeServiceNames(".nubisave_service_name.session"+sessionNumber);
			} else if(config.get("splitter").containsKey("load")){
				setServicesMapping(config);
				config = loadDatabaseMetaData(config);
				reloadDatabase(config);
			}
			
		} catch (IOException e) {
			config.getFile().delete();
			throw new FuseException("IO Exception on persisting Splitter's configuration.")
				.initErrno(FuseException.EIO);
		}
		vtSplitterConfig.setText(IniUtil.getString(config));
		setRedundancy(config.fetch("splitter", "redundancy", Integer.class));
		setStorageStrategyName(config.fetch("splitter", "storagestrategy", String.class));
		updateVTSplitterConfigFile();
		config.getFile().delete();
	}

	private void reloadDatabase(final Ini config) {
		log.debug("Overwriting current database with saved session");
		final String dbPath = config.fetch("database", "path", String.class);
		ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
			  worker.schedule(new Runnable() {
				@Override
				public void run() {
					PropertiesUtil props = new PropertiesUtil("../bin/nubi.properties");
					try {
						FileUtil.copy(new File(System.getProperty("user.home")+"/.nubisave/nubisavemount/data"+dbPath), new File(props.getProperty("splitter_database_location")+".db"));
						new File(props.getProperty("splitter_database_location")+".lg").delete();
						metaDataStore.reloadDataBase();
						fileStore.reloadDatabase();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, 1, TimeUnit.SECONDS);
	}

	/**
	 * Sets the mapping of current to previous service names in the splitter's configuration file
	 * The section name is MapOfCurrentToPreviousServices and the parameter names are the current services' names
	 * with the corresponding previous services' names as values. The services need to be renamed in order to access the files
	 * from the previous session, after the database has been reloaded.
	 */
	private void setServicesMapping(Ini config) {String sessionNumber = config.fetch("splitter", "load", String.class);
		Map<String,String> newToPreviousServiceNames =  storageServiceMgr.getServices().getServiceNameMapping(".nubisave_service_name.session"+sessionNumber);
		for(Entry<String, String> newToPreviousServiceName: newToPreviousServiceNames.entrySet()){
			config.put("MapOfCurrentToPreviousServices", newToPreviousServiceName.getKey(), newToPreviousServiceName.getValue());
		}
		vtSplitterConfig.setText(IniUtil.getString(config)); //TODO: synchronize
	}

	private Ini loadDatabaseMetaData(Ini config) throws IOException,
			UnsupportedEncodingException {
		log.debug("load Splitter's configuration");
		byte[] splitter_config = fileStore.readMetaData("/.nubisave_database.meta"+config.fetch("splitter", "load", Integer.class));
		log.debug("new String(splitter_config)"+new String(splitter_config));
		vtSplitterConfig.setText(new String(splitter_config, "UTF-8")); 
		config = IniUtil.getIni(new String(splitter_config, "UTF-8"));
		config.remove("splitter", "load");

		String dbPath = config.fetch("database", "path", String.class);
		long dbSize = config.fetch("database", "size", long.class);
		ArrayList<String> fragmentNames = new ArrayList<String>();
		ArrayList<byte[]> checksums;
		ArrayList<String> absoluteFragmentNames;
		int nr_of_file_fragments_required;
		int nr_of_file_fragments;
		String sectionName, dbPartPath;
		int databasePartNr = 0;
		while(true){
			sectionName = "databasePartNr_"+databasePartNr;
			if( ! config.containsKey(sectionName) ){
				break;
			}
			databasePartNr++;
			dbPartPath = config.fetch(sectionName, "name", String.class);
			nr_of_file_fragments_required = config.fetch(sectionName, "nrOfFileFragmentsRequired", Integer.class);
			nr_of_file_fragments = config.fetch(sectionName, "nrOfFileFragments", Integer.class);
			fragmentNames = new ArrayList<String>();
			checksums = new ArrayList<byte[]>();
			for(int i=0; i<nr_of_file_fragments; i++){
				fragmentNames.add(config.fetch(sectionName, "fileFragmentName_"+i, String.class));
				log.debug("fragment name "+i+": "+fragmentNames.get(i));
				checksums.add( Hex.decode(config.fetch(sectionName, "fileFragmentNameChecksum_"+i, String.class)) );
			}
			absoluteFragmentNames = getAbsoluteFragmentPaths(fragmentNames, nr_of_file_fragments);
			fileStore.fileFragmentMetaDataStore.setFragments(dbPartPath, absoluteFragmentNames, nr_of_file_fragments_required, nr_of_file_fragments, checksums, dbSize);
			((FilePartFragmentMetaDataStore)fileStore.fileFragmentMetaDataStore).put(dbPath,dbSize);
		}		
		FileEntry fileEntry;
			//metaDataStore.makeFolderEntry(dbPath);
			fileEntry = metaDataStore.makeFileEntry(dbPath);
			fileEntry.uid = UID;
			fileEntry.gid = GID;
			log.debug("size:"+fileStore.getSize(dbPath));
			metaDataStore.putFileEntry(dbPath, fileEntry);
		log.debug("Metadata of database successfully created");
		return config;
	}

	/**
	 * Get the absolute fragment paths of the fragment names
	 * Since the paths of the back end services might differ on an other machine, only the relative fragment names
	 * are stored online for global access. Here they are prefixed with the available local back end services' paths, 
	 * if a file fragment exists on this back end. If not enough file fragments exist on the local back ends,
	 * fake paths are included, so that the number of returned paths equals the nr_of_file_fragments.
	 * This is important, as the number parameterizes the reconstruction mechanism.
	 */
	private ArrayList<String> getAbsoluteFragmentPaths(ArrayList<String> fragmentNames, int nr_of_file_fragments) {
		ArrayList<String> absoluteFragmentNames = new ArrayList<String>();
		boolean matchFound ;
		for(String fragmentName: fragmentNames){
			matchFound = false;
			for(BackendService backend: storageServiceMgr.getServices().getFrontEndStorageServices()){
				if(new File(backend.getDataDirPath()+fragmentName).exists()){
					absoluteFragmentNames.add(backend.getDataDirPath()+fragmentName);
					log.debug("absolute fragment name: "+backend.getDataDirPath()+fragmentName);
					matchFound = true;
					break;
				}
			}
			if( ! matchFound ){
				absoluteFragmentNames.add("fakePath");
			}
		}
		return absoluteFragmentNames;
	}

	private void saveDatabase(Ini config) throws IOException {
		String dbPath = config.fetch("database", "path", String.class);
		FilePartFragmentMetaDataStore filePartFragmentDataStore = (FilePartFragmentMetaDataStore)fileStore.fileFragmentMetaDataStore;
		long size = fileStore.getSize(dbPath);
		log.debug("database dbPath: "+dbPath);
		log.debug("database size: "+size);
		config.put("database", "size", size);
		List<String> fragments;
		List<byte[]> checksums;
		String sectionName;
		int databasePartNr = 0;
		for(String dbPartPath:  filePartFragmentDataStore.getFilePartPaths(dbPath)){
			sectionName = "databasePartNr_"+databasePartNr;
			fragments = getFragments(dbPartPath);
			checksums = ((FileFragmentMetaDataStore)fileStore.fileFragmentMetaDataStore).getFragmentsChecksums(dbPartPath);
			config.put(sectionName, "name", dbPartPath);
			config.put(sectionName, "nrOfFileFragmentsRequired", filePartFragmentDataStore.getNrOfRequiredFragments(dbPartPath));
			config.put(sectionName, "nrOfFileFragments", filePartFragmentDataStore.getNrOfFragments(dbPartPath));
			for(int i=0; i<fragments.size();i++){
				log.debug("fragment name "+i+": "+fragments.get(i));
				config.put(sectionName, "fileFragmentName_"+i, fragments.get(i));
				log.debug("checksums.get(i)="+checksums.size());
				config.put(sectionName, "fileFragmentNameChecksum_"+i, new String(Hex.encode(checksums.get(i))) );
				databasePartNr++;
			}
		}
	}
	
	private void deletePreviousSession(Ini config) throws IOException{
		String sectionName;
		Ini previousSession;
		int databasePartNr = 0;
		while(true){ // remove previous database parts
			sectionName = "databasePartNr_"+databasePartNr;
			if( ! config.containsKey(sectionName) ){
				break;
			}
			config.remove(sectionName);
			databasePartNr++;
		}
		byte[] splitter_config = fileStore.readMetaData("/.nubisave_database.meta"+config.fetch("splitter", "load", Integer.class));
		previousSession = IniUtil.getIni(new String(splitter_config, "UTF-8"));

		ArrayList<String> fragmentNames = new ArrayList<String>();
		ArrayList<String> absoluteFragmentNames;
		int nr_of_file_fragments;
		databasePartNr = 0;
		while(true){
			sectionName = "databasePartNr_"+databasePartNr;
			if( ! previousSession.containsKey(sectionName) ){
				break;
			}
			databasePartNr++;
			nr_of_file_fragments = previousSession.fetch(sectionName, "nrOfFileFragments", Integer.class);
			fragmentNames = new ArrayList<String>();
			for(int i=0; i<nr_of_file_fragments; i++){
				fragmentNames.add(previousSession.fetch(sectionName, "fileFragmentName_"+i, String.class));
			}
			absoluteFragmentNames = getAbsoluteFragmentPaths(fragmentNames, nr_of_file_fragments);
			for(String previousFragment: absoluteFragmentNames){
				new File(previousFragment).delete();
			}
		}		
	}

	private List<String> getFragments(String dbPartPath) throws IOException {
		List<String> ret = new ArrayList<String>();
		if(fileStore.fileFragmentMetaDataStore instanceof FilePartFragmentMetaDataStore){
			FilePartFragmentMetaDataStore filePartFragmentMetaDataStore = ((FilePartFragmentMetaDataStore)fileStore.fileFragmentMetaDataStore);
			String fragmentName;
			int indexAfterDataDir;
			int i=0;
			for(String filePartFragmentPath: filePartFragmentMetaDataStore.getFilePartFragments(dbPartPath)){
				log.debug("dbPartPath:"+filePartFragmentPath);
				indexAfterDataDir = filePartFragmentPath.indexOf(DATA_DIR)+DATA_DIR.length();
				fragmentName = filePartFragmentPath.substring(indexAfterDataDir);
				ret.add(fragmentName);
				i++;
			}
		} /*else {
			List<String> absoluteFragmentNames =  fileStore.fileFragmentMetaDataStore.getFragments(dbPath);
			String fragmentName = null; 
			List<byte[]> checksums = fileStore.fileFragmentMetaDataStore.getFragmentsChecksums(dbPath);
			for(int i=0; i<absoluteFragmentNames.size(); i++){
				fragmentName =  absoluteFragmentNames.get(i).substring(absoluteFragmentNames.get(i).indexOf(DATA_DIR)+DATA_DIR.length());
				log.debug("fragment name "+i+": "+fragmentName);
				ret.put(fragmentName, checksums.get(i));
			}
		}*/
		return ret;
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
			if( vf == vtSplitterConfig ){
				throw new FuseException("Cannot remove "+path)
				.initErrno(FuseException.EACCES);
			}
			String uniqueServiceName = new File(path).getName();
			boolean unmounted = storageServiceMgr.unmount(uniqueServiceName);
			if(unmounted) {
				virtualFolder.remove(path);
			} else {
				throw new FuseException("IO Exception on removing store.")
					.initErrno(FuseException.EIO);
			}
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
	

	/**
	 * Unmount backend stores
	 */
	@Override
	public void close() {		
		boolean unmounted;
		for(String storeName: storageServiceMgr.getServices().getStorageServicesNames()){
			if( ! storageServiceMgr.unmount(storeName)) {
				log.error("Store "+storeName+" could not be unmounted.");
			}
		}
	}
}
