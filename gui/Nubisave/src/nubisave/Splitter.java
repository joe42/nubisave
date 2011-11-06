/*
 * Representation to abstract configuration of the actual Splitter module.
 */

package nubisave;

import java.io.File;
import java.io.IOException;
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
    public void mount(){
        /**Not yet implemented; should mount the Splitter module.**/
        throw new NotImplementedException();
    }
    public void unmount(){
        /**Not yet implemented; should unmount the Splitter module.**/
        throw new NotImplementedException();
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
        String path = configurationDirPath + "/" + service.getUniqName();
        try{
            Ini serviceIni = service.getConfig();
            int serviceIndex = 1;
            for(StorageService s: service.getBackendServices()){
                    serviceIni.put("parameter", "backendservice"+serviceIndex++, s.getUniqName());
                }
                if(service.isBackendModule()){
                    serviceIni.put("splitter", "isbackendmodule", true);
            }

            serviceIni.store(new File(path));
        } catch(Exception e){
            e.printStackTrace();
            return;
        }
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
            System.out.println("failed to configure Splitter:");
            e.printStackTrace();
        }
    }
    public int getRedundancy() throws IOException{
        /**Gets the redundancy level for Splitter module**/
            Ini splitterConfig = new Ini(new File(configurationFilePath));
            return Integer.parseInt(splitterConfig.get("splitter", "redundancy"));
    }

}
