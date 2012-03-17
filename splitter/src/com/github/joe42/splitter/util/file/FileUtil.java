package com.github.joe42.splitter.util.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Scanner;

public class FileUtil {

	public static boolean writeFile(File file, String str){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(str);
			writer.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String readFile(File file) {
	    StringBuilder fileContents = new StringBuilder();
	    Scanner scanner;
		try {
			scanner = new Scanner(file, "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	    String lineSeparator = System.getProperty("line.separator");
	    try {
	        while(scanner.hasNextLine()) {        
	            fileContents.append(scanner.nextLine() + lineSeparator);
	        }
	        return fileContents.toString();
	    } finally {
	        scanner.close();
	    }
	}

	public static boolean writeFile(File file, ByteBuffer bb){
		try {
			FileChannel wChannel = new FileOutputStream(file, false).getChannel();
		    wChannel.write(bb);
		    wChannel.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static boolean writeFile(File file, ByteBuffer bb, long offset){
		try {
			FileChannel wChannel = new RandomAccessFile(file, "rw").getChannel();
		    wChannel.write(bb, offset);
		    wChannel.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * Copy src file to dst file.
	 * If the dst file does not exist, it is created.	
	 * @param src
	 * @param dst
	 * @throws IOException
	 */
	public static void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}

}
