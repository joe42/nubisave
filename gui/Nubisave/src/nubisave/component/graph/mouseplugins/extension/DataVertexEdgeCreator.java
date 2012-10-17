package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseEvent;
import javax.swing.Timer;
import nubisave.component.graph.mouseplugins.extension.NubisaveGraphEventListener;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.DataVertex;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import org.apache.commons.collections15.Factory;

/**
 * A NubiSaveGraphEventListener, which creates edges between DataVertex instances.
 * Moves data between component instances and displays the progress in an animation.
 * @author joe
 */
public class DataVertexEdgeCreator implements NubisaveGraphEventListener {
    protected VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;
    protected Factory<? extends DataVertexEdge> dataTransferEdgeFactory;

    public DataVertexEdgeCreator(VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv, Graph<NubiSaveVertex, NubiSaveEdge> graph, Factory<? extends DataVertexEdge> dataTransferEdgeFactory){
        this.vv = vv;
        this.graph = graph;
        this.dataTransferEdgeFactory = dataTransferEdgeFactory;
    }

    @Override
    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e) {
    }

    @Override
    public void processClickOnEmptySpace(MouseEvent e) {
    }
    @Override
    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e) {
        //TODO: add animation
        final AbstractNubisaveComponent start = (AbstractNubisaveComponent) ((VertexGroup) startVertex).getParentComponent();
        final AbstractNubisaveComponent end = (AbstractNubisaveComponent) ((VertexGroup) endVertex).getParentComponent();
        if(startVertex == endVertex) //prevent reflective connections
            return;
        if (startVertex instanceof DataVertex && endVertex instanceof DataVertex) {
            start.migrateDataTo(end);
            //visualization is deferred to JNotifyConfigUpdater
        }
    }

    @Override
    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception {
    }
}
