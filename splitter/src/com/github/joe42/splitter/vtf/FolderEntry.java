package com.github.joe42.splitter.vtf;

import java.io.Serializable;

import fuse.FuseFtype;

public class FolderEntry extends Entry implements Serializable{
	private static final long serialVersionUID = 1L;
	public FolderEntry(){
		nlink = 1;
		size = 0;
		this.blocksize = 512;
		mode = FuseFtype.TYPE_DIR | 755;
	}
	public FolderEntry(int blocksize){
		nlink = 1;
		this.blocksize = blocksize;
		size = 0;
	}
	public void setMode(int mode) {
		this.mode = FuseFtype.TYPE_DIR | mode;
	}
}
