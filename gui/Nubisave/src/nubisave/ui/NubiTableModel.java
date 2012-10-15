/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import javax.swing.table.AbstractTableModel;
import nubisave.*;

/**
 * @deprecated
 */
public class NubiTableModel extends AbstractTableModel {

    public final String headers[] = {"Type", "Description", "Parameters", "Remove", "Mounted", "Backend"};
    public enum Headers { TYPE, DESCRIPTION, OPTIONS, REMOVE, MOUNTED, CONNECT };
    private Class[] types = new Class[]{
        java.lang.String.class, java.lang.String.class, javax.swing.JButton.class,javax.swing.JButton.class, java.lang.Boolean.class
    };
    private boolean[] canEdit = new boolean[]{
        false, false, true,true,true,true
    };

    @Override
    public int getRowCount() {
        int rows = 0;
        rows += Nubisave.services.size();
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
        if (columnIndex == Headers.OPTIONS.ordinal()) {
            return true;
        }
        return Nubisave.services.get(rowIndex).isSupported() && canEdit[columnIndex];
    }

    @Override
    public Object getValueAt(int i, int j) {
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
                        return "Configure";
                    case AGREEMENT:
                    case CUSTOM:
                        return "Configure";
                }
            case 3:
                return "Remove";
            case 4:
                return Nubisave.mainSplitter.isModuleMounted(Nubisave.services.get(i));
            case 5:
                return "Configure";
        }

        return null;
    }
    
    @Override
    public void setValueAt(Object o,int row,int column) {
        if (column == Headers.MOUNTED.ordinal()) {
            if(! Nubisave.mainSplitter.isModuleMounted(Nubisave.services.get(row))){
                Nubisave.mainSplitter.mountStorageModule(Nubisave.services.get(row)); // mount the module
                fireTableDataChanged(); // reload table, since backend modules might have been mounted along with this storage module
            } else {
                Nubisave.mainSplitter.unmountStorageModule(Nubisave.services.get(row)); // unmount the module
            }
        }
    }
}
