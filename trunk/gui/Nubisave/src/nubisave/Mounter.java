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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import nubisave.ui.MainWindow;

/**
 *
 * @author demo
 */
public class Mounter {

    private File storagesDir;
    private File splitter = new File(Properties.getProperty("splitter"));
    private File cloudfusionDir = new File(Properties.getProperty("cloudfusion"));

    public Mounter() {
        System.out.println(System.getProperty("user.dir"));
        storagesDir = new File(home() + "/.cache/nubisave/storages");
    }

    private String home() {
        return System.getProperty("user.home");
    }

    public void mount() {
        mountServices();

        String mntPointStr = Properties.getProperty("mntPoint");
        if (mntPointStr == null) {
            JOptionPane.showMessageDialog(null, "Error mntPoint: " + mntPointStr, "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String redundancyStr = Properties.getProperty("redundancy");
        if (redundancyStr == null) {
            JOptionPane.showMessageDialog(null, "Error redundancy: " + redundancyStr, "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File mntPoint = new File(mntPointStr);
        if (!mntPoint.isDirectory()) {
            JOptionPane.showMessageDialog(null, mntPoint.getPath() + " isn't a directory!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!mntPoint.exists()) {
            JOptionPane.showMessageDialog(null, mntPoint.getPath() + " doesn't exists!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (mntPoint.list().length != 0) {
            JOptionPane.showMessageDialog(null, mntPoint.getPath() + " isn't empty!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //TODO: don't hardcode splitterDir
        if (!splitter.exists()) {
            JOptionPane.showMessageDialog(null, "Couldn't find Splitter module: " + splitter.getPath(), "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String homePath = System.getProperty("user.home");

        if (!storagesDir.exists()) {
            System.err.println(storagesDir.getPath() + " doesn't exists!");
            if (storagesDir.mkdirs()) {
                System.out.println("Created:" + storagesDir.getPath());
            } else {
                JOptionPane.showMessageDialog(null, "Couldn't create " + storagesDir.getPath(), "Mount Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        //                splitter_mount.sh ~/nubisave/ ~/.cache/nubisave/storages [redundant_files]
        String mountCmd = splitter.getPath() + " " + mntPoint.getPath() + " " + storagesDir.getPath() + " " + redundancyStr;
        System.out.println("exec: " + mountCmd);
        try {
            Process p = Runtime.getRuntime().exec(mountCmd, null, splitter.getParentFile());

        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Exception: " + ex.getMessage(), "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (isMounted(mntPoint)) {
            JOptionPane.showMessageDialog(null, "Nubisave mounted successfull!", "Mount success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Error while mount!", "Mount Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mountServices() {
        for (MatchmakerService s : Nubisave.services.getMmServices()) {
            mountMatchmakerService(s);
        }

        for (CustomMntPoint cmp : Nubisave.services.getCstmMntPnts()) {
            mountCustom(cmp);
        }
    }

    public void mountService(StorageService service) {
        switch (service.getType()) {
            case MATCHMAKER:
                mountMatchmakerService((MatchmakerService) service);
                break;
            case CUSTOM:
                break;
            default:
                throw new UnsupportedOperationException("Not yet implemented");
        }
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

    public boolean isMounted(File mntPoint) {
        if (mntPoint == null) {
            return false;
        }

        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader("/etc/mtab"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (line.contains(mntPoint.getAbsolutePath())) {
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return false;
    }

    private void mountMatchmakerService(MatchmakerService service) {
        if (!service.isSupported() || !service.isEnabled()) {
            return;
        }

        if (service.getName() == null || service.getUser() == null || service.getPass() == null) {
            return;
        }
        File mntPoint = new File(storagesDir + "/" + service.getUniqName());
        mntPoint.mkdirs();

        String cmd = "python -m cloudfusion.main" + " "
                + mntPoint.getAbsolutePath() + " "
                + service.getName() + " "
                + service.getUser() + " "
                + service.getPass() + " "
                + "cache";
        System.out.println("exec: " + cmd);
        try {
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            builder.directory(cloudfusionDir.getParentFile());
            if (builder.start().waitFor() != 0) {
                System.err.println(cmd + " failed!");
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(Mounter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mounter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void mountCustom(CustomMntPoint cmp) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}