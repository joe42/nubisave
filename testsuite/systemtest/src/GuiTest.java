import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.python.modules.math;
import org.sikuli.script.*;
public class GuiTest {
	private static final String CUSTOM_SERVICE_DIALOG_OK_BTN = "img/custom_service_dialog_ok_btn.png";
	private static final String CUSTOM_SERVICE_DIALOG_FILENAME_FIELD = "img/custom_service_dialog_filename_field.png";
	private static final String OPTIONS_TAB = "img/Options_tab.png";
	private static final String OPEN_MOUNT_DIRECTORY_BTN = "img/Open_mount_directory_btn.png";
	private static final String DROPBOX_CONFIG_BTN = "img/Dropbox_User_Pass_btn.png";
	private static final String SUGARSYNC_INI_PATH = "/home/joe/Sugarsync.ini";
	private static final String DROPBOX_INI_PATH = "/home/joe/Dropbox.ini";
	private static final String ENCFS_INI_PATH = "/home/joe/workspace/nubisave/splitter/mountscripts/encfs.ini";
	private static final String DIRECTORY_INI_PATH1 = "/home/joe/directory1.ini";
	private static final String DIRECTORY_INI_PATH2 = "/home/joe/directory2.ini";
	private static final String DIRECTORY_INI_PATH3 = "/home/joe/directory3.ini";
	private static final String CUSTOM_SERVICE_DIALOG = "img/custom_service_dialog.png";
	private static final String CUSTOM_SERVICE_BTN = "img/custom.png";
	private static final String MOUNTED_CHECKBOX = "img/mounted_checkbox.png";
	private static final String MOUNTED_CHECKBOX_CHECKED = "img/mounted_checkbox_checked.png";
	private static final String HOME_DIR = System.getProperty("user.home");
	private static final String MOUNTPOINT = HOME_DIR+"/nubisavemount";
	private static final String STORAGES_DIR = HOME_DIR+"/.nubisave/storages";
	private static final String NUBISAVE_DIR = new File(System.getProperty("user.dir")).getParentFile().getParent();
	
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
			Runtime rt =  Runtime.getRuntime();
			rt.exec(new String[]{"/bin/bash", "-c", NUBISAVE_DIR+"/start.sh  &> /tmp/nubisave_log"});
			//chooseCustomService(SUGARSYNC_INI_PATH);
			//chooseCustomService(DROPBOX_INI_PATH);
			chooseCustomService(DIRECTORY_INI_PATH1);
			chooseCustomService(DIRECTORY_INI_PATH2);
			chooseCustomService(DIRECTORY_INI_PATH3);
			
			//screen.find(DROPBOX_CONFIG_BTN);
			//screen.getLastMatch().click(CONFIG_BTN);
			//screen.wait(CONFIG_DIALOG,10);
			//screen.type(null, "hello world\thohoho", 0);
			
			screen.click(MOUNTED_CHECKBOX,0);
			screen.wait(MOUNTED_CHECKBOX_CHECKED,10);
			screen.click(MOUNTED_CHECKBOX,0);
			Thread.sleep(2000);
			screen.click(MOUNTED_CHECKBOX,0);
			Thread.sleep(2000);
			screen.click(OPTIONS_TAB,0);
			Thread.sleep(1000);
			screen.click(OPEN_MOUNT_DIRECTORY_BTN,0);
			Thread.sleep(5000);
			
			rt.exec(new String[]{"/bin/bash", "-c", NUBISAVE_DIR+"/testsuite/fuse_tests.sh "+MOUNTPOINT+"/data/ mytestfile > /tmp/nubisave_systemtest_log"});
			
			Thread.sleep(15000);
			rt.exec(new String[]{"/bin/bash", "-c", "fusermount -zu "+MOUNTPOINT+"; " +
					NUBISAVE_DIR+"/start.sh"});
			Thread.sleep(5000);
			rt.exec(new String[]{"/bin/bash", "-c", "fusermount -zu "+MOUNTPOINT+"; "});
			Thread.sleep(5000);
			rt.exec(new String[]{"/bin/bash", "-c", "rm -r "+STORAGES_DIR+"/*; rm splitter/database_of_splitter.*"});
			Iterator<Match> matches = screen.findAll(MOUNTED_CHECKBOX_CHECKED);
			int waitXTimes = 10;
			while(getNrOfMatches(matches) != 2 && waitXTimes > 0){
				matches = screen.findAll(MOUNTED_CHECKBOX_CHECKED);
				waitXTimes--;
			}
			if(getNrOfMatches(matches) != 2){
				throw new NoSuchElementException("At least one store is not mounted successfully."+getNrOfMatches(matches));
			}
		} catch (FindFailed e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private int getNrOfMatches(Iterator<Match> matches){
		int matchesCnt = 0;
		while(matches.hasNext()){
			matches.next();
			matchesCnt++;
		}
		return matchesCnt;
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
