package nubisave.web.test;

import java.util.Calendar;

import nubisave.ui.util.SystemIntegration;
import nubisave.web.*;

public class NativeSwingTest {
	public static void testTime() {
		NubiSaveWeb browser =  new NubiSaveWeb();
		Calendar cal = Calendar.getInstance();
		int second = cal.get(cal.SECOND);
		int miliSecond = cal.get(cal.MILLISECOND);
		System.out.println(second);
		System.out.println(miliSecond);
		miliSecond = second*1000+miliSecond;
		SystemIntegration.openLocation("http://localhost/index.html?time="+miliSecond);
//		SystemIntegration.openLocationbyBrowser("http://localhost/index.html?time="+miliSecond);
//		browser.openURL("http://localhost/index.html?time="+miliSecond);
		
	}

	public static void main(String[] args) {
//		NubiSaveWeb browser =  new NubiSaveWeb();
//		browser.openURL("http://localhost/nubivis/index.html");
		testTime();
	}

}
