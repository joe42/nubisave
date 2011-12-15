package com.github.joe42.splitter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.backend.BackendServices;
import com.github.joe42.splitter.storagestrategies.StorageStrategy;
import com.github.joe42.splitter.storagestrategies.StorageStrategyFactory;
import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.ConcurrentMultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.util.file.SerialMultipleFileHandler;
import com.github.joe42.splitter.vtf.FileEntry;

import fuse.FuseException;

public class CauchyReedSolomonSplitter { //Rename to CauchyReedSolomonSplitter and abstract interface
	private static final int CAUCHY_WORD_LENGTH = 1;
	private static final Logger  log = Logger.getLogger("Splitter");
	private MultipleFileHandler concurrent_multi_file_handler;
	private MultipleFileHandler serial_multi_file_handler;
	private StorageStrategy storageStrategy;
	private StorageStrategyFactory storageStrategyFactory;
	
	public CauchyReedSolomonSplitter(BackendServices services){
		storageStrategyFactory = new StorageStrategyFactory(services);
		concurrent_multi_file_handler = new ConcurrentMultipleFileHandler();
		serial_multi_file_handler = new SerialMultipleFileHandler();
	}
	
	public void splitFile(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path, FileChannel temp, int redundancy) throws FuseException, IOException {
		
		int nr_of_file_parts_successfully_stored = 0;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		ArrayList<String> fragmentFileNames = new ArrayList<String>();
		int nr_of_file_fragments;
		int nr_of_redundant_fragments;
		int nr_of_file_fragments_required;
		int nrOfRequiredSuccessfullyStoredFragments;
		String fragment_name;
		if(! fileFragmentMetaDataStore.hasFragments(path) || storageStrategyFactory.changeToCurrentStrategy()){
			List<String> fragmentDirectories;
			storageStrategy = storageStrategyFactory.createStrategy("RoundRobin", redundancy);
			 fragmentDirectories = storageStrategy.getFragmentDirectories();
			 nr_of_file_fragments = fragmentDirectories.size();
			String uniquePath, uniqueFileName;
			uniquePath = StringUtil.getUniqueAsciiString(path);
			uniqueFileName = uniquePath.replaceAll("/", "_");
			for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
				fragment_name = fragmentDirectories.get(fragment_nr) +"/"+ uniqueFileName 
						+ '#' + fragment_nr;
				fragmentFileNames.add(fragment_name);
			}
			nr_of_redundant_fragments =  storageStrategy.getNrOfRedundantFragments();
			nr_of_file_fragments_required =  nr_of_file_fragments - nr_of_redundant_fragments;
			nrOfRequiredSuccessfullyStoredFragments = storageStrategy.getNrOfRequiredSuccessfullyStoredFragments();
		} else {
			fragmentFileNames = fileFragmentMetaDataStore.getFragments(path);
			nr_of_file_fragments = fragmentFileNames.size();
			nr_of_file_fragments_required = fileFragmentMetaDataStore.getNrOfRequiredFragments(path);
			nr_of_redundant_fragments = nr_of_file_fragments - nr_of_file_fragments_required;
			nrOfRequiredSuccessfullyStoredFragments = fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path);
		}
		logStoreProperties(nr_of_file_fragments, nr_of_file_fragments_required, nr_of_redundant_fragments, nrOfRequiredSuccessfullyStoredFragments);
		if(nr_of_file_fragments_required == 1){
			fileParts = replicateFile( temp, path, fragmentFileNames);
		} else {
			
			Digest digestFunc = new SHA256Digest();
			byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
			String hexString = null;
			InformationDispersalCodec crsidacodec;
			InformationDispersalEncoder encoder;
			try {
				crsidacodec = new CauchyInformationDispersalCodec(
						nr_of_file_fragments, nr_of_redundant_fragments, 1);
				encoder = crsidacodec.getEncoder();
			} catch (Exception e) {
				e.printStackTrace();
				throw new FuseException("IDA could not be initialized")
						.initErrno(FuseException.EACCES);
			}
			try {
				byte[] arr = new byte[(int) temp.size()];
				temp.read(ByteBuffer.wrap(arr));
				digestFunc.update(arr, 0, arr.length);
				digestFunc.doFinal(digestByteArray, 0);
				List<byte[]> result = encoder.process(arr);
	
				for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
	
					fragment_name = fragmentFileNames.get(fragment_nr);
					log.debug("write: " + fragment_name);
					byte[] b = result.get(fragment_nr);
					digestFunc.reset();
					digestFunc.update(b, 0, b.length);
					digestFunc.doFinal(digestByteArray, 0);
	
					hexString = new String(Hex.encode(digestByteArray));
					fileParts.put(fragment_name, b);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new FuseException("IO error: " + e.toString(), e)
						.initErrno(FuseException.EIO);
			}
		}
		nr_of_file_parts_successfully_stored = concurrent_multi_file_handler.writeFilesAsByteArrays(fileParts);

		//if (log.isDebugEnabled())
		  log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+nr_of_file_fragments_required);
		//TODO: replace with nrOfRequiredSuccessfullyStoredFragments
		if (nr_of_file_parts_successfully_stored < nr_of_file_fragments_required) {
			throw new FuseException(
					"IO error: Not enough file parts could be stored.")
					.initErrno(FuseException.EIO);
		} else {
			//TODO: delete previous Fragments
			fileFragmentMetaDataStore.setFragment(path, fragmentFileNames, nr_of_file_fragments_required, nrOfRequiredSuccessfullyStoredFragments, "");
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

	private HashMap<String, byte[]> replicateFile(FileChannel temp, String path,
			ArrayList<String> fragmentFileNames) throws IOException {
		byte[] arr;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		for (String fragmentFileName: fragmentFileNames) {
			log.debug("write: " + fragmentFileName);
			arr = new byte[(int) temp.size()];
			temp.read(ByteBuffer.wrap(arr));
			fileParts.put(fragmentFileName, arr);
		}
		return fileParts;
	}

	public RandomAccessTemporaryFileChannel glueFilesTogether(FileFragmentMetaDataStore fileFragmentMetaDataStore, String path) throws FuseException {
		RandomAccessTemporaryFileChannel ret = null;
		List<byte[]> receivedFileSegments = new ArrayList<byte[]>();
		Digest digestFunc = new SHA256Digest();
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		InformationDispersalCodec crsidacodec;
		InformationDispersalDecoder decoder;
		try {
			int nr_of_file_fragments_required = fileFragmentMetaDataStore.getNrOfRequiredFragments(path);
			List<String> fragmentNames = fileFragmentMetaDataStore.getFragments(path);
			int nr_of_redundant_fragments = fragmentNames.size() - nr_of_file_fragments_required;
			logStoreProperties(fileFragmentMetaDataStore.getNrOfFragments(path), nr_of_file_fragments_required, nr_of_redundant_fragments, fileFragmentMetaDataStore.getNrOfRequiredSuccessfullyStoredFragments(path));
			if(nr_of_file_fragments_required == 1){
				ret = new RandomAccessTemporaryFileChannel();
				List<byte[]> segmentBuffers = serial_multi_file_handler
						.getFilesAsByteArrays(fragmentNames.toArray(new String[0]), nr_of_redundant_fragments);
				System.out.println(fileFragmentMetaDataStore.getNrOfFragments(path)+" "+fileFragmentMetaDataStore.getNrOfRequiredFragments(path));
				ret.getChannel().write(ByteBuffer.wrap(segmentBuffers.get(0)));
				return ret;
			} else {
				
				try {
					crsidacodec = new CauchyInformationDispersalCodec(
							fileFragmentMetaDataStore.getNrOfFragments(path), nr_of_file_fragments_required, CAUCHY_WORD_LENGTH);
					System.out.println(fileFragmentMetaDataStore.getNrOfFragments(path)+" "+fileFragmentMetaDataStore.getNrOfRequiredFragments(path));
					decoder = crsidacodec.getDecoder();
				} catch (Exception e) {
					e.printStackTrace();
					throw new FuseException("IDA could not be initialized")
							.initErrno(FuseException.EACCES);
				}
				String hexString = null;
				log.info("glue ");
				int readBytes;
				ret = new RandomAccessTemporaryFileChannel();
				int validSegment = 0;
	
				List<byte[]> segmentBuffers = concurrent_multi_file_handler
						.getFilesAsByteArrays(fragmentNames.toArray(new String[0]), fileFragmentMetaDataStore.getNrOfRequiredFragments(path));
				for (byte[] segmentBuffer : segmentBuffers) {
					//log.info("glue " + new String(segmentBuffer));
	
					digestFunc.reset();
	
					digestFunc.update(segmentBuffer, 0, segmentBuffer.length);
	
					digestFunc.doFinal(digestByteArray, 0);
	
					hexString = new String(Hex.encode(digestByteArray));
	
					/*
					 * if (!hexString.equals(file_fragment.getName())) {
					 * log.error("this file segment is invalid! " +
					 * file_fragment.getName() + " <> " + hexString); } else {
					 */
					receivedFileSegments.add(segmentBuffer);
					validSegment++;
	
					if (validSegment >= fileFragmentMetaDataStore.getNrOfRequiredFragments(path)) {
						break;
					}
					// }
				}
				byte[] recoveredFile = decoder.process(receivedFileSegments);
	
				digestFunc.reset();
	
				digestFunc.update(recoveredFile, 0, recoveredFile.length);
	
				digestFunc.doFinal(digestByteArray, 0);
	
				ret.getChannel().write(ByteBuffer.wrap(recoveredFile));
	
				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
	}

}
