/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nubisave.ui;

import java.awt.Dialog;
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

import javax.swing.JComboBox;

import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

/**
 * 
 * @author alok
 */
public class CustomServiceDlg extends javax.swing.JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7284175277529145890L;

	private String dir = null;

	private HashMap<String, String> inimap = new HashMap<String, String>();

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel desLabel;

	private javax.swing.JButton jCancelButton;

	private javax.swing.JComboBox<String> jComboBox1;

	private javax.swing.JLabel jLabel1;

	private javax.swing.JButton jOkButton;

	private javax.swing.JScrollPane jScrollPane1;

	private javax.swing.JSeparator jSeparator1;

	private javax.swing.JTextPane jmoduledesText;
	// End of variables declaration//GEN-END:variables
	
	/**
	 * Creates new form CustomServiceDlg
	 */
	public CustomServiceDlg() {
		initComponents();
		dir = nubisave.Nubisave.properties.getProperty("mount_script_directory");
		File fileObj = new File(dir);
		File[] files = fileObj.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".ini");
			}
		});
		List<String> listModuleName = new ArrayList<String>();
		String strModuleName = null;
		for (File file : files) {
			strModuleName = getModuleName(file);
			if (strModuleName == null) {
				strModuleName = file.getName().split("\\.")[0];
			}
			listModuleName.add(strModuleName);
		}
		Comparator<String> comparator = new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				return arg0.compareTo(arg1);
			}
		};
		Collections.sort(listModuleName,comparator);
		for(String strModuleNameItem : listModuleName) {
			jComboBox1.addItem(strModuleNameItem);
		}
		jComboBox1.addItem("Custom...");
		
		this.pack();
		System.out.println(this.getSize());
		this.setLocationRelativeTo(null);
		this.setTitle("Module Selection");
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	
	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		jLabel1 = new javax.swing.JLabel();
		jComboBox1 = new JComboBox<String>();
		jSeparator1 = new javax.swing.JSeparator();
		jCancelButton = new javax.swing.JButton();
		jOkButton = new javax.swing.JButton();
		desLabel = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		jmoduledesText = new javax.swing.JTextPane();

		setResizable(false);

		jLabel1.setBackground(new java.awt.Color(221, 40, 85));
		jLabel1.setText("Module Selection");

		jComboBox1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jComboBox1ActionPerformed(evt);
			}
		});

		jCancelButton.setText("Cancel");
		jCancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jCancelButtonActionPerformed(evt);
			}
		});

		jOkButton.setText("OK");
		jOkButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jOkButtonActionPerformed(evt);
			}
		});

		desLabel.setText("Module Description");

		jmoduledesText.setEditable(false);
		jScrollPane1.setViewportView(jmoduledesText);
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jSeparator1,
				javax.swing.GroupLayout.Alignment.TRAILING).addGroup(
				layout.createSequentialGroup().addContainerGap().addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
								layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(jCancelButton,
										javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(18, 18, 18).addComponent(
										jOkButton, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(
								layout.createSequentialGroup().addGroup(
										layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel1,
												javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(desLabel))
										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE).addGroup(
												layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jScrollPane1)
														.addComponent(jComboBox1, 0, 202, Short.MAX_VALUE)))).addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup().addContainerGap().addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(jComboBox1,
								javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(
						18, 18, 18).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(desLabel).addComponent(jScrollPane1,
								javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(jSeparator1,
						javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(
						javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(
						layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jOkButton).addComponent(jCancelButton,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));

		pack();
	} // </editor-fold>//GEN-END:initComponents

	public String getModuleName(File file) {
		String modulename = null;
		try {
			Ini ini = new Ini(file);
			modulename = ini.get("module", "name"); // parse the ini file to get
			// the name
			if (modulename != null) {
				inimap.put(modulename, file.getName());
			} else {
				inimap.put(file.getName().split("\\.")[0], file.getName());
			}
		} catch (InvalidFileFormatException ex) {
			Logger.getLogger(CustomServiceDlg.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(CustomServiceDlg.class.getName()).log(Level.SEVERE, null, ex);
		}
		return modulename;
	}

	public Object getItemName() {
		if (jComboBox1.getSelectedItem().equals("Custom...")) {
			return null;
		}
		return inimap.get(jComboBox1.getSelectedItem());
	}

	private void jCancelButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jCancelButtonActionPerformed
		dispose();
	} // GEN-LAST:event_jCancelButtonActionPerformed

	private void jOkButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jOkButtonActionPerformed
		dispose();
	} // GEN-LAST:event_jOkButtonActionPerformed

	private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_jComboBox1ActionPerformed
		String inifile = (String) getItemName();
		if (inifile != null) {
			inifile = dir + inifile;
			try {
				Ini ini = new Ini(new File(inifile));
				String desc = ini.get("module", "desc");
				jmoduledesText.setText(desc);
			} catch (InvalidFileFormatException ex) {
				Logger.getLogger(CustomServiceDlg.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex) {
				Logger.getLogger(CustomServiceDlg.class.getName()).log(Level.SEVERE, null, ex);
			}
		} else {
			jmoduledesText.setText("");
		}
	} // GEN-LAST:event_jComboBox1ActionPerformed
}
