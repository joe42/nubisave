/*
 * Representation to abstract configuration of the actual Splitter module.
 */

package nubisave;

import com.github.joe42.splitter.backend.BackendService;
import com.github.joe42.splitter.util.file.FileUtil;
import com.github.joe42.splitter.util.file.PropertiesUtil;
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
    private final String mountScriptDir;
    public Splitter(String splitterMountpoint){
        this.splitterMountpoint = splitterMountpoint;
        this.configurationDirPath = splitterMountpoint+"/config";
        this.configurationFilePath = splitterMountpoint+"/config/config";
        this.dataDir = splitterMountpoint+"/data";
        mountScriptDir = "../splitter/mountscripts";
    }
    public Splitter(){
        this.splitterMountpoint = null;
        this.configurationDirPath = splitterMountpoint+"/config";
        this.configurationFilePath = splitterMountpoint+"/config/config";
        this.dataDir = splitterMountpoint+"/data";
        mountScriptDir = "../splitter/mountscripts";
    }
    
    public String getMountpoint(){
        return splitterMountpoint;
    }
    public String getDataDir(){
        return dataDir;
    }
    public String getConfigDir(){
        return configurationDirPath;
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


    /**Loads a stored session with all back end modules and the Splitter's configuration**/
    public void loadSession(int sessionNumber){
        try{
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            splitterConfig.put("splitter", "load", sessionNumber);
            splitterConfig.store();
            waitForSessionToLoad();
            //rename services
            StorageService service;
            String uniqueServiceName;
            String previousName = null;
            for(int i=0; i<Nubisave.services.size();i++){
                service = Nubisave.services.get(i);
                uniqueServiceName = service.getUniqName();
                try{
                    previousName = splitterConfig.fetch("MapOfCurrentToPreviousServices", uniqueServiceName, String.class);
                } catch(NullPointerException e){
                    e.printStackTrace();
                }
                if(previousName != null){
                    System.out.println("mapping: "+uniqueServiceName+"->"+previousName);
                    service.setUniqName(previousName);
                    Nubisave.services.add(service,i);
                    Nubisave.services.remove(i);
                    new File(configurationDirPath + "/" + uniqueServiceName).delete();
                    mountStorageModule(service);
                }
            }

            //load the back end modules:
            Nubisave.services.loadFromDataBase(dataDir+"/.nubisave_session_"+sessionNumber);
            Nubisave.services.update();
        } catch(Exception e){
            System.err.println("Splitter.loadSession(int sessionNumber): Failed to configure Splitter "+" - "+e.getMessage()==null?e.getMessage():"");
        }
    }

    /**Stores a session with all back end modules and the Splitter's configuration**/
    public void storeSession(int sessionNumber){
        try{
            String dbPath = System.getProperty("user.home")+"/.nubisave/nubisavemount/data/.nubisave_database"+sessionNumber;
            //store the back end modules:
            File dir = new File(dataDir+"/.nubisave_session_"+sessionNumber);
            dir.mkdirs();
            Nubisave.services.storeToDatabase(dataDir+"/.nubisave_session_"+sessionNumber);
            FileUtil.copy(new File(new PropertiesUtil("nubi.properties").getProperty("splitter_database_location")+".db"), new File(dbPath));
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            splitterConfig.put("splitter", "save", sessionNumber);
            System.out.println( "gui db path: "+"/.nubisave_database"+sessionNumber);
            splitterConfig.put("database", "path", "/.nubisave_database"+sessionNumber);
            splitterConfig.store();

        } catch(Exception e){
            System.err.println("Splitter.storeSession(int sessionNumber): Failed to configure Splitter "+" - "+e.getMessage()==null?e.getMessage():"");
        }
    }

    private void waitForSessionToLoad() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Splitter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @return the mountScriptDir
     */
    public String getMountScriptDir() {
        return mountScriptDir;
    }

}
