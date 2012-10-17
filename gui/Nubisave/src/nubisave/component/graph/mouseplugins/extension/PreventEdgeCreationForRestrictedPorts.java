package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseEvent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.edge.RestrictedEdgeVertex;

/**
 *  Stops edge creations when the starting point is a vertex of a RestrictedPort which has already reached its maximum number of connections.
 */
public class PreventEdgeCreationForRestrictedPorts implements NubisaveGraphEventListener{
    protected VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;

    public PreventEdgeCreationForRestrictedPorts(VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv, Graph<NubiSaveVertex, NubiSaveEdge> graph){
        this.vv = vv;
        this.graph = graph;
    }

    @Override
    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e) {
    }

    @Override
    public void processClickOnEmptySpace(MouseEvent e) {
    }

    @Override
    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e) {
    }

    @Override
    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception {
        if(vertex instanceof RestrictedEdgeVertex) {
            if(((RestrictedEdgeVertex) vertex).getMaxDegree()<= graph.getIncidentEdges(vertex).size())
                throw new Exception("RestrictedEdgeVertex instance has reached maximum number of edges.");
        }
    }

}
