package org.jigdfs.ida.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.util.encoders.Hex;
import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.cauchyreedsolomon.CauchyInformationDispersalCodec;


public class CRSBinaryFileEncodeTestClass {
private static Logger logger = Logger.getLogger(CRSBinaryFileEncodeTestClass.class.getName());
	
	public static void main(String[] args) throws Exception{
		File file = new File("ReadMe.txt");
		
		if(!file.exists()) {
			throw new IOException("File " + file.getName() + "doesn't exist!");
		}
		
		Digest digestFunc = new SHA256Digest();	
		logger.info("Digest Function: " + digestFunc.getAlgorithmName());
		
		byte[] digestByteArray = new byte[digestFunc.getDigestSize()];
		String hexString = null;
		
		InformationDispersalCodec crsidacodec = new CauchyInformationDispersalCodec(10, 3, 4096);
		
		InformationDispersalEncoder encoder = crsidacodec.getEncoder();
		
		long fileSize = file.length();
		logger.info("file size is " + fileSize + " bytes!");
		
		byte[] buffer = new byte[(int) (fileSize)];
		int readBytes;
		
		try{
			InputStream in = new FileInputStream(file);
			/*
			while ((readBytes = in.read(buffer)) != -1) {
			    logger.info("sized processed: " + sizeProcessed + "; " + Math.round(((double)sizeProcessed/(double)fileSize)) * 100 + "%");
			    
			    sizeProcessed += readBytes;
			    
			    
			}*/
			
			readBytes = in.read(buffer);
			logger.info("read " + readBytes + " bytes!");
			in.close();			
			
			digestFunc.update(buffer, 0, buffer.length);
			
			digestFunc.doFinal(digestByteArray, 0);
			
			//hexString = HexUtil.bytesToHex(digestByteArray);
			hexString = new String(Hex.encode(digestByteArray));
			
			logger.info("file: " + file.getName() + "; hash: " + hexString);
			
			File fileSegmentFolder = new File(hexString);
			if(!fileSegmentFolder.exists()){
				fileSegmentFolder.mkdir();
			}			
			
			List<byte[]> result = encoder.process(buffer);
			
			logger.info(result.size());			
			
			for(byte[] b:result){
				logger.info("segment size: " + b.length + " bytes");				
				digestFunc.reset();	
				digestFunc.update(b, 0, b.length);
				
				digestFunc.doFinal(digestByteArray, 0);
				
				hexString = new String(Hex.encode(digestByteArray));
				
				logger.info("the hash value: " + hexString);
				
				File fileSegment = new File(fileSegmentFolder, hexString);
				OutputStream out = new FileOutputStream(fileSegment);
				
				out.write(b);				
				out.flush();
				out.close();
			}		
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}    
		
	}
}
