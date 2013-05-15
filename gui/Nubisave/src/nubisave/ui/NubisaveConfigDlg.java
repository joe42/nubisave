/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import nubisave.*;
import nubisave.component.graph.splitteradaption.NubisaveEditor;

/**
 *
 * @author alok
 */
public class NubisaveConfigDlg extends javax.swing.JDialog {

    /**
     * Creates new form NubisaveConfigDlg
     */
    public NubisaveConfigDlg() {
        initComponents();
        
       mntDirTxtField.setText(Nubisave.mainSplitter.getMountpoint());

        String redundancyStr = Nubisave.properties.getProperty("redundancy");
        if (redundancyStr == null) {
            redundancyStr = "100";
        }
        int redundancy = Integer.parseInt(redundancyStr);
        redundancySlider.setValue(redundancy);
        setAvailability();
        splitterIsMountedCheckBox.setSelected(Nubisave.mainSplitter.isMounted());
    }
    
    private void setAvailability() {
        availabilityLabel.setText("Availability: " + Nubisave.mainSplitter.getAvailability() * 100 + "%");
    }
    
    private void setIsSplitterMounted() {
        if (Nubisave.mainSplitter.isMounted()) {
            splitterIsMountedCheckBox.setText("Unmount Splitter");
            splitterIsMountedCheckBox.setSelected(true);
        } else {
            splitterIsMountedCheckBox.setText("Mount Splitter");
            splitterIsMountedCheckBox.setSelected(false);
        }
    }
  
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitterIsMountedCheckBox = new javax.swing.JCheckBox();
        splitterSessionComboBox = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        loadSessionButton = new javax.swing.JButton();
        saveSessionButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        mntDirTxtField = new javax.swing.JTextField();
        openMntDirBtn = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        redundancySlider = new javax.swing.JSlider();
        jLabel4 = new javax.swing.JLabel();
        matchMakerURLField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        storageStrategyComboBox = new javax.swing.JComboBox();
        availabilityLabel = new javax.swing.JLabel();
        changeMatchMakerURLBtn = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();

        splitterIsMountedCheckBox.setText("Unmount Spiltter");
        splitterIsMountedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitterIsMountedCheckBoxActionPerformed(evt);
            }
        });

        splitterSessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", " " }));
        splitterSessionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitterSessionComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setText("Choose Session");

        loadSessionButton.setText("Load Session");
        loadSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSessionButtonActionPerformed(evt);
            }
        });

        saveSessionButton.setText("Save Session");
        saveSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSessionButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Mount Directory");

        mntDirTxtField.setText("jTextField1");
        mntDirTxtField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntDirTxtFieldActionPerformed(evt);
            }
        });

        openMntDirBtn.setText("Open");
        openMntDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMntDirBtnActionPerformed(evt);
            }
        });

        jLabel3.setText("Redundancy");

        redundancySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redundancySliderStateChanged(evt);
            }
        });

        jLabel4.setText("Matchmaker");

        matchMakerURLField.setText("jTextField2");
        matchMakerURLField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchMakerURLFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("Storage strategy");

        storageStrategyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UseAllInParallel", "RoundRobin" }));
        storageStrategyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageStrategyComboBoxActionPerformed(evt);
            }
        });

        availabilityLabel.setText("Availability");

        changeMatchMakerURLBtn.setText("Apply");
        changeMatchMakerURLBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeMatchMakerURLBtnActionPerformed(evt);
            }
        });

        jButton4.setText("Close");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(redundancySlider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, 576, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(openMntDirBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(matchMakerURLField, javax.swing.GroupLayout.PREFERRED_SIZE, 583, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(changeMatchMakerURLBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(splitterIsMountedCheckBox)
                                .addGap(40, 40, 40)
                                .addComponent(jLabel1)
                                .addGap(31, 31, 31)
                                .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(53, 53, 53)
                                .addComponent(loadSessionButton)
                                .addGap(55, 55, 55)
                                .addComponent(saveSessionButton))
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(80, 80, 80)
                                .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel4)
                            .addComponent(availabilityLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(42, 42, 42))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(46, 46, 46)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(splitterIsMountedCheckBox)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadSessionButton)
                    .addComponent(saveSessionButton))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openMntDirBtn))
                .addGap(33, 33, 33)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(redundancySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchMakerURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeMatchMakerURLBtn))
                .addGap(34, 34, 34)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addComponent(availabilityLabel)
                .addGap(18, 18, 18)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void splitterIsMountedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitterIsMountedCheckBoxActionPerformed
        // TODO add your handling code here:
        if(!splitterIsMountedCheckBox.isSelected()){ //the selected state is toggled before entering this method
            Nubisave.mainSplitter.unmount();
        } else {
            Nubisave.mainSplitter.mount();
        }
        setIsSplitterMounted();
        setAvailability();
    }//GEN-LAST:event_splitterIsMountedCheckBoxActionPerformed

    private void splitterSessionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitterSessionComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_splitterSessionComboBoxActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void mntDirTxtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mntDirTxtFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mntDirTxtFieldActionPerformed

    private void saveSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionButtonActionPerformed
        // TODO add your handling code here:
        int sessionNumber = Integer.parseInt((String)splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.storeSession(sessionNumber);
    }//GEN-LAST:event_saveSessionButtonActionPerformed

    private void storageStrategyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageStrategyComboBoxActionPerformed
        // TODO add your handling code here:
        JComboBox cb = (JComboBox)evt.getSource();
        String storageStrategy = (String)cb.getSelectedItem();
        Nubisave.mainSplitter.setStorageStrategy(storageStrategy);
        setAvailability();
    }//GEN-LAST:event_storageStrategyComboBoxActionPerformed

    private void openMntDirBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMntDirBtnActionPerformed
        // TODO add your handling code here:
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(Nubisave.mainSplitter.getDataDir()));
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);

                // Fallback when desktop handlers are not available
                try {
                    Runtime.getRuntime().exec(new String[]{"xdg-open", Nubisave.mainSplitter.getDataDir()});
                } catch (IOException ex2) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex2);
                }
            }
        }
        
    }//GEN-LAST:event_openMntDirBtnActionPerformed

    private void changeMatchMakerURLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeMatchMakerURLBtnActionPerformed
        // TODO add your handling code here:
        Nubisave.properties.setProperty("matchmakerURI", matchMakerURLField.getText());
    }//GEN-LAST:event_changeMatchMakerURLBtnActionPerformed

    private void matchMakerURLFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchMakerURLFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_matchMakerURLFieldActionPerformed

    private void redundancySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redundancySliderStateChanged
        // TODO add your handling code here:
        Nubisave.properties.setProperty("redundancy", String.valueOf(redundancySlider.getValue()));
        Nubisave.mainSplitter.setRedundancy(redundancySlider.getValue());
        setAvailability();
    }//GEN-LAST:event_redundancySliderStateChanged

    private void loadSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSessionButtonActionPerformed
        // TODO add your handling code here:
        int sessionNumber = Integer.parseInt((String)splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.loadSession(sessionNumber);
        tableModel.fireTableDataChanged();
        redundancySlider.setValue(Nubisave.mainSplitter.getRedundancy());
        storageStrategyComboBox.setSelectedItem(Nubisave.mainSplitter.getStorageStrategy());
    }//GEN-LAST:event_loadSessionButtonActionPerformed
    public NubiTableModel tableModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availabilityLabel;
    private javax.swing.JButton changeMatchMakerURLBtn;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton loadSessionButton;
    private javax.swing.JTextField matchMakerURLField;
    private javax.swing.JTextField mntDirTxtField;
    private javax.swing.JButton openMntDirBtn;
    private javax.swing.JSlider redundancySlider;
    private javax.swing.JButton saveSessionButton;
    private javax.swing.JCheckBox splitterIsMountedCheckBox;
    private javax.swing.JComboBox splitterSessionComboBox;
    private javax.swing.JComboBox storageStrategyComboBox;
    // End of variables declaration//GEN-END:variables
}
