/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.ui.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for string operations.
 * @author joe
 */
public class StringUtil {
    
    /**
     * Read the contents of a file, and return it as a string.
     * @return the text of the file pathname, or null if an IOException occurred
     */
    public static String readFile(String pathname) {
        try {
            return new String(Files.readAllBytes(Paths.get(pathname)));
        } catch (IOException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if(!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();

            // previous code: destination.transferFrom(source, 0, source.size());
            // to avoid infinite loops, should be:
            long count = 0;
            long size = source.size();              
            while((count += destination.transferFrom(source, count, size-count))<size);
        }
        finally {
            if(source != null) {
                source.close();
            }
            if(destination != null) {
                destination.close();
            }
        }
    }
}
