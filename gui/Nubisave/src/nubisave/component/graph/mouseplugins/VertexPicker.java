package nubisave.component.graph.mouseplugins;

/*
 * Pick vertex plus all vertexes in its group, if the vertex implements the interface VertexGroup. 
 *
 */

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;

import nubisave.component.graph.vertice.interfaces.VertexGroup;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * PickingGraphMousePlugin supports the picking of graph elements with the
 * mouse. MouseButtonOne picks a single vertex or edge, and MouseButtonTwo adds
 * to the set of selected Vertices or EdgeType. If a Vertex is selected and the
 * mouse is dragged while on the selected Vertex, then that Vertex will be
 * repositioned to follow the mouse until the button is released.
 * 
 * @author joe
 */
public class VertexPicker<V, E> extends
		AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {

    /**
     * the picked Vertex, if any
     */
    protected V vertex;

    /**
     * the picked Edge, if any
     */
    protected E edge;


    /**
     * additional modifiers for the action of adding to an existing selection
     */
    protected int addToSelectionModifiers;

    /**
     * create an instance with default settings
     */
    public VertexPicker() {
        this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK
                        | InputEvent.SHIFT_MASK);
    }

    /**
     *
     * @param selectionModifiers
     *            for primary selection
     * @param addToSelectionModifiers
     *            for additional selection
     */
    public VertexPicker(int selectionModifiers,
                    int addToSelectionModifiers) {
        super(selectionModifiers);
        this.addToSelectionModifiers = addToSelectionModifiers;
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    /**
     * For primary modifiers (default, MouseButton1): pick a single Vertex or
     * Edge that is under the mouse pointer. If no Vertex or edge is under the
     * pointer, unselect all picked Vertices and edges. For additional selection
     * (default Shift+MouseButton1): Add to the selection, a single Vertex or
     * Edge that is under the mouse pointer.
     *
     * @param e the event
     */
    @SuppressWarnings("unchecked")
    public void mousePressed(MouseEvent e) {
        System.out.println("trying to pick");
        down = e.getPoint();
        VisualizationViewer<V, E> vv = (VisualizationViewer) e.getSource();
        GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
        PickedState<V> pickedVertexState = vv.getPickedVertexState();
        PickedState<E> pickedEdgeState = vv.getPickedEdgeState();
        Set<V> picked = new HashSet<V>();
        if (pickSupport != null && pickedVertexState != null) {
            Layout<V, E> layout = vv.getGraphLayout();
            if (e.getModifiers() == modifiers) {
                //  p is the screen point for the mouse event
                Point2D ip = e.getPoint();
                vertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if (vertex != null) {
                    e.consume();
                    System.out.println("getroffen");
                    if (pickedVertexState.isPicked(vertex) == false) {
                        System.out.println("picked");
                        pickedVertexState.clear();
                        if (vertex instanceof VertexGroup<?>) {
                                picked = ((VertexGroup<V>) vertex)
                                                .getVertexGroupMembers();
                        } else {
                                picked.add(vertex);
                        }
                        for (V v : picked) {
                            if (pickedVertexState.isPicked(v) == false) {
                                    assert false: "the vertex group should either be picked or not picked";
                            }
                            pickedVertexState.pick(v, true);
                            layout.transform(v);
                        }
                    }
                } else if ((edge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    e.consume();
                    System.out.println("daneben");
                    pickedEdgeState.clear();
                    pickedEdgeState.pick(edge, true);
                }
            }
        }
    }

    /**
     * clean up settings from mousePressed
     */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
        down = null;
        vertex = null;
        edge = null;
    }

    /**
     * If the mouse is over a picked vertex, drag the picked vertex with the mouse
     * plus all vertexes in its group, if the vertex implements the interface VertexGroup.
     */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        VisualizationViewer<V, E> vv = (VisualizationViewer) e.getSource();
        if (vertex != null) {
            e.consume();
            Point p = e.getPoint();
            Point2D graphPoint = vv.getRenderContext()
                            .getMultiLayerTransformer().inverseTransform(p);
            Point2D graphDown = vv.getRenderContext()
                            .getMultiLayerTransformer().inverseTransform(down);
            Layout<V, E> layout = vv.getGraphLayout();
            double dx = graphPoint.getX() - graphDown.getX();
            double dy = graphPoint.getY() - graphDown.getY();
            PickedState<V> ps = vv.getPickedVertexState();

            for (V v : ps.getPicked()) {
                Point2D vp = layout.transform(v);
                vp.setLocation(vp.getX() + dx, vp.getY() + dy);
                layout.setLocation(v, vp);
            }
            down = p;
        }
        vv.repaint();
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        JComponent c = (JComponent) e.getSource();
        c.setCursor(cursor);
    }

    public void mouseExited(MouseEvent e) {
        JComponent c = (JComponent) e.getSource();
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(MouseEvent e) {
    }

}
