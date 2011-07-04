/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import nubisave.*;

/**
 *
 * @author demo
 */
public class NubiTableModel extends AbstractTableModel {

    private List<MatchmakerService> mmServices;
    private List<AgreementService> aServices;
    private List<CustomMntPoint> cstmMntPnts;
    
    private final String headers[] = {"Use", "Type", "Description", "Edit"};
    
    private Class[] types = new Class[]{
        java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, javax.swing.JButton.class
    };
    
    private boolean[] canEdit = new boolean[]{
        true, false, false, true
    };

    public NubiTableModel() {
        mmServices = Nubisave.services.getMmServices();
        aServices = Nubisave.services.getAServices();
        cstmMntPnts = Nubisave.services.getCstmMntPnts();
    }

    @Override
    public int getRowCount() {
        int rows = 0;
        rows += mmServices.size();
        rows += aServices.size();
        rows += cstmMntPnts.size();
        return rows;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int i) {
        return headers[i];
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types[columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit[columnIndex];
    }

    @Override
    public Object getValueAt(int i, int j) {
        if (i < 0 || i > getRowCount()) {
            return null;
        }

        int mmServicesSize = mmServices.size();
        int aServicesSize = aServices.size();

        // Object is MatchmakerService
        if (i < mmServicesSize) {
            switch (j) {
                case 0:
                    return new Boolean(mmServices.get(i).isEnabled());
                case 1:
                    return "Service";
                case 2:
                    return mmServices.get(i).getName();
                case 3:
                    return "...";
            }
        } else if (i < mmServicesSize + aServicesSize - 1) { // Object is AgreementService
            i -= mmServicesSize - 1;
            switch (j) {
                case 0:
                    return new Boolean(aServices.get(i).isEnabled());
                case 1:
                    return "Agreement";
                case 2:
                    return aServices.get(i).getName();
                case 3:
                    return "...";
            }
        } else { // Object is CustomMntPoint
            i -= mmServicesSize;
            i -= aServicesSize;
            switch (j) {
                case 0:
                    return new Boolean(cstmMntPnts.get(i).isEnabled());
                case 1:
                    return "Custom";
                case 2:
                    return cstmMntPnts.get(i).getName();
                case 3:
                    return "...";
            }
        }
        return null;
    }
}
