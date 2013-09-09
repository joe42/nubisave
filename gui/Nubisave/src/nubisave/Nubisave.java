/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import com.github.joe42.splitter.util.file.PropertiesUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author demo
 */
public class Nubisave {

    public static Services services;
    public static Splitter mainSplitter;
    public static PropertiesUtil properties;
    public Nubisave(){
        services = new Services();
        String database_directory = new PropertiesUtil("nubi.properties").getProperty("splitter_configuration_directory");
        services.loadFromDataBase(database_directory);
    }

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
        if (args.length == 1) {
            if(args[0].equals("-h") || args[0].equals("--help")) {
                System.out.println("Syntax: ");
                System.exit(0);
            }
        }
        properties = new PropertiesUtil("nubi.properties");
        if(args.length < 1) {
            Nubisave.mainSplitter = new Splitter();
        } else {
            String splitterMountpoint = args[0];
            Nubisave.mainSplitter = new Splitter(splitterMountpoint);
        }
        Nubisave.mainSplitter.setRedundancy(Integer.parseInt(properties.getProperty("redundancy")));
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
