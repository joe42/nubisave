package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map;

import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.storagestrategies.StorageStrategyFactory;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;

import fuse.FuseException;

public interface Splitter {

	public abstract int getRedundancy();

	public abstract void setRedundancy(int redundancy);

	public abstract String getStorageStrategyName();

	public abstract void setStorageStrategyName(String storageStrategyName);

	public abstract BackendServices getBackendServices(); 

	public abstract FileFragments splitFile(
			FileFragmentMetaDataStore fileFragmentMetaDataStore, String path,
			FileChannel temp) throws FuseException, IOException;

	public abstract RandomAccessTemporaryFileChannel glueFilesTogether(
			FileFragmentMetaDataStore fileFragmentMetaDataStore, String path)
			throws FuseException;

	/**
	 * Get the minimal availability of files stored by the current Splitter instance.
	 * @return the availability in percent
	 */
	public abstract double getStorageAvailability();
	
	public StorageStrategyFactory getStorageStrategyFactory();
	
	public void remove(String path);

	public abstract void rename(String from, String to);
	
	/**
	 * Get estimated redundancy factor for the data stored.
	 * @return the redundancy factor, which is one if the size of the data stored remains the same as the original data's size
	 */
	public abstract double getStorageRedundancy();

	public abstract Map<String, String> getCodecInfo();

}