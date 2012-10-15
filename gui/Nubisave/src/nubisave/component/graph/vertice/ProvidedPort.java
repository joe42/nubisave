package nubisave.component.graph.vertice;

import nubisave.component.graph.vertice.interfaces.VertexGroup;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;



public class ProvidedPort<V> extends AbstractPort<V> {	
	/**
	 * Create a new instance with the given visual representation and the parent of this VertexGroup.
	 * @param visualRepresentation
	 * @param parentComponent
	 */
	public ProvidedPort(VertexGroup<V> parentComponent, BufferedImage visualRepresentation){
		super(parentComponent, visualRepresentation);
	}

}
