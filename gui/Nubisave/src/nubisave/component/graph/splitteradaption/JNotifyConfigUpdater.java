package nubisave.component.graph.splitteradaption;

import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import net.contentobjects.jnotify.JNotifyListener;
import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.component.graph.mouseplugins.extension.TransferProgressVisualizer;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.DataVertex;
import nubisave.component.graph.vertice.GenericNubiSaveComponent;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Predicate;

public class JNotifyConfigUpdater implements JNotifyListener {
    Factory<? extends DataVertexEdge> dataTransferEdgeFactory;
    private Graph<NubiSaveVertex, NubiSaveEdge> graph;
    private final VisualizationViewer vv;

    public JNotifyConfigUpdater(Factory<? extends DataVertexEdge> dataTransferEdgeFactory, Graph<NubiSaveVertex, NubiSaveEdge> graph, VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv) {
        this.dataTransferEdgeFactory = dataTransferEdgeFactory;
        this.graph = graph;
        this.vv = vv;
    }

    public void fileRenamed(int wd, String rootPath, String oldName,
            String newName) { //Means moving of data
        System.out.println("renamed " + rootPath + " : " + oldName + " -> " + newName);
        Timer migrationStateUpdateTimer = null;
        DataVertex start = getGenericNubiSaveComponentByName(oldName).getDataVertex();
        DataVertex end = getGenericNubiSaveComponentByName(newName).getDataVertex();
        migrationStateUpdateTimer = new Timer(500, null);
        migrationStateUpdateTimer.addActionListener(new TransferProgressVisualizer(start, end, dataTransferEdgeFactory, migrationStateUpdateTimer, graph, vv));
        migrationStateUpdateTimer.start();
    }

    public void fileModified(int wd, String rootPath, String name) { //Data tranfer
        System.out.println("modified " + rootPath + "/" + name);
        GenericNubiSaveComponent component = getGenericNubiSaveComponentByName(name);
        component.refreshConfiguration();
    }

    public void fileDeleted(int wd, String rootPath, String name) { // remove component from graph
        System.out.println("deleted " + rootPath + " : " + name);
        PickedState<NubiSaveVertex> pickedVertexState = vv.getPickedVertexState();
        AbstractNubisaveComponent component = getGenericNubiSaveComponentByName(name);
        ((AbstractNubisaveComponent) component).deactivate();
        ((AbstractNubisaveComponent) component).remove();
        Set<NubiSaveVertex> verticesToRemove = ((VertexGroup<NubiSaveVertex>) component).getVertexGroupMembers();
        System.out.println("\n\nremove vertex group of " + verticesToRemove.size());
        for (NubiSaveVertex NubiSaveVertex : verticesToRemove) {
            pickedVertexState.pick(NubiSaveVertex, false);
        }
        int index = Nubisave.services.getIndexByUniqueName(name);
        Nubisave.services.remove(index);
        vv.repaint();
    }

    public void fileCreated(int wd, String rootPath, String name) {
        GenericNubiSaveComponent newVertex = null;
        System.out.println("created " + rootPath + " : " + name);
        StorageService newService = new StorageService(new File(rootPath + "/" + name));
        try {
            newVertex = new GenericNubiSaveComponent(newService);
        } catch (IOException ex) {
            Logger.getLogger(JNotifyConfigUpdater.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(graph.containsVertex(newVertex)){
            System.out.println("hohohohohohoho");
            return;
        }
        Layout<NubiSaveVertex, NubiSaveEdge> layout = vv.getModel().getGraphLayout();
        newVertex.addToGraph((VisualizationViewer<NubiSaveVertex, NubiSaveEdge>) vv, new java.awt.Point((int)layout.getSize().getHeight()/2,(int)layout.getSize().getWidth()/2));
    }

    private GenericNubiSaveComponent getGenericNubiSaveComponentByName(final String name) {
        Graph<AbstractNubisaveComponent, Object> nubisaveComponentGraph = new VertexPredicateFilter(new Predicate() {
            @Override
            public boolean evaluate(Object vertex) {
                return vertex instanceof GenericNubiSaveComponent &&
                        ((GenericNubiSaveComponent)vertex).getUniqueName().equals(name);
            }
        }).transform(graph);
        if(! nubisaveComponentGraph.getVertices().iterator().hasNext() ){
            return null;
        }
        return (GenericNubiSaveComponent)nubisaveComponentGraph.getVertices().iterator().next();
    }

}
