/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.ini4j.Ini;
import com.github.joe42.splitter.util.file.PropertiesUtil;

/**
 *
 * @author demo
 */
public class StorageService {
    private boolean supported;
    private String name;
    private String uniqName;
    private StorageType type;
    private int nrOfBackends;
    private boolean isBackendModule;
    private LinkedList<StorageService> backendServices;
    private Map<String, String> parameterMap = new HashMap<String, String>();
    private Ini config = null;
    private Point graphLocation;
    private int nrOfFilePartsToStore;
    private File file;
    
    public StorageService(String name) {
        this.name = name;
        uniqName = name + new Random().nextInt(10000000);
        supported = false;
        nrOfBackends = 0;
        nrOfFilePartsToStore = 1;
        backendServices = new LinkedList<StorageService>();
        String nubisavedir = new PropertiesUtil("nubi.properties").getProperty("nubisave_directory");
        file = new File(nubisavedir + "/splitter/mountscripts/" + name + ".ini");
        if(file.exists()){
            loadFromFile();
        }
    }

    /**
     * @param file an ini configuration file for a custom service
     */
    public StorageService(File file) {
            this.file = file;
            String filename = file.getName().split("\\.")[0]; //use the filename as a service name
            if(filename.matches(".*[0-9]$")){ //filename is already name + number
                uniqName = filename;
                name = filename.replace("\\d*$", ""); //remove trailing numbers
            } else {
                name = filename;
                uniqName = name + new Random().nextInt(10000000);
            }
            type = StorageType.CUSTOM;
            supported = true;
            nrOfBackends = 0;
            nrOfFilePartsToStore = 1;
            backendServices = new LinkedList<StorageService>();
            loadFromFile();
    }

    public void loadFromFile() {
        try {
            setConfig(new Ini(file));
        } catch (IOException e) {
            System.err.println("StorageService instance " + name + ".loadFromFile(): Cannot read ini file " + file.getAbsolutePath() + " - " + e.getMessage() == null ? e.getMessage() : "");
        }
    }

    /**@return the configuration file for this service*/
    public Ini getConfig(){
        return config;
    }

    /**Sets the configuration file for this service*/
    public void setConfig(Ini config){
        this.config = config;
        if(config != null){
            try{
                nrOfFilePartsToStore = config.get("splitter", "fileparts", Integer.class);
            } catch (NullPointerException e) {
                System.err.println("StorageService instance "+getName()+".setConfig(Ini config): configuration has no fileparts parameter"+" - "+e.getMessage()==null?e.getMessage():"");
            }
            try{
                int x = config.get("gui", "graphlocationx", Integer.class);
                int y = config.get("gui", "graphlocationy", Integer.class);
                graphLocation = new Point(x,y);
            } catch (NullPointerException e) {
                System.err.println("StorageService instance "+getName()+".setConfig(Ini config): configuration has no graphlocationx and graphlocationy parameter"+" - "+e.getMessage()==null?e.getMessage():"");
            }
            try{
                nrOfBackends = config.get("gui", "nrofbackends", Integer.class);
            } catch (NullPointerException e) {
                System.err.println("StorageService instance "+name+".setConfig(Ini config): configuration has no nrOfBackends parameter"+" - "+e.getMessage()==null?e.getMessage():"");
            }
            try{
                isBackendModule = config.get("splitter", "isbackendmodule", Boolean.class);
            } catch (NullPointerException e) {
                System.err.println("StorageService instance "+name+".setConfig(Ini config): configuration has no isbackendmodule parameter"+" - "+e.getMessage()==null?e.getMessage():"");
            }
            try{
                int backendServiceIndex = 1;
                String backendServiceName;
                while(true){
                    backendServiceName = config.get("parameter", "backendservice"+backendServiceIndex++, String.class);
                    if(backendServiceName == null){
                        return;
                    }
                    addBackendService(Nubisave.services.getByUniqueName(backendServiceName));
                }
            } catch (NullPointerException e) {
                System.out.println("StorageService instance "+name+".setConfig(Ini config): configuration has no more backendservice parameters"+" - "+e.getMessage()==null?e.getMessage():"");
            }
        }
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

    /**
     * @return the number of local backends for this service
     */
    public int getNrOfBackends() {
        return nrOfBackends;
    }

    /**
     * @return true iff this storage service is used as a backend for an other service
     */
    public boolean isBackendModule() {
        return isBackendModule;
    }

    /**
     * @param isBackendModule iff true set this storage service to be used as a backend for an other service
     */
    public void setBackendModule(boolean isBackendModule) {
        this.isBackendModule = isBackendModule;
    }

    public List<StorageService> getBackendServices() {
        return this.backendServices;
    }

    /**
     * Adds service as a backend service.
     * Then removes the backend service added before all others if more than #getNrOfBackends() backendservices have been added.
     * If service is already a backend service it is not added.
     */
    public void addBackendService(StorageService service) {
        if(backendServices.contains(service)){
            return;
        }
        backendServices.add(service);
        if(nrOfBackends < backendServices.size()){
            backendServices.removeFirst();
        }
    }

    /**
     * Removes service from this service's list of backend services.
     */
    public void removeBackendService(StorageService service) {
        backendServices.remove(service);
    }

    /**
     * Store the configuration as a file with the name of this service's unique name
     * Backend services are substituted and isbackendmodule parameter is set as well
     * @param directory the directory to store the configuration file
     */
    public void storeConfiguration(String directory) {
        String path = directory + "/" + getUniqName();
        System.out.println("Store module configuration to " + path);
        if(config == null) {
            try {
                config = new Ini();
            } catch(Exception e) {
                System.err.println("Configuration issue: " + e.toString());
            }
        }
        try{
            int serviceIndex = 1;
            for(StorageService s: getBackendServices()){
                config.put("parameter", "backendservice"+serviceIndex++, s.getUniqName());
            }
            if(isBackendModule()){
                config.put("splitter", "isbackendmodule", true);
            }
            if(graphLocation != null){
                config.put("gui", "graphlocationx", graphLocation.x);
                config.put("gui", "graphlocationy", graphLocation.y);
                config.get("gui").putComment("graphlocationx", "hidden");
                config.get("gui").putComment("graphlocationy", "hidden");
            }
            config.put("splitter", "fileparts", nrOfFilePartsToStore);
            config.store(new File(path));
        } catch(Exception e){
            System.err.println("Error writing configuration for StorageService instance " + getUniqName());
            System.err.println("Cause: " + e.toString());
            System.err.println("Cause detail: " + (e.getMessage() != null ? e.getMessage() : "(unknown)"));
            return;
        }
    }

    /**
     * Get the location of the services vertex representation on the graph.
     * @return the location or null otherwise
     */
    public Point getGraphLocation(){
        return graphLocation;
    }
    /**
     * Set the location of the services vertex representation on the graph.
     */
    public void setGraphLocation(Point location){
        this.graphLocation = location;
    }

    /**
     * @return the number of file parts the splitter stores in the module described by this StorageService instance
     */
    public Integer getNrOfFilePartsToStore() {
        return nrOfFilePartsToStore;
    }

    /**
     * Set the number of file parts the splitter stores in the module described by this StorageService instance.
     * @param nrOfFilePartsToStore the nrOfFilePartsToStore to set
     */
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
        this.nrOfFilePartsToStore = nrOfFilePartsToStore;
    }

}
