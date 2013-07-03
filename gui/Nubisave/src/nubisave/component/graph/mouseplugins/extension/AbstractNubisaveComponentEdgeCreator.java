package nubisave.component.graph.mouseplugins.extension;

import com.github.joe42.splitter.util.file.PropertiesUtil;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nubisave.component.graph.mouseplugins.extension.NubisaveGraphEventListener;
import nubisave.component.graph.splitteradaption.NubisaveEditor.WeightedNubisaveVertexEdgeFactory;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.ProvidedPort;
import nubisave.component.graph.vertice.RequiredPort;
import nubisave.component.graph.edge.RestrictedEdgeVertex;
import nubisave.component.graph.edge.WeightedNubisaveVertexEdge;
import nubisave.component.graph.vertice.interfaces.VertexGroup;

/**
 * Create NubiSaveEdge instances between NubiSaveVertex instances. Also add the edge to the graph.
 */
public class AbstractNubisaveComponentEdgeCreator implements NubisaveGraphEventListener{
    protected VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv;
    protected Graph<NubiSaveVertex, NubiSaveEdge> graph;
    protected WeightedNubisaveVertexEdgeFactory edgeFactory;
    private String storage_directory;

    public AbstractNubisaveComponentEdgeCreator(VisualizationViewer<NubiSaveVertex, NubiSaveEdge> vv, Graph<NubiSaveVertex, NubiSaveEdge> graph, WeightedNubisaveVertexEdgeFactory edgeFactory){
        this.vv = vv;
        this.graph = graph;
        this.edgeFactory = edgeFactory;
        storage_directory = new PropertiesUtil("nubi.properties").getProperty("storage_configuration_directory");
    }

    @Override
    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e) {
    }

    @Override
    public void processClickOnEmptySpace(MouseEvent e) {
    }

    @Override
    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e) {
        if (endVertex instanceof RequiredPort) { //switch required port to startVertex (=sourceVertex)
            NubiSaveVertex tmp = startVertex;
            startVertex = endVertex;
            endVertex = tmp;
        }
        connect(startVertex, endVertex);
    }

    @Override
    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception {
    }

    /**
     * Connect vertices in the graph.
     * Connects vertices if startVertex and endVertex may be connected by a directed edge.
     * Ensures that:
     * 	* startVertex is a RequiredPort and endVertex is a ProvidedPort
     *  * the required port has not yet reached the maximal number of connections {@link RequiredPort.getMaxDegree() getMaxDegree}
     *  * the ports do not belong to the same component
     *  * the ports are not yet connected
     * @param startVertex
     * @param endVertex
     * @param graph
     * @return true if the two vertexes were connected
     */
    protected boolean connect(NubiSaveVertex startVertex, NubiSaveVertex endVertex) {
        boolean shouldNotConnect = shouldNotConnect(startVertex, endVertex);
        System.out.println("nubisavecomponentgraphmouseplugin: connect");
        if (shouldNotConnect) {
            System.out.println("nubisavecomponentgraphmouseplugin: returning false");
            return false;

        }
        AbstractNubisaveComponent start = (AbstractNubisaveComponent) ((RequiredPort) startVertex).getParentComponent();
        AbstractNubisaveComponent end = (AbstractNubisaveComponent) ((ProvidedPort) endVertex).getParentComponent();
        if(! isConnected(startVertex, endVertex)) {     
                BufferedWriter writer = null ;
            try {
                System.out.println("is not connected");
                start.connectToProvidedPort(end);
                WeightedNubisaveVertexEdge edge = edgeFactory.create();
                edge.setWeight(end.getNrOfFilePartsToStore());
                graph.addEdge(edge, startVertex, endVertex, EdgeType.DIRECTED);
                File file = new File(storage_directory+"/"+ "connections.txt");
                if(!file.exists()){
                    file.createNewFile();
                }
                writer = new BufferedWriter(new FileWriter(file,true));
                writer.write(start.getUniqueName());
                writer.write(" ");
                writer.write(end.getUniqueName());
                writer.newLine();
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(AbstractNubisaveComponentEdgeCreator.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(AbstractNubisaveComponentEdgeCreator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        } else {
            System.out.println("is  connected --> increase weight");
            WeightedNubisaveVertexEdge edge = (WeightedNubisaveVertexEdge) graph.findEdge(startVertex, endVertex);
            System.out.println("edge weight: "+edge.getWeight());
            int before = end.getNrOfFilePartsToStore();
            System.out.println("nroffileparts1: "+before);
            end.setNrOfFilePartsToStore(end.getNrOfFilePartsToStore()+1);
            System.out.println("nroffileparts2: "+end.getNrOfFilePartsToStore());
            assert (end.getNrOfFilePartsToStore() - 1) == before;
            edge.setWeight(end.getNrOfFilePartsToStore());
            assert (edge.getWeight() - 1) == before;
            System.out.println("incrreased edge weight: "+edge.getWeight());
        }
        System.out.println("nubisavecomponentgraphmouseplugin: returning true");
        return true;

    }

    protected boolean shouldNotConnect(NubiSaveVertex startVertex, NubiSaveVertex endVertex) {
        boolean shouldNotConnect = hasReachedMaxEdgeNumber(startVertex)
                || !(startVertex instanceof RequiredPort<?>)
                || !(endVertex instanceof ProvidedPort<?>);
                //|| hasSameVertexGroup(endVertex, startVertex);
        
        return shouldNotConnect;
    }

    private boolean isConnected(NubiSaveVertex vertex1, NubiSaveVertex vertex2) {
        return graph.isNeighbor(vertex1, vertex2);
    }

    private boolean hasReachedMaxEdgeNumber(NubiSaveVertex vertex) {
        return (vertex instanceof RestrictedEdgeVertex)
                && ((RestrictedEdgeVertex) vertex).getMaxDegree() <= graph.getIncidentEdges(vertex).size();
    }

    /**
     * @param vertex1
     * @param vertex2
     * @return true if vertex1 and vertex2 are in the same vertex group
     */
    private boolean hasSameVertexGroup(NubiSaveVertex vertex1, NubiSaveVertex vertex2) {
        return vertex1 instanceof VertexGroup<?> && ((VertexGroup) vertex1).getVertexGroupMembers().contains(vertex2);
    }

}
