package com.github.joe42.splitter.util.file;

import java.io.IOException;

import org.ini4j.Ini;

public class IniUtil {
	public static Ini getIni(String iniText){
    	TemporaryTextFile temp = new TemporaryTextFile();
    	temp.write(iniText);
		try {
			return new Ini(temp.getTempFile());
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	return null;
    }    

	public static Boolean isIni(String iniText){
    	TemporaryTextFile temp = new TemporaryTextFile();
    	temp.write(iniText);
		try {
			new Ini(temp.getTempFile());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} 
    	return false;
    }    

	public static String getString(Ini ini) {
		TemporaryTextFile temp = new TemporaryTextFile();
		try {
			ini.store(temp.getTempFile());
			return temp.getText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T> T get(Ini ini, String sectionname, String optionname, Class<T> c) {
		try{
			return ini.get(sectionname, optionname, c);
		} catch(NullPointerException e){
		}
		return null;
	}
	
	public static String get(Ini ini, String sectionname, String optionname) {
		try{
			return ini.get(sectionname, optionname, String.class);
		} catch(NullPointerException e){
		}
		return null;
	}
}
