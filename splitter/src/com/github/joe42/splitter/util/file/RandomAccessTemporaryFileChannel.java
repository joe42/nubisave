package com.github.joe42.splitter.util.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.math.BigInteger;


/**
 * Provides a simple interface to create and randomly access temporary files. The file is automatically removed when the JVM is terminated normally.
 */
public class RandomAccessTemporaryFileChannel {
	private File temp;
	private RandomAccessFile ramFile;
	
	/**
	 * Create a new temporary file for random access.
	 */
	public RandomAccessTemporaryFileChannel(){
		try {
			temp = File.createTempFile(new BigInteger(130, new SecureRandom()).toString(32), ".splitter.tmp");
			temp.deleteOnExit();
			ramFile = new RandomAccessFile(temp, "rw");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Get a FileChannel instance of the temporary file
	 * @return a FileChannel instance of the temporary file
	 */
	public FileChannel getChannel(){
		return ramFile.getChannel();
	}
	
	/**
	 * Get a FileChannel instance of the temporary file starting at the offset given by position
	 * @param position the offset of the FileChannel instance
	 * @return a FileChannel instance of the temporary file
	 */
	public FileChannel getChannel(long position){
		try {
			ramFile.seek(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ramFile.getChannel();
	}
	
	/**
	 * Remove the temporary file to free resources
	 */
	public void delete(){
		temp.delete();
		try {
			ramFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the length of this file.
	 * 	Can be used as a simple means to create a sparse file.
	 * If the present length of the file as returned by the length method is greater than the newLength argument then the file will be truncated. In this case, if the file offset as returned by the getFilePointer method is greater than newLength then after this method returns the offset will be equal to newLength.
	 * If the present length of the file as returned by the length method is smaller than the newLength argument then the file will be extended. In this case, the contents of the extended portion of the file are not defined.
	 * @param newLength The desired length of the file 
	 */
	public void setLength(long newLength) {
		try {
			ramFile.setLength(newLength);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
