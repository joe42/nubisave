package nubisave.web;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class NativeSwingBrowser extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public NativeSwingBrowser() {
			super(new BorderLayout());
			JPanel webBrowserPanel = new JPanel(new BorderLayout());
			//webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
			final JWebBrowser webBrowser = new JWebBrowser();
			webBrowser.navigate("http://localhost/nubivis/index.html");
			webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
			add(webBrowserPanel, BorderLayout.CENTER);
		}
		public NativeSwingBrowser(String url) {
			super(new BorderLayout());
			JPanel webBrowserPanel = new JPanel(new BorderLayout());
			webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
			final JWebBrowser webBrowser = new JWebBrowser();
			webBrowser.navigate(url);
			webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
			add(webBrowserPanel, BorderLayout.CENTER);
		}	
}
