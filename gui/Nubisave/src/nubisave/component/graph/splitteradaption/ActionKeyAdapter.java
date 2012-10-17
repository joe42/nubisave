/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.component.graph.splitteradaption;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.picking.PickedState;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;
import nubisave.component.graph.vertice.interfaces.VertexGroup;

/**
 * Listens to key events of a {@link VisualizationViewer}.
 * Removes vertices or members of a {@link VertexGroup} if they are picked according to a {@link PickedState} and the Del Key is pressed.
 * Also deactivates and removes {@link AbstractNubiSaveComponent} instances.
 */
public class ActionKeyAdapter<V> extends KeyAdapter {
    protected PickedState<V> pickedVertexState;
    protected Graph graph;

    public ActionKeyAdapter(PickedState<V> pickedVertexState, Graph graph) {
        this.pickedVertexState = pickedVertexState;
        this.graph = graph;
    }

    @Override
    public void keyTyped(KeyEvent event) {
        char keyChar = event.getKeyChar();
        if(keyChar == KeyEvent.VK_DELETE) {
            Set<V> verticesToRemove = new HashSet<V>();
            Set<V> picked = pickedVertexState.getPicked();
            for(V vertex: picked){
                if (vertex instanceof VertexGroup<?>) {
                        verticesToRemove.addAll( ((VertexGroup<V>) vertex).getVertexGroupMembers() );
                } else {
                    verticesToRemove.add(vertex);
                }
            }
            for (V v : verticesToRemove) {
                System.out.println("\nremove vertex: "+v);
                pickedVertexState.pick(v, false);
                if(v instanceof AbstractNubisaveComponent) {
                    ((AbstractNubisaveComponent)v).deactivate();
                    ((AbstractNubisaveComponent)v).remove();
                }
                graph.removeVertex(v);
            }
        }
    }
}
