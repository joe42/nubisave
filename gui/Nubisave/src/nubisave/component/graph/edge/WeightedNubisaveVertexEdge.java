package nubisave.component.graph.edge;

import nubisave.component.graph.edge.NubiSaveEdge;

/**
 * A weighted {@link NubiSaveEdge} between two {@link NubiSaveVertex} instances.
 * The weight determines the amount of file parts stored on the backend.
 */
public class WeightedNubisaveVertexEdge implements NubiSaveEdge {
    private int weight;

    /**
     * Create a weighted edge with weight 1.
     */
    public WeightedNubisaveVertexEdge() {
        setWeight(1);
    }
    /**
     * Create a weighted edge with weight <code>weight</code>.
     * @param weight
     */
    public WeightedNubisaveVertexEdge(int weight) {
        setWeight(weight);
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString(){
        return String.valueOf(weight);
    }

}
