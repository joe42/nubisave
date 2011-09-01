package com.github.joe42.splitter.util.file;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class RobustConcurrentMultipleFileHandler extends
		ConcurrentMultipleFileHandler {
	private int nr_of_files_needed;
	public RobustConcurrentMultipleFileHandler(int nr_of_files_needed){
		this.nr_of_files_needed = nr_of_files_needed;
	}
	public void setNumberOfFilesNeeded(int nr_of_files_needed){
		this.nr_of_files_needed = nr_of_files_needed;
	}
	public List<byte[]> getFilesAsByteArrays(String[] file_names){
		List<byte[]> ret = new ArrayList<byte[]>();
		if(nr_of_files_needed == 0 || file_names == null || file_names.length == 0 || nr_of_files_needed > file_names.length){
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
			int collected_files = 0;
			Future<byte[]> future;
			byte[] result;
			while (collected_files < nr_of_files_needed) {
				try {
					for (int i = 0; i < list.size(); i++) {
						future = list.get(i);
						result = future.get(1, TimeUnit.SECONDS);
						ret.add(result);
						collected_files++;
						list.remove(i);//done with processing the future
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				} catch (TimeoutException e) {
					// do nothing
				} 
			}
			for (int i = 0; i < list.size(); i++) { //cancel all unneeded file reads
				future = list.get(i);
				future.cancel(true);
			}
			executor.shutdown();

		return ret;
	}
}
