/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.joe42.splitter.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility class to simplify access to property files
 * 
 */
public class PropertiesUtil {

    private java.util.Properties properties;

    private String file;

    /**
     * Creates a Properties object
     * @param file the property file to operate on
     */
    public PropertiesUtil(String file) {
        this.file = file;
    }


    private boolean loadProperties() {
        properties = new java.util.Properties();
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        try {
            properties.load(fis);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * Writes all properties back
     * @return true iff successful
     */
    private boolean writeProperties() {
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        try {
            properties.store(fos, null);
        } catch (IOException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    /**
     * Gets a property value
     * @param key
     * @return the value of the property
     */
    public String getProperty(String key) {
        if (properties==null) loadProperties();
        if (properties==null) return null;
        return properties.getProperty(key);
    }

    /**
     * Sets a property
     * @param key the name of the property
     * @param value the value of the property
     */
    public void setProperty(String key, String value) {
        if (properties==null) loadProperties();
        if (properties==null) return;
        properties.setProperty(key, value);
        writeProperties();
    }
}