package com.github.joe42.splitter.vtf;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.*;

import com.github.joe42.splitter.util.StringUtil;

import fuse.compat.FuseStat;

public class VirtualFile {
	private String path, text;
	private FileEntry entry;
	public VirtualFile(String path) {
		this.path = path;
		this.text = "";
		entry = new FileEntry();
	}
	protected FileEntry getFileEntry(){
		return entry;
	}

	public FuseStat getAttr(){
		entry.size = getNumOfBytes();
		return entry.getFuseStat();
	}
	private int getNumOfBytes() {
		try {
			return text.getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void truncate(){
		text = "";
	}

	public String getText(){
		return text;
	}

	public String getPath() {
		/**
		 * @return the full path to this virtual file
		 **/
		return path;
	}

	public String toString() {
		return text;
	}

	public void read(ByteBuffer buf, long offset) {
		StringUtil.writeUTF8StringToByteBuffer(text, buf);
	}

	public void write(ByteBuffer buf, long offset) {
		text = StringUtil.getUTF8FromByteBuffer(buf); // FIXME: file cannot be
														// larger than 4096
														// bytes
	}

	public void setText(String text){
		this.text = text;
	}

	public String getDir() {
		String parent = new File(path).getParent();
		if(parent == null){
			parent = path;
		}
		return parent;
	}

	public String getName() {
		return new File(path).getName();
	}
}
