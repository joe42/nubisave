/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author demo
 */
public class Nubisave {

    public static final Services services = new Services();
    
    public static String[] supportedProvider = {"Dropbox","Sugarsync"};

    public static Splitter mainSplitter;
    
    private void initalize() {
        // look like a native app
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Nubisave.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Nubisave.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Nubisave.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(Nubisave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Must specify mountpoint of splitter module");
            System.exit(-1);
        }
        String splitterMountpoint = args[0];
        Nubisave.mainSplitter = new Splitter(splitterMountpoint);
        Nubisave.mainSplitter.setRedundancy(Integer.parseInt(Properties.getProperty("redundancy")));
        Nubisave nubi = new Nubisave();
        nubi.initalize();
        //new CoreReader().readExistingServices();
        
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new nubisave.ui.MainWindow().setVisible(true);
            }
        });
    }
}
