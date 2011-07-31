package com.github.joe42.splitter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test {

	public static void main(String[] args) throws IOException {
		///////////////////write to bb
		ByteBuffer bb = ByteBuffer.allocateDirect(26);
		for (int i = 0; i < 26; i++)
	        bb.put((byte) ('A' + i));
		bb.rewind();
		///////////////////write to tmp
		File temp = File.createTempFile("tmpig", ".tmp");
	    FileChannel wChannel = new FileOutputStream(temp, false).getChannel();
    	wChannel.position(0);
	    wChannel.write(bb);
	    wChannel.close();
		///////////////////write to bb
		ByteBuffer bb2 = ByteBuffer.allocateDirect(26);
		for (int i = 0; i < 26; i++)
	        bb2.put((byte) ('A' + i));
		bb2.rewind();
		///////////////////write to tmp at certain pos
	    FileChannel wChannel3 = new RandomAccessFile(temp, "rw").getChannel();
    	wChannel3.position(5);
	    wChannel3.write(bb2); 
		bb2.rewind();
		///////////////////write to tmp at certain pos
		wChannel3.position(wChannel3.size());
		wChannel3.write(bb2);
		wChannel3.close();
	    //////////////////
	    byte[] ret = null;
		FileInputStream in = new FileInputStream(temp);
		ret = new byte[(int) temp.length()];
		in.read(ret);
	    System.out.println("output: "+new String(ret));
	    bb2.rewind();
	    System.out.println("output: "+((ByteBuffer)bb2.flip()).asCharBuffer().get(0));
	}

}
