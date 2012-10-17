package nubisave.component.graph.mouseplugins;

import nubisave.component.graph.mouseplugins.extension.NubisaveGraphEventListener;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Set;

import javax.swing.JComponent;

import org.apache.commons.collections15.Factory;

import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.NubiSaveComponent;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.ProvidedPort;
import nubisave.component.graph.vertice.RequiredPort;
import nubisave.component.graph.edge.RestrictedEdgeVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.util.ArrowFactory;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.LinkedHashSet;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.edge.WeightedNubisaveVertexEdge;
import sun.org.mozilla.javascript.ast.CatchClause;

/**
 * A plugin that can create vertices, and edges using mouse gestures.
 * Can be extended by adding NubisaveGraphEventListener instances.
 * 
 */
public class ExtensibleNubisaveComponentMousePlugin extends AbstractGraphMousePlugin
        implements MouseListener, MouseMotionListener {

    protected NubiSaveVertex startVertex;
    protected Point2D down;

    protected Line2D rawEdge = new Line2D.Float();
    protected Shape edgeShape;
    protected Shape rawArrowShape;
    protected Shape arrowShape;
    protected VisualizationServer.Paintable edgePaintable;
    protected VisualizationServer.Paintable arrowPaintable;
    protected EdgeType edgeIsDirected;
    protected Factory<AbstractNubisaveComponent> vertexFactory;
    protected Factory<? extends NubiSaveEdge> edgeFactory;
    protected HashSet<NubisaveGraphEventListener> eventListeners;

    public ExtensibleNubisaveComponentMousePlugin(Factory<AbstractNubisaveComponent> vertexFactory,
            Factory<? extends NubiSaveEdge> edgeFactory) {
        this(MouseEvent.BUTTON1_MASK, vertexFactory, edgeFactory);
    }

    /**
     * create instance and prepare shapes for visual effects
     *
     * @param modifiers
     */
    public ExtensibleNubisaveComponentMousePlugin(int modifiers, Factory<AbstractNubisaveComponent> vertexFactory,
            Factory<? extends NubiSaveEdge> edgeFactory) {
        super(modifiers);
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        rawEdge.setLine(0.0f, 0.0f, 1.0f, 0.0f);
        rawArrowShape = ArrowFactory.getNotchedArrow(20, 16, 8);
        edgePaintable = new EdgePaintable();
        arrowPaintable = new ArrowPaintable();
        this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
        eventListeners = new LinkedHashSet<NubisaveGraphEventListener>();
    }

    public void addEventListener(NubisaveGraphEventListener l){
        eventListeners.add(l);
    }

    public HashSet<NubisaveGraphEventListener> getEventListeners(){
        return eventListeners;
    }

    public void removeEventListener(NubisaveGraphEventListener l){
        eventListeners.remove(l);
    }

    /**
     * Overridden to be more flexible, and pass events with key combinations.
     * The default responds to both ButtonOne and ButtonOne+Shift
     */
    @Override
    public boolean checkModifiers(MouseEvent e) {
        return (e.getModifiers() & modifiers) != 0;
    }

    /**
     * If the mouse is pressed in an empty area, create a new vertex there. If
     * the mouse is pressed on an existing vertex, prepare to create an edge
     * from that vertex to another
     */
    @SuppressWarnings("unchecked")
    public void mousePressed(MouseEvent e) {
        if (checkModifiers(e)) {
            final VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv = (VisualizationViewer<NubiSaveVertex, NubiSaveEdge>) e
                    .getSource();
            final Point2D p = e.getPoint();
            GraphElementAccessor<NubiSaveVertex, NubiSaveEdge> pickSupport = vv.getPickSupport();
            Set<NubiSaveVertex> picked;
            if (pickSupport != null) {
                Graph<NubiSaveVertex, NubiSaveEdge> graph = vv.getModel().getGraphLayout().getGraph();
                // set default edge type
                if (graph instanceof DirectedGraph) {
                    edgeIsDirected = EdgeType.DIRECTED;
                } else {
                    edgeIsDirected = EdgeType.UNDIRECTED;
                }

                final NubiSaveVertex vertex = pickSupport.getVertex(vv.getModel()
                        .getGraphLayout(), p.getX(), p.getY());

                if (vertex != null) { // get ready to make an edge

                    if(e.getModifiers() == modifiers && e.getClickCount() == 2){
                        if(vertex instanceof NubiSaveVertex) {
                            for(NubisaveGraphEventListener l: eventListeners){
                                l.processDoubleClickOnNubisaveVertex(vertex, e);
                            }
                        }
                    }

                    for(NubisaveGraphEventListener l: eventListeners){
                        try{
                            l.processEdgeCreationStart(vertex, e);
                        } catch(Exception exception){
                            return;
                        }
                    }
                    System.out.println("start new edge");
                    startVertex = vertex;
                    down = e.getPoint();
                    transformEdgeShape(down, down);
                    vv.addPostRenderPaintable(edgePaintable);
                    if ((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0
                            && vv.getModel().getGraphLayout().getGraph() instanceof UndirectedGraph == false) {
                        edgeIsDirected = EdgeType.DIRECTED;
                    }
                    if (edgeIsDirected == EdgeType.DIRECTED) {
                        transformArrowShape(down, e.getPoint());
                        vv.addPostRenderPaintable(arrowPaintable);
                    }
                } else { 
                    for(NubisaveGraphEventListener l: eventListeners){
                        l.processClickOnEmptySpace(e);
                    }
                }
            }
            vv.repaint();
        }
    }

    /**
     * If startVertex is non-null, and the mouse is released over an existing
     * vertex, create an undirected edge from startVertex to the vertex under
     * the mouse pointer. If shift was also pressed, create a directed edge
     * instead.
     */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
        if (checkModifiers(e)) {
            final VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv = (VisualizationViewer<NubiSaveVertex, NubiSaveEdge>) e.getSource();
            final Point2D p = e.getPoint();
            Layout<NubiSaveVertex, NubiSaveEdge> layout = vv.getModel().getGraphLayout();
            GraphElementAccessor<NubiSaveVertex, NubiSaveEdge> pickSupport = vv.getPickSupport();
            if (pickSupport != null) {
                NubiSaveVertex vertex = pickSupport.getVertex(layout, p.getX(),
                        p.getY());
                if (vertex != null && startVertex != null) {
                    for(NubisaveGraphEventListener l: eventListeners){
                        l.processCreateNubisaveEdge(startVertex, vertex, e);
                    }
                }
            }
            vv.repaint();
            startVertex = null;
            down = null;
            edgeIsDirected = EdgeType.UNDIRECTED;
            vv.removePostRenderPaintable(edgePaintable);
            vv.removePostRenderPaintable(arrowPaintable);
        }
    }

    /**
     * If startVertex is non-null, stretch an edge shape between startVertex and
     * the mouse pointer to simulate edge creation
     */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        if (checkModifiers(e)) {
            if (startVertex != null) {
                e.consume();
                transformEdgeShape(down, e.getPoint());
                if (edgeIsDirected == EdgeType.DIRECTED) {
                    transformArrowShape(down, e.getPoint());
                }
            }
            VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv = (VisualizationViewer<NubiSaveVertex, NubiSaveEdge>) e.getSource();
            vv.repaint();
        }
    }

    /**
     * code lifted from PluggableRenderer to move an edge shape into an
     * arbitrary position
     */
    private void transformEdgeShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        xform.scale(dist / rawEdge.getBounds().getWidth(), 1.0);
        edgeShape = xform.createTransformedShape(rawEdge);
    }

    private void transformArrowShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        arrowShape = xform.createTransformedShape(rawArrowShape);
    }

    /**
     * Used for the edge creation visual effect during mouse drag
     */
    class EdgePaintable implements VisualizationServer.Paintable {
        public void paint(Graphics g) {
            if (edgeShape != null) {
                Color oldColor = g.getColor();
                g.setColor(Color.black);
                ((Graphics2D) g).draw(edgeShape);
                g.setColor(oldColor);
            }
        }

        public boolean useTransform() {
            return false;
        }
    }

    /**
     * Used for the directed edge creation visual effect during mouse drag
     */
    class ArrowPaintable implements VisualizationServer.Paintable {
        public void paint(Graphics g) {
            if (arrowShape != null) {
                Color oldColor = g.getColor();
                g.setColor(Color.black);
                ((Graphics2D) g).fill(arrowShape);
                g.setColor(oldColor);
            }
        }

        public boolean useTransform() {
            return false;
        }
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
