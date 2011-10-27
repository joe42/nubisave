/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import javax.swing.table.AbstractTableModel;
import nubisave.*;

/**
 *
 * @author demo
 */
public class NubiTableModel extends AbstractTableModel {

    private final String headers[] = {"Type", "Description", "Password", "Remove", "Mounted", "Backend"};
    public enum Headers { TYPE, DESCRIPTION, PASSWORD, REMOVE, MOUNTED, CONNECT };
    private Class[] types = new Class[]{
        java.lang.String.class, java.lang.String.class, javax.swing.JButton.class,javax.swing.JButton.class, java.lang.Boolean.class
    };
    private boolean[] canEdit = new boolean[]{
        false, false, true,true,true,true
    };

    @Override
    public int getRowCount() {
        int rows = 0;
        rows += Nubisave.services.getMmServices().size();
        rows += Nubisave.services.getAServices().size();
        rows += Nubisave.services.getCstmMntPnts().size();
        return rows;
    }

    @Override
    public int getColumnCount() {
        return headers.length;
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
        if (columnIndex == Headers.PASSWORD.ordinal()) {
            return true;
        }
        return Nubisave.services.get(rowIndex).isSupported() && canEdit[columnIndex];
    }

    @Override
    public Object getValueAt(int i, int j) {
        if (i < 0 || i > getRowCount()) {
            return null;
        }

        int mmServicesSize = Nubisave.services.getMmServices().size();
        int aServicesSize = Nubisave.services.getAServices().size();

        StorageService service = Nubisave.services.get(i);

        switch (j) {
            case 0:
                switch (service.getType()) {
                    case MATCHMAKER:
                        return "Service";
                    case AGREEMENT:
                        return "Agreement";
                    case CUSTOM:
                        return "Custom";
                }
            case 1:
                return service.getName();
            case 2:
                switch (service.getType()) {
                    case MATCHMAKER:
                        String label = "User/Passwd";
                        StorageService mmService = (StorageService)service;
                        if (mmService.getUser() != null) {
                            if (mmService.getUser().length() > 0) {
                                return label;
                            }
                        }
                        return label + "*";
                    case AGREEMENT:
                    case CUSTOM:
                        return "User/Passwd";
                }
            case 3:
                return "remove";
            case 4:
                return Nubisave.mainSplitter.isModuleMounted(Nubisave.services.get(i));
            case 5:
                return "configure";
        }

        return null;
    }
    
    @Override
    public void setValueAt(Object o,int row,int column) {
        if (column == Headers.MOUNTED.ordinal()) {
            Nubisave.mainSplitter.mountStorageModule(Nubisave.services.get(row));
        }
    }
}
