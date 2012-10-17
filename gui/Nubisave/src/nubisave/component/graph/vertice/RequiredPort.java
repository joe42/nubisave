package nubisave.component.graph.vertice;

import nubisave.component.graph.vertice.interfaces.VertexGroup;
import nubisave.component.graph.edge.RestrictedEdgeVertex;
import java.awt.image.BufferedImage;
import java.util.Set;

import javax.swing.ImageIcon;



public class RequiredPort<V> extends AbstractPort<V> implements RestrictedEdgeVertex {
	protected int maxDegree;
	

	/**
	 * Create a new instance with the given visual representation and the parent of this VertexGroup.
	 * The maximal degree defaults to Integer.MAX_VALUE.
	 * @param visualRepresentation
	 * @param parentComponent
	 */
	public RequiredPort(VertexGroup<V> parentComponent, BufferedImage visualRepresentation){
		super(parentComponent, visualRepresentation);
		maxDegree = Integer.MAX_VALUE;
	}
	/**
	 * Create a new instance with the given visual representation and a degree of max.
	 * Create a new instance with the given visual representation and the parent of this VertexGroup.
	 * The maximal degree is set to max.
	 * @param visualRepresentation
	 * @param parentComponent
	 * @param max
	 */
	public RequiredPort(VertexGroup<V> parentComponent, BufferedImage visualRepresentation, int max) {
		super(parentComponent, visualRepresentation);
		maxDegree = max;
	}

	@Override
	public int getMaxDegree() {
		return maxDegree;
	}
	
	public void setMaxDegree(int max){
		maxDegree = max;
	}
	
}
