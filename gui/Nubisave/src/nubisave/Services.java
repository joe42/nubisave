/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author demo
 */
public class Services {

    private List<StorageService> mmServices;

    public Services() {
        mmServices = new LinkedList<StorageService>();
    }

    public List<StorageService> getMmServices() {
        return mmServices;
    }

    public StorageService getMmService(String name) {
        for(StorageService s: mmServices){
            if(s.getName().equals(name)){
                return s;
            }
        }
        return null;
    }

    /**
     * Get the StorageService instance at position i
     * @param i index
     * @return the StorageService instance at index i or null if there is no such instance at this position
     */
    public StorageService get(int i) {
        if (i < 0 || i >= mmServices.size()) {
            return null;
        }
        return mmServices.get(i);
    }

    /**
     * Remove the StorageService instance at position i
     * @param i index
     */
    public void remove(int i) {
        if (i < 0 || i >= mmServices.size()) {
            return;
        }
        mmServices.remove(i);
    }
}
