package com.github.joe42.splitter;

import java.util.List;

public interface MultipleFileHandler {
	public List<byte[]> getFilesAsByteArrays(String[] file_names);
}