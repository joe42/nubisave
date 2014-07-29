package com.github.joe42.splitter.vtf;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.FileUtil;

import fuse.compat.FuseStat;

/**
 * Represents a real file similar to a symbolic link.
 */
public class VirtualRealFile extends VirtualFile {
	File realFile = null;
	public VirtualRealFile(String vtPath, String realPath) {
		super(vtPath);
		realFile = new File(realPath);
	}
	public FuseStat getAttr(){
		FileEntry entry = getFileEntry();
		try {
			entry.size = (int) getText().getBytes("UTF-8").length;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return entry.getFuseStat(); 
	}
	public void truncate(){
		FileUtil.writeFile(realFile, "");
	}
	public String getText(){
		return new String(FileUtil.readFile(realFile).getBytes(), Charset.forName("UTF-8"));
	}
	public String getRealPath(){
		/**
		 * @return the real path of this virtual file
		 */
		return realFile.getAbsolutePath();
	}

	public String toString() {
		return new String(FileUtil.readFile(realFile).getBytes(), Charset.forName("UTF-8"));
	}

	public void read(ByteBuffer buf, long offset) {
		byte[] text = getText().getBytes(Charset.forName("UTF-8"));
		int limit = text.length;
		if(limit>buf.limit()){
			limit = buf.limit();
		}
		buf.put(text, (int)offset, limit);
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
