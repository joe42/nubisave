package com.github.joe42.splitter;

import java.io.Serializable;

public class FolderEntry extends Entry implements Serializable{
	private static final long serialVersionUID = 1L;
	public FolderEntry(){
		nlink = 1;
		size = 512;
	}
}
