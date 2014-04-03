package nubisave.web;

public class BrowserFactory {
	
	public static AbstractBrowser getBrowser(String name){
		switch (name){
		case "DJNativeSwing":
			return new DJNativeSwingBrowser();
		default:
			return new DJNativeSwingBrowser();
		}
	}

}
