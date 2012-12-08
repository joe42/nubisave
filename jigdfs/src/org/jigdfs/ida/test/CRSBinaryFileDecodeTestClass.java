package org.jigdfs.ida.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;

import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;


public class CRSBinaryFileDecodeTestClass {
	private static Logger logger = Logger
			.getLogger(CRSBinaryFileDecodeTestClass.class.getName());

	public static void main(String[] args) throws Exception {
		String fileName = "idg166-recovered.rar";
		String fileHash = "74af6434a007a59ee2469cdd213d642f741d5917dea64df90896177a7f683da5";

		InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(
				10, 3, 4096);

		InformationDispersalDecoder decoder = crsidacodec.getDecoder();

		List<byte[]> receivedFileSegments = new ArrayList<byte[]>();

		Digest digestFunc = new SHA256Digest();
		logger.info("Digest Function: " + digestFunc.getAlgorithmName());

		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		String hexString = null;

		InputStream in = null;
		int readBytes;

		try {
			File fileSegmentFolder = new File(fileHash);

			if (!fileSegmentFolder.exists()) {
				throw new IOException(fileSegmentFolder + " doesn't exist!");
			}
			/*
			// get the segment with the biggest length to create the
			// segmentBuffer
			// it's possible that the first is corrupted and the size is smaller
			// than the rest...
			
			long fileSegmentSize = 0;
			long thisSegmentSize = 0;

			for (File fileSegment : fileSegmentFolder.listFiles()) {
				thisSegmentSize = fileSegment.length();
				if (thisSegmentSize > fileSegmentSize) {
					fileSegmentSize = thisSegmentSize;
				}
			}
*/
			
			int validSegment = 0;

			for (File fileSegment : fileSegmentFolder.listFiles()) {
				if (fileSegment.exists() && fileSegment.isFile()) {
					in = new FileInputStream(fileSegment);
					byte[] segmentBuffer = new byte[(int) fileSegment.length()];
					
					readBytes = in.read(segmentBuffer);
					logger.info("read file segment: " + fileSegment.getName()
							+ "; " + readBytes + " bytes!");

					digestFunc.reset();

					digestFunc.update(segmentBuffer, 0, segmentBuffer.length);

					digestFunc.doFinal(digestByteArray, 0);

					hexString = new String(Hex.encode(digestByteArray));

					if (!hexString.equals(fileSegment.getName())) {
						logger.error("this file segment is invalid! "
								+ fileSegment.getName() + " <> " + hexString);
					} else {

						receivedFileSegments.add(segmentBuffer);
						validSegment++;
						if (validSegment > 2) {
							break;
						}
					}

					in.close();
				}
			}

			byte[] recoveredFile = decoder.process(receivedFileSegments);
			
			digestFunc.reset();

			digestFunc.update(recoveredFile, 0, recoveredFile.length);

			digestFunc.doFinal(digestByteArray, 0);

			hexString = new String(Hex.encode(digestByteArray));

			if (!hexString.equals(fileHash)) {
				throw new Exception(
						"the recovered file hash value is not the same as original! "
								+ hexString + " <> " + fileHash);
			}

			File file = new File(fileName);
			OutputStream out = new FileOutputStream(file);

			out.write(recoveredFile);
			out.flush();
			out.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
