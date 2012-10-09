package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseEvent;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;

/**
 * Toggles AbstractNubisaveComponents between active and deactivated state, when the corresponding vertex is doubleclicked.
 */
public class ToggleActivateNubisaveComponentOnDoubleClick implements NubisaveGraphEventListener{
    protected VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;

    public ToggleActivateNubisaveComponentOnDoubleClick(VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv, Graph<NubiSaveVertex, NubiSaveEdge> graph){
        this.vv = vv;
        this.graph = graph;
    }

    @Override
    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e) {
        if(vertex instanceof AbstractNubisaveComponent) {
            ((AbstractNubisaveComponent) vertex).toggleActivate();
            System.out.println("doubleclick");
        }
    }

    @Override
    public void processClickOnEmptySpace(MouseEvent e) {
    }

    @Override
    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e) {
    }

    @Override
    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception {
    }

}
