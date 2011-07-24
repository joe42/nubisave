package com.github.joe42.splitter;

import java.nio.channels.FileChannel;
import java.util.HashMap;

public class RandomAccessTemporaryFileChannels {
	private HashMap<String, RandomAccessTemporaryFileChannel> tempMap = new HashMap<String, RandomAccessTemporaryFileChannel>();
	public void putNewFileChannel(String key){
		tempMap.put(key, new RandomAccessTemporaryFileChannel());
	}
	public void put(String key, RandomAccessTemporaryFileChannel temp){
		tempMap.put(key, temp);
	}
	public FileChannel getFileChannel(String key){
		if(tempMap.get(key) == null)
			return null;
		return tempMap.get(key).getChannel();
	}
	public void delete(String key){
		tempMap.get(key).delete();
		tempMap.remove(key);
	}
}
