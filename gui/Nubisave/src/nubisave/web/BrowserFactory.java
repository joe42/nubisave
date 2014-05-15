package nubisave.web;

import nubisave.web.impl.DJNativeSwingBrowser;
import nubisave.web.interfaces.Browser;

public class BrowserFactory {
	
	public static Browser getBrowser(String name){
		switch (name){
		case "DJNativeSwing":
			return new DJNativeSwingBrowser();
		default:
			return new DJNativeSwingBrowser();
		}
	}

}
