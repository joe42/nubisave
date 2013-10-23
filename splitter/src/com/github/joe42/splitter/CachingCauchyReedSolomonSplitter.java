package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.storagestrategies.StorageStrategyFactory;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;

import fuse.FuseException;
/**
 *Decorator caching access to an instance of {@link CauchyReedSolomonSplitter CauchyReedSolomonSplitter}.
 */
public class CachingCauchyReedSolomonSplitter implements Splitter {
	private FileSplittingQueue splittingQueue;
	private Splitter core;

	public CachingCauchyReedSolomonSplitter(Splitter core){
		this.core = core;
		splittingQueue = new FileSplittingQueue(core);
	}
	
	public int getRedundancy() {
		return core.getRedundancy();
	}

	public void setRedundancy(int redundancy) {
		core.setRedundancy(redundancy);
	}

	public String getStorageStrategyName() {
		return core.getStorageStrategyName();
	}

	public void setStorageStrategyName(String storageStrategyName) {
		core.setStorageStrategyName(storageStrategyName);
	}

	public BackendServices getBackendServices() {
		return core.getBackendServices();
	}

	public FileFragments splitFile(FileFragmentMetaDataStore fileFragmentMetaDataStore,
			String path, FileChannel temp) throws FuseException, IOException {
		splittingQueue.set(fileFragmentMetaDataStore); 
		return splittingQueue.put(path, temp);
	}

	public RandomAccessTemporaryFileChannel glueFilesTogether(
			FileFragmentMetaDataStore fileFragmentMetaDataStore, String path)
			throws FuseException {
		splittingQueue.set(fileFragmentMetaDataStore); 
		RandomAccessTemporaryFileChannel ret = splittingQueue.get(path);
		byte[] arr = new byte[6]; 
		try {
			ret.getChannel().read(ByteBuffer.wrap(arr));
			ret.getChannel().position(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("CachingStore read:"+new String(arr,0));
		return ret;
	}

	public double getStorageAvailability() {
		return core.getStorageAvailability();
	}

	@Override
	public StorageStrategyFactory getStorageStrategyFactory() {
		return core.getStorageStrategyFactory();
	}
	
	public void remove(String path){
		splittingQueue.remove(path);
	}

	@Override
	public void rename(String from, String to) {
		splittingQueue.rename(from, to);
	}
}
