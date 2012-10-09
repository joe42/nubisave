package nubisave.component.graph.vertice;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.Timer;
import nubisave.*;
import nubisave.component.graph.vertice.DataVertex;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.ui.ServiceParameterDialog;
import org.ini4j.Ini;


public class GenericNubiSaveComponent extends AbstractNubisaveComponent {
    protected final StorageService component;
    protected final int checkLabelHorizontalPos;
    private DataVertex dataVertex;
    protected Timer timer;

    public GenericNubiSaveComponent(StorageService component) throws IOException {
        super(component.getName(), ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/GenericNubiSaveComponent.png")));
        addProvidedPort();
        if(component.getNrOfBackends() > 0) { 
            addRequiredPort(component.getNrOfBackends());
        }
        this.component = component;
        JLabel l = new JLabel(getName());
        l.setFont(new Font("Helvetica", Font.PLAIN, 12));
        Dimension d = l.getPreferredSize();
        checkLabelHorizontalPos = d.width/2+5;
        if(Nubisave.mainSplitter.isModuleMounted(component)){
            drawCheckMark(checkLabelHorizontalPos, 0);
        }
    }

    /**
     * Adds data vertex for visually moving data between components.
     */
    @Override
    public <E> void addToGraph(VisualizationViewer<NubiSaveVertex, E> vv, Point location){
        super.addToGraph(vv, location);
        Graph<NubiSaveVertex, E> graph = vv.getModel().getGraphLayout().getGraph();
        Layout <NubiSaveVertex, E> layout = vv.getModel().getGraphLayout();
        try {
            setDataVertex(new DataVertex(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/data.png"))));
            graph.addVertex(getDataVertex());
            Point dataVertexPos = (Point) location.clone();
            dataVertexPos.translate(visualRepresentation.getWidth()-getDataVertex().getBufferedImage().getWidth(), -visualRepresentation.getHeight()+getDataVertex().getBufferedImage().getHeight());
            layout.setLocation( getDataVertex(), vv.getRenderContext().getMultiLayerTransformer().inverseTransform(dataVertexPos));
          } catch (IOException ex) {
            Logger.getLogger(GenericNubiSaveComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
  }

    @Override
    public void toggleActivate() {
        System.out.println("toggle activation state");
        if(! Nubisave.mainSplitter.isModuleMounted(component)){
            Nubisave.mainSplitter.mountStorageModule(component); // mount the module
            drawCheckMark(checkLabelHorizontalPos, 0);
        } else {
            Nubisave.mainSplitter.unmountStorageModule(component); // unmount the module
            undoCheckMark();
        }
    }

    @Override
    public void showConfigurationDialog() {
        ServiceParameterDialog editDialog = new ServiceParameterDialog(null, true, component);
        editDialog.setTitle(component.getName());
        editDialog.setVisible(true);
    }

    public void refreshConfiguration(){
        component.loadFromFile();
    }

    @Override
    public void deactivate() {
        if(Nubisave.mainSplitter.isModuleMounted(component)){
            Nubisave.mainSplitter.unmountStorageModule(component); // unmount the module
            undoCheckMark();
        }
    }

    @Override
    public void activate() {
        if(! Nubisave.mainSplitter.isModuleMounted(component)){
            Nubisave.mainSplitter.mountStorageModule(component); // mount the module
            drawCheckMark(checkLabelHorizontalPos, 0);
        }
    }

    @Override
    public boolean isActive() {
        return Nubisave.mainSplitter.isModuleMounted(component);
    }

    @Override
    public void remove() {
        Nubisave.services.remove(component);
    }

    @Override
    public void setGraphLocation(Point location) {
        component.setGraphLocation(location);
        Nubisave.services.update(component);
    }

    @Override
    public Point getGraphLocation() {
        return component.getGraphLocation();
    }


    @Override
    public Set<NubiSaveVertex> getVertexGroupMembers() {
            Set<NubiSaveVertex> ret = super.getVertexGroupMembers();
            ret.add(getDataVertex());
            return ret;
    }

    @Override
    public void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
            System.out.println("connected???");
        if(abstractNubiSaveComponent instanceof GenericNubiSaveComponent){
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

    // TODO check if migration is active, check if component is active
    @Override
    public void migrateDataTo(AbstractNubisaveComponent componentTo) {
        initializeMigration(Nubisave.mainSplitter.getConfigFile(component.getUniqName()));
        Nubisave.mainSplitter.moveStoreData(component.getUniqName(), ((GenericNubiSaveComponent)componentTo).component.getUniqName());
    }

    @Override
    public int getMigrationProgress() {
        Ini ini;
        try {
            ini = new Ini(Nubisave.mainSplitter.getConfigFile(component.getUniqName()));
            return ini.get("splitter", "migrationprogress", Integer.class);
        } catch (IOException ex) {
            return 0;
        } catch (NullPointerException e) {
            return 0;
        }
    }

    @Override
    public Boolean migrationIsSuccessful() {
        Ini ini;
        try {
            ini = new Ini(Nubisave.mainSplitter.getConfigFile(component.getUniqName()));
            return ini.get("splitter", "migrationissuccessful", Boolean.class);
        } catch (IOException ex) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }


    protected void initializeMigration(File storeConfig) {
        Ini ini;
        try {
            ini = new Ini(storeConfig);
            ini.remove("splitter", "migrationprogress");
            ini.remove("splitter", "migrationissuccessful");
            ini.store();
        } catch (IOException ex) {
            Logger.getLogger(GenericNubiSaveComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public int getNrOfFilePartsToStore() {
		return component.getNrOfFilePartsToStore();
	}

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
		component.setNrOfFilePartsToStore(nrOfFilePartsToStore);
        Nubisave.services.update(component);
	}

    @Override
    public String getUniqueName() {
        return component.getUniqName();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GenericNubiSaveComponent){
            return ((GenericNubiSaveComponent)obj).getUniqueName().equals(getUniqueName());
        } else
            return super.equals(obj);
    }

    @Override
    public int hashCode(){
        return getUniqueName().hashCode();
    }

    /**
     * @return the dataVertex
     */
    public DataVertex getDataVertex() {
        return dataVertex;
    }

    /**
     * @param dataVertex the dataVertex to set
     */
    public void setDataVertex(DataVertex dataVertex) {
        this.dataVertex = dataVertex;
    }

}
