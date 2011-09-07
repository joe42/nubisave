package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.SecureRandom;
import java.math.BigInteger;


public class RandomAccessTemporaryFileChannel {
	private FileChannel channel;
	private File temp;
	public RandomAccessTemporaryFileChannel(){
		try {
			temp = File.createTempFile(new BigInteger(130, new SecureRandom()).toString(32), ".splitter.tmp");
			temp.deleteOnExit();
			channel = new RandomAccessFile(temp, "rw").getChannel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public FileChannel getChannel(){
		return channel;
	}
	public void delete(){
		temp.delete();
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
