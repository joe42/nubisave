package com.github.joe42.splitter;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
public class ConcurrentMultipleFileHandler implements MultipleFileHandler{
		private static final Logger  log = Logger.getLogger("concurrent multiple filehandler");
		public ConcurrentMultipleFileHandler() {
			PropertyConfigurator.configure("log4j.properties");
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
						if(future.get() != null){
							ret.add(future.get());
						}
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
				if (file.exists()) {
					try {
						FileInputStream in = new FileInputStream(file);
						int len = (int) file.length();
						if(len < 0){
							log.error("file length returned must not be negative, but is:"+len);
						} 
						ret = new byte[(int) file.length()];
						if(len == 0){
							return ret;
						}
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
}



