/*
 * Representation to abstract configuration of the actual Splitter module.
 */

package nubisave;

import com.github.joe42.splitter.backend.BackendService;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ini4j.Ini;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * @author joe
 */
public class Splitter {
    private final String splitterMountpoint;
    private final String configurationDirPath;
    private final String configurationFilePath;
    private final String dataDir;
    public Splitter(String splitterMountpoint){
        this.splitterMountpoint = splitterMountpoint;
        this.configurationDirPath = splitterMountpoint+"/config";
        this.configurationFilePath = splitterMountpoint+"/config/config";
        this.dataDir = splitterMountpoint+"/data";
    }
    public String getMountpoint(){
        return splitterMountpoint;
    }
    public String getDataDir(){
        return dataDir;
    }
    /**
     * Mount the Splitter module.
     * Waits at most 10 seconds until the splitter is mounted.
     **/
    public void mount(){
        try {
            new ProcessBuilder("/bin/bash", "-c", "../start.sh headless > /home/joe/headless").start();
            int timeUsed = 0;
            while(!isMounted() && timeUsed < 1000*10){
                    try {
                            Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    timeUsed += 500;
            }
        } catch (IOException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**Unmount the Splitter module.
     * Waits at most 10 seconds until the splitter is unmounted.
     **/
    public void unmount(){
        try {
            new ProcessBuilder("/bin/bash", "-c", "../splitter/unmount.sh").start();
            int timeUsed = 0;
            while(isMounted() && timeUsed < 1000*10){
                    try {
                            Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    timeUsed += 500;
            }
        } catch (IOException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public boolean isMounted(){
        return new File(configurationFilePath).exists();
    }
    public boolean isModuleMounted(StorageService s){
        /**Determines if a module is mounted by checking if the configuration file exists.**/
        String path = configurationDirPath + "/" + s.getUniqName();
        return new File(path).exists();
    }
    public void mountStorageModule(StorageService service){
        for(StorageService backendStore: service.getBackendServices()){
            backendStore.storeConfiguration(configurationDirPath);
        }
        service.storeConfiguration(configurationDirPath);
    }
    public void unmountStorageModule(StorageService service){
        String path = configurationDirPath + "/" + service.getUniqName();
        new File(path).delete();
    }
    public void setRedundancy(int redundancy){
        /**Sets the redundancy level for Splitter module**/
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            splitterConfig.put("splitter", "redundancy", redundancy);
            splitterConfig.store();
        } catch(Exception e){
            System.err.println("Splitter.setRedundancy(int redundancy): Failed to configure Splitter "+" - "+e.getMessage()==null?e.getMessage():"");
        }
    }
    /**Gets the redundancy level for Splitter module**/
    public int getRedundancy(){
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            return Integer.parseInt(splitterConfig.get("splitter", "redundancy"));
        } catch(Exception e){
            return 100;
        }
    }

    /**Sets the storage strategy for Splitter module**/
    public void setStorageStrategy(String storageStrategy) {
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            splitterConfig.put("splitter", "storagestrategy", storageStrategy);
            splitterConfig.store();
        } catch(Exception e){
            System.err.println("Splitter.setStorageStrategy(String storageStrategy): Failed to configure Splitter "+" - "+e.getMessage()==null?e.getMessage():"");
        }
    }

    /**Gets the storage strategy from the Splitter module**/
    public String getStorageStrategy() {
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            return splitterConfig.get("splitter", "storagestrategy");
        } catch(Exception e){
            return "RoundRobin";
        }
    }

    /**
     * Gets the storage configuration file from the splitter module
     * @param uniqueStorageName unique name of the storage service
     * @return a file representing the storage services configuration in the splitter module
     */
    public File getConfigFile(String uniqueStorageName) {
        try{
            return new File(configurationDirPath+"/"+uniqueStorageName);
        } catch(Exception e){
            return null;
        }
    }

    /**Gets the availability from the Splitter module**/
    public double getAvailability() {
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            return splitterConfig.get("splitter", "availability", Double.class);
        } catch(Exception e){
            return 0;
        }
    }

    public void moveStoreData(String sourceStoreName, String destinationStoreName) {
        try {
            new ProcessBuilder("/bin/bash", "-c", "mv "+configurationDirPath+"/"+sourceStoreName+" "+configurationDirPath+"/"+destinationStoreName).start();
        } catch (IOException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
