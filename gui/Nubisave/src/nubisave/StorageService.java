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
    private boolean enabled;
    private boolean supported;
    
    private String name;
    private String uniqName;
    private StorageType type;
    
    public StorageService(String name) {
        this.name = name;
        uniqName = name + new Random().nextInt(10000000);
        enabled = false;
        supported = false;
        for (String s :Nubisave.supportedProvider) {
            if (s.equalsIgnoreCase(name)) {
                supported = true;
                enabled = true;
                break;
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
