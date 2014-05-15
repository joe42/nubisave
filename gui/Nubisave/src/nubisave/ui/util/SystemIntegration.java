/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.ui.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import nubisave.component.graph.splitteradaption.NubisaveEditor;
import nubisave.web.BrowserFactory;
import nubisave.web.NubiSaveWeb;
import nubisave.web.interfaces.Browser;

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
    
    public final static void openLocationbyBrowser(final String location) {
		if(NubisaveEditor.browser == null) {
			NubisaveEditor.browser = new NubiSaveWeb();
		}
		Thread browserThread = new Thread() {
		      public void run() {
		    	  NubisaveEditor.browser.openURL(location);
		      }
		};
		browserThread.start();
    }
    
    public final static boolean isAvailable() {
    	return true;
    }
}
