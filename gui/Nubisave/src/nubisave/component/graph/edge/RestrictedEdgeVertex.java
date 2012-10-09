package nubisave.component.graph.edge;

/**
 * A vertex with a restricted number of edges. 
 * 
 * @author joe
 */
public interface RestrictedEdgeVertex {
	/**
	 * @return the maximal number of edges for the vertex
	 */
	public int getMaxDegree();
	/**
	 * Set the maximal number of edges according to the policies of the component.
	 * @param max the desired maximum number of edges for the component
	 */
	public void setMaxDegree(int max);
}
