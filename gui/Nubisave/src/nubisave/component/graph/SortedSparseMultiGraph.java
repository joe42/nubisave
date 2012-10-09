package nubisave.component.graph;


import java.util.LinkedHashMap;
import java.util.Set;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * A SparseMultigraph which keeps vertices sorted by insertion order, 
 * so that in the visual representation recently added vertices 
 * overlap previously added vertices.  
 */
@SuppressWarnings("serial")
public class SortedSparseMultiGraph<V,E> extends SparseMultigraph<V,E> {
	

    /**
     * Creates a new instance.
     */
    public SortedSparseMultiGraph()
    {
        vertices = new LinkedHashMap<V, Pair<Set<E>>>();
    }
}
