package nubisave.ui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import nubisave.Nubisave;
import nubisave.ui.util.SystemIntegration;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

public class PolicySelectionDlg extends javax.swing.JDialog {

    public static boolean okstatus = false;

    private String dir = null;

    private HashMap<String, String> inimap = new HashMap<String, String>();

    private javax.swing.JButton jCancelButton;

    private javax.swing.JComboBox<String> ca;

    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;

    private javax.swing.JButton jOkButton;
    private javax.swing.JButton jNewButton;

    private List<String> listPolicyName = new ArrayList<String>();
    /**
     *
     */
    private static final long serialVersionUID = 4506941459188215056L;

    public PolicySelectionDlg() {
        //final ComboBoxText ct = new ComboBoxText();  
        this.setLayout(null);
        this.setSize(300, 100);
        this.setLocationRelativeTo(this.getParent());

        jLabel1 = new javax.swing.JLabel();
        jLabel1.setText("   Select Policy: ");

        jLabel2 = new javax.swing.JLabel();
        jLabel2.setText("   ");

        jLabel3 = new javax.swing.JLabel();
        jLabel3.setText("   ");

        jLabel1.setBounds(50, 50, 50, 50);
        ca = new JComboBox();
        ca.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Policy " + ca.getSelectedItem() + " is selected...");
            }
        });
        ca.setSelectedIndex(-1);
		//jOkButton = new JButton("ok");
        //jCancelButton = new JButton("Cancel");
        jNewButton = new JButton("Edit");

        initData();

        this.getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
        this.add(jLabel1);
        this.add(ca);
        this.add(jLabel2);
        this.add(jNewButton);
        this.add(jLabel3);
//		this.add(jOkButton);
//		this.add(jCancelButton);
        jNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("new policy...");

                String location = Nubisave.properties.getProperty("policy_directory");

                FileDialog fd = new FileDialog(new Frame(), "Custom Policy", FileDialog.SAVE);
                fd.setLocationRelativeTo(fd.getParent());
                fd.setDirectory(location);

                FilenameFilter ff = new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        if (name.endsWith("xml")) {
                            return true;
                        }
                        return false;
                    }
                };
                fd.setFilenameFilter(ff);
                fd.setVisible(true);

                if (fd.getFile() == null) {
                    System.out.println("new policy is cancelled...");
                    return;
                }

                if (!fd.getFile().endsWith("xml")) {
                    System.out.println("new policy is cancelled...");
                    return;
                }

                String strFileToEdit = fd.getDirectory() + fd.getFile();
                System.out.println(fd.getDirectory() + fd.getFile());

                File fileToEdit = new File(strFileToEdit);

                boolean result = false;
                // if the file does not exist, create it
                if (!fileToEdit.exists()) {
                    try {
                        result = fileToEdit.createNewFile();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }

                try {
                    Runtime.getRuntime().exec("xdg-open " + fileToEdit);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                if (result) {
                    initData();
                    validate();
                }
            }
        });
        this.pack();
//		this.setVisible(true);  

    }

    private void initData() {
        String location = Nubisave.properties.getProperty("policy_directory");

        if (location == null) {
            System.out.println("Policy location is undefined.");
            return;
        } else {
            System.out.println("Policy location: " + location);
        }

        ca.removeAllItems();
        listPolicyName.clear();

        File folder = new File(location);

        // if the directory does not exist, create it
        if (!folder.exists()) {
            System.out.println("creating directory: " + folder);
            boolean result = folder.mkdir();
            if (result) {
                System.out.println("directory created");
            }
        }

        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return;
        }

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                //System.out.println("File " + listOfFiles[i].getName());
                listPolicyName.add(listOfFiles[i].getName().substring(0, listOfFiles[i].getName().length() - 4));
            } else if (listOfFiles[i].isDirectory()) {
                //System.out.println("Directory " + listOfFiles[i].getName());
            }
        }

        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return arg0.compareTo(arg1);
            }
        };
        Collections.sort(listPolicyName, comparator);
        for (String strPolicyName : listPolicyName) {
            ca.addItem(strPolicyName);
        }
    }
}
