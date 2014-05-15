package nubisave.web.impl;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import nubisave.web.interfaces.Browser;
import chrriis.dj.nativeswing.NSComponentOptions;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class DJNativeSwingBrowser extends JPanel implements Browser{
	private static final long serialVersionUID = 1L;
	private JWebBrowser webBrowser = null;
	public DJNativeSwingBrowser() {
		super(new BorderLayout());
		initialize();
	}
	
	public void initialize() {
		NativeSwing.initialize();
		setEngine();
		setStyle();
		setLayout();
		
	}
	
	public void setEngine() {
		webBrowser  = new JWebBrowser(NSComponentOptions.destroyOnFinalization());
	}
	
	public void setLayout() {
		JPanel webBrowserPanel = new JPanel(new BorderLayout());
		webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
		add(webBrowserPanel, BorderLayout.CENTER);
	}
	
	public void setStyle() {
		webBrowser.setBarsVisible(false);
	}
	
	public void browseTo(String url) {
		webBrowser.navigate(url.toString());
	}
	
	public void reloadPage() {
		webBrowser.reloadPage();
	}
}

