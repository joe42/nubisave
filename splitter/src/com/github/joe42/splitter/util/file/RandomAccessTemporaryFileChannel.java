package com.github.joe42.splitter.util.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.math.BigInteger;


public class RandomAccessTemporaryFileChannel {
	private File temp;
	private RandomAccessFile ramFile;
	public RandomAccessTemporaryFileChannel(){
		try {
			temp = File.createTempFile(new BigInteger(130, new SecureRandom()).toString(32), ".splitter.tmp");
			temp.deleteOnExit();
			ramFile = new RandomAccessFile(temp, "rw");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public FileChannel getChannel(){
		return ramFile.getChannel();
	}
	public FileChannel getChannel(long position){
		try {
			ramFile.seek(position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ramFile.getChannel();
	}
	public void delete(){
		temp.delete();
		try {
			ramFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
