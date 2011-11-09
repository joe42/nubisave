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
    private List<AgreementService> aServices;

    public Services() {
        mmServices = new LinkedList<StorageService>();
        aServices = new LinkedList<AgreementService>();
    }

    public List<AgreementService> getAServices() {
        return aServices;
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

    public StorageService get(int i) {
        if (i < 0) {
            return null;
        }
        int size = aServices.size() + mmServices.size();
        if (i >= size) {
            return null;
        }
        if (i < mmServices.size()) {
            return mmServices.get(i);
        } else { // Object is AgreementService
            i -= mmServices.size() - 1;
            return aServices.get(i);
        } 
    }

    public void remove(int i) {
        if (i < 0) {
            return;
        }
        int size = aServices.size() + mmServices.size();
        if (i >= size) {
            return;
        }
        

        if (i < mmServices.size()) {
            mmServices.remove(i);
        } else { // Object is AgreementService
            i -= mmServices.size() - 1;
            aServices.remove(i);
        } 

    }
}
