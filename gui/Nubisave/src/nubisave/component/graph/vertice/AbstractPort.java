package nubisave.component.graph.vertice;

import nubisave.component.graph.vertice.interfaces.VertexGroup;
import nubisave.component.graph.vertice.interfaces.Port;
import java.awt.image.BufferedImage;
import java.util.Set;
import nubisave.component.graph.vertice.interfaces.Port;
import nubisave.component.graph.vertice.interfaces.VertexGroup;

public abstract class AbstractPort<V> implements Port<V> {

    protected BufferedImage visualRepresentation;
    protected VertexGroup<V> parentComponent;
    /**
     * Create a new instance with the given visual representation and the parent of this VertexGroup.
     * @param visualRepresentation
     * @param parentComponent
     */
    public <X extends VertexGroup,V> AbstractPort(X parentComponent, BufferedImage visualRepresentation){
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
            return parentComponent.getVertexGroupMembers();
    }

    @Override
    public V getParentComponent() {
            return (V)parentComponent;
    }
}
