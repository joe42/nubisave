/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import com.github.joe42.splitter.util.file.PropertiesUtil;
import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.ini4j.Ini;

/**
 * Persistently manages the current list of StorageServices. They can be added, removed, iterated over and retrieved by index or name.
 */
public class Services implements Iterable<StorageService>{

    private List<StorageService> mmServices;
    private String database_directory;

    public Services() {
        mmServices = new LinkedList<StorageService>();
        database_directory = new PropertiesUtil("nubi.properties").getProperty("splitter_configuration_directory");
        File dir = new File(database_directory);
        dir.mkdirs();
        if(dir.isDirectory()){
            String service_name, unique_name_of_service;
            for(String file: dir.list()){
                unique_name_of_service = file;
                service_name = unique_name_of_service.split("[0-9]")[0]; // remove number
                StorageService newService = new StorageService(new File(dir.getPath()+"/"+file));
                newService.setName(service_name);
                newService.setUniqName(unique_name_of_service);
                mmServices.add(newService);
            }
        }
    }

    /**
     * Add a new StorageService instance to the list.
     * @param newService the instance to add
     */
    public void add(StorageService newService){
        mmServices.add(newService);
        try{
            Ini serviceIni = newService.getConfig();
            int serviceIndex = 1;
            for(StorageService s: newService.getBackendServices()){
                    serviceIni.put("parameter", "backendservice"+serviceIndex++, s.getUniqName());
                }
                if(newService.isBackendModule()){
                    serviceIni.put("splitter", "isbackendmodule", true);
            }
            serviceIni.store(new File(database_directory+"/"+newService.getUniqName()));
        } catch(Exception e){
            System.err.println("Services instance add(StorageService newService): Error storing configuration for StorageService instance "+newService.getUniqName()+" - "+e.getMessage()==null?e.getMessage():"");
            return;
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
     * @param i index of the element to remove
     * @throws IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size())
     */
    public void remove(int i) {
        mmServices.remove(i);
    }
}
