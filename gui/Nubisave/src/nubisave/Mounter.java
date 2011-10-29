/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import nubisave.ui.MainWindow;
import org.ini4j.*;

/**
 *
 * @author demo
 */
public class Mounter {

    private final String splitterMountpointPath = Properties.getProperty("splitter_mountpoint");
    private File splitterMountpoint;

    public Mounter() {
        System.out.println("splitter mountpoint: "+splitterMountpointPath);
        splitterMountpoint = new File(splitterMountpointPath);
    }

    private String home() {
        return System.getProperty("user.home");
    }

    public void configureSplitter(){
        /**Sets the redundancy level for Splitter module**/
        String redundancyStr = Properties.getProperty("redundancy");
        if (redundancyStr == null) {
            JOptionPane.showMessageDialog(null, "Error redundancy: " + redundancyStr, "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String path = splitterMountpointPath + "/config/config";
        try{
            Ini splitterConfig = new Ini(new File(path));
            splitterConfig.put("splitter", "redundancy", Properties.getProperty("redundancy"));
            splitterConfig.store();
        } catch(Exception e){
            System.out.println("failed to configure splitter:");
            e.printStackTrace();
        }

    }

    public void mount() {
        if (!splitterMountpoint.isDirectory()) {
            JOptionPane.showMessageDialog(null, splitterMountpoint.getPath() + " isn't a directory!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!splitterMountpoint.exists()) {
            JOptionPane.showMessageDialog(null, splitterMountpoint.getPath() + " doesn't exists!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        
        mountServices();
    }

    public void mountServices() {
  

        /*for (CustomMntPoint cmp : Nubisave.services.getCstmMntPnts()) {
            mountCustom(cmp);
        }*/
    }

    public void mountService(StorageService service) {
        Nubisave.mainSplitter.mountStorageModule((MatchmakerService) service);
    }

    public void umountFuse(File mntPoint) {
        String cmd = "fusermount -u " + mntPoint.getAbsolutePath();
        try {
            int exitValue;
            try {
                System.out.println("exec: " + cmd);
                exitValue = Runtime.getRuntime().exec(cmd).waitFor();
            } catch (InterruptedException ex) {
                Logger.getLogger(Mounter.class.getName()).log(Level.SEVERE, null, ex);
                exitValue = 1;
            }

            if (exitValue == 0) {
                JOptionPane.showMessageDialog(null, mntPoint.getAbsoluteFile() + "successfully unmounted.", "Unmount success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            Logger.getLogger(Mounter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Unmount Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void mountCustom(CustomMntPoint cmp) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void umountService(StorageService service) {
        File serviceConfig = new File(splitterMountpointPath + "/config/" + service.getUniqName());
        serviceConfig.delete();
        //umountFuse(mntPoint);
        //mntPoint.delete();
    }
}