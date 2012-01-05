package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.SortedSet;

import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;

import fuse.FuseException;

public class FilePartFragmentStore extends FileFragmentStore{
	private static final long MAX_FILESIZE = 4096*80;
	private String lastFilePartPathWrittenTo = null;
	private String lastFilePartPathReadFrom = null;
	
	public FilePartFragmentStore(CauchyReedSolomonSplitter splitter) throws IOException {
		super(splitter);
		fileFragmentMetaDataStore = new FilePartFragmentMetaDataStore(MAX_FILESIZE);
	}

	public void write(String path, ByteBuffer buf, long offset) throws IOException, FuseException {
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset);
		System.out.println("filePartPath: "+filePartPath);
		if (tempFiles.getFileChannel(filePartPath) == null) {
			if( ! ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasFilePartFragments(filePartPath) ){
				System.out.println("Create new filePartPath: "+filePartPath);
				((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).put(path, offset);
				tempFiles.putNewFileChannel(filePartPath);
			} else {
				System.out.println("Get existing filePartPath: "+filePartPath);
				tempFiles.put(filePartPath, getFilePart(filePartPath));
			}
		}
		FileChannel wChannel = tempFiles.getFileChannel(filePartPath);
		wChannel.write(buf,offset % MAX_FILESIZE);		
		flushLastFilePartFromCache(filePartPath);
	}
	
	private void flushLastFilePartFromCache(String currentFilePartPath) throws FuseException, IOException {
		if(lastFilePartPathWrittenTo == null){
			lastFilePartPathWrittenTo = currentFilePartPath;
		} else if( ! lastFilePartPathWrittenTo.equals(currentFilePartPath) ){
			FileChannel temp = tempFiles.getFileChannel(lastFilePartPathWrittenTo);
			if(temp != null){
				System.out.println("flush lastFilePartPathWrittenTo: "+lastFilePartPathWrittenTo+" size: "+temp.size());
				tempReadChannel = null;
				splitter.splitFile(fileFragmentMetaDataStore, lastFilePartPathWrittenTo, temp, getRedundancy());
				tempFiles.delete(lastFilePartPathWrittenTo);
			}
			lastFilePartPathWrittenTo = currentFilePartPath;
		}
	}

	public void read(String path, ByteBuffer buf, long offset) throws FuseException, IOException {
		System.out.println("begin");
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset);
		if (tempReadChannel == null || lastFilePartPathReadFrom == null || ! lastFilePartPathReadFrom.equals(filePartPath)) { 
			if(tempReadChannel != null){
				tempReadChannel.getChannel().close();
			}
			tempReadChannel = getFilePart(filePartPath);
			 lastFilePartPathReadFrom = filePartPath;
		}
		System.out.println("has next: "+((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasNextFilePart(path, offset));

		 tempReadChannel.getChannel().read(buf, offset%MAX_FILESIZE);
		 System.out.println("buf.limit(): "+buf.limit()+" buf.position(): "+buf.position()+" "+"file size: "+((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getSize(path)+" offset: "+offset+" end of read: "+(offset+buf.limit()));
		if(buf.position() != buf.limit()  //available space in buffer exceeds file part size read from offset (important for sparse files: tempReadChannel.getChannel().size() < offset%MAX_FILESIZE+buf.limit())
				&& ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasNextFilePart(path, offset)){
			tempReadChannel.getChannel().close();
			filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset/MAX_FILESIZE*MAX_FILESIZE+MAX_FILESIZE);
			tempReadChannel = getFilePart(filePartPath);
			lastFilePartPathReadFrom = filePartPath;
			ByteBuffer bb = ByteBuffer.allocate(buf.remaining());
			tempReadChannel.getChannel().read(bb, 0);
			System.out.println("buf.position(); "+buf.position());
			bb.position(0);
			buf.put(bb);
		 }
	}

	/**
	 * Return a RandomAccessTemporaryFileChannel instance with the content of the file part under filePartPath.
	 * @param filePartPath the path of the file part
	 * @return a RandomAccessTemporaryFileChannel with the content of the file part
	 * @throws FuseException
	 * @throws IOException 
	 */
	private RandomAccessTemporaryFileChannel getFilePart(String filePartPath)
			throws FuseException, IOException {
		RandomAccessTemporaryFileChannel ret;
		if( ! ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasFilePartFragments(filePartPath) ){
			ret = new RandomAccessTemporaryFileChannel(); //empty (sparse) file
			ret.setLength(MAX_FILESIZE);
			return ret;
		}
		ret = splitter.glueFilesTogether(fileFragmentMetaDataStore, filePartPath);
		ret.setLength(MAX_FILESIZE);
		return ret;
	}
	
	/**
	 * Find out if the file associated with path has already been persisted.
	 * @param path path to the file
	 * @return true iff the file has been stored completely
	 * @throws IOException 
	 */
	public boolean hasFlushed(String path) throws IOException {
		for(String filePartPath:  ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPaths(path)){
			if(! hasFlushedFilePart(filePartPath)){
				return false;
			}
		}
		return true;
	}
	
	private boolean hasFlushedFilePart(String filePartPath) {
		return tempFiles.getFileChannel(filePartPath) == null;
	}

	public void rename(String from, String to) throws IOException, FuseException {
		remove(to);
		flushCache(from);
		((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).renameFileParts(from, to);
	}

	public void remove(String path) throws IOException {
		for (String fragmentName : ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFragments(path)) {
			new File(fragmentName).delete();
		}
		fileFragmentMetaDataStore.remove(path);
	}

	public void flushCache(String path) throws IOException, FuseException {
		for(String filePartPath:  ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPaths(path)){
			System.out.println("filePartPath to flush: "+filePartPath);
			if(! hasFlushedFilePart(filePartPath)){
				splitter.splitFile(fileFragmentMetaDataStore, filePartPath, tempFiles.getFileChannel(filePartPath), getRedundancy());
				removeCache(filePartPath);
			}
		}
	}

	public void truncate(String path, long size) throws IOException, FuseException {
		flushCache(path);
		String filePartPath;
		SortedSet<Integer> filePartNumbers = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartNumbers(path);
		int filePartNumber;
		while(filePartNumbers.size() != 0){
			filePartNumber =  filePartNumbers.last();
			filePartPath = path+"#"+filePartNumber+"#";
			if( filePartNumber*MAX_FILESIZE > size){
				if(((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasFilePartFragments(filePartPath)){
					for(String filePartFragment: ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartFragments(filePartPath)) {
						new File(filePartFragment).delete();
					}
				}
				((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).remove(path, filePartNumber);
			} else { 
				RandomAccessTemporaryFileChannel firstFilePart = getFilePart(filePartPath);
				firstFilePart.getChannel().truncate(size);
				tempFiles.put(filePartPath, firstFilePart);
				flushLastFilePartFromCache(filePartPath);
				return;
			}
		}
	}
	
	public void mknod(String path) throws IOException, FuseException{
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, 0);
		((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).put(path, 0);
		tempFiles.putNewFileChannel(filePartPath);
		flushLastFilePartFromCache(filePartPath);
	}
	
	public long getSize(String path) throws IOException {
		return ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getSize(path);
	}
}
