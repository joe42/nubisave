/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.ui.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Desktop;
import java.io.IOException;
import java.io.File;

import nubisave.web.AbstractBrowser;
import nubisave.web.BrowserFactory;

public class SystemIntegration {
    public final static void openLocation(String location) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(location));
            } catch (Exception ex) {
                Logger.getLogger(SystemIntegration.class.getName()).log(Level.SEVERE, null, ex);

                // Fallback when desktop handlers are not available (IllegalArgumentException or IOException)
                try {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", location});
                } catch (IOException ex2) {
                    Logger.getLogger(SystemIntegration.class.getName()).log(Level.SEVERE, null, ex2);
                }
            }
        }
    }
    
    public static void openLocationbyBrowser(String location) {
    	AbstractBrowser browser =  BrowserFactory.getBrowser("DJNativeSwing");
		browser.start(location);
    }
    
    public static boolean isAvailable() {
    	return true;
    }
}
