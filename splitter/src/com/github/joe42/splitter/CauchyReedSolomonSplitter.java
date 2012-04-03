package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;
import org.jigdfs.ida.exception.IDADecodeException;
import org.jigdfs.ida.exception.IDANotInitializedException;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
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

public class CauchyReedSolomonSplitter { //Rename to CauchyReedSolomonSplitter and abstract interface
	private static final int CAUCHY_WORD_LENGTH = 1;
	private static final Logger  log = Logger.getLogger("CauchyReedSolomonSplitter");
	private MultipleFileHandler concurrent_multi_file_handler;
	private MultipleFileHandler serial_multi_file_handler;
	private StorageStrategy storageStrategy;
	private int redundancy;
	private String storageStrategyName;
	private StorageStrategyFactory storageStrategyFactory;
	private BackendServices services;
	private Digest digestFunc;
	
	public CauchyReedSolomonSplitter(BackendServices services){
		this.services = services;
		redundancy = 50;
		storageStrategyName = "";
		digestFunc = new MD5Digest();
		storageStrategyFactory = new StorageStrategyFactory(services);
		concurrent_multi_file_handler = new ConcurrentMultipleFileHandler(digestFunc);
		serial_multi_file_handler = new SerialMultipleFileHandler(digestFunc);
	}
	
	public int getRedundancy() {
		return redundancy;
	}

	public void setRedundancy(int redundancy) {
		this.redundancy = redundancy;
	}

	public String getStorageStrategyName() {
		return storageStrategyName;
	}

	public void setStorageStrategyName(String storageStrategyName) {
		if(storageStrategyName == null){
			storageStrategyName = "";
		}
		this.storageStrategyName = storageStrategyName;
	}

	public BackendServices getBackendServices(){
		return services;
	}
	
	public void splitFile(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path, FileChannel temp) throws FuseException, IOException {
		
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
		logStoreProperties(nr_of_file_fragments, nr_of_file_fragments_required, nr_of_redundant_fragments, nrOfRequiredSuccessfullyStoredFragments);
		if(nr_of_file_fragments_required == 1){
			fileParts = replicateFile( temp, path, fragmentPaths);
		} else if(nr_of_file_fragments_required == nr_of_file_fragments){ //no redundancy but several stores
			log.debug(" 1 . temp.size():"+ temp.size());
			fileParts = simpleSplitFile( temp, path, fragmentPaths);
		} else {
			
			crsSplitFile(temp, fileParts, fragmentPaths,
					nr_of_file_fragments, nr_of_redundant_fragments);
		}
		log.debug(" temp.size():"+ temp.size());
		MultipleFiles multipleFiles = concurrent_multi_file_handler.writeFilesAsByteArrays(fileParts);
		nr_of_file_parts_successfully_stored = multipleFiles.getNrOfSuccessfullyTransferedFiles();

		//if (log.isDebugEnabled())
		  log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+nr_of_file_fragments_required);
		//TODO: replace with nrOfRequiredSuccessfullyStoredFragments
		if (nr_of_file_parts_successfully_stored < nr_of_file_fragments_required) {
			throw new FuseException(
					"IO error: Not enough file parts could be stored.")
					.initErrno(FuseException.EIO);
		} else {
			//TODO: delete previous Fragments
			fileFragmentMetaDataStore.setFragments(path, fragmentPaths, nr_of_file_fragments_required, nrOfRequiredSuccessfullyStoredFragments, multipleFiles.getChecksums(fragmentPaths), temp.size());
		}
	}

	private void crsSplitFile(FileChannel temp, HashMap<String, byte[]> fileParts, List<String> fragmentFileNames, 
			int nr_of_file_fragments, int nr_of_redundant_fragments) throws FuseException {
		String fragment_name;
		InformationDispersalCodec crsidacodec;
		InformationDispersalEncoder encoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(nr_of_file_fragments, nr_of_redundant_fragments, CAUCHY_WORD_LENGTH);
			encoder = crsidacodec.getEncoder();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IDA could not be initialized")
					.initErrno(FuseException.EACCES);
		}
		try {
			byte[] arr = new byte[(int) temp.size()];
			temp.read(ByteBuffer.wrap(arr));
			List<byte[]> result = encoder.process(arr);
			for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
				fragment_name = fragmentFileNames.get(fragment_nr);
				log.debug("write: " + fragment_name);
				byte[] b = result.get(fragment_nr);
				fileParts.put(fragment_name, b);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error: " + e.toString(), e)
					.initErrno(FuseException.EIO);
		}
	}

	private HashMap<String, byte[]> simpleSplitFile(FileChannel temp, String path,
			List<String> fragmentFileNames) throws IOException {
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

	private HashMap<String, byte[]> replicateFile(FileChannel temp, String path,
			List<String> fragmentFileNames) throws IOException {
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
				
				return crsGlueFilesTogether(path, nr_of_redundant_fragments,
						nr_of_file_fragments_required, fragmentPathsToChecksum);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
	}

	private RandomAccessTemporaryFileChannel crsGlueFilesTogether(String path,
			int nr_of_redundant_file_fragments,
			int nr_of_file_fragments_required,
			Map<String, byte[]> fragmentPathsToChecksum)
			throws FuseException, IOException, IDADecodeException,
			IDANotInitializedException {

		RandomAccessTemporaryFileChannel ret;
		List<byte[]> receivedFileSegments;
		InformationDispersalCodec crsidacodec;
		InformationDispersalDecoder decoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(fragmentPathsToChecksum
					.size(), nr_of_redundant_file_fragments, CAUCHY_WORD_LENGTH);
			log.debug(fragmentPathsToChecksum.keySet().toArray(new String[0]) + " "
					+ nr_of_redundant_file_fragments);
			decoder = crsidacodec.getDecoder();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IDA could not be initialized")
					.initErrno(FuseException.EACCES);
		}
		ret = new RandomAccessTemporaryFileChannel();
		MultipleFiles multipleFiles = ((ConcurrentMultipleFileHandler) concurrent_multi_file_handler)
				.getFilesAsByteArrays(fragmentPathsToChecksum,
						nr_of_file_fragments_required);
		receivedFileSegments = multipleFiles.getSuccessfullyTransferedFiles();
		log.debug("received fragments: "+ receivedFileSegments.size());
		byte[] recoveredFile = decoder.process(receivedFileSegments);
		ret.getChannel().write(ByteBuffer.wrap(recoveredFile));
		return ret;
	}

	/**
	 * Get a map from each file path to the corresponding checksumof the file
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

	/**
	 * Get the minimal availability of files stored by the current Splitter instance.
	 * @return the availability in percent
	 */
	public double getStorageAvailability(){
		storageStrategy = storageStrategyFactory.createStrategy(storageStrategyName, redundancy);
		return storageStrategy.getStorageAvailability(); //forward call to the storage strategy factory
	}
}
