/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractSpinnerModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import nubisave.*;
import nubisave.component.graph.splitteradaption.NubisaveEditor;
import org.apache.commons.lang.NumberUtils;


/**
 *
 * @author alok, joe
 */
public class NubisaveConfigDlg extends javax.swing.JDialog {
    
    private static final String MORE_INFO_PNG = "/nubisave/images/LeftArrow2.png";
    private static final String LESS_INFO_PNG = "/nubisave/images/RightArrow2.png";
    
    /**
     * Number model for the spinner for manipulating the availability configuration of Nubisave.
     * The spinner's up and down buttons are meant to fine tune a desired availability setting.
     * By pressing one of the buttons, the next higher or lower availability that is possible
     * is set.
     */
    class AvailabilityModel extends SpinnerNumberModel {
        Object currentValue;
        
        public AvailabilityModel() {
            currentValue = (int) (Nubisave.mainSplitter.getAvailability()*100);
        }
        
        @Override
        public Object getNextValue() {
            Double desiredAvDouble;
            Integer desiredAv = (Integer) NubisaveConfigDlg.this.desiredAvailabilitySpinner.getValue();
            NubisaveConfigDlg.this.desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
            desiredAv++;
            desiredAvDouble = desiredAv / 100.0;
            ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForAvailability(desiredAvDouble);
            refreshSplitterParameters();
            desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
            return (int) (Nubisave.mainSplitter.getAvailability() * 100);
        } 

        @Override
        public Object getPreviousValue() {
            Double desiredAvDouble;
            Integer desiredAv = (Integer) NubisaveConfigDlg.this.desiredAvailabilitySpinner.getValue();
            NubisaveConfigDlg.this.desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
            desiredAv--;
            desiredAvDouble = desiredAv / 100.0;
            ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForAvailabilityLessOrEqual(desiredAvDouble);
            refreshSplitterParameters();
            desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
            return (int) (Nubisave.mainSplitter.getAvailability() * 100);
        }

        @Override
        public void setValue(Object o) {
            throwOnIllegalArgument(o);
            currentValue = o;
            super.setValue(o);
        }

        private void throwOnIllegalArgument(Object o) {
            try {
                if(Integer.valueOf(o.toString())<0 || Integer.valueOf(o.toString())>100)
                throw new IllegalArgumentException(o.toString());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(o.toString());
            }
        }
    }
    
    /**
     * Number model for the spinner for manipulating the redundancy factor configuration of Nubisave.
     * The spinner's up and down buttons are meant to fine tune a desired redundancy factor setting.
     * By pressing one of the buttons, the next higher or lower availability that is possible
     * is set.
     */
    class RedundancyFactorModel extends SpinnerNumberModel {
        Object currentValue;

        public RedundancyFactorModel() {
            currentValue = (int) (Nubisave.mainSplitter.getRedundancyFactor());
        }
        
        @Override
        public Object getNextValue() {
            Double desiredRepFactor;
            desiredRepFactor = (Double) NubisaveConfigDlg.this.desiredRedundancySpinner.getValue();
            NubisaveConfigDlg.this.desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
            desiredRepFactor += 0.001;
            ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForRedundancy(desiredRepFactor);
            refreshSplitterParameters();
            desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
            return (double) (Nubisave.mainSplitter.getRedundancyFactor());
        } 

        @Override
        public Object getPreviousValue() {
            Double desiredRepFactor;
            desiredRepFactor = (Double) NubisaveConfigDlg.this.desiredRedundancySpinner.getValue();
            NubisaveConfigDlg.this.desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
            desiredRepFactor -= 0.001;
            ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForRedundancyEqualOrLess(desiredRepFactor);
            refreshSplitterParameters();
            Integer i=1;
            desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
            return (double) (Nubisave.mainSplitter.getRedundancyFactor());
        }

        @Override
        public Object getValue() {
            return Double.valueOf(currentValue.toString());
        }

        @Override
        public void setValue(Object o) {
            throwOnIllegalArgument(o);
            currentValue = o;
            super.setValue(o);
        }

        private void throwOnIllegalArgument(Object o) {
            try {
                if(Double.valueOf(o.toString())<1 || Double.valueOf(o.toString())>100)
                throw new IllegalArgumentException(o.toString());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(o.toString());
            }
        }
    }
    
    
    /**
     * Creates new form NubisaveConfigDlg
     */
    public NubisaveConfigDlg() {
        initComponents();
        mntDirTxtField.setText(Nubisave.mainSplitter.getMountpoint());
        refreshSplitterParameters();
        splitterIsMountedCheckBox.setSelected(Nubisave.mainSplitter.isMounted());
        SwingUtilities.invokeLater(new Runnable() { //invoke when GUI is constructed
            public void run() { 
                showLessInfo();
                desiredAvailabilitySpinner.setModel(new AvailabilityModel());
                desiredRedundancySpinner.setModel(new RedundancyFactorModel());
                JSpinner.NumberEditor editor = (JSpinner.NumberEditor)desiredRedundancySpinner.getEditor();  
                DecimalFormat format = editor.getFormat();  
                format.setMinimumFractionDigits(3);  
                pack();
            }
        });
    }

    /**
     * Make left side of the splitpane (the information area) invisible, and change toggle button image.
     */
    private void showLessInfo() {
        jSplitPane1.setDividerLocation(0.0); // splitpanel is now invisible!
        ImageIcon moreInfoIcon = new javax.swing.ImageIcon(getClass().getResource(LESS_INFO_PNG ));
        Image img = moreInfoIcon.getImage() ;  
        Image scaledImg = img.getScaledInstance( jToggleButton1.getWidth(), jToggleButton1.getHeight(),  java.awt.Image.SCALE_SMOOTH ) ;  
        ImageIcon scaledMoreInfoIcon = new ImageIcon( scaledImg );
        jToggleButton1.setIcon(scaledMoreInfoIcon); 
        setPreferredSize(new Dimension(710, 390));
        pack();
    }
    
    /**
     * Make left side of the splitpane (the information area) visible, and change toggle button image.
     */
    private void showMoreInfo() {
        jSplitPane1.setDividerLocation(0.12); // splitpanel is now visible!
        ImageIcon moreInfoIcon = new javax.swing.ImageIcon(getClass().getResource(MORE_INFO_PNG));
        Image img = moreInfoIcon.getImage() ;  
        Image scaledImg = img.getScaledInstance( jToggleButton1.getWidth(), jToggleButton1.getHeight(),  java.awt.Image.SCALE_SMOOTH ) ;  
        ImageIcon scaledMoreInfoIcon = new ImageIcon( scaledImg );
        jToggleButton1.setIcon(scaledMoreInfoIcon);
        setPreferredSize(new Dimension(970, 390));
        pack();
    }
    
    /**
     * Refreshes parameters from the Nubisave Splitter component for display.
     */
    private void refreshSplitterParameters() {
        availabilityLabel.setText("<html><b>Availability:</b> " + Nubisave.mainSplitter.getAvailability() * 100 + "%");
        availabilityPerYearLabel.setText("<html><b>Unavailability per year:</b> <br>" + Nubisave.mainSplitter.getUnavailabilityPerYear() + "</html>");
        redundancyFactorLabel.setText("<html><b>Redundancy factor:</b> " + Nubisave.mainSplitter.getRedundancyFactor());
        jLabel3.setText(Nubisave.mainSplitter.getCodecInfo());
        desiredAvailabilitySpinner.setValue((int)(Nubisave.mainSplitter.getAvailability() * 100));
        desiredRedundancySpinner.setValue((double) Nubisave.mainSplitter.getRedundancyFactor());
        double redundancyFactor = Nubisave.mainSplitter.getRedundancyFactor();
        if(redundancyFactor > 1)
            desiredRedundancySpinner.setValue(redundancyFactor);
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        mntDirTxtField = new javax.swing.JTextField();
        openMntDirBtn = new javax.swing.JButton();
        desiredAvailabilityLabel = new javax.swing.JLabel();
        desiredAvailabilityOkButton = new javax.swing.JButton();
        desiredAvailabilityInfoLabel = new javax.swing.JLabel();
        redundancyFactorLabel = new javax.swing.JLabel();
        availabilityLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        splitterSessionComboBox = new javax.swing.JComboBox();
        loadSessionButton = new javax.swing.JButton();
        saveSessionButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        splitterIsMountedCheckBox = new javax.swing.JCheckBox();
        jSeparator5 = new javax.swing.JSeparator();
        jSeparator6 = new javax.swing.JSeparator();
        jButton4 = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        desiredAvailabilitySpinner = new javax.swing.JSpinner();
        desiredAvailabilityLabel1 = new javax.swing.JLabel();
        desiredRedundancyOkButton = new javax.swing.JButton();
        desiredRedundancySpinner = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        availabilityPerYearLabel = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        storageStrategyComboBox = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jToggleButton1 = new javax.swing.JToggleButton();

        jSplitPane1.setDividerSize(0);
        jSplitPane1.setContinuousLayout(true);

        jPanel1.setPreferredSize(new java.awt.Dimension(500, 543));

        jLabel2.setText("Mount directory:");

        mntDirTxtField.setText("jTextField1");

        openMntDirBtn.setText("Open");
        openMntDirBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMntDirBtnActionPerformed(evt);
            }
        });

        desiredAvailabilityLabel.setText("Desired availability:");

        desiredAvailabilityOkButton.setText("OK");
        desiredAvailabilityOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                desiredAvailabilityOkButtonActionPerformed(evt);
            }
        });

        desiredAvailabilityInfoLabel.setText("Press OK to check");

        redundancyFactorLabel.setText("Redundancy factor:");

        availabilityLabel.setText("Availability");

        jLabel1.setText("Choose session:");

        splitterSessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", " " }));
        splitterSessionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitterSessionComboBoxActionPerformed(evt);
            }
        });

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

        splitterIsMountedCheckBox.setText("Splitter mount status");
        splitterIsMountedCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitterIsMountedCheckBoxActionPerformed(evt);
            }
        });

        jButton4.setText("Close");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        desiredAvailabilitySpinner.setOpaque(false);

        desiredAvailabilityLabel1.setText("Desired replication factor:");

        desiredRedundancyOkButton.setText("OK");
        desiredRedundancyOkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                desiredRedundancyOkButtonActionPerformed(evt);
            }
        });

        desiredRedundancySpinner.setOpaque(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 624, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63)
                        .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(loadSessionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(saveSessionButton))
                    .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(availabilityLabel)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jSeparator6)
                                    .addGap(91, 91, 91))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addComponent(jLabel2)
                                            .addGap(51, 51, 51)
                                            .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, 311, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(desiredAvailabilityInfoLabel)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(desiredAvailabilitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(desiredAvailabilityOkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(desiredAvailabilityLabel))
                                            .addGap(90, 90, 90)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addComponent(desiredRedundancySpinner)
                                                    .addGap(18, 18, 18)
                                                    .addComponent(desiredRedundancyOkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(desiredAvailabilityLabel1))))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)))
                            .addComponent(openMntDirBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)))
                    .addComponent(redundancyFactorLabel)
                    .addComponent(splitterIsMountedCheckBox))
                .addGap(198, 198, 198)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 922, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(350, 350, 350)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(splitterIsMountedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator5, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(mntDirTxtField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(openMntDirBtn))
                .addGap(24, 24, 24)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(desiredAvailabilityLabel)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(desiredAvailabilitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(desiredAvailabilityOkButton)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(desiredAvailabilityLabel1)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(desiredRedundancySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(desiredRedundancyOkButton))))
                .addGap(41, 41, 41)
                .addComponent(desiredAvailabilityInfoLabel)
                .addGap(8, 8, 8)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(availabilityLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(redundancyFactorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(splitterSessionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(loadSessionButton)
                    .addComponent(saveSessionButton))
                .addGap(15, 15, 15)
                .addComponent(jButton4)
                .addGap(28, 28, 28))
        );

        jSplitPane1.setRightComponent(jPanel1);

        jLabel3.setText("Information Area");

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator3.setToolTipText("");

        availabilityPerYearLabel.setText("<html><b>Unavailability per year:</html></b>");

        jLabel5.setText("<html><b>Storage strategy:<html></b>");

        storageStrategyComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UseAllInParallel", "RoundRobin" }));
        storageStrategyComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storageStrategyComboBoxActionPerformed(evt);
            }
        });

        jLabel4.setText("<html><b>Codec information:</html></b>");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(71, 71, 71))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(availabilityPerYearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 174, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(77, 77, 77))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27)
                .addComponent(availabilityPerYearLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(46, 46, 46)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(storageStrategyComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 83, Short.MAX_VALUE))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jSeparator3)
                .addContainerGap())
        );

        jSplitPane1.setLeftComponent(jPanel2);

        jToggleButton1.setToolTipText("More Information");
        jToggleButton1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jToggleButton1StateChanged(evt);
            }
        });
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 2083, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(104, 104, 104)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(129, Short.MAX_VALUE))
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void splitterIsMountedCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitterIsMountedCheckBoxActionPerformed
        // TODO add your handling code here:
        if (!splitterIsMountedCheckBox.isSelected()) { //the selected state is toggled before entering this method
            Nubisave.mainSplitter.unmount();
        } else {
            Nubisave.mainSplitter.mount();
        }
        setIsSplitterMounted();
        refreshSplitterParameters();
    }//GEN-LAST:event_splitterIsMountedCheckBoxActionPerformed

    private void saveSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveSessionButtonActionPerformed
        // TODO add your handling code here:
        int sessionNumber = Integer.parseInt((String) splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.storeSession(sessionNumber);
    }//GEN-LAST:event_saveSessionButtonActionPerformed

    private void loadSessionButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSessionButtonActionPerformed
        // TODO add your handling code here:
        int sessionNumber = Integer.parseInt((String) splitterSessionComboBox.getSelectedItem());
        Nubisave.mainSplitter.loadSession(sessionNumber);
        tableModel.fireTableDataChanged();
//        storageStrategyComboBox.setSelectedItem(Nubisave.mainSplitter.getStorageStrategy());
    }//GEN-LAST:event_loadSessionButtonActionPerformed

    private void splitterSessionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitterSessionComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_splitterSessionComboBoxActionPerformed
    /**
     * Configure Nubisave's splitter component so that the overall availability
     * is greater than or equal to the desired availability expressed in the
     * desiredAvailabilityTextField.
     *
     * @param evt
     */
    private void desiredAvailabilityOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_desiredAvailabilityOkButtonActionPerformed
        Integer desiredAv;
        Double desiredAvDouble;
        desiredAv = (Integer) desiredAvailabilitySpinner.getValue();
        desiredAvDouble = desiredAv / 100.0;
        desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
        ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForAvailability(desiredAvDouble);
        refreshSplitterParameters();
        desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
    }//GEN-LAST:event_desiredAvailabilityOkButtonActionPerformed

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

    private void jToggleButton1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton1StateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton1StateChanged

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        if(jToggleButton1.isSelected()){
            showMoreInfo();
        } else {
            showLessInfo();
        }
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void storageStrategyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storageStrategyComboBoxActionPerformed
        // TODO add your handling code here:
        JComboBox cb = (JComboBox) evt.getSource();
        String storageStrategy = (String) cb.getSelectedItem();
        Nubisave.mainSplitter.setStorageStrategy(storageStrategy);
        refreshSplitterParameters();
    }//GEN-LAST:event_storageStrategyComboBoxActionPerformed

    private void desiredRedundancyOkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_desiredRedundancyOkButtonActionPerformed
        Double desiredRed = (Double) desiredRedundancySpinner.getValue();
        desiredAvailabilityInfoLabel.setText("Searching for optimal configuration.");
        ((AutonomousSplitter) Nubisave.mainSplitter).findConfigurationForRedundancyEqualOrLess(desiredRed);
        refreshSplitterParameters();
        desiredAvailabilityInfoLabel.setText("Found optimal configuration.");
    }//GEN-LAST:event_desiredRedundancyOkButtonActionPerformed

    public NubiTableModel tableModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel availabilityLabel;
    private javax.swing.JLabel availabilityPerYearLabel;
    private javax.swing.JLabel desiredAvailabilityInfoLabel;
    private javax.swing.JLabel desiredAvailabilityLabel;
    private javax.swing.JLabel desiredAvailabilityLabel1;
    private javax.swing.JButton desiredAvailabilityOkButton;
    private javax.swing.JSpinner desiredAvailabilitySpinner;
    private javax.swing.JButton desiredRedundancyOkButton;
    private javax.swing.JSpinner desiredRedundancySpinner;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton loadSessionButton;
    private javax.swing.JTextField mntDirTxtField;
    private javax.swing.JButton openMntDirBtn;
    private javax.swing.JLabel redundancyFactorLabel;
    private javax.swing.JButton saveSessionButton;
    private javax.swing.JCheckBox splitterIsMountedCheckBox;
    private javax.swing.JComboBox splitterSessionComboBox;
    private javax.swing.JComboBox storageStrategyComboBox;
    // End of variables declaration//GEN-END:variables
}
