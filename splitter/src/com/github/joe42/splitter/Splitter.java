package com.github.joe42.splitter;

import java.io.File;
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

import com.github.joe42.splitter.util.StringUtil;
import com.github.joe42.splitter.util.file.ConcurrentMultipleFileHandler;
import com.github.joe42.splitter.util.file.MultipleFileHandler;
import com.github.joe42.splitter.util.file.RandomAccessTemporaryFileChannel;
import com.github.joe42.splitter.vtf.FileEntry;

import fuse.FuseException;

public class Splitter {
	private static int MAX_FILE_FRAGMENTS;
	private static int MAX_FILE_FRAGMENTS_NEEDED;
	public Map<String, List<String>> pathToFragmentNamesMap = new HashMap<String, List<String>>();
	private String storages;
	private int redundancy;
	private static final Logger  log = Logger.getLogger("Splitter");
	private MultipleFileHandler multi_file_handler;
	
	public Splitter(String storages, int redundancy){
		this.storages = storages;
		this.redundancy = redundancy;
		multi_file_handler = new ConcurrentMultipleFileHandler();
	}
	public Splitter(String storages){
		this(storages, 0);
	}

	public String getStorages(){
		return storages;
	}
	public List<String> getFragmentNames(String path){
		return pathToFragmentNamesMap.get(path);
	}

	public void setFragmentNames(String path, List<String> fragmentPaths){
		pathToFragmentNamesMap.put(path, fragmentPaths);
	}

	public void removeFragmentNames(String path){
		pathToFragmentNamesMap.put(path, null);
	}
	
	public void moveFragmentNames(String from, String to){
		pathToFragmentNamesMap.put(to, pathToFragmentNamesMap.get(from));
		pathToFragmentNamesMap.put(from, null);
	}

	public int getNrOfFragments(String path){
		return MAX_FILE_FRAGMENTS;
	}
	
	public int getNrOfRequiredFragments(String path){
		return MAX_FILE_FRAGMENTS_NEEDED;
	}
	
	private List<String> getFragmentStores() {
		log.debug("getting fragment stores");
		List<String> ret = new ArrayList<String>();
		File storageFolder = new File(storages);
		File dataStorages;
		String[] folders = storageFolder.list();
		String[] dataFolders;
		ret.clear();
		if (folders == null) {
			log.debug(storages + " is not a directory!");
		} else {
			for (int i = 0; i < folders.length; i++) {
				log.debug("checking "+storageFolder.getAbsolutePath()+"/"+folders[i]);
				dataStorages = new File(storageFolder.getAbsolutePath()+"/"+folders[i]);
				dataFolders = dataStorages.list();
				if (dataStorages == null) {
					log.debug(storageFolder.getAbsolutePath()+"/"+folders[i] + " has no data directory!");
				} else {
					for (int j = 0; j < dataFolders.length;j++) {
						if(dataFolders[j].equals("data")){
							log.debug(dataStorages.getAbsolutePath()+"/data"+ " added");
							ret.add(dataStorages.getAbsolutePath()+"/data");
						}
					}
				}
			}
		}
		return ret;
	}
	
	public void splitFile(FileEntry fileEntry, FileChannel temp) throws FuseException {
		
		int nr_of_file_parts_successfully_stored = 0;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		List<String> fragmentStores = getFragmentStores();
		MAX_FILE_FRAGMENTS = fragmentStores.size();
		log.debug("MAX_FILE_FRAGMENTS:" + MAX_FILE_FRAGMENTS);
		MAX_FILE_FRAGMENTS_NEEDED = (int) (MAX_FILE_FRAGMENTS *(100-redundancy) /100f); //100% redundancy -> only one file is enough to restore everything
		if(MAX_FILE_FRAGMENTS_NEEDED <1){
			MAX_FILE_FRAGMENTS_NEEDED=1;
		}
		String fragment_name;
		String uniquePath;
		List<String> fileFragments = pathToFragmentNamesMap.get(fileEntry.path);
		if(fileFragments == null) {
			fileFragments = new ArrayList<String>();
			pathToFragmentNamesMap.put(fileEntry.path, fileFragments);
		}
		if (fileFragments.isEmpty()) {
			uniquePath = StringUtil.getUniqueAsciiString(fileEntry.path);
			for (int fragment_nr = 0; fragment_nr < MAX_FILE_FRAGMENTS; fragment_nr++) {
				fragment_name = fragmentStores.get(fragment_nr) + uniquePath
						+ '#' + fragment_nr;
				fileFragments.add(fragment_name);
			}
		}
		Digest digestFunc = new SHA256Digest();
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		String hexString = null;
		InformationDispersalCodec crsidacodec;
		InformationDispersalEncoder encoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(
					MAX_FILE_FRAGMENTS, MAX_FILE_FRAGMENTS_NEEDED, 1);
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

			for (int fragment_nr = 0; fragment_nr < MAX_FILE_FRAGMENTS; fragment_nr++) {

				fragment_name = fileFragments.get(fragment_nr);
				log.debug("write: " + fragment_name);
				fileFragments.add(fragment_name);
				byte[] b = result.get(fragment_nr);
				digestFunc.reset();
				digestFunc.update(b, 0, b.length);
				digestFunc.doFinal(digestByteArray, 0);

				hexString = new String(Hex.encode(digestByteArray));
				fileParts.put(fragment_name, b);
			}
			nr_of_file_parts_successfully_stored = multi_file_handler.writeFilesAsByteArrays(fileParts);
			fileEntry.size = (int) temp.size();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error: " + e.toString(), e)
					.initErrno(FuseException.EIO);
		}

		//if (log.isDebugEnabled())
		  log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+MAX_FILE_FRAGMENTS_NEEDED);
		if (nr_of_file_parts_successfully_stored < MAX_FILE_FRAGMENTS_NEEDED) {
			throw new FuseException(
					"IO error: Not enough file parts could be stored.")
					.initErrno(FuseException.EIO);
		}
	}
	
	
	
	


	public RandomAccessTemporaryFileChannel glueFilesTogether(FileEntry fileEntry) throws FuseException {
		RandomAccessTemporaryFileChannel ret = null;
		List<byte[]> receivedFileSegments = new ArrayList<byte[]>();
		Digest digestFunc = new SHA256Digest();
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		InformationDispersalCodec crsidacodec;
		InformationDispersalDecoder decoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(
					MAX_FILE_FRAGMENTS, MAX_FILE_FRAGMENTS_NEEDED, 1);
			decoder = crsidacodec.getDecoder();
		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IDA could not be initialized")
					.initErrno(FuseException.EACCES);
		}
		String hexString = null;
		log.info("glue ");
		int readBytes;
		try {
			ret = new RandomAccessTemporaryFileChannel();
			int validSegment = 0;
			List<String> fragmentNames = pathToFragmentNamesMap.get(fileEntry.path);

			List<byte[]> segmentBuffers = multi_file_handler
					.getFilesAsByteArrays(fragmentNames.toArray(new String[0]), MAX_FILE_FRAGMENTS_NEEDED);
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

				if (validSegment >= MAX_FILE_FRAGMENTS_NEEDED) {
					break;
				}
				// }
			}
			byte[] recoveredFile = decoder.process(receivedFileSegments);

			digestFunc.reset();

			digestFunc.update(recoveredFile, 0, recoveredFile.length);

			digestFunc.doFinal(digestByteArray, 0);

			ret.getChannel().write(ByteBuffer.wrap(recoveredFile));

		} catch (Exception e) {
			e.printStackTrace();
			throw new FuseException("IO error", e).initErrno(FuseException.EIO);
		}
		return ret;
	}
}
