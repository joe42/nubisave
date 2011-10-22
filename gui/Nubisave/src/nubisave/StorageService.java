/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.util.Random;

/**
 *
 * @author demo
 */
public class StorageService {
    private boolean supported;


    private String user;
    private String pass;
    private String name;
    private String uniqName;
    private StorageType type;
    
    public StorageService(String name) {
        this.name = name;
        uniqName = name + new Random().nextInt(10000000);
        supported = false;
        for (String s :Nubisave.supportedProvider) {
            if (s.equalsIgnoreCase(name)) {
                supported = true;
                break;
            }
        }
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setSupported(boolean supported) {
        this.supported = supported;
    }
 
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isSupported() {
        return supported;
    }

    public void setType(StorageType type) {
        this.type = type;
    }

    public StorageType getType() {
        return type;
    }

    public String getUniqName() {
        return uniqName;
    }

    public void setUniqName(String uniqName) {
        this.uniqName = uniqName;
    }

    
}
