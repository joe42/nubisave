package nubisave.web;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import nubisave.ui.util.SystemIntegration;
import nubisave.web.interfaces.Browser;
import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;


public class NubiSaveWeb extends javax.swing.JDialog{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean isInitialized;
	
	private Browser browser = null;
	
	private String defaultBrowserName = null;
	
	public NubiSaveWeb() {
		defaultBrowserName = getDefaultBrowserName();
		if(!defaultBrowserName.equals(""))
			initialize();
	}

	public void openURL(final String url) {
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				NubiSaveWeb dialog = new NubiSaveWeb();
				JPanel buttonPanel = new JPanel();
				JButton buttonBrowser = new JButton("System browser");
				JButton buttonReload = new JButton("Reload page");
				buttonBrowser.addActionListener(new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
			          // open system default web browser
			        	SystemIntegration.openLocation(url.toString());
//			        	SystemIntegration.openLocationbyBrowser(url.toString());
			        }
			      });
				buttonReload.addActionListener(new ActionListener() {
			        public void actionPerformed(ActionEvent e) {
			        	browser.reloadPage();
			        }
			      });
				buttonPanel.add(buttonReload);
				buttonPanel.add(buttonBrowser);
				browser.browseTo(url);
				
				dialog.add((JPanel)browser,BorderLayout.CENTER);
				dialog.add(buttonPanel,BorderLayout.SOUTH);
				dialog.setVisible(true);
			}
		});
		NativeInterface.runEventPump();
	}

	public void initialize() {
		if (!isInitialized) {
			this.browser =  BrowserFactory.getBrowser(defaultBrowserName);
//			this.browser.initialize();
			setStyle();
//			setButtons();
//			setLayout();
			isInitialized = true;
			}
	}
	
	public void setStyle() {
		this.setLayout(new BorderLayout());
		this.setTitle("NubiVis - Visualization of distributed data.");
		this.setSize(800, 600);
	}
	
	public String getDefaultBrowserName() {
		String browserConfig = readBrowserConfig();
		if(!browserConfig.equals("")) {
			return browserConfig;
		}
		return "";
	}
	
	public String readBrowserConfig() {
		return "DJNativeSwing";
	}
}