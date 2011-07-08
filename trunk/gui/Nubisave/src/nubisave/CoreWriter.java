/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author demo
 */
public class CoreWriter {

    private File confDir;

    public CoreWriter() {
        String home = System.getProperty("user.home");
        confDir = new File(home + "/.config/nubisave/storages");
    }

    public boolean writeToCoreModule(Services services) {

        if (!confDir.exists() || !confDir.isDirectory()) {
            return false;
        }

        for (StorageService s : services.getMmServices()) {
            if (s.isSupported()) {
                writeCloudStorageFile(s);
            }
        }


        return true;
    }

    private void writeCloudStorageFile(StorageService s) {

        File cloudFile = new File(confDir + File.separator + s.getUniqName() + ".cloudstorage");
        if (!cloudFile.exists() && !s.isEnabled()) {
            return;
        }
        java.util.Properties props;
        props = new java.util.Properties();
        props.setProperty("Name", s.getUniqName());
        props.setProperty("Priority", "0");
        props.setProperty("Module", s.getName());
        String user = ((MatchmakerService) s).getUser();
        props.setProperty("Username", (user != null) ? user : "");
        String pass = ((MatchmakerService) s).getPass();
        props.setProperty("Password", (pass != null) ? pass : "");
        props.setProperty("Size", "0");
        props.setProperty("FreeSize", "0");
        props.setProperty("Deleted", (s.isEnabled())?"0":"1");


        try {
            FileWriter fr = new FileWriter(cloudFile);
            BufferedWriter br = new BufferedWriter(fr);
            br.write("#written by Nubsave-GUI");
            br.write("\n[NubiSaveCloudStorage]");
            String prop = props.getProperty("Name");
            br.write("\nName=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Priority");
            br.write("\nPriority=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Module");
            br.write("\nModule=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Username");
            br.write("\nUsername=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Password");
            br.write("\nPassword=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Size");
            br.write("\nSize=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("FreeSize");
            br.write("\nFreeSize=" + ((prop != null) ? prop : ""));
            prop = props.getProperty("Deleted");
            br.write("\nDeleted=" + ((prop != null) ? prop : ""));
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(CoreWriter.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    
}
