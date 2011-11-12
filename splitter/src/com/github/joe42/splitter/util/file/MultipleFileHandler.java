package com.github.joe42.splitter.util.file;

import java.util.*;

public interface MultipleFileHandler {
	public List<byte[]> getFilesAsByteArrays(String[] file_names);
	public List<byte[]> getFilesAsByteArrays(String[] file_names, int files_needed);
	public int writeFilesAsByteArrays(HashMap<String, byte[]> files);
}