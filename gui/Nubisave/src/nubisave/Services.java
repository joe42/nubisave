/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Persistently manages the current list of StorageServices. They can be added, removed, iterated over and retrieved by index or name.
 */
public class Services implements Iterable<StorageService>{

    private List<StorageService> mmServices;

    public Services() {
        mmServices = new LinkedList<StorageService>();
    }

    /**
     * Add a new StorageService instance to the list.
     * @param newService the instance to add
     */
    public void add(StorageService newService){
        mmServices.add(newService);
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
     * Get a StorageService instance with the given name
     * @param name the name of the element to retrieved
     * @retu a StorageService instance with the given name
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
