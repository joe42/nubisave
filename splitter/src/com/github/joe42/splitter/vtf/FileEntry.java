package com.github.joe42.splitter.vtf;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fuse.FuseFtype;

public class FileEntry extends Entry implements Serializable{
	private static final long serialVersionUID = 1L;
	public List<String> fragment_names = new ArrayList<String>();
	public FileEntry(){
		nlink = 1;
		size = 0;
		mode = FuseFtype.TYPE_FILE | 755;
		this.blocksize = 512;
	}
	public FileEntry(int blocksize){
		nlink = 1;
		size = 0;
		this.blocksize = blocksize;
	}
	public void setMode(int mode){
		this.mode = FuseFtype.TYPE_FILE | mode;
	}
}
