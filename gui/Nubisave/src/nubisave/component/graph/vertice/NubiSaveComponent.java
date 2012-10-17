package nubisave.component.graph.vertice;

import java.awt.Point;
import java.io.IOException;
import nubisave.Nubisave;


public class NubiSaveComponent extends AbstractNubisaveComponent {
    private Point graphLocation;

    public NubiSaveComponent() throws IOException{
        addRequiredPort();
        addProvidedPort();
        if(Nubisave.mainSplitter.isMounted()){
            drawCheckMark(40, 0);
        }
    }

    @Override
    public void showConfigurationDialog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void toggleActivate() {
        System.out.println("toggle activation state");
        if(! Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.mount();
            drawCheckMark(40, 0);
        } else {
            Nubisave.mainSplitter.unmount();
            undoCheckMark();
        }
    }

    @Override
    public void deactivate() {
        if(Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.unmount();
            undoCheckMark();
        }
    }

    @Override
    public void activate() {
        if(! Nubisave.mainSplitter.isMounted()){
            Nubisave.mainSplitter.mount();
            drawCheckMark(40, 0);
        }
    }

    @Override
    public boolean isActive() {
        return Nubisave.mainSplitter.isMounted();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setGraphLocation(Point location) {
        graphLocation = location;
    }

    @Override
    public Point getGraphLocation() {
        return graphLocation;
    }

    @Override
    public void connectToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
    }

    @Override
    public void removeConnectionTo(AbstractNubisaveComponent abstractNubiSaveComponent) {
    }

    @Override
    public boolean isConnectedToProvidedPort(AbstractNubisaveComponent abstractNubiSaveComponent) {
        return false;
    }

    @Override
    public String getUniqueName() {
        return "NubiSave";
    }

    @Override
    public Boolean migrationIsSuccessful() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void migrateDataTo(AbstractNubisaveComponent componentTo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMigrationProgress() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getNrOfFilePartsToStore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
