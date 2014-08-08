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

import org.apache.commons.collections15.Factory;

import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * Mouse plugin to support drag and drop of graph elements
 * with the mouse. MouseButtonOne picks a single vertex or edge, and
 * MouseButtonTwo adds to the set of selected Vertices or EdgeType. If a Vertex
 * is selected and the mouse is dragged while on the selected Vertex, then that
 * Vertex will be moved to follow the mouse until the button is released.
 *
 * @author joe
 */
public class VertexSelector<V, E> extends AbstractGraphMousePlugin implements MouseListener, MouseMotionListener {

    /**
     * the picked Vertex, if any
     */
    protected V currrentlySelectedVertex;

    /**
     * the picked Edge, if any
     */
    protected E selectedEdge;

    /**
     * additional modifiers for the action of adding to an existing selection
     */
    protected int addToSelectionModifiers;

    /**
     * create an instance with default settings
     */
    public VertexSelector() {
        this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
    }
    
    public void setCurrentlySelectedVertex(V vertexToSelect){
        currrentlySelectedVertex = vertexToSelect;
    }

    /**
     *
     * @param selectionModifiers for primary selection
     * @param addToSelectionModifiers for additional selection
     */
    public VertexSelector(int selectionModifiers, int addToSelectionModifiers) {
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
        down = e.getPoint();
        VisualizationViewer<V, E> graphDisplay = (VisualizationViewer<V, E>) e.getSource();
        //picking is the same as selecting
        GraphElementAccessor<V, E> pickSupport = graphDisplay.getPickSupport();
        PickedState<V> selectedVertexes = graphDisplay.getPickedVertexState();
        PickedState<E> pickedEdgeState = graphDisplay.getPickedEdgeState();
        if (pickSupport != null && selectedVertexes != null) {
            Layout<V, E> layout = graphDisplay.getGraphLayout();
            if (e.getModifiers() == modifiers) {
                // p is the screen point where the mouse click occured
                Point2D ip = e.getPoint();
                currrentlySelectedVertex = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if (currrentlySelectedVertex != null) {
                    boolean addToCurrentSelection = e.getModifiers() == addToSelectionModifiers;
                    selectVertexes(graphDisplay, currrentlySelectedVertex, addToCurrentSelection);
                } else if ((selectedEdge = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    e.consume();
                    //an edge has been picked
                    selectedVertexes.clear();
                    pickedEdgeState.clear();
                    pickedEdgeState.pick(selectedEdge, true);
                } else {
                    //nothing has been picked
                    selectedVertexes.clear();
                    pickedEdgeState.clear();
                }
            }
        }
    }

    /**
     * clean up settings from mousePressed
     */
    public void mouseReleased(MouseEvent e) {
        down = null;
        selectedEdge = null;
    }

    /**
     * If the mouse is over a selected vertex, drag the picked vertex with the
     * mouse. Also move all vertexes in its group, if the vertex implements the
     * interface VertexGroup.
     */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        VisualizationViewer<V, E> graphDisplay = (VisualizationViewer<V, E>) e.getSource();
        if (currrentlySelectedVertex != null) {
            e.consume();
            Point p = e.getPoint();
            Point2D graphPoint = graphDisplay.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
            Point2D graphDown = graphDisplay.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
            Layout<V, E> layout = graphDisplay.getGraphLayout();
            //Get relative mouse movement
            double dx = graphPoint.getX() - graphDown.getX();
            double dy = graphPoint.getY() - graphDown.getY();
            PickedState<V> ps = graphDisplay.getPickedVertexState();
            //move all selected vertices by the relative mouse movement
            for (V v : ps.getPicked()) {
                Point2D vertexPos = layout.transform(v);
                vertexPos.setLocation(vertexPos.getX() + dx, vertexPos.getY() + dy);
                layout.setLocation(v, vertexPos);
            }
            down = p;
        }
        graphDisplay.repaint();
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

    
    @SuppressWarnings("unchecked")
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * Select the currently selected vertex vertexToSelect, or the complete group 
     * of vertexes it belongs to. Nubisave components for instance 
     * consist of a group of vertexes (VertexGroup) including the component,
     * as well as the in- and outgoing ports.
     * @param graphDisplay the VisualizationViewer responsible for displaying the graph
     * @param vertexToSelect the vertex to select
     * @param addToCurrentSelection if true, add vertexToSelect to current selection \
instead of clearing the current selection
     */
    protected void selectVertexes(VisualizationViewer<V, E> graphDisplay, V vertexToSelect, boolean addToCurrentSelection) {
        PickedState<V> selectedVertexes = graphDisplay.getPickedVertexState();
        Layout<V, E> layout = graphDisplay.getGraphLayout();
        Set<V> picked = new HashSet<V>();
        if(!addToCurrentSelection)
            selectedVertexes.clear();
        if (selectedVertexes.isPicked(vertexToSelect) == false) {
            if (vertexToSelect instanceof VertexGroup<?>) { 
                //select the whole group
                picked = ((VertexGroup<V>) vertexToSelect).getVertexGroupMembers();
            } else {
                picked.add(vertexToSelect);
            }
            for (V v : picked) {
                selectedVertexes.pick(v, true); //pick means select
                layout.transform(v);
            }
        }
    }
}
