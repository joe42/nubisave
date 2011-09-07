/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author demo
 */
public class Properties {

    private static java.util.Properties properties;

    private static String file = "nubi.properties";
    
    public Properties() {
    }

    private static boolean loadProperties() {
        properties = new java.util.Properties();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        try {
            properties.load(fis);
        } catch (IOException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }
    
    private static boolean writeProperties() {
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        try {
            properties.store(fos, null);
        } catch (IOException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }
    
    public static String getProperty(String key) {
        if (properties==null) loadProperties();
        if (properties==null) return null;
        return properties.getProperty(key);
    }
    
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        writeProperties();
    }
}