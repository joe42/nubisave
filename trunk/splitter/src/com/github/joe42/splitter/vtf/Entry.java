package com.github.joe42.splitter.vtf;

import fuse.FuseFtype;
import fuse.compat.FuseStat;

public abstract class Entry {
	public String path;
	public int nlink;
	public int uid;
	public int gid;
	public int size;
	public int atime;
	public int mtime;
	public int ctime;
	public int blocksize;
	public int mode;
	public abstract void setMode(int mode);
	public FuseStat getFuseStat(){
		FuseStat stat = new FuseStat();
		stat.mode = mode;
		stat.nlink = nlink;
		stat.uid = uid;
		stat.gid = gid;
		stat.size = size;
		stat.atime = atime;
		stat.mtime = mtime;
		stat.ctime = ctime;
		stat.blocks = (size + blocksize-1) / blocksize;
		return stat;
	}
}
