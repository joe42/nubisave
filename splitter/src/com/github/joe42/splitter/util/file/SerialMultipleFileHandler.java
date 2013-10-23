package com.github.joe42.splitter.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;

public class SerialMultipleFileHandler implements MultipleFileHandler {
	private static final Logger  log = Logger.getLogger("concurrent multiple filehandler");
	private Digest digestFunc;
	public SerialMultipleFileHandler() {
		PropertyConfigurator.configure("log4j.properties");
		this.digestFunc = new SHA256Digest();
	}
	public SerialMultipleFileHandler(Digest digestFunc) {
		PropertyConfigurator.configure("log4j.properties");
		this.digestFunc = digestFunc;
	}

	public synchronized MultipleFiles writeFilesAsByteArrays(HashMap<String, byte[]> files){
		MultipleFiles multipleFiles = new MultipleFiles(files.keySet(), digestFunc);
		if(files == null || files.size() == 0){
			return multipleFiles;
		}
		for (String filePath : files.keySet()) {
			if(write(filePath, files.get(filePath))){
				multipleFiles.addTransferedFile(filePath, files.get(filePath));
			} else {
				multipleFiles.addFailedFilePath(filePath);
			}
		}
		return multipleFiles;
	}

	private boolean write(String fileName, byte[] bytes) {
		OutputStream out;
		try {
			out = new FileOutputStream(fileName);
			out.write(bytes);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	public synchronized MultipleFiles getFilesAsByteArrays(Map<String, byte[]> filePathsToChecksum, int files_needed){
		MultipleFiles multipleFiles = new MultipleFiles(filePathsToChecksum, digestFunc);
		List<String> filePaths = new ArrayList<String>(filePathsToChecksum.keySet());
		if(filePathsToChecksum.size() == 0){
			return null;
		}
		int canFail = filePaths.size() - files_needed;
		byte[] file;
		for (String filePath : filePaths) {
			file = getFileAsByteArray(filePath);
			if(file != null){
				multipleFiles.addTransferedFile(filePath, file);
			} else {
				multipleFiles.addFailedFilePath(filePath);
			}
			if(multipleFiles.getNrOfSuccessfullyTransferedFiles()==files_needed || multipleFiles.getNrOfUnsuccessfullyTransferedFiles() > canFail){
				break;
			}
		}
		return multipleFiles;
	}
	
	public synchronized MultipleFiles getFilesAsByteArrays(Map<String, byte[]> filePathsToChecksum){
		return getFilesAsByteArrays(filePathsToChecksum, filePathsToChecksum.size());
	}

	private byte[] getFileAsByteArray(String file_name) {
		byte[] ret = null;
		File file = new File(file_name);
		if (file.exists()) {
			try {
				FileInputStream in = new FileInputStream(file);
				int len = (int) file.length();
				if(len < 0){
					log.error("file length returned must not be negative, but is:"+len);
					return null;
				} 
				ret = new byte[len];
				in.read(ret);
				in.close();
			} catch (IOException e) {
				//don't care
				e.printStackTrace();
			}
		}
		return ret;
	}
}
