package nubisave.component.graph.vertice;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

import org.ini4j.Ini;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.ui.NubisaveConfigDlg;
import nubisave.ui.ServiceParameterDialog;
import nubisave.ui.util.SystemIntegration;

public class DataDirectoryComponent extends GenericNubiSaveComponent {
    private boolean isConnected;

	public DataDirectoryComponent(StorageService component) throws IOException { 
        super(component);
        isConnected = false;
        drawCheckMark(checkLabelHorizontalPos-8, 0);
	}
	
	/**
     * Adds data vertex for visually moving data between components.
     */
    @Override
    public <E> void addToGraph(VisualizationViewer<NubiSaveVertex, E> vv, Point location){
        try {
			setDataVertex(new DataVertex(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/folder.png"))));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        super.addToGraph(vv, location);
  }
	
	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public static boolean has() {
		return false;
	}

	@Override
	public String getUniqueName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public void setGraphLocation(Point location) {
	        super.setGraphLocation(location);
	    }

	@Override
	public Point getGraphLocation() {
	        return super.getGraphLocation();
	    }

	@Override
	public int getNrOfFilePartsToStore() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleActivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void showConfigurationDialog() {
		super.showConfigurationDialog();
//        ServiceParameterDialog editDialog = new ServiceParameterDialog(null, true, component);
//        editDialog.setTitle(component.getName());
//        editDialog.setVisible(true);
//        if(editDialog.getApplyStatus()){
//        	//get and update latest name.
//        	Ini config = this.component.getConfig();
//            this.name = config.get("module", "name");
//            System.out.println("this.name: "+this.name);
//        }
//	     
	}

	@Override
	public void openLocation() {
		//String location = System.getProperty("user.home") + "/.storages/" + component.getUniqName() + "/data/";
		String location = this.component.getConfig().get("parameter", "path");
		location = System.getProperty("user.home")+location.substring(1);;
        SystemIntegration.openLocation(location);
	}

	@Override
	public void visualizeLocation() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub

	}

	@Override
	public void activate() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void remove() {
		super.remove();
		Nubisave.services.removeDataDir(component);

	}

	@Override
    public void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
        System.out.println("connected...");
        if(abstractNubiSaveComponent instanceof NubiSaveComponent){
            component.addBackendService(((GenericNubiSaveComponent)abstractNubiSaveComponent).component);
            Nubisave.services.update();
            System.out.println(component.getName()+" adds "+((GenericNubiSaveComponent)abstractNubiSaveComponent).component.getName()+" as backend service");
        }
    }

    @Override
    public void removeConnectionTo(AbstractNubisaveComponent abstractNubiSaveComponent) {
        if(abstractNubiSaveComponent instanceof GenericNubiSaveComponent){
            ((GenericNubiSaveComponent)abstractNubiSaveComponent).component.removeBackendService(component);
            Nubisave.services.update();
            System.out.println("unconnected");
        }
    }

    @Override
    public boolean isConnectedToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
        if(abstractNubiSaveComponent instanceof GenericNubiSaveComponent){
            for(StorageService backendServices: ((GenericNubiSaveComponent)abstractNubiSaveComponent).component.getBackendServices()){
                if(backendServices.equals(component)){
                    return true;
                }
            }
        }
        return abstractNubiSaveComponent instanceof NubiSaveComponent && ! component.isBackendModule();
    }

	@Override
	public void migrateDataTo(AbstractNubisaveComponent componentTo) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getMigrationProgress() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Boolean migrationIsSuccessful() {
		// TODO Auto-generated method stub
		return null;
	}
}
