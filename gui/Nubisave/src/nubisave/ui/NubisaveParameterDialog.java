/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * ServiceEditDialog.java
 *
 * Created on Jul 4, 2011, 1:01:04 PM
 */
package nubisave.ui;

import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;
import nubisave.StorageService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import nubisave.Nubisave;
import org.ini4j.Ini;


public class NubisaveParameterDialog extends javax.swing.JDialog {

    
    /** Creates new form ServiceParameterDialog */
    public NubisaveParameterDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        String redundancyStr = Nubisave.properties.getProperty("redundancy");
        if (redundancyStr == null) {
            redundancyStr = "100";
        }
        int redundancy = Integer.parseInt(redundancyStr);
        redundancySlider.setValue(redundancy);
        setAvailability();


        mntDirTxtField.setText(Nubisave.mainSplitter.getMountpoint());
    }

    private void setAvailability() {
        availabilityLabel.setText("Availability: " + Nubisave.mainSplitter.getAvailability() * 100 + "%");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        optionPanel = new javax.swing.JPanel();
        mntDirTxtField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        redundancySlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        openMntDirBtn = new javax.swing.JButton();
        matchMakerLabel1 = new javax.swing.JLabel();
        matchMakerField = new javax.swing.JTextField();
        availabilityLabel = new javax.swing.JLabel();
        storageStrategyComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        splitterSessionComboBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        saveSessionButton = new javax.swing.JButton();
        loadSessionButton = new javax.swing.JButton();
        matchMakerURLField = new javax.swing.JTextField();
        changeMatchMakerURLBtn = new javax.swing.JButton();
        matchMakerLabel = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(334, 122));

        mntDirTxtField.setEditable(false);
        mntDirTxtField.setText("mntDirTxtField");
        mntDirTxtField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntDirTxtFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Mount directory");

        redundancySlider.setValue(100);
        redundancySlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                redundancySliderStateChanged(evt);
            }
        });

        jLabel3.setText("Redundancy");

        openMntDirBtn.setText("Open");
        openMntDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMntDirBtnActionPerformed(evt);
            }
        });

        matchMakerLabel1.setText("MatchMaker ");

        matchMakerField.setEditable(false);
        matchMakerField.setText("MatchMaker URL");
        matchMakerField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchMakerFieldActionPerformed(evt);
            }
        });

        availabilityLabel.setText("Availability:");

        storageStrategyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UseAllInParallel", "RoundRobin" }));
        storageStrategyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageStrategyComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("Storage strategy:");

        splitterSessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }));

        jLabel5.setText("Choose Session:");

        saveSessionButton.setText("Save Session");
        saveSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveSessionButtonActionPerformed(evt);
            }
        });

        loadSessionButton.setText("Load Session");
        loadSessionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSessionButtonActionPerformed(evt);
            }
        });

        matchMakerURLField.setText(Nubisave.properties.getProperty("matchmakerURI"));

        changeMatchMakerURLBtn.setText("Apply");
        changeMatchMakerURLBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeMatchMakerURLBtnActionPerformed(evt);
            }
        });

        matchMakerLabel.setText("MatchMaker ");

        javax.swing.GroupLayout optionPanelLayout = new javax.swing.GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPanelLayout.createSequentialGroup()
                                .addComponent(mntDirTxtField, javax.swing.GroupLayout.DEFAULT_SIZE, 816, Short.MAX_VALUE)
                                .addGap(36, 36, 36)
                                .addComponent(openMntDirBtn))
                            .addComponent(redundancySlider, javax.swing.GroupLayout.DEFAULT_SIZE, 903, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addGroup(optionPanelLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(67, 67, 67)
                                .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(availabilityLabel)
                            .addGroup(optionPanelLayout.createSequentialGroup()
                                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(optionPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addGap(38, 38, 38)
                                        .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(76, 76, 76)
                                        .addComponent(loadSessionButton))
                                    .addComponent(jLabel2))
                                .addGap(68, 68, 68)
                                .addComponent(saveSessionButton)))
                        .addContainerGap())
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(matchMakerLabel)
                            .addComponent(matchMakerURLField, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 757, Short.MAX_VALUE))
                        .addGap(36, 36, 36)
                        .addComponent(changeMatchMakerURLBtn)
                        .addGap(72, 72, 72))))
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveSessionButton)
                    .addComponent(loadSessionButton))
                .addGap(27, 27, 27)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openMntDirBtn))
                .addGap(29, 29, 29)
                .addComponent(jLabel3)
                .addComponent(redundancySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(30, 30, 30)
                .addComponent(availabilityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                .addComponent(matchMakerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(matchMakerURLField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(changeMatchMakerURLBtn))
                .addContainerGap())
        );

        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(optionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(402, 402, 402)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(423, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(optionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton1)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mntDirTxtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mntDirTxtFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_mntDirTxtFieldActionPerformed

    private void redundancySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redundancySliderStateChanged
        Nubisave.properties.setProperty("redundancy", String.valueOf(redundancySlider.getValue()));
        Nubisave.mainSplitter.setRedundancy(redundancySlider.getValue());
        setAvailability();
}//GEN-LAST:event_redundancySliderStateChanged

    private void openMntDirBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMntDirBtnActionPerformed
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(Nubisave.mainSplitter.getDataDir()));
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
}//GEN-LAST:event_openMntDirBtnActionPerformed

    private void matchMakerFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchMakerFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_matchMakerFieldActionPerformed

    private void storageStrategyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageStrategyComboBoxActionPerformed
        JComboBox cb = (JComboBox)evt.getSource();
        String storageStrategy = (String)cb.getSelectedItem();
        Nubisave.mainSplitter.setStorageStrategy(storageStrategy);
        setAvailability();
}//GEN-LAST:event_storageStrategyComboBoxActionPerformed

    private void saveSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionButtonActionPerformed
        int sessionNumber = Integer.parseInt((String)splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.storeSession(sessionNumber);
}//GEN-LAST:event_saveSessionButtonActionPerformed

    private void loadSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSessionButtonActionPerformed
        int sessionNumber = Integer.parseInt((String)splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.loadSession(sessionNumber);
        redundancySlider.setValue(Nubisave.mainSplitter.getRedundancy());
        storageStrategyComboBox.setSelectedItem(Nubisave.mainSplitter.getStorageStrategy());
}//GEN-LAST:event_loadSessionButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void changeMatchMakerURLBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeMatchMakerURLBtnActionPerformed
        Nubisave.properties.setProperty("matchmakerURI", matchMakerURLField.getText());
}//GEN-LAST:event_changeMatchMakerURLBtnActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availabilityLabel;
    private javax.swing.JButton changeMatchMakerURLBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JButton loadSessionButton;
    private javax.swing.JTextField matchMakerField;
    private javax.swing.JLabel matchMakerLabel;
    private javax.swing.JLabel matchMakerLabel1;
    private javax.swing.JTextField matchMakerURLField;
    private javax.swing.JTextField mntDirTxtField;
    private javax.swing.JButton openMntDirBtn;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JSlider redundancySlider;
    private javax.swing.JButton saveSessionButton;
    private javax.swing.JComboBox splitterSessionComboBox;
    private javax.swing.JComboBox storageStrategyComboBox;
    // End of variables declaration//GEN-END:variables
}
