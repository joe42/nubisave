package nubisave.component.graph.vertice;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.Timer;

import org.ini4j.Ini;

import com.github.joe42.splitter.util.file.PropertiesUtil;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.component.graph.edge.RestrictedEdgeVertex;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.ui.NubisaveConfigDlg;
import nubisave.ui.ServiceParameterDialog;
import nubisave.ui.util.SystemIntegration;

/**
 * Central component of the NubisaveEditor, which represents the folder on the
 * user's Desktop, that is used to access Nubisave's data.
 *
 * @author joe
 */
public class CloudEntranceComponent extends AbstractNubisaveComponent {

    protected final StorageService component;
    protected final int checkLabelHorizontalPos;
    private DataVertex dataVertex;
    protected Timer timer;

    public CloudEntranceComponent(StorageService component) throws IOException {
        super(component.getName(), ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/CloudComponent.png")));
        addRequiredPort(1);

        this.component = component;
        this.name = "CloudEntrance";
        JLabel l = new JLabel(getName());

        l.setFont(new Font("Helvetica", Font.PLAIN, 12));
        Dimension d = l.getPreferredSize();
        checkLabelHorizontalPos = d.width / 2 + 5;
        drawCheckMark(checkLabelHorizontalPos - 8, -6);
    }

    /**
     * Adds data vertex for visually moving data between components.
     */
    @Override
    public <E> void addToGraph(VisualizationViewer<NubiSaveVertex, E> vv,
            Point location) {
        super.addToGraph(vv, location);
        Graph<NubiSaveVertex, E> graph = vv.getModel().getGraphLayout()
                .getGraph();
        Layout<NubiSaveVertex, E> layout = vv.getModel().getGraphLayout();
        try {
            if (this.getDataVertex() == null) {
                setDataVertex(new DataVertex(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/folder.png"))));
            }
            graph.addVertex(getDataVertex());
            Point dataVertexPos = (Point) location.clone();
            dataVertexPos.translate(-visualRepresentation.getWidth() + getDataVertex().getBufferedImage().getWidth(),
                    -visualRepresentation.getHeight() + getDataVertex().getBufferedImage().getHeight());
            layout.setLocation(getDataVertex(), vv.getRenderContext().getMultiLayerTransformer().inverseTransform(dataVertexPos));
        } catch (IOException ex) {
            Logger.getLogger(GenericNubiSaveComponent.class.getName()).log(
                    Level.SEVERE, null, ex);
        }
    }

    @Override
    public void toggleActivate() {
    }

    @Override
    public void showConfigurationDialog() {
    }

    @Override
    public void openLocation() {
        String relativePath = "/nubisavemount/data";
        String location = Nubisave.properties.getProperty("nubisave_directory");
        location = location + relativePath.substring(1);
        SystemIntegration.openLocation(location);
    }

    @Override
    public void visualizeLocation() {
    }

    public void refreshConfiguration() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void activate() {
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void remove() {
    }

    @Override
    public void setGraphLocation(Point location) {
        component.setGraphLocation(location);
        Nubisave.services.updateCloudEntrance(component);
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
    public void connectToProvidedPort(
            AbstractNubisaveComponent abstractNubiSaveComponent) {
        System.out.println("connected???");
        if (abstractNubiSaveComponent instanceof GenericNubiSaveComponent) {
            component.addBackendService(((GenericNubiSaveComponent) abstractNubiSaveComponent).component);
            Nubisave.services.update();
            System.out.println(component.getName() + " adds " + ((GenericNubiSaveComponent) abstractNubiSaveComponent).component.getName() + " as backend service");
        }
    }

    @Override
    public void removeConnectionTo(
            AbstractNubisaveComponent abstractNubiSaveComponent) {
        if (abstractNubiSaveComponent instanceof GenericNubiSaveComponent) {
            ((GenericNubiSaveComponent) abstractNubiSaveComponent).component.removeBackendService(component);
            Nubisave.services.update();
            System.out.println("unconnected");
        }
    }

    @Override
    public boolean isConnectedToProvidedPort(
            AbstractNubisaveComponent abstractNubiSaveComponent) {
        if (abstractNubiSaveComponent instanceof GenericNubiSaveComponent) {
            for (StorageService backendServices : ((GenericNubiSaveComponent) abstractNubiSaveComponent).component.getBackendServices()) {
                if (backendServices.equals(component)) {
                    return true;
                }
            }
        }
        return abstractNubiSaveComponent instanceof NubiSaveComponent
                && !component.isBackendModule();
    }

    @Override
    public String getUniqueName() {
        return component.getUniqName();
    }

    /**
     * Returns true if obj is a CloudEntrance, since there must only be one 
     * instance of it.
     * @param obj
     * @return true if obj is a CloudEntrance
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof CloudEntranceComponent;
    }

    @Override
    public int hashCode() {
        return 1;
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

    @Override
    public int getNrOfFilePartsToStore() {
        return 0;
    }

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
    }

    @Override
    public void migrateDataTo(AbstractNubisaveComponent componentTo) {
    }

    @Override
    public int getMigrationProgress() {
        return 0;
    }

    @Override
    public Boolean migrationIsSuccessful() {
        return null;
    }

}
