package com.github.joe42.splitter;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

public interface MultipleFileHandler {
	public List<byte[]> getFilesAsByteArrays(String[] file_names);
	public List<byte[]> getFilesAsByteArrays(String[] file_names, int files_needed);
	public int writeFilesAsByteArrays(HashMap<String, byte[]> files);
}