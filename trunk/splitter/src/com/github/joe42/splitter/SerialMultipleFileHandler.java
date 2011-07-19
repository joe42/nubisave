package com.github.joe42.splitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class SerialMultipleFileHandler implements MultipleFileHandler {
	public SerialMultipleFileHandler() {
	}

	public List<byte[]> getFilesAsByteArrays(String[] file_names){
		List<byte[]> ret = new ArrayList<byte[]>();
		if(file_names == null || file_names.length == 0){
			return ret;
		}
		for (String file_name : file_names) {
			ret.add(getFileAsByteArray(file_name));
		}
		return ret;
	}

	private byte[] getFileAsByteArray(String file_name) {
		byte[] ret = null;
		File file = new File(file_name);
		if (file.exists() && file.isFile()) {
			try {
				FileInputStream in = new FileInputStream(file);
				ret = new byte[(int) file.length()];
				in.read(ret);
			} catch (IOException e) {
				//don't care
			}
		}
		return ret;
	}
}
