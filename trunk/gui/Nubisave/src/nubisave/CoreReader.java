/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author demo
 */
public class CoreReader {
    
    private File confDir;

    public CoreReader() {
        String home = System.getProperty("user.home");
        confDir = new File(home + "/.config/nubisave/storages");
        confDir.mkdirs();
    }
    
    public void readExistingServices() {
        if (!confDir.exists()) {
            return;
        }
        String[] confs = confDir.list();
        List<MatchmakerService> newServices = new LinkedList<MatchmakerService>();
        for (String fileStr : confs) {
            File cloudConfig = new File(confDir + "/"+fileStr); 
            if (!cloudConfig.isFile()) {
                continue;
            }
            
            MatchmakerService service = getServiceFromCloudStorageFile(cloudConfig);
            if (service != null) {
                newServices.add(service);
            }
        }
        Nubisave.services.getMmServices().addAll(newServices);
    }
    
    private MatchmakerService getServiceFromCloudStorageFile(File cloudFile) {
        FileInputStream fis = null;
        BufferedReader br;
        java.util.Properties props = new java.util.Properties();
        try {
            fis = new FileInputStream(cloudFile);
            br = new BufferedReader(new InputStreamReader(fis));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Properties.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("Name=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Name", line.split("=")[1]);
                    }
                    continue;
                }
                if (line.startsWith("Priority=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Priority", lineSplit[1]);
                    }
                    continue;
                }
                if (line.startsWith("Module=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Module", lineSplit[1]);
                    }

                    continue;
                }
                if (line.startsWith("Username=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Username", lineSplit[1]);
                    }
                    continue;
                }
                if (line.startsWith("Password=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Password", lineSplit[1]);
                    }
                    continue;
                }
                if (line.startsWith("Size=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("Size", lineSplit[1]);
                    }
                    continue;
                }
                if (line.startsWith("FreeSize=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {
                        props.setProperty("FreeSize", lineSplit[1]);
                    }
                    continue;
                }
                if (line.startsWith("Deleted=")) {
                    String[] lineSplit = line.split("=");
                    if (lineSplit.length > 1) {

                        props.setProperty("Deleted", lineSplit[1]);
                    }
                    continue;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(CoreWriter.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        String prop = props.getProperty("Module");
        if (prop == null) {
            return null;
        }
        
        MatchmakerService service = new MatchmakerService(prop);
           
            prop = props.getProperty("Name");
            if (prop != null) {
                service.setUniqName(prop);
            }
            prop = props.getProperty("Username");
            if (prop != null) {
                service.setUser(prop);
            }
            prop = props.getProperty("Password");
            if (prop != null) {
                service.setPass(prop);
            }
            prop = props.getProperty("Deleted");
            if (prop != null) {
                service.setEnabled(prop.equals("0"));
            }
        return service;
    }
}
