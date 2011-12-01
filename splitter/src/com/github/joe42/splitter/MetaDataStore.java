package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
public class MetaDataStore {
	private static final Logger  log = Logger.getLogger("FileFragmentStore");
	private HTree fileMap;
	private HTree dirMap;
	private HTree fileFragmentsMap;
	private RecordManager recman;

	/**Create a new FileFragmentStore instance
	 * @param storages */
	public MetaDataStore() throws IOException { //
		PropertiesUtil props = new PropertiesUtil("../bin/nubi.properties");
		recman = RecordManagerFactory.createRecordManager(props.getProperty("splitter_database_location"), props.getProperties());
		// create or load
		dirMap = loadPersistentMap(recman, "dirmap");
		try {
			dirMap.put("/", new FolderEntry());
			recman.commit();
		} catch (IOException e) {
			throw new IOException("IO Exception on accessing metadata");
		}
		fileMap = loadPersistentMap(recman, "filemap");
		fileFragmentsMap = loadPersistentMap(recman, "fileFragmentsMap"); 
	}
	
	private HTree loadPersistentMap(RecordManager recman, String mapName) throws IOException {
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

	/** Gets the fragments of the file
	 * @param fileName the path to the whole file 
	 * @return the paths to all fragments of the file
	 * @throws IOException */
	public ArrayList<String> getFragments(String fileName) throws IOException{
		return ((FileFragments)fileFragmentsMap.get(fileName)).getPaths();
	}
	
	/** Check if the store has fragments of the file fileName
	 * @param fileName the path to the whole file 
	 * @return true iff the store contains fragments of the file
	 * @throws IOException */
	public boolean hasFragments(String fileName) throws IOException{
		return fileFragmentsMap.get(fileName) != null;
	}

	/** Rename the path used to reference the file fragments
	 * @param from a path to a whole file 
	 * @param to the new path of a whole file
	 * @throws IOException */
	public void moveFragments(String from, String to) throws IOException{
		fileFragmentsMap.put(to, fileFragmentsMap.get(from));
		fileFragmentsMap.remove(from);
	}

	/**Sets a list of fragment paths for a whole file
	 * @param fileName the whole file
	 * @param fragmentPaths the fragments of the file 
	 * @throws IOException 
	 */
	public void setFragment(String fileName, ArrayList<String> fragmentPaths, int requiredFragments) throws IOException{
		fileFragmentsMap.put(fileName, new FileFragments(fragmentPaths, requiredFragments));
	}

	/**Get the number of fragments required to reconstruct the file
	 * @param fileName the file to reconstruct
	 * @return the number of fragments required to reconstruct the file or 0 if the file is not yet associated with any fragments
	 * @throws IOException 
	 */
	public int getNrOfRequiredFragments(String fileName) throws IOException{
		FileFragments fragments = (FileFragments) fileFragmentsMap.get(fileName);
		if(fragments == null){
			return 0;
		}
		return fragments.getNrOfRequiredFragments();
	}
	
	/**Get the number of fragments of the file
	 * @param fileName the complete file
	 * @return the number of all fragments for the complete file or 0 if the file is not yet associated with any fragments
	 * @throws IOException 
	 */
	public int getNrOfFragments(String fileName) throws IOException{
		FileFragments fragments = (FileFragments) fileFragmentsMap.get(fileName);
		if(fragments == null){
			return 0;
		}
		return fragments.getNrOfFragments();
	}

	/**Set the number of fragments required to reconstruct the file
	 * @param fileName the file to reconstruct
	 * @param requiredFragments the number of fragments required to reconstruct the file
	 * @throws IOException 
	 */
	public void setNrOfRequiredFragments(String fileName, int requiredFragments) throws IOException {
		((FileFragments) fileFragmentsMap.get(fileName)).setNrOfRequiredFragments(requiredFragments);		
	}

	public FileEntry makeFileEntry(String path) throws IOException {
		FileEntry entry = new FileEntry();
		fileMap.put(path, entry);
		return entry;
	}
	public FolderEntry makeFolderEntry(String path) throws IOException {
		FolderEntry entry = new FolderEntry();
		dirMap.put(path, entry);
		return entry;
	}

	public void commit() throws IOException {
		recman.commit();
	}

	public FileEntry getFileEntry(String path) throws IOException {
		return (FileEntry) fileMap.get(path);
	}

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
		fileFragmentsMap.remove(path);
	}

	public void rename(String from, String to) throws IOException { 
		Entry fromEntry = (Entry) getEntry(from);
		if(fromEntry instanceof FolderEntry){
			dirMap.put(to, fromEntry);
		} else {
			fileMap.put(to, fromEntry);
			moveFragments(from, to);
		}
		remove(from);
	}

}