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
	
	/**
	 * @param fileFragmentPaths the paths of the fragments belonging to one file
	 * @param requiredFragments the number of fragments required to reconstruct the file
	 */
	public FileFragments(ArrayList<String> fileFragmentPaths, int requiredFragments){
		this.fileFragmentPaths = fileFragmentPaths;
		this.requiredFragments = requiredFragments;
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
	
}
