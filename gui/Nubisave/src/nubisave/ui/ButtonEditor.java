/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import nubisave.*;

/**
 * @deprecated 
 * @author demo
 */
public class ButtonEditor extends DefaultCellEditor {

    protected JButton button;
    private String label;
    private boolean isPushed;
    private MainWindow owner;
    private int row;
    private int column;

    public ButtonEditor(JCheckBox checkBox, MainWindow owner) {
        super(checkBox);
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped();
            }
        });
        this.owner = owner;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        label = (value == null) ? "" : value.toString();
        button.setText(label);
        isPushed = true;
        this.row = row;
        this.column = column;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {

            switch (column) {
                case 2:
                    String type = (String) owner.tableModel.getValueAt(row, NubiTableModel.Headers.TYPE.ordinal());
                    if ("Service".equals(type) || "Custom".equals(type)) {
                        StorageService service = Nubisave.services.get(row);
                        ServiceParameterDialog editDialog = new ServiceParameterDialog(owner, true, service);
                        editDialog.setTitle(service.getName());
                        editDialog.setVisible(true);
                    } 
                    break;
                case 3:
                    String desc = (String) owner.tableModel.getValueAt(row, NubiTableModel.Headers.DESCRIPTION.ordinal());
                    String[] options = {"No", "Remove"};
                    int n = JOptionPane.showOptionDialog(owner,
                            "Remove and unmount " + desc + "?",
                            "Remove?",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            options,
                            options[1]);
                    if (n == 1) {
                        if(Nubisave.mainSplitter.isModuleMounted(Nubisave.services.get(row))){
                            Nubisave.mainSplitter.unmountStorageModule(Nubisave.services.get(row));
                        }
                        Nubisave.services.remove(row);
                        
                    }
                    break;
                case 5:
                    //type = (String) owner.tableModel.getValueAt(row, NubiTableModel.Headers.TYPE.ordinal());
                    StorageService service = Nubisave.services.get(row);
                    BackendConfigurationDialog editDialog = new BackendConfigurationDialog(owner, true, service);
                    editDialog.setTitle(service.getName());
                    editDialog.setVisible(true);
                    break;
            }
        }
        isPushed = false;


        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }

    @Override
    protected void fireEditingStopped() {
        super.fireEditingStopped();
    }
}
