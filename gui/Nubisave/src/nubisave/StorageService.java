/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.ini4j.Ini;

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
    private int nrOfBackends;
    private boolean isBackendModule;
    private LinkedList<StorageService> backendServices;
    
    public StorageService(String name) {
        this.name = name;
        uniqName = name + new Random().nextInt(10000000);
        supported = false;
        nrOfBackends = 0;
        backendServices = new LinkedList<StorageService>();
        for (String s :Nubisave.supportedProvider) {
            if (s.equalsIgnoreCase(name)) {
                supported = true;
                break;
            }
        }
    }

    public StorageService(File file) {
        /**
         * @param file an ini configuration file for a custom service
         */
            name = file.getName().split("\\.")[0]; //use the filename as a service name
            uniqName = name + new Random().nextInt(10000000);
            type = StorageType.CUSTOM;
            supported = true;
            nrOfBackends = 0;
            backendServices = new LinkedList<StorageService>();
            try{
                Ini config = new Ini(file);
                nrOfBackends = config.get("gui", "nrofbackends", Integer.class);
                isBackendModule = config.get("splitter", "isbackendmodule", Boolean.class);
            } catch(IOException e){
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
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

    public int getNrOfBackends() {
        /**
         * @return the number of local backends for this service
         */
        return nrOfBackends;
    }

    public boolean isBackendModule() {
        /**
         * @return true iff this storage service is used as a backend for an other service
         */
        return isBackendModule;
    }

    public void setBackendModule(boolean isBackendModule) {
        /**
         * @param isBackendModule iff true set this storage service to be used as a backend for an other service
         */
        this.isBackendModule = isBackendModule;
    }

    public List<StorageService> getBackendServices() {
        return this.backendServices;
    }

    public void addBackendService(StorageService service) {
        /**
         * Adds service as a backend service.
         * Then removes the backend service added before all others if more than #getNrOfBackends() backendservices have been added.
         * If service is already a backend service it is not added.
         */
        if(backendServices.contains(service)){
            return;
        }
        backendServices.add(service);
        if(nrOfBackends < backendServices.size()){
            backendServices.removeFirst();
        }
    }

    public void removeBackendService(StorageService service) {
        /**
         * Removes service from this services list of backend services.
         */
        backendServices.remove(service);
    }
    
}
