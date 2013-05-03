package nubisave.component.graph.mouseplugins;


import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import org.apache.commons.collections15.Factory;

import nubisave.component.graph.vertice.interfaces.VertexGroup;


import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.*;
import nubisave.component.graph.vertice.GenericNubiSaveComponent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;

/**
 * a plugin that uses popup menus to delete vertices.
 */
public class PopupEditor extends AbstractPopupGraphMousePlugin {
    
    protected Factory<? extends NubiSaveEdge> edgeFactory;
    protected JPopupMenu popup = new JPopupMenu();

    public PopupEditor(Factory<? extends NubiSaveEdge> edgeFactory) {
        this.edgeFactory = edgeFactory;
    }
    
    @SuppressWarnings({ "unchecked", "serial", "serial" })
    protected void handlePopup(MouseEvent NubiSaveEdge) {
        final VisualizationViewer<NubiSaveVertex,NubiSaveEdge> vv =
            (VisualizationViewer<NubiSaveVertex,NubiSaveEdge>)NubiSaveEdge.getSource();
        final Layout<NubiSaveVertex,NubiSaveEdge> layout = vv.getGraphLayout();
        final Graph<NubiSaveVertex,NubiSaveEdge> graph = layout.getGraph();
        final Point2D p = NubiSaveEdge.getPoint();
        final Point2D ivp = p;
        GraphElementAccessor<NubiSaveVertex,NubiSaveEdge> pickSupport = vv.getPickSupport();
        if(pickSupport != null) {
            
            final NubiSaveVertex vertex = pickSupport.getVertex(layout, ivp.getX(), ivp.getY());
            final NubiSaveEdge edge = pickSupport.getEdge(layout, ivp.getX(), ivp.getY());
            final PickedState<NubiSaveEdge> pickedEdgeState = vv.getPickedEdgeState();
            final PickedState<NubiSaveVertex> pickedVertexState = vv.getPickedVertexState();

            popup.removeAll();
            if(vertex != null) {
                popup.add(new AbstractAction("Configure Component") {
                    public void actionPerformed(ActionEvent NubiSaveEdge) {
                    	System.out.println("configure component");
                      
                         if(vertex instanceof AbstractNubisaveComponent) {
                       
                            ((AbstractNubisaveComponent)vertex).showConfigurationDialog();                
                           vv.repaint(); 
                        }
                    }
                });

                popup.add(new AbstractAction("Delete Component") {
                    public void actionPerformed(ActionEvent NubiSaveEdge) {
                        Set<NubiSaveVertex> verticesToRemove = new HashSet<NubiSaveVertex>();
                        System.out.println("remove vertex");
                        if (vertex instanceof AbstractNubisaveComponent) {
                            String[] options = {"No", "Remove"};
                            int n = JOptionPane.showOptionDialog(null,
                                    "Remove and unmount " + ((AbstractNubisaveComponent)vertex).getName() + "?",
                                    "Remove?",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE,
                                    null,
                                    options,
                                    options[1]);
                            if (n == 1) {
                                ((AbstractNubisaveComponent)vertex).deactivate();
                                ((AbstractNubisaveComponent)vertex).remove();
                                verticesToRemove = ((VertexGroup<NubiSaveVertex>) vertex).getVertexGroupMembers();
                                System.out.println("\n\nremove vertex group of "+verticesToRemove.size());

                            }
                            vv.repaint();
                        }
                        if (vertex instanceof VertexGroup<?>) {
                            verticesToRemove = ((VertexGroup<NubiSaveVertex>) vertex).getVertexGroupMembers();
                        } else {
                            verticesToRemove.add(vertex);
                        }
                        for (NubiSaveVertex NubiSaveVertex : verticesToRemove) {
                            System.out.println("\nremove vertex: "+NubiSaveVertex);
                            pickedVertexState.pick(NubiSaveVertex, false);
                            System.out.println("removal successful: "+graph.removeVertex(NubiSaveVertex));
                        }
                            
                    vv.repaint();
                }});

                popup.addSeparator();

                popup.add(new AbstractAction("Open Location") {
                    public void actionPerformed(ActionEvent NubiSaveEdge) {
                        System.out.println("open location ...");
                        if (vertex instanceof AbstractNubisaveComponent) {
                            ((AbstractNubisaveComponent)vertex).openLocation();
                        }
                    }
                });

                popup.addSeparator();

                String mount = "Mount";
                if(((AbstractNubisaveComponent)vertex).isActive()) {
                    mount = "Unmount";
                }
                popup.add(new AbstractAction(mount) {
                    public void actionPerformed(ActionEvent NubiSaveEdge) {
                        System.out.println("mount or unmount storage...");
                        if (vertex instanceof AbstractNubisaveComponent) {
                            if(((AbstractNubisaveComponent)vertex).isActive()) {
                                ((AbstractNubisaveComponent)vertex).deactivate();
                            } else {
                                ((AbstractNubisaveComponent)vertex).activate();
                            }
                            vv.repaint();
                        }
                    }
                });
            } else if(edge != null) {
                popup.add(new AbstractAction("Delete Edge") {
                    public void actionPerformed(ActionEvent NubiSaveEdge) {
                        pickedEdgeState.pick(edge, false);
                        graph.removeEdge(edge);
                        if(graph.getSource(edge) instanceof AbstractNubisaveComponent && graph.getDest(edge) instanceof GenericNubiSaveComponent){
                            GenericNubiSaveComponent source = (GenericNubiSaveComponent) graph.getSource(edge);
                            GenericNubiSaveComponent dest = (GenericNubiSaveComponent) graph.getDest(edge);
                            source.removeConnectionTo(dest);
                        }
                        vv.repaint();
                    }
                });
            } 
            if(popup.getComponentCount() > 0) {
                popup.show(vv, NubiSaveEdge.getX(), NubiSaveEdge.getY());
            }
        }
    }
}

