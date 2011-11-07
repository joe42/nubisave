package com.github.joe42.splitter;

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
	private static final int CAUCHY_WORD_LENGTH = 1;
	private FileFragmentStore fileFragmentStore;
	private String storages;
	private int redundancy;
	private static final Logger  log = Logger.getLogger("Splitter");
	private MultipleFileHandler multi_file_handler;
	
	public Splitter(String storages, int redundancy){
		fileFragmentStore = new FileFragmentStore(storages);
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
	public int getNrOfFragments(String path){
		return  fileFragmentStore.getNrOfFragments(path);
	}
	
	public int getNrOfRequiredFragments(String path){
		return  fileFragmentStore.getNrOfRequiredFragments(path);
	}
	
	public void splitFile(FileEntry fileEntry, FileChannel temp) throws FuseException {
		
		int nr_of_file_parts_successfully_stored = 0;
		HashMap<String, byte[]> fileParts = new HashMap<String, byte[]>();
		List<String> fragmentStores = fileFragmentStore.getFragmentStores();
		int nr_of_file_fragments = fragmentStores.size();
		log.debug("nr_of_stores:" + nr_of_file_fragments);
		int nr_of_file_fragments_required = (int) (nr_of_file_fragments *(100-redundancy) /100f); //100% redundancy -> only one file is enough to restore everything
		if(nr_of_file_fragments_required <1){
			nr_of_file_fragments_required=1;
		}
		String fragment_name;
		String uniquePath;
		uniquePath = StringUtil.getUniqueAsciiString(fileEntry.path);
		ArrayList<String> fragmentFileNames = new ArrayList<String>();
		for (int fragment_nr = 0; fragment_nr < nr_of_file_fragments; fragment_nr++) {
			fragment_name = fragmentFileNames.get(fragment_nr) + uniquePath 
					+ '#' + fragment_nr;
			fragmentFileNames.add(fragment_name);
		}
		Digest digestFunc = new SHA256Digest();
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		String hexString = null;
		InformationDispersalCodec crsidacodec;
		InformationDispersalEncoder encoder;
		try {
			crsidacodec = new CauchyInformationDispersalCodec(
					nr_of_file_fragments, nr_of_file_fragments_required, 1);
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
				fragmentFileNames.add(fragment_name);
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
		  log.debug("nr_of_file_parts_successfully_stored: "+nr_of_file_parts_successfully_stored+" - MAX_FILE_FRAGMENTS_NEEDED "+nr_of_file_fragments_required);
		if (nr_of_file_parts_successfully_stored < nr_of_file_fragments_required) {
			throw new FuseException(
					"IO error: Not enough file parts could be stored.")
					.initErrno(FuseException.EIO);
		} else {
			//TODO: delete previous Fragments
			fileFragmentStore.setFragment(fileEntry.path, fragmentFileNames, nr_of_file_fragments_required);
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
					fileFragmentStore.getNrOfFragments(fileEntry.path), fileFragmentStore.getNrOfRequiredFragments(fileEntry.path), CAUCHY_WORD_LENGTH);
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
			List<String> fragmentNames = fileFragmentStore.getFragments(fileEntry.path);

			List<byte[]> segmentBuffers = multi_file_handler
					.getFilesAsByteArrays(fragmentNames.toArray(new String[0]), fileFragmentStore.getNrOfRequiredFragments(fileEntry.path));
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

				if (validSegment >= fileFragmentStore.getNrOfRequiredFragments(fileEntry.path)) {
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

	public FileFragmentStore getFragmentStore() {
		return fileFragmentStore;
	}
}
