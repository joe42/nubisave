package com.github.joe42.splitter.vtf;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import com.github.joe42.splitter.util.StringUtil;

import fuse.FuseFtype;
import fuse.compat.FuseStat;

public class VirtualFile {
	private String path, text;
	private FileEntry entry;
	public VirtualFile(String path) {
		this.path = path;
		entry = new FileEntry();
	}
	protected FileEntry getFileEntry(){
		return entry;
	}
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#getAttr()
	 */
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
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#truncate()
	 */
	public void truncate(){
		text = "";
	}
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#getText()
	 */
	public String getText(){
		return text;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#getPath()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#toString()
	 */
	public String toString() {
		return text;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#read(java.nio.ByteBuffer, long)
	 */
	public void read(ByteBuffer buf, long offset) {
		StringUtil.writeUTF8StringToByteBuffer(text, buf);
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#write(java.nio.ByteBuffer, long)
	 */
	public void write(ByteBuffer buf, long offset) {
		text = StringUtil.getUTF8FromByteBuffer(buf); // FIXME: file cannot be
														// larger than 4096
														// bytes
	}
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#setText(java.lang.String)
	 */
	public void setText(String text){
		this.text = text;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#getDir()
	 */
	public String getDir() {
		String parent = new File(path).getParent();
		if(parent == null){
			parent = path;
		}
		return parent;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.vtf.IVirtualFile#getName()
	 */
	public String getName() {
		return new File(path).getName();
	}
}
