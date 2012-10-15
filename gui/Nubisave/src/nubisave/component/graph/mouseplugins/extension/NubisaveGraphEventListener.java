package nubisave.component.graph.mouseplugins.extension;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import java.awt.event.MouseEvent;
import nubisave.component.graph.edge.NubiSaveEdge;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;

/**
 * Listens and processes events from the NubisaveEditor.
 */
public interface NubisaveGraphEventListener {


    public void processDoubleClickOnNubisaveVertex(NubiSaveVertex vertex, MouseEvent e);

    public void processClickOnEmptySpace(MouseEvent e);

    public void processCreateNubisaveEdge(NubiSaveVertex startVertex, NubiSaveVertex endVertex, MouseEvent e);

    public void processEdgeCreationStart(NubiSaveVertex vertex, MouseEvent e) throws Exception;
}
