package fuse.zipfs;

public abstract class Entry {
	public String path;
	public int nlink;
	public int uid;
	public int gid;
	public int size;
	public int atime;
	public int mtime;
	public int ctime;
}
