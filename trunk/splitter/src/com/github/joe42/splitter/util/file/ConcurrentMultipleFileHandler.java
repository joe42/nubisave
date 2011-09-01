package com.github.joe42.splitter.util.file;

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
		public int writeFilesAsByteArrays(HashMap<String, byte[]> files){
			int nr_of_files_written = 0;
			if(files == null || files.size() == 0){
				return nr_of_files_written;
			}
			ExecutorService executor = Executors.newFixedThreadPool(files.size());
			List<Future<Boolean>> list = new ArrayList<Future<Boolean>>();

			for (String file_name : files.keySet()) {
				Callable<Boolean> worker = new Write(file_name, files.get(file_name));
				Future<Boolean> submit = executor.submit(worker);
				list.add(submit);
			}
			// Now retrieve the result
			for (Future<Boolean> future : list) {
				try {
					if(future.get() == true){
						nr_of_files_written++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} 
			}
			executor.shutdown();
			return nr_of_files_written;
		}

		class Write implements Callable<Boolean> {
			private String file_name;
			private byte[] content;

			public Write(String fileName, byte[] bytes){
				this.file_name = fileName;
				this.content = bytes;
			}
			@Override
			public Boolean call() throws Exception {
				return write(this.file_name, this.content);
			}
			
			private Boolean write(String fileName, byte[] bytes) {
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

		}	

		public List<byte[]> getFilesAsByteArrays(String[] file_names, int files_needed){
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
						if(ret.size()>= files_needed){
							break;
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

		public List<byte[]> getFilesAsByteArrays(String[] file_names){
			return getFilesAsByteArrays(file_names, file_names.length);
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
							return null;
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



