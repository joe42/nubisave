/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * MainWindow.java
 *
 * Created on May 24, 2011, 6:20:38 PM
 */
package nubisave.ui;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import nubisave.*;

/**
 *
 * @author demo
 */
public class MainWindow extends javax.swing.JFrame {

    /** Creates new form MainWindow */
    public MainWindow() {
        tableModel = new NubiTableModel();
        initComponents();
        providerTable.setDefaultRenderer(String.class, new ShowSupportedCellRenderer());
        providerTable.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        providerTable.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        String mntPoint = Properties.getProperty("mntPoint");
        if (mntPoint == null) {
            mntPoint = "";
        }
        mntDirTxtField.setText(mntPoint);
 
        String redundancyStr = Properties.getProperty("redundancy");
        if (redundancyStr == null) {
            redundancyStr = "100";
        }
        int redundancy = Integer.parseInt(redundancyStr);
        redundancySlider.setValue(redundancy);
        //matchmakerURIField.setText(Properties.getProperty("matchmakerURI"));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        providerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        providerTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        applyBtn = new javax.swing.JButton();
        mountBtn = new javax.swing.JButton();
        optionPanel = new javax.swing.JPanel();
        mntDirTxtField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        redundancySlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        mntDirOpenBtn = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Nubisave");

        providerTable.setModel(tableModel);
        providerTable.setRowHeight(30);
        jScrollPane1.setViewportView(providerTable);

        jButton1.setText("Service");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Agreement");
        jButton2.setEnabled(false);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("custom");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jLabel1.setText("add by:");

        applyBtn.setText("Apply");
        applyBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyBtnActionPerformed(evt);
            }
        });

        mountBtn.setText("Mount");
        mountBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mountBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout providerPanelLayout = new javax.swing.GroupLayout(providerPanel);
        providerPanel.setLayout(providerPanelLayout);
        providerPanelLayout.setHorizontalGroup(
            providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, providerPanelLayout.createSequentialGroup()
                .addContainerGap(593, Short.MAX_VALUE)
                .addComponent(mountBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, providerPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 106, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addComponent(applyBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap()))
        );
        providerPanelLayout.setVerticalGroup(
            providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(providerPanelLayout.createSequentialGroup()
                .addGap(345, 345, 345)
                .addComponent(mountBtn)
                .addContainerGap(49, Short.MAX_VALUE))
            .addGroup(providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(providerPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(providerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                        .addGroup(providerPanelLayout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton2)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton3)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 244, Short.MAX_VALUE)
                            .addComponent(applyBtn)))
                    .addContainerGap()))
        );

        jTabbedPane1.addTab("Provider", providerPanel);

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

        mntDirOpenBtn.setText("...");
        mntDirOpenBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mntDirOpenBtnActionPerformed(evt);
            }
        });

        jButton5.setText("open");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout optionPanelLayout = new javax.swing.GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(redundancySlider, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, optionPanelLayout.createSequentialGroup()
                        .addComponent(mntDirTxtField, javax.swing.GroupLayout.DEFAULT_SIZE, 603, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mntDirOpenBtn)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton5)))
                .addContainerGap())
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5)
                    .addComponent(mntDirOpenBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(redundancySlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(267, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Options", optionPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 719, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        AddServiceDialog addServiceDlg = new AddServiceDialog(this, true);
        addServiceDlg.setVisible(true);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        AddCustomDialog addCstmDlg = new AddCustomDialog(this, true);
        addCstmDlg.setVisible(true);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyBtnActionPerformed
        CoreWriter writer = new CoreWriter();
        writer.writeToCoreModule(Nubisave.services);
    }//GEN-LAST:event_applyBtnActionPerformed

    private void mntDirTxtFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mntDirTxtFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mntDirTxtFieldActionPerformed

    private void mntDirOpenBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mntDirOpenBtnActionPerformed
        final JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File dir = fc.getSelectedFile();

            Properties.setProperty("mntPoint", dir.getPath());
            mntDirTxtField.setText(dir.getPath());
        }
    }//GEN-LAST:event_mntDirOpenBtnActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(Properties.getProperty("mntPoint")));
            } catch (IOException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void mountBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mountBtnActionPerformed
        String mntPointStr = Properties.getProperty("mntPoint");
        if (mntPointStr == null) {
            JOptionPane.showMessageDialog(this, "Error mntPoint: " + mntPointStr, "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String redundancyStr = Properties.getProperty("redundancy");
        if (redundancyStr == null) {
            JOptionPane.showMessageDialog(this, "Error redundancy: " + redundancyStr, "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        File mntPoint = new File(mntPointStr);
        if (!mntPoint.isDirectory()) {
            JOptionPane.showMessageDialog(this, mntPoint.getPath() + " isn't a directory!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!mntPoint.exists()) {
            JOptionPane.showMessageDialog(this, mntPoint.getPath() + " doesn't exists!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (mntPoint.list().length != 0) {
            JOptionPane.showMessageDialog(this, mntPoint.getPath() + " isn't empty!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //TODO: don't hardcode splitterDir
        File splitter = new File("/home/demo/development/svn/trunk/splitter/splitter_mount.sh");
        if (!splitter.exists()) {
            JOptionPane.showMessageDialog(this, "Couldn't find Splitter module: " + splitter.getPath(), "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String homePath = System.getProperty("user.home");
        File storagesDir = new File(homePath + "/.cache/nubisave/storages");
        if (!storagesDir.exists()) {
            System.err.println(storagesDir.getPath() + " doesn't exists!");
            if (storagesDir.mkdirs()) {
                System.out.println("Created:" + storagesDir.getPath());
            } else {
                JOptionPane.showMessageDialog(this, "Couldn't create " + storagesDir.getPath(), "Mount Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        //                splitter_mount.sh ~/nubisave/ ~/.cache/nubisave/storages [redundant_files]
        String mountCmd = splitter.getPath() + " " + mntPoint.getPath() + storagesDir.getPath() + " " + redundancyStr;
        System.out.println("exec: " + mountCmd);
        boolean error = false;
        try {
            Process p = Runtime.getRuntime().exec(mountCmd, null, splitter.getParentFile());
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = br.readLine()) != null) {
                error = true;
                System.err.println(line);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, "Exception: " + ex.getMessage(), "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (error) {
            JOptionPane.showMessageDialog(this, "Error in mount script!", "Mount Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }//GEN-LAST:event_mountBtnActionPerformed

    private void redundancySliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_redundancySliderStateChanged
        Properties.setProperty("redundancy", redundancySlider.getValue()+"");
    }//GEN-LAST:event_redundancySliderStateChanged
    public NubiTableModel tableModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyBtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton mntDirOpenBtn;
    private javax.swing.JTextField mntDirTxtField;
    private javax.swing.JButton mountBtn;
    private javax.swing.JPanel optionPanel;
    private javax.swing.JPanel providerPanel;
    private javax.swing.JTable providerTable;
    private javax.swing.JSlider redundancySlider;
    // End of variables declaration//GEN-END:variables
}
