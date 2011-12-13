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
}
