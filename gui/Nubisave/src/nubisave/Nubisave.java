/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.jb2011.lnf.beautyeye.BeautyEyeLNFHelper;

/**
 * 
 * @author demo
 */
public class Nubisave {

	public static Services services = null;

	public static Splitter mainSplitter = null;

	public static PropertiesUtil properties = null;

	private static void initalizeServices() {
		Nubisave.services = new Services();
		String database_directory = Nubisave.properties.getProperty("splitter_configuration_directory");
		Nubisave.services.initializeEntranceComponent();
		Nubisave.services.loadFromDataBase(database_directory);
	}

	private static void initalizeSplitter(String mountPoint) {
		if (mountPoint.equals("")) {
			Nubisave.mainSplitter = new Splitter();
		} else {
			Nubisave.mainSplitter = new Splitter(mountPoint);
		}
		String redundancy = Nubisave.properties.getProperty("redundancy");
		
		// As 3rd party class com.github.joe42.splitter.util.file.PropertiesUtil may return null pointer, we have to check here.
		Nubisave.mainSplitter.setRedundancy(Integer.parseInt((String) (redundancy!=null ? redundancy : "-1")));
	}

	private static void initalizeProperties() {
		
		// An unsolved problem is when file "nubi.properties" is missing, PropertiesUtil Constructor still succeed. 
		Nubisave.properties = new PropertiesUtil("nubi.properties");
	}

	private static void initalizeTheme() {
		
		try {
			BeautyEyeLNFHelper.frameBorderStyle = BeautyEyeLNFHelper.FrameBorderStyle.osLookAndFeelDecorated;
			BeautyEyeLNFHelper.launchBeautyEyeLNF();
			UIManager.put("RootPane.setupButtonVisible", false);
			UIManager.put("TabbedPane.tabAreaInsets", new javax.swing.plaf.InsetsUIResource(3, 3, 2, 20));
		} catch (Exception ex) {
			Logger.getLogger(Nubisave.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		
		if (args.length == 1) {
			if (args[0].equals("-h") || args[0].equals("--help")) {
				System.out.println("Syntax: ");
				System.exit(0);
			}
		}
		
		initalizeProperties();
		initalizeSplitter(args.length >= 1 ? args[0] : "");
		initalizeServices();
		initalizeTheme();
		// new CoreReader().readExistingServices();

		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new nubisave.ui.MainWindow().setVisible(true);
			}
		});
	}
}
