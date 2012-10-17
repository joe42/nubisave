/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.MouseEvent;
import java.util.Set;
import nubisave.component.graph.mouseplugins.extension.NubisaveGraphEventListener;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import org.apache.commons.collections15.Factory;

/**
 * Creates new AbstractNubiSaveComponent instances when a free space on the graph is clicked.
 */
public class AbstractNubiSaveComponentCreator implements NubisaveGraphEventListener {
    protected VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;
    protected Factory<AbstractNubisaveComponent> vertexFactory;

    public AbstractNubiSaveComponentCreator(VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv, Graph<NubiSaveVertex, NubiSaveEdge> graph, Factory<AbstractNubisaveComponent> vertexFactory){
        this.vv = vv;
        this.graph = graph;
        this.vertexFactory = vertexFactory;
    }

    @Override
    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e) {
    }

    @Override
    public void processClickOnEmptySpace(MouseEvent e) {
        System.out.println("make new component");
        AbstractNubisaveComponent newVertex = null;
        newVertex = vertexFactory.create();
        if(newVertex != null){
            newVertex.addToGraph((VisualizationViewer<NubiSaveVertex, NubiSaveEdge>) vv, e.getPoint());
            PickedState<NubiSaveVertex> pickedVertexState = vv.getPickedVertexState();
            pickedVertexState.clear();
            Set<NubiSaveVertex> picked = ((VertexGroup<NubiSaveVertex>) newVertex).getVertexGroupMembers();
            for (NubiSaveVertex v : picked) {
                    pickedVertexState.pick((NubiSaveVertex)v, true);
            }
        }
    }

    @Override
    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e) {
    }

    @Override
    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception {
    }

}
