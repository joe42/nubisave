/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

/**
 *
 * @author demo
 */
public class StorageService {
    private boolean enabled;
    private String name;
    
    public StorageService(String name) {
        this.name = name;
        enabled = true;
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
}
