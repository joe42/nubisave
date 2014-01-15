package nubisave.web;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class DJNativeSwingBrowser implements AbstractBrowser{

	@Override
	public void start(final String url) {
		NativeInterface.open();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {       
				JFrame frame = new JFrame("NubiSave Web Browser");
				//frame.setUndecorated(true);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new DJNativeSwing(url),BorderLayout.CENTER);
				frame.setSize(800, 600);
				frame.setMenuBar(null);
				frame.setLocationByPlatform(true);
				frame.setVisible(true);
			}
		});
		NativeInterface.runEventPump();
		
	}
	
	class DJNativeSwing extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public DJNativeSwing() {
			super(new BorderLayout());
			JPanel webBrowserPanel = new JPanel(new BorderLayout());
			//webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
			final JWebBrowser webBrowser = new JWebBrowser();
			webBrowser.navigate("http://localhost/nubivis/index.html");
			webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
			add(webBrowserPanel, BorderLayout.CENTER);
		}
		public DJNativeSwing(String url) {
			super(new BorderLayout());
			JPanel webBrowserPanel = new JPanel(new BorderLayout());
			webBrowserPanel.setBorder(BorderFactory.createTitledBorder("Native Web Browser component"));
			final JWebBrowser webBrowser = new JWebBrowser();
			webBrowser.navigate(url);
			webBrowserPanel.add(webBrowser, BorderLayout.CENTER);
			add(webBrowserPanel, BorderLayout.CENTER);
		}	
	}
	
}
