package com.github.joe42.splitter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.Map.Entry;

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



public class OptimalCauchyReedSolomonSplitter extends CauchyReedSolomonSplitter { //Rename to CauchyReedSolomonSplitter and abstract interface
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
		super(services);
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
	 * @see com.github.joe42.splitter.Splitter#splitFile(com.github.joe42.splitter.FileFragmentMetaDataStore, java.lang.String, java.nio.channels.FileChannel)
	 */
	@Override
	public FileFragments splitFile(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path, FileChannel temp) throws FuseException, IOException {
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
		if( ! (storageStrategy instanceof OptimalRedundancyStategy) || nr_of_file_fragments_required == 1 || nr_of_file_fragments_required == nr_of_file_fragments ) {
			return super.splitFile(fileFragmentMetaDataStore, path, temp);
		}
		logStoreProperties(nr_of_file_fragments, nr_of_file_fragments_required, nr_of_redundant_fragments, nrOfRequiredSuccessfullyStoredFragments);
		crsSplitFileOptimally(temp, fileParts, fragmentPaths, nr_of_file_fragments, nr_of_redundant_fragments);
		log.debug(" temp.size():"+ temp.size());
		MultipleFiles multipleFiles = concurrent_multi_file_handler.writeFilesAsByteArrays(fileParts);
		nr_of_file_parts_successfully_stored = multipleFiles.getNrOfSuccessfullyTransferedFiles();
		log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+nr_of_file_fragments_required);
	    return new FileFragments(fragmentPaths, nr_of_file_fragments_required, nrOfRequiredSuccessfullyStoredFragments, multipleFiles.getChecksums(fragmentPaths), temp.size());
	}

	private void crsSplitFileOptimally(FileChannel temp, HashMap<String, byte[]> fileParts, List<String> fragmentFileNames, 
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
			for(Entry<String, Integer> e : elementsForStorages.entrySet()){
				log.debug("intend to write "+e.getValue()+" elements to " + new File(e.getKey()).getParent());
			}
			int i=0;
			for(String fragment_name: fragmentFileNames) {
				String storageDir = new File(fragment_name).getParent().toString();
				System.out.println("storage directory "+storageDir);
				elements = elementsForStorages.get(storageDir);
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

	private void logStoreProperties(int nrOfFileFragments,
			int nrOfFileFragmentsRequired,
			int nrOfRedundantFragments,
			int nrOfRequiredSuccessfullyStoredFragments) {
		log.debug("nrOfFileFragments:" + nrOfFileFragments);
		log.debug("nrOfFileFragmentsRequired:" + nrOfFileFragmentsRequired);
		log.debug("nrOfRedundantFragments:" + nrOfRedundantFragments);
		log.debug("nrOfRequiredSuccessfullyStoredFragments:" + nrOfRequiredSuccessfullyStoredFragments);
		
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
			if( ! (storageStrategy instanceof OptimalRedundancyStategy) || nr_of_file_fragments_required == 1 || nr_of_file_fragments_required == fragmentNames.size() ) {
				return super.glueFilesTogether(fileFragmentMetaDataStore, path);
			}
			log.debug("read: " + fragmentNames);
     		return crsGlueFilesTogetherOptimally(nr_of_redundant_fragments,
						nr_of_file_fragments_required, fragmentNames, fragmentPathsToChecksum, (int) fileFragmentMetaDataStore.getFragmentsSize(path));
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
	}

	private RandomAccessTemporaryFileChannel crsGlueFilesTogetherOptimally(int nr_of_redundant_fragments,
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
		for(byte[] data: data_ptrs){
			fchan.write(ByteBuffer.wrap(data));
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

}
