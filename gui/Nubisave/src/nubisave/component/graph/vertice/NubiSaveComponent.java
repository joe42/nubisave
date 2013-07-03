package nubisave.component.graph.vertice;

import java.awt.Point;
import java.io.IOException;
import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.ui.NubisaveConfigDlg;


public class NubiSaveComponent extends AbstractNubisaveComponent {
    private Point graphLocation;
    protected final StorageService component;
    
    public NubiSaveComponent(StorageService component) throws IOException{
        this.component=component;
        addRequiredPort();
        addProvidedPort();
        if(Nubisave.mainSplitter.isMounted()){
            drawCheckMark(40, 0);
       }
    }

    @Override
    public void showConfigurationDialog() {
     NubisaveConfigDlg nubi=new NubisaveConfigDlg();
     nubi.setModal(true);
     nubi.setTitle("Nubisave Component Configuration");
     nubi.pack();
     nubi.setLocationRelativeTo(null);
     nubi.setVisible(true);
     
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
        //graphLocation = location;
        component.setGraphLocation(location);
        //Nubisave.services.updateNubisave(component);
        Nubisave.services.update(component);
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
        //return "NubiSave";
        return component.getUniqName();
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
        return component.getNrOfFilePartsToStore();
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNrOfFilePartsToStore(Integer nrOfFilePartsToStore) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void openLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
