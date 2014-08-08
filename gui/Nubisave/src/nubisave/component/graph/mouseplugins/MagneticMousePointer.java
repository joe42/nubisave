package nubisave.component.graph.mouseplugins;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.mouseplugins.VertexSelector;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import org.apache.commons.collections15.Factory;

/**
 * Moves a Nubisave component with the mouse until the user performs a mouse click.
 *
 * @author joe
 */
public class MagneticMousePointer<V, E>  extends AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {
    private V magneticComponent;
    
    /**
     * create an instance with default settings
     */
    public MagneticMousePointer() {
        super(InputEvent.BUTTON1_MASK);
        magneticComponent = null;
    }
    
    /**
     * Move a vertex with the mouse pointer.
     * @param vertex an existing vertex on the graph to move
     */
    public void setMagneticComponent(V vertex){
        magneticComponent = vertex;
    }
    
    public V getMagneticComponent(){
        return magneticComponent;
    }
    
    /**
     * Moves magnetic component with the mouse until the user 
     * performs a mouse click.
     * @param e 
     */
    @SuppressWarnings("unchecked")
    public void mouseMoved(MouseEvent e) {
        if(magneticComponent != null) {
            VisualizationViewer<V, E> graphDisplay = (VisualizationViewer<V, E>) e.getSource();
            Layout<V, E> layout = graphDisplay.getGraphLayout();
            Point p = e.getPoint();
            Point2D graphPoint = graphDisplay.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
            Set<V> magneticComponents = new HashSet<V>();
            if (magneticComponent instanceof VertexGroup<?>) { 
                //select the whole group
                magneticComponents = ((VertexGroup<V>) magneticComponent).getVertexGroupMembers();
            } else {
                magneticComponents.add(magneticComponent);
            }
            
            V oneComponent = magneticComponents.iterator().next();
            Point2D relativePos = layout.transform(oneComponent);
            double dx = graphPoint.getX() - relativePos.getX();
            double dy = graphPoint.getY() - relativePos.getY();
            for (V v : magneticComponents) {
                Point2D pos = layout.transform(v);
                pos.setLocation(pos.getX() + dx, pos.getY() + dy);
                layout.setLocation(v, pos);
            }
            graphDisplay.repaint();
        }
    }
    
    /**
     * The magnetic component has been planted on the graph 
     * and does not need to be moved around with the mouse anymore
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if(magneticComponent != null){
            e.consume();
            magneticComponent = null;
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void mouseDragged(MouseEvent me) {
    }
}
