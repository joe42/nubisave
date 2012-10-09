package nubisave.component.graph.mouseplugins;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import nubisave.component.graph.mouseplugins.VertexPicker;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;

/**
 *
 * @author joe
 */
public class StorageServicePicker<V, E> extends VertexPicker {
    /**
     * clean up settings from mousePressed
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if(vertex != null) {
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
