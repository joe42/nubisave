package com.github.joe42.splitter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

/**
 *Stores the paths to the fragments of a single file along with the required number of fragments to reconstruct this file.
 */
public class FileFragments implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<String> fileFragmentPaths;
	private int requiredFragments;
	private int nrOfRequiredSuccessfullyStoredFragments;
	private String checksum;
	private long filesize;
	
	/**
	 * @param fileFragmentPaths the paths of the fragments belonging to one file
	 * @param requiredFragments the number of fragments required to reconstruct the file
	 * @param nrOfRequiredSuccessfullyStoredFragments the number of file fragments that must be stored successfully
	 * @param checksum the complete file's checksum 
	 * @param filesize the complete file's size 
	 * @param offset the offset of the complete file, if it is yet just another fragment of a greater complete file and 0 otherwise
	 */
	public FileFragments(ArrayList<String> fileFragmentPaths, int requiredFragments, int nrOfRequiredSuccessfullyStoredFragments, String checksum, long filesize){
		this.fileFragmentPaths = fileFragmentPaths;
		this.requiredFragments = requiredFragments;
		this.nrOfRequiredSuccessfullyStoredFragments = nrOfRequiredSuccessfullyStoredFragments;
		this.checksum = checksum;
		this.filesize = filesize;
	}

	public void setFilesize(long filesize){
		this.filesize= filesize;
	}

	public long getFilesize(){
		return filesize;
	}
	
	public void setChecksum(String checksum){
		this.checksum= checksum;
	}

	public String getChecksum(){
		return checksum;
	}
	
	public void setNrOfRequiredSuccessfullyStoredFragments(int nrOfRequiredSuccessfullyStoredFragments){
		this.nrOfRequiredSuccessfullyStoredFragments= nrOfRequiredSuccessfullyStoredFragments;
	}

	public int getNrOfRequiredSuccessfullyStoredFragments(){
		return nrOfRequiredSuccessfullyStoredFragments;
	}

	
	public void setRequiredFragments(int requiredFragments){
		this.requiredFragments= requiredFragments;
	}

	public int getNrOfRequiredFragments(){
		return requiredFragments;
	}
	
	public int getNrOfFragments(){
		return fileFragmentPaths.size();
	}

	public ArrayList<String> getPaths(){
		return fileFragmentPaths;
	}
	
	public void setPaths(ArrayList<String> fileFragmentPaths){
		this.fileFragmentPaths = fileFragmentPaths;
	}

	public void setNrOfRequiredFragments(int requiredFragments) {
		this.requiredFragments = requiredFragments;
	}

	public void rename(String from, String to) {
		int index = fileFragmentPaths.indexOf(from);
		fileFragmentPaths.remove(from);
		fileFragmentPaths.add(index, to);
	}

	public boolean containsPath(String fragmentPath) {
		return fileFragmentPaths.contains(fragmentPath);
	}
	
}
