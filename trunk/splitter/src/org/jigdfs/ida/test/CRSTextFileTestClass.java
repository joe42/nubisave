package org.jigdfs.ida.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;


public class CRSTextFileTestClass {
private static Logger logger = Logger.getLogger(CRSTextFileTestClass.class.getName());
	
	public static void main(String[] args) throws Exception{
		File file = new File("testinput.txt");
		InputStream is = new FileInputStream(file);
		
		// Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            
            throw new Exception("file is too large!");
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
        
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        
        
		InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(10, 3, 4096);
		
		InformationDispersalEncoder encoder = crsidacodec.getEncoder();
		
		List<byte[]> result = encoder.process(bytes);
		
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
