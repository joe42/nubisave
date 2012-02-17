package com.github.joe42.splitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;

/**
 * Extends FileFragmentMetaDataStore to allow management of file parts referenced by an offset.
 * This extension is used by StreamSplittingStore, which divides complete files to file parts with a maximal size, which in turn are fragmented by a Splitter. 
 */
public class FilePartFragmentMetaDataStore extends FileFragmentMetaDataStore{
	private HTree filePathToFilePartNumbersMap;
	private long maxFilePartSize;
	
	public FilePartFragmentMetaDataStore(long maxFilePartSize) throws IOException {
		super();
		this.maxFilePartSize = maxFilePartSize;
		filePathToFilePartNumbersMap = FileMetaDataStore.loadPersistentMap(recman, "filenameTofilePartNumbersMap"); 
	}
	
	/**Store a file part name along with its number persistently
	 * @param filePartName the path of the file part
	 * @param offset the offset, which is automatically assigned to a file part number 
	 * @throws IOException 
	 */
	public void put(String filePartName, long offset) throws IOException{
		SortedSet<Integer> filePartNumbers = (SortedSet<Integer>) filePathToFilePartNumbersMap.get(filePartName);
		if(filePartNumbers == null){
			filePartNumbers = new TreeSet<Integer>();
		}
		filePartNumbers.add((int)(offset/maxFilePartSize));
		filePathToFilePartNumbersMap.put(filePartName, filePartNumbers);
		commit();
	}

	/** Gets the fragments of the file filePath
	 * @param filePath the path to a whole file or a file part
	 * @return the paths to all fragments of the whole file or the file part specified by filePath
	 * @throws IOException*/ 
	public ArrayList<String> getFragments(String filePath) throws IOException{
		if(hasFragments(filePath)){
			return getFilePartFragments(filePath);
		}
		ArrayList<String> ret = new ArrayList<String>();
		for(int filePartNumber: getFilePartNumbers(filePath)) {
			if( hasFragments(filePath+"#"+filePartNumber+"#") ) {
				ret.addAll( super.getFragments(filePath+"#"+filePartNumber+"#"));
			}
		}
		return ret;
	}

	/** Gets the fragments of one file part 
	 * @param filePath the path to the complete file
	 * @param filePartNumber the number of the file part of the complete file
	 * @return the paths to all fragments of the file part
	 * @throws IOException*/ 
	public ArrayList<String> getFilePartFragments(String filePath, int filePartNumber) throws IOException{
		return  super.getFragments(filePath+filePartNumber);
	}
	
	/** Gets the fragments of one file part 
	 * @param filePartPath the path to the file part 
	 * @return the paths to all fragments of the file part
	 * @throws IOException*/ 
	public ArrayList<String> getFilePartFragments(String filePartPath) throws IOException{
		return super.getFragments(filePartPath);
	}
	
	/** Check if the store has any fragments of the file part
	 * @param filePartPath the path to the file part 
	 * @return true iff the store has any fragments of the file part
	 * @throws IOException
	 */
	public boolean hasFilePartFragments(String filePartPath) throws IOException{
		return super.hasFragments(filePartPath);
	}

	/** Get the numbers of all file parts of the complete file
	 * Empty file parts, that is file parts to which no content has been written, are not considered.
	 * @param filePath the path to the complete file
	 * @return all numbers of existing file parts of the complete file
	 * @throws IOException
	 */
	public SortedSet<Integer> getFilePartNumbers(String filePath) throws IOException{
		SortedSet<Integer> ret = (SortedSet<Integer>) filePathToFilePartNumbersMap.get(filePath);
		if(ret == null){
			return new TreeSet<Integer>();
		}
		return ret;
	}

	/** Maps a path of a complete file to the path of this file's file part of the given offset
	 * @param filePath the path to a complete file
	 * @param offset the offset, which is automatically assigned to a file part number 
	 * @return the path of the file part
	 * @throws IOException
	 */
	public String getFilePartPath(String filePath, long offset) throws IOException{
		return filePath+"#"+offset/maxFilePartSize+"#";
	}

	/** Check if a file part has a succeeding file part 
	 * @param filePath the path to a complete file
	 * @param offset the offset, which is automatically assigned to a file part number 
	 * @return true iff the store has a file part of the complete file filePath with a higher number than the one associated with offset
	 * @throws IOException
	 */
	public boolean hasNextFilePart(String filePath, long offset) throws IOException{
		SortedSet<Integer> filePartNumbers = getFilePartNumbers(filePath);
		//if(filePartNumbers.size() != 0)
			//System.out.println("filePartNumbers.last()*maxFilePartSize:"+filePartNumbers.last()*maxFilePartSize);
		return filePartNumbers.size() != 0 && filePartNumbers.last()*maxFilePartSize > offset;
	}
	
	/** Get all file part paths of the complete file filePath
	 * @param filePath the path to a complete file
	 * @return the paths to all the file parts
	 * @throws IOException
	 */
	public ArrayList<String> getFilePartPaths(String filePath) throws IOException{
		ArrayList<String> ret = new ArrayList<String>();
		for(int filePartNumber: getFilePartNumbers(filePath)){
			ret.add(filePath+"#"+filePartNumber+"#");
		}
		return ret;
	}
	
	/** Rename the paths used to reference all file parts of a complete file
	 * @param fromFilePath a path to a complete file  
	 * @param toFilePath the new path to the complete file 
	 * @throws IOException */
	public void renameFileParts(String fromFilePath, String toFilePath) throws IOException{
		for(int filePartNumber: getFilePartNumbers(fromFilePath)){
			super.moveFragments(fromFilePath+"#"+filePartNumber+"#", toFilePath+"#"+filePartNumber+"#");
		}
		filePathToFilePartNumbersMap.put(toFilePath, (Set<Integer>) filePathToFilePartNumbersMap.get(fromFilePath));
		filePathToFilePartNumbersMap.remove(fromFilePath);
		commit();
	}

	/**
	 * Remove the mapping from the complete file filePath to any part numbers and fragments.
	 * @param filePath
	 * @throws IOException
	 */
	public void remove(String filePath) throws IOException{
		for(int filePartNumber: getFilePartNumbers(filePath)){
			super.remove(filePath+"#"+filePartNumber+"#");
		}
		filePathToFilePartNumbersMap.remove(filePath);
		recman.commit();
	}
	
	/**
	 * Remove the mapping from the file part filePath to the part number filePartNumber and any fragments thereof.
	 * @param filePath path to a complete file
	 * @param filePartNumber number of a file part of the complete file
	 * @throws IOException
	 */
	public void remove(String filePath, int filePartNumber) throws IOException{
		super.remove(filePath+"#"+filePartNumber+"#");
		SortedSet<Integer> filePartNumbers = (SortedSet<Integer>) filePathToFilePartNumbersMap.get(filePath);
		filePartNumbers.remove(filePartNumber);
		filePathToFilePartNumbersMap.put(filePath, filePartNumbers);
		recman.commit();
	}

	/** Get the size of the complete file under filePath
	 * @param filePath path to a complete file
	 * @return the size of the complete file specified by filePath
	 * @throws IOException
	 */
	public long getSize(String filePath) throws IOException{
		long ret = 0;
		SortedSet<Integer> filePartNumbers = getFilePartNumbers(filePath);
		if(filePartNumbers.isEmpty()){
			return 0;
		}
		int highestFilePartNumber = filePartNumbers.last();
		if(highestFilePartNumber == 0){
			return getFragmentsSize(filePath+"#"+highestFilePartNumber+"#");
		}
		ret = highestFilePartNumber*maxFilePartSize+getFragmentsSize(filePath+"#"+highestFilePartNumber+"#");
		return ret;
	}
	
}
