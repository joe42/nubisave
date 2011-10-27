package com.github.joe42.splitter.vtf;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.FileUtil;

import fuse.compat.FuseStat;

public class VirtualRealFile extends VirtualFile {
	File realFile = null;
	public VirtualRealFile(String vtPath, String realPath) {
		super(vtPath);
		realFile = new File(realPath);
	}
	public FuseStat getAttr(){
		FileEntry entry = getFileEntry();
		entry.size = (int) realFile.length()*2;
		return entry.getFuseStat(); 
	}
	public void truncate(){
		FileUtil.writeFile(realFile, "");
	}
	public String getText(){
		return FileUtil.readFile(realFile);
	}
	public String getRealPath(){
		/**
		 * @return the real path of this virtual file
		 */
		return realFile.getAbsolutePath();
	}

	public String toString() {
		return FileUtil.readFile(realFile);
	}

	public void read(ByteBuffer buf, long offset) {
		String text = FileUtil.readFile(realFile);
		CharBuffer cbuf = buf.asCharBuffer();
		int limit = text.length();
		if(text.length()>buf.limit()){
			limit = buf.limit();
		}
		cbuf.put(text.substring((int) offset, limit));
		buf.position(limit*2);
	}

	public void write(ByteBuffer buf, long offset) {
		//FileUtil.writeFile(realFile, StringUtil.getUTF8FromByteBuffer(buf));
		FileUtil.writeFile(realFile, buf, offset); 
		// FIXME: file cannot be
														// larger than 4096
														// bytes
	}
	public void setText(String text){
		FileUtil.writeFile(realFile, text);
	}

}
