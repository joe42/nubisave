package nubisave.component.graph.vertice;

import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;



import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * Compound NubiSaveComponent container (Composite design pattern).
 * 
 * @author joe
 *
 */
public class CompoundNubiSaveComponent extends AbstractNubisaveComponent {
	
	ImageIcon visualRepresentation;
	protected Set<AbstractNubisaveComponent> children = new HashSet<AbstractNubisaveComponent>();

	public CompoundNubiSaveComponent(String name, BufferedImage visualRepresentation){
		super(name, visualRepresentation);
	}

	public CompoundNubiSaveComponent() throws IOException{
		this("NubiSave",ImageIO.read(AbstractNubisaveComponent.class.getResource("/nubisave/images/NubiSaveCompoundComponent.png")));
	}

	@Override
	public boolean addChild(AbstractNubisaveComponent child){
		return children.add(child);
	}	
	
	@Override
	public boolean rmChild(AbstractNubisaveComponent child){
		return children.remove(child);
	}

	@Override
	public Set<AbstractNubisaveComponent> getChildren(){
		return children;
	}
	
	@Override
	public void addRequiredPort(){
        try {
            requiredPorts.add(new RequiredPort<NubiSaveVertex>(this, ImageIO.read(RequiredPort.class.getResource("/nubisave/images/CompoundPort.png"))));
        } catch (IOException ex) {
            Logger.getLogger(CompoundNubiSaveComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
	}
	
    @Override
	public <E> void addToGraph(VisualizationViewer<NubiSaveVertex, E> vv, Point location){
		Graph<NubiSaveVertex, E> graph = vv.getModel().getGraphLayout().getGraph();
		Layout<NubiSaveVertex, E> layout = vv.getModel().getGraphLayout();
		graph.addVertex((NubiSaveVertex) this);
		//get estimate of compound size of children:
		int estimatedWidth = 0, estimatedHeight = 0;
		int verticalDistance = 10, horizontalDistance = 20;
		for(AbstractNubisaveComponent child: children){
			estimatedWidth += child.getBufferedImage().getWidth() + horizontalDistance;
			estimatedHeight += child.getBufferedImage().getHeight() + verticalDistance;
		}
		setBufferedImage((BufferedImage)getBufferedImage().getScaledInstance(estimatedWidth, estimatedHeight, java.awt.Image.SCALE_SMOOTH));
		super.addToGraph(vv, location);
	}

	/*public boolean equals(Object obj) {
		*return this.equals(obj);
	}*/

	@Override
	public Set<NubiSaveVertex> getVertexGroupMembers() {
		Set<NubiSaveVertex> ret = super.getVertexGroupMembers();
		ret.addAll(children);
		return ret;
	}

    @Override
    public void toggleActivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void showConfigurationDialog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deactivate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void activate() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGraphLocation(Point location) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Point getGraphLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConnectedToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeConnectionTo(AbstractNubisaveComponent abstractNubiSaveComponent) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getUniqueName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isActive() {
        throw new UnsupportedOperationException("Not supported yet.");
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	@Override
	public void openLocation() {
		// TODO Auto-generated method stub
		
	}

}

