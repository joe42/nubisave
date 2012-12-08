package org.jigdfs.ida.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;
import org.jigdfs.ida.exception.IDADecodeException;
import org.jigdfs.ida.exception.IDAEncodeException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;

public class CRSStringTestClass {
	
	private static Logger logger = Logger.getLogger(CRSStringTestClass.class.getName());
	
	public static void main(String[] args) throws IDAInvalidParametersException, IDANotInitializedException, IDAEncodeException, IDADecodeException{
		String testString = "hello world";
		byte[] testByteArray = testString.getBytes();
		
		InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(16, 4, 4096);
		
		InformationDispersalEncoder encoder = crsidacodec.getEncoder();
		
		List<byte[]> result = encoder.process(testByteArray);
		
		logger.info(result.size());
		/*
		for(byte[] b:result){
			logger.debug(new String(b));
		}
		*/
		InformationDispersalDecoder decoder = crsidacodec.getDecoder();
		
		//manually remove some packets...
		List<byte[]> receivedPackets = new ArrayList<byte[]>();
		
		//should need only 4 of them
		receivedPackets.add(result.get(1));
		receivedPackets.add(result.get(3));
		receivedPackets.add(result.get(5));
		receivedPackets.add(result.get(7));
		receivedPackets.add(result.get(9));
		
		logger.info("Recovered string:" + new String(decoder.process(receivedPackets)));
		
		
	}
}
