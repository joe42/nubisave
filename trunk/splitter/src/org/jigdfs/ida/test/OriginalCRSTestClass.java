package org.jigdfs.ida.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jigdfs.references.ida.originalcrs.CauchyEncode;
import org.jigdfs.references.ida.originalcrs.Parameters;

public class OriginalCRSTestClass {
	private static Logger logger = Logger.getLogger(OriginalCRSTestClass.class
			.getName());

	public static int messageSize = 0;
	public static int fragmentSize = 0;
	public static int numSlices = 0;
	public static int threshold = 0;

	public static Parameters p = null;

	public static void main(String[] args) {
		String testString = "hello world";
		byte[] testByteArray = testString.getBytes();

		numSlices = 16;
		threshold = 12;
		// (12, 4, 4096), 12 data slices and 4 encoding slices
		p = new Parameters(threshold, numSlices - threshold);

		messageSize = p.Plen() * p.Mfragments();
		fragmentSize = p.Plentot();

		logger.debug("Slice count = " + numSlices);
		logger.debug("threshold = " + threshold);
		logger.debug("Message size: " + messageSize);
		logger.debug("Fragment size: " + fragmentSize);

		logger
				.debug("Blowup = "
						+ ((float) fragmentSize * (float) numSlices / (float) messageSize));

		logger.debug("Ideal = " + ((float) numSlices / (float) threshold));

		List<byte[]> returnBuffer = encode(testByteArray);
		
		logger.debug(returnBuffer.size());
		
		/*
		for(byte[] b:returnBuffer){
			logger.debug(new String(b));
		}
		*/
		

	}

	public static List<byte[]> encode(byte buffer[]) {

		int message[] = new int[messageSize];

		int fragments[] = new int[fragmentSize * numSlices];

		List<byte[]> outputBuffers = new ArrayList<byte[]>();
		for (int idx = 0; idx < numSlices; idx++) {
			outputBuffers.add(new byte[fragmentSize]);
		}

		int inputPosition = 0;
		int outputPosition = 0;

		// Calculate the size of each output buffer
		int outputSize = ((buffer.length + 1) / messageSize) * fragmentSize;

		if ((buffer.length + 1) % messageSize != 0) {
			outputSize += fragmentSize;
		}

		// If the output size is greater than the pre-allocated size
		if (outputSize > fragmentSize) {
			// Allocate new buffers for output
			outputBuffers = new ArrayList<byte[]>();

			// Allocate the output buffers
			for (int fragmentIdx = 0; fragmentIdx < numSlices; fragmentIdx++) {
				outputBuffers.add(new byte[outputSize]);
			}
		}

		while (outputPosition < outputSize) {
			byte fillerByte = 1;

			// Copy data from the input buffer into the data buffer

			for (int dataPosition = 0; dataPosition < message.length; dataPosition++) {
				if (inputPosition < buffer.length) {
					message[dataPosition] = buffer[inputPosition++];
				} else {
					message[dataPosition] = fillerByte;
					fillerByte = 0;
				}
			}

			// Encode the data buffer into the fragments array

			CauchyEncode.encode(fragments, message, p);

			// For each fragment
			for (int fragmentIdx = 0; fragmentIdx < numSlices; fragmentIdx++) {
				byte fragment[] = outputBuffers.get(fragmentIdx);

				int fragmentOffset = fragmentIdx * fragmentSize;

				for (int idx = 0; idx < fragmentSize; idx++) {
					fragment[outputPosition + idx] = (byte) fragments[fragmentOffset
							+ idx];
				}
			}

			outputPosition += fragmentSize;
		}

		// this.initialized = false;

		return outputBuffers;
	}
}
