package fuse.zipfs;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FileEntry extends Entry implements Serializable{
	private static final long serialVersionUID = 1L;
	List<String> fragment_names = new ArrayList<String>();
	public FileEntry(){
		nlink = 1;
		size = 0;
	}
}
