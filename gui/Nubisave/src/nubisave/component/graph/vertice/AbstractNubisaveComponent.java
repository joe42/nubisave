package nubisave.component.graph.vertice;

import nubisave.component.graph.edge.RestrictedEdgeVertex;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.imageio.ImageIO;


import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.renderers.Checkmark;
import java.awt.Color;
import java.awt.Dimension;
import nubisave.component.graph.vertice.interfaces.BufferedImageDelegator;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import nubisave.ui.util.BufferedImageUtil;

public abstract class AbstractNubisaveComponent implements VertexGroup<NubiSaveVertex>, BufferedImageDelegator, NubiSaveVertex, RestrictedEdgeVertex {
    double throughput;
    double cost;
    double underlyingVertex;
    protected BufferedImage visualRepresentation;
    protected Set<RequiredPort<NubiSaveVertex>> requiredPorts;
    protected Set<ProvidedPort<NubiSaveVertex>> providedPorts;
    protected String name;
    private BufferedImage original;

    /**
     * Create a new instance.
     * @param componentName the name of this component {@link #getName() getName}
     * @param visualRepresentation the icon representing the component in the graph
     */
    public AbstractNubisaveComponent(String componentName, BufferedImage visualRepresentation) {
        name = componentName;
        this.visualRepresentation = visualRepresentation;
        requiredPorts = new LinkedHashSet<RequiredPort<NubiSaveVertex>>();
        providedPorts = new LinkedHashSet<ProvidedPort<NubiSaveVertex>>();
    }

    /**
     * Create a new instance.
     * componentName defaults to NubiSave, while visualRepresentation defaults to an ImageIcon of "/nubisave/images/NubiSaveComponent.png"
     * @throws IOException
     */
    public AbstractNubisaveComponent() throws IOException {
        this("NubiSave", ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/NubiSaveComponent.png")));
    }

    public void addRequiredPort() {
        try {
            requiredPorts.add(new RequiredPort<NubiSaveVertex>(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/RequiredPort.png"))));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Add a required Port to the component with a restricted number of use connections.
     * @param maxDegree maximum number of connections to all provided ports
     */
    public void addRequiredPort(int maxDegree) {
        try {
            requiredPorts.add(new RequiredPort<NubiSaveVertex>(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/RequiredPort.png")), maxDegree));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addProvidedPort() {
        try {
            providedPorts.add(new ProvidedPort<NubiSaveVertex>(this, ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/ProvidedPort.png"))));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Set<RequiredPort<NubiSaveVertex>> getRequiredPorts() {
        return requiredPorts;
    }

    public Set<ProvidedPort<NubiSaveVertex>> getProvidedPorts() {
        return providedPorts;
    }

    /**
     * Add child to the composite or return false if it is not possible.
     * @param child
     * @return true if child was added
     */
    public boolean addChild(AbstractNubisaveComponent child) {
        return false;
    }

    /**
     * Remove child from the composite or return false if it is not possible.
     * @param child
     * @return true if child was removed
     */
    public boolean rmChild(AbstractNubisaveComponent child) {
        return false;
    }

    /**
     * Return the child components
     * @return a set with the children of this component
     */
    public Set<AbstractNubisaveComponent> getChildren() {
        return new HashSet<AbstractNubisaveComponent>();
    }

    public <E> void addToGraph(VisualizationViewer<NubiSaveVertex, E> vv, Point location) {
        //Point2D graphLayoutPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(mouseEvent.getPoint());
        Graph<NubiSaveVertex, E> graph = vv.getModel().getGraphLayout().getGraph();
        Layout<NubiSaveVertex, E> layout = vv.getModel().getGraphLayout();
        graph.addVertex((NubiSaveVertex) this);
        layout.setLocation(this,
                vv.getRenderContext().getMultiLayerTransformer().inverseTransform(location));

        int iteration = 0;
        for (RequiredPort<NubiSaveVertex> p : requiredPorts) {
            graph.addVertex(p);
            Point requiredPortPos = (Point) location.clone();
            requiredPortPos.translate(-visualRepresentation.getWidth() / 2 - p.getBufferedImage().getWidth() / 2, p.getBufferedImage().getHeight() * iteration);
            layout.setLocation(p,
                    vv.getRenderContext().getMultiLayerTransformer().inverseTransform(requiredPortPos));
            iteration++;
        }
        iteration = 0;
        for (ProvidedPort<NubiSaveVertex> p : providedPorts) {
            graph.addVertex(p);
            Point providedPortPos = (Point) location.clone();
            providedPortPos.translate(visualRepresentation.getWidth() / 2 + p.getBufferedImage().getWidth() / 2, p.getBufferedImage().getHeight() * iteration);
            layout.setLocation(p,
                    vv.getRenderContext().getMultiLayerTransformer().inverseTransform(providedPortPos));
            iteration++;
        }
    }

    @Override
    public Set<NubiSaveVertex> getVertexGroupMembers() {
        Set<NubiSaveVertex> ret = new LinkedHashSet<NubiSaveVertex>();
        ret.addAll(providedPorts);
        ret.addAll(requiredPorts);
        ret.add(this);
        return ret;
    }

    @Override
    public AbstractNubisaveComponent getParentComponent() {
        return this;
    }

    @Override
    public BufferedImage getBufferedImage() {
        return visualRepresentation;
    }

    ;

    @Override
    public int getMaxDegree() {
        return 0;
    }

    /**
     * Does nothing, since this vertex should not have connections.
     */
    @Override
    public void setMaxDegree(int max) {
    }

    @Override
    public void setBufferedImage(BufferedImage bufferedImage) {
        visualRepresentation = bufferedImage;
    }

    /**
     * Should be overwritten in subclasses
     * @return the name of this component
     */
    public String getName() {
        return name;
    }

    /**
     * @return unique name 
     */
    public abstract String getUniqueName();

    @Override
    public String toString() {
        String ret = "NubiSave component: " + name;
        String retPart;
        ret += "\nrequired ports:\n";
        for (RequiredPort<NubiSaveVertex> p : requiredPorts) {
            ret += "Required port with degree of " + getMaxDegree() + "\n";
        }
        ret += "\nprovided ports:\n";
        for (ProvidedPort<NubiSaveVertex> p : providedPorts) {
            ret += "Provided port \n";
        }
        if (getChildren().size() == 0) {
            return ret;
        }
        ret += "\n children:";
        retPart = "\n";
        for (AbstractNubisaveComponent child : getChildren()) {
            ret += child.toString() + "\n";
        }
        ret += retPart.replaceAll("\n", "\n    ");
        return ret;
    }

    public abstract void setGraphLocation(Point location);

    public abstract Point getGraphLocation();

    public abstract int getNrOfFilePartsToStore();

    public abstract void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore);

    /**
     * Draws a checkmark on the component and stores the original visual representation to be restored by {@link #undoCheckMark() }.
     * The parameters indicate the position relative to the center of the component.
     * @param dxFromMid
     * @param dyFromMid
     */
    protected void drawCheckMark(int dxFromMid, int dyFromMid) {
        original = BufferedImageUtil.deepCopy(visualRepresentation);
        Checkmark checkmark = new Checkmark(Color.green);
        Dimension id = new Dimension(visualRepresentation.getWidth(), visualRepresentation.getHeight());
        int dx = (id.width - checkmark.getIconWidth()) / 2 + dxFromMid;
        int dy = (id.height - checkmark.getIconHeight()) / 2 + dyFromMid;
        Graphics g = visualRepresentation.getGraphics();
        checkmark.paintIcon(null, g, 0 + dx, 0 + dy);
    }

    /**
     * Restore the visual representation after a call to {@link #drawCheckMark(int, int) }.
     */
    protected void undoCheckMark() {
        visualRepresentation = original;
        original = null;
    }

    /**
     * Activate the component if it is deactivated and deactivate the component if it is activated.
     */
    public abstract void toggleActivate();

    public abstract void showConfigurationDialog();

    /**
     * Deactivate the component instance
     */
    public abstract void deactivate();

    /**
     * Activate the component instance
     */
    public abstract void activate();

    /**
     * Check if the component instance is active
     * @return true if the component instance is active
     */
    public abstract boolean isActive();

    /**
     * Do internal cleanup.
     * Should be called when the component is not used in the graph anymore.
     * Does not remove visual components from graph.
     */
    public abstract void remove();

    /**
     * Internally connects abstractNubiSaveComponent to this component's provided Port.
     * This will influence the graphical connection only after a reinitialization of the graph.
     * @param abstractNubiSaveComponent
     */
    public abstract void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent);

    /**
     * Internally checks if abstractNubiSaveComponent is connected to this component's provided Port.
     * @param abstractNubiSaveComponent
     * @return true if abstractNubiSaveComponent is connected to this component's provided Port
     */
    public abstract boolean isConnectedToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent);

    /**
     * Internally removes the connection of abstractNubiSaveComponent to this component's provided Port.
     * This will influence the graphical connection only after a reinitialization of the graph.
     * @param abstractNubiSaveComponent
     */
    public abstract void removeConnectionTo(AbstractNubisaveComponent abstractNubiSaveComponent);

    /**
     * Migrate all stored data from this component to <code>componentTo</code>.
     * Only migrate data if both components are active. Display migration visually.
     * @param component
     */
    public abstract void migrateDataTo(AbstractNubisaveComponent componentTo);

    /**
     * Get the migration progress in percent
     * @return value between 0 and 100
     */
    public abstract int getMigrationProgress();

    /**
     * Get the final status of the last migration progress in percent or null if this configuration's store is not the source of a complete migration progress.
     * @return value true if the last data migration from this service has been successful, false if it failed or null if there is no complete migration
     */
    public abstract Boolean migrationIsSuccessful();
}
