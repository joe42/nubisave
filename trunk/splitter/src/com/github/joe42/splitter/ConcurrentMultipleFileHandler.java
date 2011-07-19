package com.github.joe42.splitter;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentMultipleFileHandler implements MultipleFileHandler{
		public ConcurrentMultipleFileHandler() {
		}

		public List<byte[]> getFilesAsByteArrays(String[] file_names){
			List<byte[]> ret = new ArrayList<byte[]>();
			if(file_names == null || file_names.length == 0){
				return ret;
			}
				ExecutorService executor = Executors.newFixedThreadPool(file_names.length);
				List<Future<byte[]>> list = new ArrayList<Future<byte[]>>();
				for (String file_name : file_names) {
					Callable<byte[]> worker = new GetFileAsByteArray(file_name);
					Future<byte[]> submit = executor.submit(worker);
					list.add(submit);
				}
				// Now retrieve the result
				for (Future<byte[]> future : list) {
					try {
						ret.add(future.get());
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} 
				}
				executor.shutdown();

			return ret;
		}

		class GetFileAsByteArray implements Callable<byte[]> {
			private String file_name;

			public GetFileAsByteArray(String file_name){
				this.file_name = file_name;
			}
			@Override
			public byte[] call() throws Exception {
				return getFileAsByteArray(this.file_name);
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
}



