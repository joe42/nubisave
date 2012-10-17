package nubisave.component.graph.vertice;

import nubisave.component.graph.vertice.interfaces.BufferedImageDelegator;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import java.awt.image.BufferedImage;
import java.util.Set;
import nubisave.component.graph.DontHighlight;

/**
 *
 * @author joe
 */
public class DataVertex<V> implements BufferedImageDelegator, DontHighlight, NubiSaveVertex, VertexGroup<V> {

    protected BufferedImage visualRepresentation;
    protected VertexGroup<V> parentComponent;
    /**
     * Create a new instance with the given visual representation and the parent of this VertexGroup.
     * @param visualRepresentation
     * @param parentComponent the parent component in a hierarchically ordered group or null otherwise
     */
    public <X extends VertexGroup,V> DataVertex(X parentComponent, BufferedImage visualRepresentation){
            this.parentComponent = parentComponent;
            this.visualRepresentation = visualRepresentation;
    }

    @Override
    public BufferedImage getBufferedImage() {
            return visualRepresentation;
    }

    @Override
    public void setBufferedImage(BufferedImage bufferedImage) {
            visualRepresentation = bufferedImage;
    }

    @Override
    public Set<V> getVertexGroupMembers() {
            return ((VertexGroup)parentComponent).getVertexGroupMembers();
    }

    @Override
    public V getParentComponent() {
            return (V)parentComponent;
    }
}

