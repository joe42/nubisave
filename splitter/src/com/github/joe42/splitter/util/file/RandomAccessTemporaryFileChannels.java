package com.github.joe42.splitter.util.file;

import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * Handles a collection of RandomAccessTemporaryFileChannel instances and provides a simple interface to access them through FileChannel instances starting at a given position.
 */
public class RandomAccessTemporaryFileChannels {
	private HashMap<String, RandomAccessTemporaryFileChannel> tempMap = new HashMap<String, RandomAccessTemporaryFileChannel>();
	
	/**
	 * Create and store a new RandomAccessTemporaryFileChannel instance along with key.
	 * @param key the key used to reference the created RandomAccessTemporaryFileChannel instance
	 */
	public void putNewFileChannel(String key){
		tempMap.put(key, new RandomAccessTemporaryFileChannel());
	}
	
	/**
	 * Store the RandomAccessTemporaryFileChannel instance temp under key
	 * @param key the key to store temp with
	 * @param temp an instance of RandomAccessTemporaryFileChannel to store with key
	 */
	public void put(String key, RandomAccessTemporaryFileChannel temp){
		tempMap.put(key, temp);
	}
	
	/**
	 * Get a FileChannel instance starting at offset 0 from the RandomAccessTemporaryFileChannel instance stored under key.
	 * @param key
	 * @return  the FileChannel instance associated with key or null if no instance is stored under key
	 */
	public FileChannel getFileChannel(String key){
		if(tempMap.get(key) == null)
			return null;
		return tempMap.get(key).getChannel(0);
	}
	
	/**
	 * Get a FileChannel instance starting at offset position from the RandomAccessTemporaryFileChannel instance stored under key.
	 * @param key the key of a RandomAccessTemporaryFileChannel instance
	 * @param position the offset where the FileChannel instance starts
	 * @return the FileChannel instance associated with key or null if no instance is stored under key
	 */
	public FileChannel getFileChannel(String key, long position){
		if(tempMap.get(key) == null)
			return null;
		return tempMap.get(key).getChannel(position);
	}
	
	/**
	 * Remove a RandomAccessTemporaryFileChannel instance associated with key
	 * @param key the key referencing the instance to remove
	 */
	public void delete(String key){
		if(tempMap.get(key) != null){
			tempMap.get(key).delete();
		}
		tempMap.remove(key);
		assert tempMap.get(key) == null;
	}
}
