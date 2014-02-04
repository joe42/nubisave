package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import org.apache.log4j.Logger;

import com.github.joe42.splitter.util.file.PropertiesUtil;
import com.github.joe42.splitter.vtf.Entry;
import com.github.joe42.splitter.vtf.FileEntry;
import com.github.joe42.splitter.vtf.FolderEntry;

import fuse.FuseFtype;

/**Manages file fragments of complete files.
 * Complete files are refered to by the pathname path, whereas their fragments are lists of pathnames to each file fragment.
 * It is used to keep track of the files which are dispersed into fragments or glued together in the class Splitter.*/
public class FileMetaDataStore {
	private static final Logger  log = Logger.getLogger("FileFragmentStore");
	private HTree fileMap;
	private HTree dirMap;
	private static final PropertiesUtil props;
	private static RecordManager recman;
	static {
		props = new PropertiesUtil("../bin/nubi.properties");
		try {
			recman = RecordManagerFactory.createRecordManager(props.getProperty("splitter_database_location"), props.getProperties());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**Create a new FileFragmentStore instance
	 * @param storages */
	public FileMetaDataStore() throws IOException { //
		// create or load
		dirMap = loadPersistentMap(recman, "dirmap");
		try {
			dirMap.put("/", new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new IOException("IO Exception on accessing metadata");
		}
		fileMap = loadPersistentMap(recman, "filemap");
	}
	
	public void reloadDataBase() throws IOException{
		try {
			recman = RecordManagerFactory.createRecordManager(props.getProperty("splitter_database_location"), props.getProperties());
		} catch (IOException e) {
			e.printStackTrace();
		}
		dirMap = loadPersistentMap(recman, "dirmap");
		try {
			dirMap.put("/", new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new IOException("IO Exception on accessing metadata");
		}
		fileMap = loadPersistentMap(recman, "filemap");
	}
	
	public static HTree loadPersistentMap(RecordManager recman, String mapName) throws IOException {
		long recid = recman.getNamedObject(mapName);
		HTree ret;
		if (recid != 0) {
			ret = HTree.load(recman, recid);
		} else {
			ret = HTree.createInstance(recman);
			recman.setNamedObject(mapName, ret.getRecid());
		}
		return ret;
	}

	/**
	 * Store a new FileEntry instance under path
	 * Before committing any changes of the returned FileEntry instance fileEntry, {@link #putFileEntry(String, FileEntry) putFileEntry(path, fileEntry)} needs to be called.
	 * @param path the path under which the FileEntry instance is stored
	 * @return the new FileEntry
	 * @throws IOException
	 */
	public FileEntry makeFileEntry(String path) throws IOException {
		FileEntry entry = new FileEntry();
		//Files.readAttributes(path, "creationTime,lastModifiedTime,lastAccessTime", LinkOption.NOFOLLOW_LINKS);
		Long nowL = (new Date()).getTime() / 1000L; 
		int now = nowL.intValue();
		entry.ctime = now;
		entry.atime = now;
		entry.mtime = now;
		fileMap.put(path, entry);
		return entry;
	}
	/**
	 * Store a new FolderEntry instance under path
	 * Before committing any changes of the returned FolderEntry instance folderEntry, {@link #putFolderEntry(String, FolderEntry) putFolderEntry(path, folderEntry)} needs to be called.
	 * @param path the path under which the FolderEntry instance is stored
	 * @return the new FolderEntry
	 * @throws IOException
	 */
	public FolderEntry makeFolderEntry(String path) throws IOException {
		FolderEntry entry = new FolderEntry();
		dirMap.put(path, entry);
		commit();
		return entry;
	}

	/**
	 * Store fileEntry under path
	 * This method must be called before committing changes made to entry.
	 * @param path the path of the FileEntry instance to store
	 * @param fileEntry the FileEntry instance to store
	 * @throws IOException
	 */
	public void putFileEntry(String path, FileEntry fileEntry) throws IOException{
		fileMap.put(path, fileEntry);
		commit();
	}

	/**
	 * Store folderEntry under path
	 * This method must be called before committing changes made to entry.
	 * @param path the path of the FolderEntry instance to store
	 * @param folderEntry the FolderEntry instance to store
	 * @throws IOException
	 */
	public void putFolderEntry(String path, FolderEntry folderEntry) throws IOException{
		dirMap.put(path, folderEntry);
	}
	
	/**
	 * Commit the entries added by the put*Entry and make*Entry methods
	 * Any changes made to the entries after calling those methods are ignored. To commit further changes, call the respective put*Entry method again, after the changes, and then call commit.
	 * @throws IOException
	 */
	public void commit() throws IOException {
		recman.commit();
	}

	/**
	 * Get the FileEntry instance stored under path
	 * Before committing any changes of the returned FileEntry instance fileEntry, {@link #putFileEntry(String, FileEntry) putFileEntry(path, fileEntry)} needs to be called.
	 * @param path
	 * @return the FileEntry instance stored under path or null if path is no FileEntry instance is stored under path
	 * @throws IOException
	 */
	public FileEntry getFileEntry(String path) throws IOException {
		return (FileEntry) fileMap.get(path);
	}

	/**
	 * Get the FolderEntry instance stored under path
	 * Before committing any changes of the returned FolderEntry instance folderEntry, {@link #putFolderEntry(String, FolderEntry) putFolderEntry(path, folderEntry)} needs to be called.
	 * @param path
	 * @return the FolderEntry instance stored under path or null if path is no FolderEntry instance is stored under path
	 * @throws IOException
	 */
	public FolderEntry getFolderEntry(String path) throws IOException {
		return (FolderEntry) dirMap.get(path);
	}
	
	public Entry getEntry(String path) throws IOException {
		Entry ret;
		if (getFileEntry(path) != null) {
			ret = getFileEntry(path);
		} else if (getFolderEntry(path) != null) {
			ret = (Entry) getFolderEntry(path);
		} else{
			return null;
		}
		return ret;
	}

	public FastIterator getFileEntryPaths() throws IOException {
		return fileMap.keys();
	}

	public FastIterator getFolderEntryPaths() throws IOException {
		return dirMap.keys();
	}
	
	public void remove(String path) throws IOException {
		dirMap.remove(path);
		fileMap.remove(path);
		commit();
	}

	public void rename(String from, String to) throws IOException { 
		Entry fromEntry = (Entry) getEntry(from);
		if(fromEntry instanceof FolderEntry){
			dirMap.put(to, fromEntry);
		} else {
			fileMap.put(to, fromEntry);
		}
		remove(from);
		commit();
	}

	public static RecordManager getRecordManager() {
		return recman;
	}


}