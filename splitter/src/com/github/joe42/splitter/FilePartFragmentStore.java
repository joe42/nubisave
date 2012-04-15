package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;

import fuse.FuseException;

public class FilePartFragmentStore extends FileFragmentStore{
	private static final Logger log = Logger.getLogger("FilePartFragmentStore");
	private static final long MAX_FILESIZE = 4096*800;
	private String lastFilePartPathWrittenTo = null;
	private String lastFilePartPathReadFrom = null;
	
	public FilePartFragmentStore(CauchyReedSolomonSplitter splitter) throws IOException {
		super(splitter);
		fileFragmentMetaDataStore = new FilePartFragmentMetaDataStore(MAX_FILESIZE);
	}

	public void write(String path, ByteBuffer buf, long offset) throws IOException, FuseException {
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset);
		if (tempFiles.getFileChannel(filePartPath) == null) {
			if( ! ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasFilePartFragments(filePartPath) ){
				log.debug("Create new filePartPath: "+filePartPath);
				((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).put(path, offset);
				tempFiles.putNewFileChannel(filePartPath);
			} else {
				log.debug("Get existing filePartPath: "+filePartPath);
				tempFiles.put(filePartPath, getFilePart(filePartPath));
			}
		}
		FileChannel wChannel = tempFiles.getFileChannel(filePartPath);
		wChannel.write(buf,offset % MAX_FILESIZE);		
		flushLastFilePartFromCache(filePartPath);
	}
	
	/**
	 * Flushes the last file part and then makes the the current file part the last file part
	 * @param currentFilePartPath the path to the file part, which will be the last file part after executing this method 
	 * @throws FuseException
	 * @throws IOException
	 */
	private void flushLastFilePartFromCache(String currentFilePartPath) throws FuseException, IOException {
		if(lastFilePartPathWrittenTo == null){
			lastFilePartPathWrittenTo = currentFilePartPath;
		} else if( ! lastFilePartPathWrittenTo.equals(currentFilePartPath) ){
			FileChannel temp = tempFiles.getFileChannel(lastFilePartPathWrittenTo);
			if(temp != null){
				log.debug("flush lastFilePartPathWrittenTo: "+lastFilePartPathWrittenTo+" size: "+temp.size());
				tempReadChannel = null;
				splitter.splitFile(fileFragmentMetaDataStore, lastFilePartPathWrittenTo, temp);
				tempFiles.delete(lastFilePartPathWrittenTo);
			}
			lastFilePartPathWrittenTo = currentFilePartPath;
		}
	}

	public void read(String path, ByteBuffer buf, long offset) throws FuseException, IOException {
		//TODO: read backward
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset);
		if (tempReadChannel == null || lastFilePartPathReadFrom == null || ! lastFilePartPathReadFrom.equals(filePartPath)) { 
			if(tempReadChannel != null){
				tempReadChannel.delete();
			}
			tempReadChannel = getFilePart(filePartPath);
			 lastFilePartPathReadFrom = filePartPath;
		}
		log.debug("has next: "+((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasNextFilePart(path, offset));
		
		tempReadChannel.getChannel().read(buf, offset%MAX_FILESIZE);
		if(buf.position() == buf.limit() || ! ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasNextFilePart(path, offset)){
			return;
		}
		//important for sparse files: add null bytes to fill up file parts smaller than MAX_FILESIZE 
		long nrOfNullBytes = MAX_FILESIZE - tempReadChannel.getChannel().size();
		while(nrOfNullBytes > 0 && buf.position() < buf.limit()){
			buf.put((byte) 0);
			nrOfNullBytes--;
		}		
		log.debug("buf.limit(): "+buf.limit()+" buf.position(): "+buf.position()+" "+"file size: "+((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getSize(path)+" offset: "+offset+" end of read: "+(offset+buf.limit()));
		if(buf.position() == buf.limit()){
			return;
		}
		//available space in buffer exceeds file part size read from offset
		tempReadChannel.delete();
		filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offset/MAX_FILESIZE*MAX_FILESIZE+MAX_FILESIZE);
		tempReadChannel = getFilePart(filePartPath);
		lastFilePartPathReadFrom = filePartPath;
		ByteBuffer bb = ByteBuffer.allocate(buf.remaining());
		tempReadChannel.getChannel().read(bb, 0);
		log.debug("buf.position(); "+buf.position());
		bb.position(0);
		buf.put(bb);
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
			log.debug("filePartPath to flush: "+filePartPath);
			if(! hasFlushedFilePart(filePartPath)){
				splitter.splitFile(fileFragmentMetaDataStore, filePartPath, tempFiles.getFileChannel(filePartPath));
				removeCache(filePartPath);
			}
		}
	}

	public void truncate(String path, long size) throws IOException, FuseException {
		String filePartPath;
		SortedSet<Integer> filePartNumbers = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartNumbers(path);
		int filePartNumber = filePartNumbers.last();
		long completeFileSizeWithoutLastFilePart = filePartNumber*MAX_FILESIZE;
		while(completeFileSizeWithoutLastFilePart > size){
			filePartPath = path+"#"+filePartNumber+"#";
			removeFilePart(path, filePartPath, filePartNumber);
			filePartNumber =  filePartNumbers.last();
			completeFileSizeWithoutLastFilePart = filePartNumber*MAX_FILESIZE;
		}
		filePartPath = path+"#"+filePartNumber+"#";
		truncateLastFilePart(path, size);
		return;
	}

	private void removeFilePart(String path, String filePartPath,
			int filePartNumber) throws IOException {
		if(((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).hasFilePartFragments(filePartPath)){
			for(String filePartFragment: ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartFragments(filePartPath)) {
				new File(filePartFragment).delete();
			}
		}
		((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).remove(path, filePartNumber);
	}

	/**
	 * Truncate the last file part, so that the whole file has the size #size. Create the file part if it does not exist.
	 * @param path path of the whole file
	 * @param size size of the whole file
	 * @throws IOException
	 * @throws FuseException
	 */
	private void truncateLastFilePart(String path, long size) throws IOException,
			FuseException {		
		String filePartPath;
		long offsetOfLastByte = size - 1; //offset of the last byte of a file with #size bytes
		filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, offsetOfLastByte);
		RandomAccessTemporaryFileChannel lastFilePart = getFilePart(filePartPath);
		lastFilePart.setLength(MAX_FILESIZE);
		if(size != 0 && size % MAX_FILESIZE == 0){ //size can have a maximum of MAX_FILESIZE
			size = MAX_FILESIZE;
		} else {
			size = size % MAX_FILESIZE;
		}
		lastFilePart.getChannel().truncate(size);
		tempFiles.put(filePartPath, lastFilePart);
		((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).put(path, offsetOfLastByte);
		flushLastFilePartFromCache(filePartPath);
	}
	
	public void mknod(String path) throws IOException, FuseException{
		String filePartPath = ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getFilePartPath(path, 0);
		tempFiles.putNewFileChannel(filePartPath);
		((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).put(path, 0);
		flushLastFilePartFromCache(filePartPath);
	}
	
	public long getSize(String path) throws IOException {
		return ((FilePartFragmentMetaDataStore)fileFragmentMetaDataStore).getSize(path);
	}
}
