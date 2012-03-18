package com.github.joe42.splitter.util.file;

import java.util.*;

public interface MultipleFileHandler {
	public MultipleFiles getFilesAsByteArrays(Map<String, byte[]> filePathsToChecksum);
	public MultipleFiles getFilesAsByteArrays(Map<String, byte[]> filePathsToChecksum, int files_needed);
	public MultipleFiles writeFilesAsByteArrays(HashMap<String, byte[]> files);
}