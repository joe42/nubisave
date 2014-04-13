package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.helper.FastIterator;
import jdbm.htree.HTree;

import com.github.joe42.splitter.storagestrategies.OptimalRedundancyStategy;
import com.github.joe42.splitter.storagestrategies.StorageStrategy;
import com.github.joe42.splitter.storagestrategies.StorageStrategyFactory;
import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.PropertiesUtil;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;

import fuse.FuseException;

//TODO: The task queue is mostly empty. So the actual bottle neck is somewhere before enqueueing data.
//Most likely it is about persisting the data to the database before committing. Check this by 
//making fileMap a hashmap. Could be improved by JDBM 4 and parallelizing writes?
public class FileSplittingQueue {

	public enum QueueElementStatus {
		DIRTY, CLEAN, PROCESSING
	
	}

	private static final Integer MAX_NUMBER_OF_ELEMENTS = 10;
	
	private ConcurrentHashMap<String, QueueElementStatus> fileStatusMap;
	private HTree fileMap; //TODO:needs to be synchronous
	private Splitter core;
	private FileFragmentMetaDataStore fileFragmentMetaDataStore;
	private RecordManager recMan;
	private Integer nrOfThreads = 3;
	private OrderedExecutorService executor = new OrderedExecutorService(nrOfThreads, MAX_NUMBER_OF_ELEMENTS);
	private Semaphore unusedQueueElements = new Semaphore(MAX_NUMBER_OF_ELEMENTS);

	private StorageStrategyFactory storageStrategyFactory;
	 //TODO: queue in thread + exponential backup 
	public FileSplittingQueue(Splitter core){
		this.core = core;
		this.storageStrategyFactory = core.getStorageStrategyFactory(); 
		String fileName = "FileSplittingQueue";

		PropertiesUtil props = new PropertiesUtil("../bin/nubi.properties");
	    this.recMan = null;
		try {
			recMan = RecordManagerFactory.createRecordManager(props.getProperty("splitter_database_location")+"."+fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fileMapName = "fileMap";
		FastIterator filePaths = null;
		try {
			long recid = recMan.getNamedObject(fileMapName);
			if (recid != 0) {
				fileMap = HTree.load(recMan, recid);
			} else {
				fileMap = HTree.createInstance(recMan);
				recMan.setNamedObject(fileMapName, fileMap.getRecid());
			}
			filePaths = fileMap.keys();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		fileStatusMap = new ConcurrentHashMap<String, QueueElementStatus>();
		String path;
		while((path=(String)filePaths.next()) != null){
			fileStatusMap.put(path, QueueElementStatus.DIRTY);
		}
	}

	public void set(FileFragmentMetaDataStore fileFragmentMetaDataStore) {
		this.fileFragmentMetaDataStore = fileFragmentMetaDataStore;
		
	}

	public FileFragments put(String path, FileChannel temp) {
		byte[] arr;
		try {
			FileFragments ret;
			arr = new byte[(int) temp.size()];
			temp.read(ByteBuffer.wrap(arr));
			
			synchronized(this){
				//->element cannot be made dirty during processing->element cannot be renamed during processing
				//TODO: also remove previous tasks with "old" path after rename/remove
				while(fileStatusMap.get(path) == QueueElementStatus.PROCESSING){
					System.out.println("Waiting for element in process");
					try {
						wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				fileMap.put(path, arr);
				fileStatusMap.put(path, QueueElementStatus.DIRTY);
				//recMan.commit();

				ret = setFragments(path, temp);
				//Metadata_ filepaths need to be registered before the files are split. Otherwise, they might differ from the returned filepaths.
				fileFragmentMetaDataStore.setFragments(path, ret.getPaths(), ret.getNrOfRequiredFragments(), ret.getNrOfRequiredSuccessfullyStoredFragments(), ret.getChecksums(), ret.getFilesize());
				
			}
			addToSplitExecutor(path);		
			return ret;
		} catch (IOException e) {
			try {
				recMan.rollback();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
		return null;
		
		
	}

	private synchronized FileFragments setFragments(String path, FileChannel temp) throws IOException {
		ArrayList<String> fragmentPaths = new ArrayList<String>();
		int nr_of_file_fragments;
		int nr_of_redundant_fragments;
		int nr_of_file_fragments_required;
		int nrOfRequiredSuccessfullyStoredFragments;
		int nrOfElements; 
		String fragment_name;
		if(! fileFragmentMetaDataStore.hasFragments(path) || storageStrategyFactory.changeToCurrentStrategy()){
			List<String> fragmentDirectories;
			StorageStrategy storageStrategy = storageStrategyFactory.createStrategy(core.getStorageStrategyName(), core.getRedundancy());
			fragmentDirectories = storageStrategy.getFragmentDirectories();
			nr_of_file_fragments = fragmentDirectories.size();
			if(storageStrategy instanceof OptimalRedundancyStategy){
				nrOfElements = ((OptimalRedundancyStategy)storageStrategy).getNrOfElements();
			} else {
				nrOfElements = nr_of_file_fragments;
			}
			String uniquePath, uniqueFileName;
			uniquePath = StringUtil.getUniqueAsciiString(path);
			uniqueFileName = uniquePath.replaceAll("/", "_");
			for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
				fragment_name = fragmentDirectories.get(fragment_nr) +"/"+ uniqueFileName 
						+ '#' + fragment_nr;
				fragmentPaths.add(fragment_name);
			}
			nr_of_redundant_fragments =  storageStrategy.getNrOfRedundantFragments();
			nr_of_file_fragments_required =  nrOfElements - nr_of_redundant_fragments;
			nrOfRequiredSuccessfullyStoredFragments = storageStrategy.getNrOfRequiredSuccessfullyStoredFragments();
		} else {
			fragmentPaths = fileFragmentMetaDataStore.getFragments(path);
			nr_of_file_fragments_required = fileFragmentMetaDataStore.getNrOfRequiredFragments(path);
			nrOfRequiredSuccessfullyStoredFragments = fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path);
		}
		return new FileFragments(fragmentPaths, nr_of_file_fragments_required, nrOfRequiredSuccessfullyStoredFragments, null, temp.size());
	}

	public synchronized RandomAccessTemporaryFileChannel get(String path) throws FuseException {
		Object byteArr = null;
		try {
			byteArr = getFile(path);
			if( byteArr != null) {
				RandomAccessTemporaryFileChannel ret = new RandomAccessTemporaryFileChannel();
				ret.getChannel().write(ByteBuffer.wrap((byte[])byteArr));
				ret.getChannel().position(0);
				return ret;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Non cached read of "+path); 
		return core.glueFilesTogether(fileFragmentMetaDataStore, path);
	}

	/**
	 * Return cached file associated with path, or null otherwise.
	 * @param path
	 * @return byte array as instance of Object
	 * @throws IOException
	 */
	private synchronized Object getFile(String path) throws IOException {
		Object byteArr;
		byteArr = (byte[])fileMap.get(path);
		return byteArr;
	}

	public void addToSplitExecutor(String path){
		try {
			System.out.println("AQUIRE element from queue: "+unusedQueueElements.availablePermits());
			unusedQueueElements.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Runnable worker = new SplitWorker(path);
		executor.submit(worker);
	}

	private synchronized Object getElementForProcessing(String path) throws IOException {
		Object ret = null;
		if(fileStatusMap.get(path) == QueueElementStatus.DIRTY){
			ret = getFile(path);
			fileStatusMap.put(path, QueueElementStatus.PROCESSING);
		}
		return ret;
	}

	private synchronized void removeElementBeingProcessed(String path, FileFragments fileFragments) throws IOException {
		unusedQueueElements.release();
		if(fileStatusMap.get(path) == QueueElementStatus.PROCESSING){ //Remove element
			//Update checksum only if the processed element is up to date
			fileFragmentMetaDataStore.setFragments(path, fileFragmentMetaDataStore.getFragments(path), fileFragmentMetaDataStore.getNrOfRequiredFragments(path), fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path), fileFragments.getChecksums(), fileFragmentMetaDataStore.getFragmentsSize(path));
			fileStatusMap.remove(path);
			fileMap.remove(path);
			//recMan.commit();
			System.out.println("RELEASE element from queue: "+unusedQueueElements.availablePermits());
		} else{
			synchronized (fileFragmentMetaDataStore) {
				fileFragmentMetaDataStore.setFragments(path, fileFragmentMetaDataStore.getFragments(path), fileFragmentMetaDataStore.getNrOfRequiredFragments(path), fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path), null, fileFragmentMetaDataStore.getFragmentsSize(path));
			}
			System.out.println("CANNOT RELEASE dirty element from queue: "+unusedQueueElements.availablePermits());
		}
		notify();
	}

	class SplitWorker implements Runnable {
		private String path;

		public SplitWorker(String path){
			this.path = path;
		}
		
		public String getPath() {
			return path;
		}

		@Override
		public boolean equals(Object obj) {
			if( obj == null || !(obj instanceof SplitWorker)){
				return false;
			}
			return getPath().equals(((SplitWorker)obj).getPath());
		}

		@Override
		public void run() {
			split();
		}
		
		private void split() {
			RandomAccessTemporaryFileChannel file = new RandomAccessTemporaryFileChannel();
			Object byteArr = null;
			System.out.println("splitting file: "+path);
			try {
				byteArr = getElementForProcessing(path);
				if(byteArr==null)
					return;
				file.getChannel().write(ByteBuffer.wrap((byte[])byteArr));
				byte[] arr = new byte[(int) file.getChannel().size()];
				file.getChannel().position(0);
				file.getChannel().read(ByteBuffer.wrap(arr));
				file.getChannel().position(0);
			}
			catch (Exception e) {
				System.out.println("failure splitting:"+byteArr);
			}
			try{
				//Path kann in der zwischenzeit gerenamed worden sein
				FileFragments fileFragments = core.splitFile(fileFragmentMetaDataStore, path, file.getChannel()); //TODO: cancel if status changed to DIRTY
				//Update checksums: -> problem: outdated checksums
				removeElementBeingProcessed(path, fileFragments);
				file.delete();
			} catch (Exception e) {
				System.out.println("Failure: resubmitting");
				try {
					recMan.commit();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				fileStatusMap.put(path, QueueElementStatus.DIRTY);
				//Rescheduling/exponential backoff? - why would it fail?
				executor.submit(this); //TODO:check if this works by introducing random errors 
			}
		}
	}

	/**
	 * Remove path from queue, maybe even before persistently storing the element.
	 * @param path
	 */
	public synchronized void remove(String path) { 
		while(fileStatusMap.get(path) == QueueElementStatus.PROCESSING){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(fileStatusMap.get(path) != null){ //DIRTY || CLEAN
			unusedQueueElements.release();
			fileStatusMap.remove(path);
			try {
				fileMap.remove(path);
				//recMan.commit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 
	}

	/**
	 * Rename path in queue before persistently storing the element. 
	 * Wait until the element is stored otherwise.
	 * @param from
	 * @param to
	 */
	public synchronized void rename(String from, String to) {
		System.out.println("Renaming cached? "+from+" "+to);
		while(fileStatusMap.get(from) == QueueElementStatus.PROCESSING){
			System.out.println("Rename waiting");
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Renaming finished waiting. status: "+fileStatusMap.get(from));
		if(fileStatusMap.get(from) == QueueElementStatus.DIRTY){ 
			fileStatusMap.put(to,fileStatusMap.remove(from));
			try {
				fileMap.put(to, fileMap.get(from));
				fileMap.remove(from);
				recMan.commit();
				System.out.println("Renamed cached: "+from+" "+to); 
			} catch (IOException e) {
				e.printStackTrace();
			}
		} 		
	}	
}
