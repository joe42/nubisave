package com.github.joe42.splitter.vtf;

import java.io.File;
import java.util.*;

import fuse.FuseFtype;
import fuse.compat.FuseDirEnt;

public class VirtualFileContainer {
	List<VirtualFile> vtf = new ArrayList<VirtualFile>();
	public void add(VirtualFile virtualFile) {
		vtf.add(virtualFile);
	}
	public void remove(VirtualFile virtualFile) {
		vtf.remove(virtualFile);
	}
	public void remove(String path) {
		for(VirtualFile file: vtf){
			if(file.getPath().equals(path)){
				vtf.remove(file);
				return;
			}
		}
	}

	public boolean containsFile(String path){
		for(VirtualFile file: vtf){
			if(file.getPath().equals(path)){
				return true;
			}
		}
		return false;
	}
	
	public List<String> getDirNames(){
		/**@return All directory names of this container*/
		List<String> ret = new ArrayList<String>();
		String dir;
		for(VirtualFile file: vtf){
			dir = file.getDir();
			while(dir != null){
				ret.add(dir);
				dir = new File(dir).getParent();
			}
		}
		return ret;
	}
	

	public List<String> getDirNames(String path){
		/**@return All directory names in path*/
		List<String> ret = new ArrayList<String>();
		String parent;
		File file;
		for(String dir: getDirNames()){
			file = new File(dir);
			parent = file.getParent();
			if(path.equals(parent)){
				ret.add(file.getName());
			}
		}
		ret.add(".");
		ret.add("..");
		return ret;
	}
	
	public List<String> getFileNames(String path){
		/**@return All file names in path*/
		List<String> ret = new ArrayList<String>();
		for(VirtualFile file: vtf){
			if(file.getDir().equals(path)){
				ret.add(file.getName());
			}
		}
		return ret;
	}
	
	public boolean containsDir(String path){
		return getDirNames().contains(path);
	}
	
	public FuseDirEnt[] getDir(String path) {
		List<FuseDirEnt> ret = new ArrayList<FuseDirEnt>();
		for (String file : getFileNames(path)) {
			FuseDirEnt entity = new FuseDirEnt();
			entity.name = file;
			entity.mode = FuseFtype.TYPE_FILE;
			ret.add(entity);
		}
		for (String dir : getDirNames(path)) {
			FuseDirEnt entity = new FuseDirEnt();
			entity.name = dir;
			entity.mode = FuseFtype.TYPE_DIR;
			ret.add(entity);
		}
		return ret.toArray(new FuseDirEnt[0]);
	}
	
	public VirtualFile get(String path){
		/**
		 * @return the first virtual File with the path path or null if it does not exist
		 */
		for(VirtualFile file: vtf){
			if(file.getPath().equals(path)){
				return file;
			}
		}
		return null;
	}
}
