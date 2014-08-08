/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;


import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ini4j.Ini;

/**
 * Persistently manages the current list of StorageServices. They can be added, removed, iterated over and retrieved by index or name.
 */
public class Services implements Iterable<StorageService>{

    private List<StorageService> mmServices;
    private String database_directory;
    private String storage_directory;

    public Services() {
        mmServices = new LinkedList<StorageService>();
        database_directory = Nubisave.properties.getProperty("splitter_configuration_directory");
        storage_directory= Nubisave.properties.getProperty("storage_configuration_directory");
    }

    /**Load services from database
     * Adds new services from the database and reloads existing ones
     * @param database_directory
     */
    public void loadFromDataBase(String database_directory){
        if(database_directory == null) {
            System.err.println("warning: could not load database directory");
            return;
        }
        
        File dir = new File(database_directory);
        for(int j=0;j<2;j++){
            dir.mkdirs();
            if(dir.isDirectory()){
                String service_name, unique_name_of_service;
                for(String file: dir.list()){
                    if(file.endsWith(".txt")) continue;
                    if(new File(dir.getPath() + "/" + file).isDirectory()) continue;
                    unique_name_of_service = file;
                    service_name = unique_name_of_service.split("[0-9]")[0]; // remove number
                    if(getByUniqueName(unique_name_of_service) == null){
                        StorageService newService = new StorageService(new File(dir.getPath()+"/"+file));
                        newService.setName(service_name);
                        newService.setUniqName(unique_name_of_service);
                        mmServices.add(newService);
                    }
                }

                //Configure services again so that backend modules, which did not exist when the constructor was called, can be added
                for(String file: dir.list()){
                    if(file.endsWith(".txt")) continue;
                    if(new File(dir.getPath() + "/" + file).isDirectory()) continue;
                    unique_name_of_service = file;
                    try {
                        getByUniqueName(unique_name_of_service).setConfig(new Ini(new File(dir.getPath() + "/" + file)));
                    } catch (IOException ex) {
                        Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            dir=new File(storage_directory);
         }
    }

    /**
     * Add a new StorageService instance to the list and persist it.
     * @param newService the instance to add
     */
    public void add(StorageService newService){
        mmServices.add(newService);
        newService.storeConfiguration(database_directory);
    }
    
    /**
     * Add a new Nubisave StorageService instance to the list and persist it. 
     * @param newService 
     */
    
    public void addNubisave(StorageService newService){
        mmServices.add(newService);
        newService.storeConfiguration(storage_directory);
    }
    
    /**
     * Add a new StorageService instance to the list and persist it.
     * @param newService the instance to add
     * @param index the position to add the service to
     */
    public void addCloudEntrance(StorageService newService){
        mmServices.add(newService);
        newService.storeConfiguration(storage_directory);
    }

    /**
     * Add a new StorageService instance to the list and persist it.
     * @param newService the instance to add
     * @param index the position to add the service to
     */
    public void add(StorageService newService, int index){
        mmServices.add(index, newService);
        newService.storeConfiguration(database_directory);
    }

    /**
     * Add a new StorageService instance to the list and persist it.
     * @param newService the instance to add
     * @param index the position to add the service to
     */
    public int getIndexByUniqueName(String uniqueName) {
        for(int i=0; i<mmServices.size(); i++){
            if(mmServices.get(i).getUniqName().equals(uniqueName)){
                return i;
            }
        }
        return -1;
    }

    /**
     * Persists changes to an existing service.
     * @param existingService the instance to persist
     */
    public void update(StorageService existingService){
        if(existingService.getName().toLowerCase().equals("nubisave") || existingService.getName().toLowerCase().equals("cloudentrance")){
            existingService.storeConfiguration(storage_directory);
        }
        else {
             existingService.storeConfiguration(database_directory);
        }
    }
    
    public void updateNubisave(StorageService existingService){
        existingService.storeConfiguration(storage_directory);
    }
    
    public void updateCloudEntrance(StorageService existingService){
        existingService.storeConfiguration(storage_directory);
    }

    /**
     * Persists the current services to a directory
     * @param database_directory path to a directory (i.e. "/mydb")
     */
    public void storeToDatabase(String database_directory){
        for(StorageService s: nubisave.Nubisave.services){
            if(s.getName().toLowerCase().equals("nubisave") || s.getName().toLowerCase().equals("cloudentrance")){
                s.storeConfiguration(storage_directory);
            }
            else {
                s.storeConfiguration(database_directory);
        }
     }
    }

    /**
     * Persists changes to the existing services.
     * Sets the correct isBackendModule value for each service.
     */
    public void update(){
        for(StorageService s1: nubisave.Nubisave.services){
            s1.setBackendModule(false);
            for(StorageService s2: nubisave.Nubisave.services){
                if(s2.getBackendServices().contains(s1)){
                    s1.setBackendModule(true);
                }
            }
            update(s1);
        }
    }

    /**
     * Returns the number of StorageService instances
     * @return the number of StorageService instances
     */
    public int size(){
        return mmServices.size();
    }

    /**
     * Returns an iterator over the StorageService instances.
     * @return an iterator to use in foreach loops
     */
    public Iterator<StorageService> iterator() {
        return mmServices.iterator();
    }

    /**
     * Get the StorageService instance with the given unique name
     * @param uniqueName the unique name of the element to retrieved
     * @return the StorageService instance with the given name
     */
    public StorageService getByUniqueName(String uniqueName) {
        for(StorageService s: mmServices){
            if(s.getUniqName().equals(uniqueName)){
                return s;
            }
        }
        return null;
    }

    /**
     * Get a StorageService instance with the given name
     * @param name the name of the element to retrieved
     * @return a StorageService instance with the given name
     */
    public StorageService get(String name) {
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
     * @return the StorageService instance at index i
     * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
     */
    public StorageService get(int i) {
        return mmServices.get(i);
    }

    /**
     * Remove the StorageService instance at position i. Shifts any subsequent elements to the left (subtracts one from their indices).
     * Unpersists the store.
     * @param i index of the element to remove
     * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
     */
    public void remove(int i) {
        new File(database_directory+"/"+mmServices.get(i).getUniqName()).delete();
        mmServices.remove(i);
    }

    /**
     * Remove the StorageService instance service. Shifts any subsequent elements to the left (subtracts one from their indices).
     * Unpersists the store.
     * @param service
     */
    public void remove(StorageService service) {
        if( mmServices.remove(service) ) {
            new File(database_directory+"/"+service.getUniqName()).delete();
        }
    }
}    