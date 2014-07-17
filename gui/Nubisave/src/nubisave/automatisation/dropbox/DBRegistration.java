package nubisave.automatisation.dropbox;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

//import org.python.modules.math; 
import org.sikuli.script.*;
public class DBRegistration {
	protected final String dbRegistrationBtn = "/home/joe/projects/eclipse_ws/nubisave_clone/nubisave/gui/Nubisave/src/images/dropbox_img/Registration_Btn.png";
	protected final String firstNameInput = "/home/joe/projects/eclipse_ws/nubisave_clone/nubisave/gui/Nubisave/src/images/dropbox_img/First_Name_Input.png";
	
	Screen screen;
	public DBRegistration(){
		screen = new Screen();
	}
	public static void main(String[] args) throws IOException, InterruptedException{
		DBRegistration test = new DBRegistration();
		test.run();
	}

	public void run() throws IOException, InterruptedException {
		try { 
			
			//new MountAll().run();
			Thread.sleep(5000);
			screen.click(dbRegistrationBtn,0);
			Thread.sleep(1000);
			if(screen.exists(firstNameInput) != null){
				screen.click(firstNameInput);
			}
			//screen.click(OPEN_MOUNT_DIRECTORY_BTN,0);
			//Thread.sleep(5000);
			//screen.rightClick(LOGO);
			//screen.click(MENU_EXIT);
			screen.type("Johannes");
			screen.type(Key.TAB);
			typeGerman("Müller");
			screen.type(Key.TAB);
			screen.type("email");
			screen.type(Key.TAB);
			screen.type("123456");
			screen.type(Key.SPACE);
			screen.click(dbRegistrationBtn,0);
		
		} catch (FindFailed e) { 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void typeGerman(String str) throws FindFailed{
		for (int i = 0; i < str.length(); i++) {
	        char c = str.charAt(i);
	        if(c == 'ü'){
	        	screen.type("[");
	        } else {
	        	screen.type(String.valueOf(c));
	        }
	    }
	}
	 
}

