/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.joe42.splitter.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.*;
/**
 * A utility class to simplify access to property files
 * 
 */
public class PropertiesUtil {

    private  PropertiesConfiguration config = null;

    private String file;

	private Properties properties;

    /**
     * Creates a PropertiesUtil object
     * @param file the property file to operate on
     */
    public PropertiesUtil(String file) {
        this.file = file;
    }
    
	/**
	 * 
	 * @return the Property instance, which this object is representing
	 */
	public Properties getProperties() {
		return properties;
	}

    private boolean loadProperties() {
        properties = new java.util.Properties();
        FileInputStream fis = null;
        try {
        	config = new PropertiesConfiguration(file);
        } catch (ConfigurationException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, "Could not load property file: "+file, ex);
            return false;
		}
        
        try {
            fis = new FileInputStream(config.getFile());
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
        	config.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(PropertiesUtil.class.getName()).log(Level.SEVERE, "Could not save property file: "+file, ex);
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
        if (config==null) loadProperties();
        if (config==null) return null;
        return config.getString(key);
    }

    /**
     * Sets a property
     * @param key the name of the property
     * @param value the value of the property
     */
    public void setProperty(String key, String value) {
        if (properties==null) loadProperties();
        if (properties==null) return;
        if (config==null) return;
        config.setProperty(key, value);
        properties.setProperty(key, value);
        writeProperties();
    }
}