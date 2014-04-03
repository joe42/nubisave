package nubisave.web.test;

import nubisave.web.*;

public class NativeSwingTest {

	public static void main(String[] args) {
		AbstractBrowser browser =  BrowserFactory.getBrowser("DJNativeSwing");
		browser.start("http://localhost/nubivis/index.html");
	}

}
