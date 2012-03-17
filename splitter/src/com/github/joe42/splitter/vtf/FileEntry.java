package com.github.joe42.splitter.vtf;
import java.io.Serializable;

import fuse.FuseFtype;

public class FileEntry extends Entry{
	public FileEntry(){
		nlink = 1;
		size = 0;
		mode = FuseFtype.TYPE_FILE | 0664;
		this.blocksize = 512;
	}
	public FileEntry(int blocksize){
		nlink = 1;
		size = 0;
		mode = FuseFtype.TYPE_FILE | 0664;
		this.blocksize = blocksize;
	}
	public void setMode(int mode){
		this.mode = FuseFtype.TYPE_FILE | mode;
	}
}
