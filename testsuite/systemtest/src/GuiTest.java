import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.python.modules.math;
import org.sikuli.script.*;
public class GuiTest extends Constants {

	
	Screen screen;
	public GuiTest(){
		screen = new Screen();
	}
	public static void main(String[] args) throws IOException, InterruptedException{
		GuiTest test = new GuiTest();
		test.run();
	}

	public void run() throws IOException, InterruptedException {
		try {
			//chooseCustomService(SUGARSYNC_INI_PATH);
			//chooseCustomService(DROPBOX_INI_PATH);
			chooseCustomService(DIRECTORY_INI_PATH1);
			chooseCustomService(DIRECTORY_INI_PATH2);
			chooseCustomService(DIRECTORY_INI_PATH3);
			
			new MountAll().run();
			
			//screen.click(OPTIONS_TAB,0);
			//Thread.sleep(1000);
			//screen.click(OPEN_MOUNT_DIRECTORY_BTN,0);
			//Thread.sleep(5000);
			screen.rightClick(LOGO);
			screen.click(MENU_EXIT);
			
			
		} catch (FindFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void chooseCustomService(String serviceConfigurationFilePath) throws FindFailed {
		/**Adds the custom service to the services table specified by the image located at the path service. */
		screen.wait(CUSTOM_SERVICE_BTN,10);
		screen.click(CUSTOM_SERVICE_BTN, 0);
		screen.wait(CUSTOM_SERVICE_DIALOG,10);
		screen.click(CUSTOM_SERVICE_DIALOG_FILENAME_FIELD, 0);
		screen.type(null, "\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b\b", 0);
		screen.paste(serviceConfigurationFilePath);
		screen.click(CUSTOM_SERVICE_DIALOG_OK_BTN, 0);
		screen.wait(CUSTOM_SERVICE_BTN,10);
	}
}
