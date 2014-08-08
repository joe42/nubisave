package nubisave.component.graph.mouseplugins;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.mouseplugins.VertexSelector;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import org.apache.commons.collections15.Factory;

/**
 * Set the graph location for dropped Nubisave components,
 * so they can remember it persistently over sessions.
 *
 * @author joe
 */
public class StorageServiceSelector<V, E> extends VertexSelector {
    /**
     * create an instance with default settings
     */
    public StorageServiceSelector() {
        this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
    }

    /**
     *
     * @param selectionModifiers for primary selection
     * @param addToSelectionModifiers for additional selection
     */
    public StorageServiceSelector(int selectionModifiers, int addToSelectionModifiers) {
        super(selectionModifiers, addToSelectionModifiers);
    }
   
    /**
     * Set the new location of Nubisave components that
     * have been dragged around and have now been dropped.
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        //Set the new locations for all NubisaveComponent parts, to be able
        //to store them persistently after application shutdown
        if(currrentlySelectedVertex != null) {
            VisualizationViewer<V, E> vv = (VisualizationViewer) e.getSource();
            PickedState<V> ps = vv.getPickedVertexState();
            Point2D p = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(e.getPoint());
            int x = (int) p.getX();
            int y = (int) p.getY();
            for (V v : ps.getPicked()) {
                if(v instanceof AbstractNubisaveComponent){
                    ((AbstractNubisaveComponent)v).setGraphLocation(new Point(x,y));
                    System.out.println("setting graph location");
                }
            }
        }
        super.mouseReleased(e);
    }
}
