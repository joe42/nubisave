package nubisave.component.graph.vertice.interfaces;

import java.util.Set;

/**
 * @param <V> Type of the vertices in the group
 */
public interface VertexGroup<V> {
	
    /**
     * @return the group members of the vertex including the vertex itself
     */
    public Set<V> getVertexGroupMembers();

    /**
     * Get the parent of the group member if the group is hierarchically ordered. Otherwise return the component itself.
     * @return the parent of the group member or the component itself
     */
    public V getParentComponent();
}
