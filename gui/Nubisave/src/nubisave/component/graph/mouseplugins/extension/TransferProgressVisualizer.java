package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.DataVertex;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import org.apache.commons.collections15.Factory;

/**
 *
 * @author joe
 */
public class TransferProgressVisualizer implements ActionListener {
    protected DataVertex startVertex;
    protected DataVertex endVertex;
    protected final VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;
    protected AbstractNubisaveComponent start;
    protected DataVertexEdge dataTransferEdge;
    protected Factory<? extends DataVertexEdge> dataTransferEdgeFactory;
    protected Timer migrationStateUpdateTimer;

    public TransferProgressVisualizer(DataVertex startVertex, DataVertex endVertex, Factory<? extends DataVertexEdge> dataTransferEdgeFactory,
            Timer migrationStateUpdateTimer, Graph<NubiSaveVertex, NubiSaveEdge> graph, VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv) {
        this.startVertex = startVertex;
        this.endVertex = endVertex;
        start = (AbstractNubisaveComponent) ((VertexGroup) startVertex).getParentComponent();
        this.dataTransferEdgeFactory = dataTransferEdgeFactory;
        this.migrationStateUpdateTimer = migrationStateUpdateTimer;
        this.vv = vv;
        this.graph = graph;
        dataTransferEdge = dataTransferEdgeFactory.create();
        graph.addEdge(dataTransferEdge, startVertex, endVertex);
        dataTransferEdge.setTransferProgress(start.getMigrationProgress());
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        dataTransferEdge.setTransferProgress(start.getMigrationProgress());
        if (start.migrationIsSuccessful() != null) {
            if (start.migrationIsSuccessful()) {
                dataTransferEdge.setTransferProgress(100);
                System.out.println("successful transfer");
                vv.paint(vv.getGraphics()); //show that transfer is complete for a second
                vv.repaint(0);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                }
                migrationStateUpdateTimer.stop();
            } else {
                System.out.println("Error when transfering data in DataVertexEdgeCreator.");
            }
            graph.removeEdge(dataTransferEdge);
        }
        vv.repaint();
    }
}
