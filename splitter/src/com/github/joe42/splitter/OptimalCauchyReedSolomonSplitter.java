package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.storagestrategies.OptimalRedundancyStategy;
import com.github.joe42.splitter.storagestrategies.StorageStrategy;
import com.github.joe42.splitter.storagestrategies.StorageStrategyFactory;
import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.ConcurrentMultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFiles;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.util.file.SerialMultipleFileHandler;
import com.github.joe42.splitter.vtf.FileEntry;

import fuse.FuseException;
import eu.vandertil.jerasure.jni.Cauchy;
import eu.vandertil.jerasure.jni.Jerasure;



public class OptimalCauchyReedSolomonSplitter implements Splitter { //Rename to CauchyReedSolomonSplitter and abstract interface
	private static final int CAUCHY_WORD_LENGTH = 8; //assert( (fragmentsize % (packetsize * w)) == 0 ) ; assert( 2^w > 100 )
	private static final Logger log = Logger.getLogger("CauchyReedSolomonSplitter");
	private static final int BLOCKSIZE = 16; //jerasure_bitmatrix_encode - packetsize() % sizeof(long) != 0
	private MultipleFileHandler concurrent_multi_file_handler;
	private MultipleFileHandler serial_multi_file_handler;
	private StorageStrategy storageStrategy;
	private int redundancy;
	private String storageStrategyName;
	private StorageStrategyFactory storageStrategyFactory;
	private BackendServices services;
	private Digest digestFunc;
	private HashMap<Integer,int[]> bitmatrixCache;
	//private InformationDispersalCodec crsidacodec;
	
	
	
	public OptimalCauchyReedSolomonSplitter(BackendServices services){
		System.loadLibrary("Jerasure.jni");
		this.services = services;
		redundancy = 50;
		storageStrategyName = "";
		digestFunc = new SHA256Digest();
		bitmatrixCache = new HashMap<Integer, int[]>();
		storageStrategyFactory = new StorageStrategyFactory(services);
		concurrent_multi_file_handler = new ConcurrentMultipleFileHandler(digestFunc);
		serial_multi_file_handler = new SerialMultipleFileHandler(digestFunc);
	}
	
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#getRedundancy()
	 */
	@Override
	public int getRedundancy() {
		return redundancy;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#setRedundancy(int)
	 */
	@Override
	public void setRedundancy(int redundancy) {
		this.redundancy = redundancy;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#getStorageStrategyName()
	 */
	@Override
	public String getStorageStrategyName() {
		return storageStrategyName;
	}
	
	public StorageStrategyFactory getStorageStrategyFactory(){
		return storageStrategyFactory;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#setStorageStrategyName(java.lang.String)
	 */
	@Override
	public void setStorageStrategyName(String storageStrategyName) {
		if(storageStrategyName == null){
			storageStrategyName = "";
		}
		this.storageStrategyName = storageStrategyName;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#getBackendServices()
	 */
	@Override
	public BackendServices getBackendServices(){
		return services;
	}
	
	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#splitFile(com.github.joe42.splitter.FileFragmentMetaDataStore, java.lang.String, java.nio.channels.FileChannel)
	 */
	@Override
	public FileFragments splitFile(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path, FileChannel temp) throws FuseException, IOException {
		//In Subclass: if storageStrategyName.equals(StorageStrategyFactory.100_splits): do this else call superclass method
		int nr_of_file_parts_successfully_stored = 0;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		ArrayList<String> fragmentPaths = new ArrayList<String>();
		int nr_of_file_fragments;
		int nr_of_redundant_fragments;
		int nr_of_file_fragments_required;
		int nrOfRequiredSuccessfullyStoredFragments;
		String fragment_name;
		if(! fileFragmentMetaDataStore.hasFragments(path) || storageStrategyFactory.changeToCurrentStrategy()){
			List<String> fragmentDirectories;
			storageStrategy = storageStrategyFactory.createStrategy(storageStrategyName, redundancy);
			fragmentDirectories = storageStrategy.getFragmentDirectories();
			nr_of_file_fragments = fragmentDirectories.size();
			String uniquePath, uniqueFileName;
			uniquePath = StringUtil.getUniqueAsciiString(path);
			uniqueFileName = uniquePath.replaceAll("/", "_");
			for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
				fragment_name = fragmentDirectories.get(fragment_nr) +"/"+ uniqueFileName 
						+ '#' + fragment_nr;
				fragmentPaths.add(fragment_name);
			}
			nr_of_redundant_fragments =  storageStrategy.getNrOfRedundantFragments();
			nr_of_file_fragments_required =  nr_of_file_fragments - nr_of_redundant_fragments; 
			nrOfRequiredSuccessfullyStoredFragments = storageStrategy.getNrOfRequiredSuccessfullyStoredFragments();
		} else {
			fragmentPaths = fileFragmentMetaDataStore.getFragments(path);
			nr_of_file_fragments = fragmentPaths.size();
			nr_of_file_fragments_required = fileFragmentMetaDataStore.getNrOfRequiredFragments(path);
			nr_of_redundant_fragments = nr_of_file_fragments - nr_of_file_fragments_required;
			nrOfRequiredSuccessfullyStoredFragments = fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path);
		}
		System.out.println("required:"+nrOfRequiredSuccessfullyStoredFragments);
		logStoreProperties(nr_of_file_fragments, nr_of_file_fragments_required, nr_of_redundant_fragments, nrOfRequiredSuccessfullyStoredFragments);
		if(nr_of_file_fragments_required == 1){
			fileParts = replicateFile( temp, fragmentPaths);
		} else if(nr_of_file_fragments_required == nr_of_file_fragments){ //no redundancy but several stores
			log.debug(" 1 . temp.size():"+ temp.size());
			fileParts = simpleSplitFile( temp, fragmentPaths);
		} else {
			
			crsSplitFile(temp, fileParts, fragmentPaths,
					nr_of_file_fragments, nr_of_redundant_fragments);
		}
		log.debug(" temp.size():"+ temp.size());
		MultipleFiles multipleFiles = concurrent_multi_file_handler.writeFilesAsByteArrays(fileParts);
		nr_of_file_parts_successfully_stored = multipleFiles.getNrOfSuccessfullyTransferedFiles();

		  log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+nr_of_file_fragments_required);
		//TODO: replace with nrOfRequiredSuccessfullyStoredFragments
		if (nr_of_file_parts_successfully_stored < nr_of_file_fragments_required && ! (storageStrategy instanceof OptimalRedundancyStategy)) {
			throw new FuseException(
					"IO error: Not enough file parts could be stored.")
					.initErrno(FuseException.EIO);
		} else {
			return new FileFragments(fragmentPaths, nr_of_file_fragments_required, nrOfRequiredSuccessfullyStoredFragments, multipleFiles.getChecksums(fragmentPaths), temp.size());
		}
	}

	private void crsSplitFile(FileChannel temp, HashMap<String, byte[]> fileParts, List<String> fragmentFileNames, 
			int nr_of_file_fragments, int nr_of_redundant_fragments) throws FuseException {
		int w = CAUCHY_WORD_LENGTH; 
	   	int[] bitmatrix; //keep matrices local for multithreaded access
	   	byte[][] coding_ptrs;
	   	byte[][] data_ptrs;
	   	OptimalRedundancyStategy strategy = (OptimalRedundancyStategy) storageStrategy;
	   	int n = strategy.getNrOfElements();
	   	int m = strategy.getNrOfRedundantFragments();
	   	int k = n - m;
		try {
			int fragmentsize = (int) temp.size()/k;//size of all data elements in bytes, divisible by BLOCKSIZE: (fragmentsize % (packetsize * w)) == 0
			//fill chunk to:
			//(fragmentsize % (packetsize * w)) == 0
			if(BLOCKSIZE > 0) {
				int diff = fragmentsize % (BLOCKSIZE * w);
				if(diff != 0 || fragmentsize == 0) { 
					fragmentsize += (BLOCKSIZE * w) - diff;
				}
			}
			bitmatrix = getBitMatrix(k, m, w);
			
			coding_ptrs = new byte[m][fragmentsize];
			data_ptrs = new byte[k][fragmentsize];

			log.debug("nr of elements in data_ptrs: "+data_ptrs.length);
			log.debug("size of data element: "+data_ptrs[0].length);
			
			try {
				for (int fragment_nr = 0; fragment_nr < k; fragment_nr++) {
					ByteBuffer bb = ByteBuffer.wrap(data_ptrs[fragment_nr]);
					java.util.Arrays.fill(data_ptrs[fragment_nr],(byte)'x');
					temp.read(bb);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new FuseException("IO error: " + e.toString(), e)
						.initErrno(FuseException.EIO);																		
			}												
			//finally encode data, which means generating additional coding elements in coding_ptrs (BLOCKSIZE is codingblock size) 

			log.debug("encode with k,m: "+new Integer(k).toString()+","+new Integer(m).toString());
			log.debug("fragmentsize: "+new Integer(fragmentsize).toString());
			Jerasure.jerasure_bitmatrix_encode(k, m, w, bitmatrix, data_ptrs, coding_ptrs, fragmentsize, BLOCKSIZE);
			//merge elements into data until #elements == elementsForStorage, then fileParts.put() 
			int elements;
			ByteBuffer chunk;
			Map<String,Integer> elementsForStorages = strategy.getFragmentNameToNrOfElementsMap();
			int i=0;
			for(String fragment_name: fragmentFileNames) {
				elements = elementsForStorages.get(fragment_name);
				chunk = ByteBuffer.wrap(new byte[elements*data_ptrs[0].length]);
				for(int j=0; j<elements; j++){
					if( i < data_ptrs.length){
						chunk.put(data_ptrs[i]);
					} else {
						chunk.put(coding_ptrs[i-data_ptrs.length]);
					}
					i++;
				}
				log.debug("write "+elements+" elements to " + fragment_name);
				fileParts.put(fragment_name, chunk.array()); //return  data parts 
			}
		
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IDA could not be initialized")
					.initErrno(FuseException.EACCES);
		}
		
	}

	private HashMap<String, byte[]> simpleSplitFile(FileChannel temp, List<String> fragmentFileNames) throws IOException {
			log.debug("Splitting with no redundancy");
			byte[] arr;
			int size=0, read_bytes;
			HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
			int normal_array_size = ((int) temp.size()+1)/fragmentFileNames.size();
			arr = new byte[normal_array_size];
			for (String fragmentFileName: fragmentFileNames) {
				if(size+normal_array_size>temp.size()){
					arr = new byte[(int)temp.size()-size];
				} else {
					arr = new byte[normal_array_size];
				}
				read_bytes = temp.read(ByteBuffer.wrap(arr));
				size += read_bytes;
				/*if(read_bytes <1){
					break;
				}*/
				log.debug("write: " + fragmentFileName);
				fileParts.put(fragmentFileName, arr);
			}
			return fileParts;
	}

	private void logStoreProperties(int nrOfFileFragments,
			int nrOfFileFragmentsRequired,
			int nrOfRedundantFragments,
			int nrOfRequiredSuccessfullyStoredFragments) {
		log.debug("nrOfFileFragments:" + nrOfFileFragments);
		log.debug("nrOfFileFragmentsRequired:" + nrOfFileFragmentsRequired);
		log.debug("nrOfRedundantFragments:" + nrOfRedundantFragments);
		log.debug("nrOfRequiredSuccessfullyStoredFragments:" + nrOfRequiredSuccessfullyStoredFragments);
		
	}

	private HashMap<String, byte[]> replicateFile(FileChannel temp, List<String> fragmentFileNames) throws IOException {
		byte[] arr;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		arr = new byte[(int) temp.size()];
		temp.read(ByteBuffer.wrap(arr));
		for (String fragmentFileName: fragmentFileNames) {
			log.debug("write: " + fragmentFileName);
			fileParts.put(fragmentFileName, arr);
		}
		return fileParts;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#glueFilesTogether(com.github.joe42.splitter.FileFragmentMetaDataStore, java.lang.String)
	 */
	@Override
	public RandomAccessTemporaryFileChannel glueFilesTogether(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path) throws FuseException {
		RandomAccessTemporaryFileChannel ret = null;
		Map<String, byte[]> fragmentPathsToChecksum;
		try {
			int nr_of_file_fragments_required = fileFragmentMetaDataStore.getNrOfRequiredFragments(path);
			List<String> fragmentNames = fileFragmentMetaDataStore.getFragments(path);
			fragmentPathsToChecksum = zip(fileFragmentMetaDataStore.getFragments(path), fileFragmentMetaDataStore.getFragmentsChecksums(path)); 
			int nr_of_redundant_fragments = fragmentNames.size() - nr_of_file_fragments_required;
			logStoreProperties(fileFragmentMetaDataStore.getNrOfFragments(path), nr_of_file_fragments_required, nr_of_redundant_fragments, fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path));
			log.debug("read: " + fragmentNames);
			if(nr_of_file_fragments_required == 1){
				ret = new RandomAccessTemporaryFileChannel();
				MultipleFiles multipleFiles = serial_multi_file_handler
						.getFilesAsByteArrays(fragmentPathsToChecksum, nr_of_file_fragments_required);
				log.debug(fileFragmentMetaDataStore.getNrOfFragments(path)+" "+fileFragmentMetaDataStore.getNrOfRequiredFragments(path));
				if(multipleFiles.getNrOfSuccessfullyTransferedFiles() < 1){
					throw new IOException("File could not be retrieved successfully.");
				}
				ret.getChannel().write(ByteBuffer.wrap(multipleFiles.getSuccessfullyTransferedFiles().get(0)));
				return ret;
			} else if(nr_of_file_fragments_required == fragmentNames.size()){ //no redundancy but several stores
				return simpleGlueFilesTogether(fileFragmentMetaDataStore.getFragments(path), fileFragmentMetaDataStore.getFragmentsChecksums(path));
			}else {
				
				return crsGlueFilesTogether(nr_of_redundant_fragments,
						nr_of_file_fragments_required, fragmentNames, fragmentPathsToChecksum, (int) fileFragmentMetaDataStore.getFragmentsSize(path));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
	}

	private RandomAccessTemporaryFileChannel crsGlueFilesTogether(int nr_of_redundant_fragments,
			int nrOfDataElements,
			List<String> fragmentNames, Map<String, byte[]> fragmentPathsToChecksum, int filesize)
			throws FuseException, IOException {
	
		int nrOfFragments = nr_of_redundant_fragments + nrOfDataElements;
		int w = CAUCHY_WORD_LENGTH; 
	   	int[] bitmatrix; //keep matrices local for multithreaded access
	   	
	   	byte[][] coding_ptrs; 
	   	byte[][] data_ptrs;

	   	OptimalRedundancyStategy strategy = (OptimalRedundancyStategy) storageStrategy;
	   	int n = strategy.getNrOfElements();
	   	int m = strategy.getNrOfRedundantFragments();
	   	int k = n - m;
	   	/**The values of the array are the number of the lost (erased) elements. 
	   	 * The elements are numbered from 0 to n, according to the order of the arrays used in the encoding step for the data, and coding elements.
	   	 * The count begins with 0 beginning with the data elements, then proceeds with the coding elements.
	   	 **/ 
	   	int [] erasures;
	   	RandomAccessTemporaryFileChannel ret; //decoded file (chunk)
	   	List<byte[]> receivedFileSegments; //the erasure code elements except erased ones
		//fill chunk to:
		//(fragmentsize % (packetsize * w)) == 0
	   	int fragmentsize = filesize/k;

		if(BLOCKSIZE > 0) {
			int diff = fragmentsize % (BLOCKSIZE * w);
			if(diff != 0 || fragmentsize == 0) { 
				fragmentsize += (BLOCKSIZE * w) - diff;
			}
		}
		
		bitmatrix = getBitMatrix(k, m, w);
		
		erasures = new int [nrOfFragments+1];
		
		coding_ptrs = new byte[m][fragmentsize];
		data_ptrs = new byte[k][fragmentsize];

		log.debug(Arrays.toString(fragmentPathsToChecksum.keySet().toArray(new String[0])) + " "
				+ nr_of_redundant_fragments);
		
		
		//use utility class to read several files into byte arrays concurrently
		MultipleFiles multipleFiles = ((ConcurrentMultipleFileHandler) concurrent_multi_file_handler)
				.getFilesAsByteArrays(fragmentPathsToChecksum,
						nrOfFragments);
		
		
		receivedFileSegments = multipleFiles.getSuccessfullyTransferedFiles();
		log.debug("successfully transfered fragments: "+ receivedFileSegments.size());
		log.debug("not transfered: "+ (multipleFiles.getNrOfUnsuccessfullyTransferedFiles()-multipleFiles.getWrongChecksumFilePaths().size()));
		log.debug("fragments with corrupted content: "+ multipleFiles.getWrongChecksumFilePaths().size());
		
		
		int erased = 0;
		Map<String,Integer> elementsForStorages = strategy.getFragmentNameToNrOfElementsMap(); //somethings getting mixed up here
		int element_nr = 0;
		for (String fragment_name: fragmentNames){
			int elements = elementsForStorages.get(fragment_name);
			if(multipleFiles.getSuccessfullyTransferedFile(fragment_name) == null) {
				erasures[erased] = element_nr;
				erased++;
				for(int j=0; j<elements; j++){
					log.debug("failed to read "+elements+" elements from " + fragment_name);
					if(element_nr<k){
						data_ptrs[element_nr] = new byte[fragmentsize];
					} else {
						coding_ptrs[element_nr-k] = new byte[fragmentsize];
					}
					element_nr++;
				}
				continue;
			}
			log.debug("read "+elements+" elements from " + fragment_name);
			byte[][] dataElements = divideArray(
					multipleFiles.getSuccessfullyTransferedFile(fragment_name),
					fragmentsize);
			log.debug("nr of elements in data_ptrs: "+dataElements.length);
			log.debug("size of data element: "+dataElements[0].length);
			for(byte[] element: dataElements){
				if(element_nr<k){
					data_ptrs[element_nr] = element;
				} else {
					coding_ptrs[element_nr-k] = element;
				}
				element_nr++;
			}
		}
		erasures[erased] = -1;
		//finally decode data (recreate erased data elements); if no parts were erased, nothing needs to be done
		log.debug("decode with k,m: "+new Integer(k).toString()+","+new Integer(m).toString());
		log.debug("fragmentsize: "+new Integer(fragmentsize).toString());
		Jerasure.jerasure_bitmatrix_decode(k, m, w, bitmatrix, false, erasures, data_ptrs, coding_ptrs, fragmentsize, BLOCKSIZE);

		ret = new RandomAccessTemporaryFileChannel(); //put data elements together into a single file
		FileChannel fchan = ret.getChannel();
		int elements = 0;
		for(byte[] data: data_ptrs){
			fchan.write(ByteBuffer.wrap(data));
			elements++;
		}
		fchan.truncate(filesize);
		return ret;
	}
	
	 private static byte[][] divideArray(byte[] source, int chunksize) {
        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];
        int pos = 0;
        for(int i = 0; i < ret.length; i++) {
            ret[i] = Arrays.copyOfRange(source,pos, pos + chunksize);
            pos += chunksize;
        }
        return ret;
	}

	private int[] getBitMatrix(int k, int m, int w) {
		int[] bitmatrix; //keep matrices local for multithreaded access
		int hash = Arrays.hashCode(new int[]{k,m,w});
		bitmatrix = bitmatrixCache.get(hash);
		if(bitmatrix == null) {
			int[] matrix;
			matrix= Cauchy.cauchy_good_general_coding_matrix(k, m, w);
			bitmatrix = Jerasure.jerasure_matrix_to_bitmatrix(k, m, w, matrix);
			bitmatrixCache.put(hash, bitmatrix);
		}
		return bitmatrix;
	}

	/**
	 * Get a map from each file path to the corresponding checksum of the file
	 * @param fragmentPaths list of fragment paths 
	 * @param checksums ordered to have the same index as the corresponding fragment path
	 * @return a map from each file path to the corresponding checksum
	 */
	private Map<String, byte[]> zip(List<String> fragmentPaths,
			List<byte[]> checksums) {
		Map<String, byte[]> fileNamesToChecksum = new HashMap<String, byte[]>();
		for (int i = 0; i < fragmentPaths.size(); i++) {
			fileNamesToChecksum.put(fragmentPaths.get(i), checksums.get(i));
		}
		return fileNamesToChecksum;
	}

	/**
	 * Glues files together in the order specified by their paths in <code>fragmentsPaths</code>
	 * @param fragmentsPaths
	 * @param fragmentsChecksums the checksum of each file fragment at the same index position as its path in fragmentsPaths
	 * @return the complete file
	 * @throws IOException
	 */
	private RandomAccessTemporaryFileChannel simpleGlueFilesTogether(
			ArrayList<String> fragmentsPaths, List<byte[]> fragmentsChecksums) throws IOException {
		RandomAccessTemporaryFileChannel ret;	
		ret = new RandomAccessTemporaryFileChannel();
		MultipleFiles multipleFiles = ((ConcurrentMultipleFileHandler) concurrent_multi_file_handler).
			getFilesAsByteArrays(zip(fragmentsPaths, fragmentsChecksums));
		if(fragmentsPaths.size() > multipleFiles.getNrOfSuccessfullyTransferedFiles()){
			throw new IOException("Could not retrieve all fragments of the file.");
		}
		ByteBuffer bb;
		for (String filePath: fragmentsPaths) {
			bb = ByteBuffer.wrap(multipleFiles.getSuccessfullyTransferedFile(filePath));
			ret.getChannel().write(bb);
		}
		return ret;
	}

	/* (non-Javadoc)
	 * @see com.github.joe42.splitter.Splitter#getStorageAvailability()
	 */
	@Override
	public double getStorageAvailability(){
		storageStrategy = storageStrategyFactory.createStrategy(storageStrategyName, redundancy);
		return storageStrategy.getStorageAvailability(); //forward call to the storage strategy factory
	}

	@Override
	public void remove(String path) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rename(String from, String to) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getStorageRedundancy() {
		return storageStrategy.getStorageRedundancy();
	}
}