package com.github.joe42.splitter.util.file;

import java.io.*;
import java.security.SecureRandom;
import java.math.BigInteger;


public class TemporaryTextFile {
		private File temp;
		private String encoding;

		public TemporaryTextFile(){
			init("UTF-8");
		}
		public TemporaryTextFile(String encoding){
			init(encoding);
		}
		private void init(String encoding) {
			this.encoding = encoding;
			try {
				temp = File.createTempFile(new BigInteger(130, new SecureRandom()).toString(32), ".tmp");
				temp.deleteOnExit();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		public Boolean write(String text){
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(temp));
				out.write(text.getBytes(encoding));
				out.close();
				return true;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		public String toString(){
			return getText();
		}
		public String getText() {
			return FileUtil.readFile(temp);
		}
		public File getTempFile(){
			return temp;
		}
		public void delete(){
			temp.delete();
		}
}
