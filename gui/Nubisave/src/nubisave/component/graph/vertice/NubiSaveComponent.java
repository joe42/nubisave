package nubisave.component.graph.vertice;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import java.awt.Point;
import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;

import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.ui.NubisaveConfigDlg;
import nubisave.ui.PolicySelectionDlg;
import nubisave.ui.util.SystemIntegration;


public class NubiSaveComponent extends AbstractNubisaveComponent {
    private Point graphLocation;
    protected final StorageService component;
    private String storage_directory;
    private PolicySelectionDlg policy = new PolicySelectionDlg();
    
    public NubiSaveComponent(StorageService component) throws IOException{
        this.component=component;
        
        Ini config = this.component.getConfig();
        this.name = config.get("module", "name");
        addRequiredPort();
        addProvidedPort();
        storage_directory= new PropertiesUtil("nubi.properties").getProperty("storage_configuration_directory");
        String path=storage_directory.split("\\.")[0]+"."+component.getUniqName();
        File file=new File(path.toLowerCase());
        file.mkdirs();
        if(Nubisave.mainSplitter.isMounted()){
            drawCheckMark(40, 0);
       }
    }

    @Override
    public void showConfigurationDialog() {
     NubisaveConfigDlg nubi=new NubisaveConfigDlg();
     nubi.setModal(true);
     nubi.setTitle("Nubisave Component Configuration");
     nubi.pack();
     nubi.setLocationRelativeTo(null);
     nubi.setVisible(true);
     
    }
    
    @Override
    public void toggleActivate() {
        System.out.println("toggle activation state");
        if(! Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.mount();
            drawCheckMark(40, 0);
        } else {
            Nubisave.mainSplitter.unmount();
            undoCheckMark();
        }
    }

    @Override
    public void deactivate() {
        if(Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.unmount();
            undoCheckMark();
        }
    }

    @Override
    public void activate() {
        if(! Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.mount();
            drawCheckMark(40, 0);
        }
    }

    @Override
    public boolean isActive() {
        return Nubisave.mainSplitter.isMounted();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGraphLocation(Point location) {
        component.setGraphLocation(location);
        Nubisave.services.updateNubisave(component);
    }

    @Override
    public Point getGraphLocation() {
        return graphLocation;
    }
    
    public void selectPolicy() {
//    	PolicySelectionDlg policy=new PolicySelectionDlg();
    	policy.setVisible(true);
//    	policy.setModal(true);
//    	policy.setTitle("Nubisave Policy Selection");
//    	//policy.pack();
//    	//policy.setLocationRelativeTo(null);
//    	policy.setVisible(true);
    }

    @Override
    public void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
          System.out.println("connected!!!");
//        if(abstractNubiSaveComponent instanceof DataDirectoryComponent){
//            component.addBackendService(((DataDirectoryComponent)abstractNubiSaveComponent).component);
//            //Nubisave.services.update();
//            System.out.println(component.getName()+" adds "+((DataDirectoryComponent)abstractNubiSaveComponent).component.getName()+" as backend service");
//        }
    }

    @Override
    public void removeConnectionTo(AbstractNubisaveComponent abstractNubiSaveComponent) {
//        if(abstractNubiSaveComponent instanceof DataDirectoryComponent){
//            ((DataDirectoryComponent)abstractNubiSaveComponent).component.removeBackendService(component);
//            //Nubisave.services.update();
//            System.out.println("unconnected");
//        }
    }

    @Override
    public boolean isConnectedToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
//        if(abstractNubiSaveComponent instanceof DataDirectoryComponent){
//            for(StorageService backendServices: ((DataDirectoryComponent)abstractNubiSaveComponent).component.getBackendServices()){
//                if(backendServices.equals(component)){
//                    return true;
//                }
//            }
//        }
//        return abstractNubiSaveComponent instanceof NubiSaveComponent && ! component.isBackendModule();
    	return false;
    }

    @Override
    public String getUniqueName() {
        //return "NubiSave";
        return component.getUniqName();
    }

    @Override
    public Boolean migrationIsSuccessful() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void migrateDataTo(AbstractNubisaveComponent componentTo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMigrationProgress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNrOfFilePartsToStore() {
        return component.getNrOfFilePartsToStore();
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openLocation() {
        String location = Nubisave.mainSplitter.getDataDir();
        SystemIntegration.openLocation(location);
    }

    @Override
    public void visualizeLocation() {
    	String location = Nubisave.properties.getProperty("nubivis_URL");
		try {
			if (java.awt.Desktop.isDesktopSupported()) {
				java.net.URI uri = java.net.URI.create(location);
				java.awt.Desktop dp = java.awt.Desktop.getDesktop();
				if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
					dp.browse(uri);
				}
			} else {
				SystemIntegration.openLocation(location);
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}   
    }

}
