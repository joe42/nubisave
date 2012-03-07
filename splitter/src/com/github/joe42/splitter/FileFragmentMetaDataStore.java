package com.github.joe42.splitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

/**
 * Stores the location of fragments of a file split by a Splitter with information on how to reconstruct the original file.
 */
public class FileFragmentMetaDataStore {
	protected HTree fileFragmentsMap;
	private HTree filePartsMap;
	protected RecordManager recman;
	
	
	public FileFragmentMetaDataStore() throws IOException{
		PropertiesUtil props = new PropertiesUtil("../bin/nubi.properties");
		recman = FileMetaDataStore.getRecordManager();
		fileFragmentsMap = FileMetaDataStore.loadPersistentMap(recman, "fileFragmentsMap"); 
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
		commit();
	}
	
	/** Rename the path used to reference a file fragment
	 * @param from a path of a file fragment
	 * @param to the new path of the file fragment
	 * @throws IOException */
	public void moveFragment(String from, String to) throws IOException{
		FastIterator iter = fileFragmentsMap.keys();
		String key =  (String) iter.next();
		FileFragments fragments;
        while ( key != null ) {
        	fragments = (FileFragments) fileFragmentsMap.get(key);
        	if(fragments.containsPath(from)){
        		fragments.rename(from, to);
        		fileFragmentsMap.put(key, fragments);
        		commit();
        		return;
        	}
    		key =  (String) iter.next();
        }
		System.out.println("None of the file parts contained the file "+from+" this should not happen.");
	}
	
	/** Check if a file fragment exists
	 * @param fragmentName a path of a file fragment
	 * @return true iff the file fragment exists
	 * @throws IOException */
	public boolean hasFragment(String fragmentName) throws IOException{
		FastIterator iter = fileFragmentsMap.keys();
		String key =  (String) iter.next();
		FileFragments fragments;
        while ( key != null ) {
        	fragments = (FileFragments) fileFragmentsMap.get(key);
        	if(fragments.containsPath(fragmentName)){
        		return true;
        	}
    		key =  (String) iter.next();
        }
		return false;
	}

	/**Sets a list of fragment paths for a whole file
	 * @param fileName the whole file
	 * @param fragmentPaths the fragments of the file 
	 * @param nrOfRequiredSuccessfullyStoredFragments the number of file fragments that must be stored successfully
	 * @param checksum the complete file's checksum 
	 * @param filesize the complete file's size 
	 * @throws IOException 
	 */
	public void setFragment(String fileName, ArrayList<String> fragmentPaths, int requiredFragments, int nrOfRequiredSuccessfullyStoredFragments, String checksum, long filesize) throws IOException{
		fileFragmentsMap.put(fileName, new FileFragments(fragmentPaths, requiredFragments, nrOfRequiredSuccessfullyStoredFragments, checksum, filesize));
		commit();
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
		commit();
	}
	
	/**Get the minimal number of fragments of the complete file that should certainly have been stored 
	 * @param fileName the complete file
	 * @return the minimal number of fragments that should certainly have been stored 
	 * @throws IOException 
	 */
	public int  getNrOfRequiredSuccessfullyStoredFragments(String fileName) throws IOException {
		FileFragments fragments = (FileFragments) fileFragmentsMap.get(fileName);
		if(fragments == null){
			return 0;
		}
		return fragments.getNrOfRequiredSuccessfullyStoredFragments();
	}
	
	/** Gets all fragments of a file 
	 * @param fileName the path to the whole file 
	 * @return the paths to all fragments of the file
	 * @throws IOException */
	public ArrayList<String> getFragments(String fileName) throws IOException{
		return ((FileFragments)fileFragmentsMap.get(fileName)).getPaths();
	}

	/**
	 * Persist the changes made by the methods moveFragments, setFragment and setNrOfRequiredFragments
	 * Any changes made to the entries after calling those methods are ignored. To commit further changes, call the respective put*Entry method again, after the changes, and then call commit.
	 * @throws IOException
	 */
	public void commit() throws IOException {
		recman.commit();
	}
	
	/**
	 * Remove the mapping from the complete file filePath to any fragments.
	 * @param filePath
	 * @throws IOException
	 */
	public void remove(String filePath) throws IOException{
		fileFragmentsMap.remove(filePath);
		recman.commit();
	}
	
	public long getFragmentsSize(String filePath) throws IOException{
		FileFragments fragments = ((FileFragments)fileFragmentsMap.get(filePath));
		if(fragments == null){
			return 0;
		}
		return fragments.getFilesize();
	}
}