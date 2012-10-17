package nubisave.component.graph.edge;

import nubisave.component.graph.edge.NubiSaveEdge;

/**
 * A {@link NubiSaveEdge} between two {@link DataVertex} instances.
 * The connection symbolizes a data transfer between the component instances corresponding to each {@link DataVertex} instances.
 */
public class DataVertexEdge implements NubiSaveEdge {
    private int transferProgress;

    /**
     * Create an edge with a {@link #getTransferProgress() transfer progress} of zero.
     */
    public DataVertexEdge() {
        transferProgress = 0;
    }

    /**
     * @return the transfer progress between 0 and 100
     */
    public int getTransferProgress() {
        return transferProgress;
    }

    /**
     * @param transferProgress the transfer progress between 0 and 100 to set
     */
    public void setTransferProgress(int transferProgress) {
        this.transferProgress = transferProgress;
    }

    @Override
    public String toString(){
        if(transferProgress == 100)
            return "complete";
        return String.valueOf(transferProgress)+"%";
    }
}
