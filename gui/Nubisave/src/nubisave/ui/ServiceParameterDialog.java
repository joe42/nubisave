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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import nubisave.StorageService;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import org.ini4j.Ini;


public class ServiceParameterDialog extends javax.swing.JDialog {

    private StorageService service;
    private ArrayList<JLabel> jLabels;
    private JLabel usernameLabel;
    private JLabel usernameTextField;
    private JLabel passwdLabel;
    private javax.swing.JPasswordField passwdField;
    private ArrayList<JTextField> jTextFields;
    private javax.swing.JButton applyBtn;
    private javax.swing.JButton cancelBtn;
    private Ini config;
    
    /** Creates new form ServiceParameterDialog */
    public ServiceParameterDialog(java.awt.Frame parent, boolean modal,StorageService service) {
        super(parent, modal);
        this.service = service;
        initComponents();
        jLabels = new ArrayList<JLabel>();
        jTextFields = new ArrayList<JTextField>();

        jLabels = new ArrayList<JLabel>();
        jTextFields = new ArrayList<JTextField>();
        Container pane = getContentPane();
        pane.setLayout(new GridBagLayout());
        GridBagConstraints c;
        JLabel label;
        JTextField textfield;
        int line = 0;
        config = service.getConfig();
        Set<String> sectionNames = config.keySet(); // get all section names
        String comment;
        for(String section: sectionNames){
            comment = config.getComment(section);
            if("hidden".equals(comment)){
                continue;
            }
            //add section name
            label = new JLabel(section);
            label.setFont(new Font("Courier New", Font.BOLD, 16));
            jLabels.add(label);
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 2;
            c.weightx = 0.5;
            c.gridx = 0;
            c.gridy = line;
            pane.add(label, c);
                
            line++;
            for (Map.Entry<String, String> parameter : config.get(section).entrySet()){
                if(parameter.getKey().equals("backendservice") || parameter.getKey().equals("isbackendmodule")){
                    continue;
                }
                comment = config.get(section).getComment(parameter.getKey());
                if("hidden".equals(comment)){
                    continue;
                }
                label = new JLabel(parameter.getKey());
                jLabels.add(label);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 0.5;
                c.gridx = 0;
                c.gridy = line;
                pane.add(label, c);

                if(parameter.getKey().equals("password")){
                    textfield = new JPasswordField(parameter.getValue());
                } else {
                    textfield = new JTextField(parameter.getValue());
                }
                jTextFields.add(textfield);
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 0.5;
                c.gridx = 1;
                c.gridy = line;
                pane.add(textfield, c);

                line++;

            }
        }
        applyBtn = new JButton("Apply");
        cancelBtn = new JButton("Cancel");

        applyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyBtnActionPerformed(e);
            }
        });
        cancelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelBtnActionPerformed(e);
            }
        });

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = line;
        pane.add(applyBtn, c);

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = line;
        pane.add(cancelBtn, c);
        pack();
        //usernameField.setText(service.getUser());
        //passwdField.setText(service.getPass());
        //usernameField.setText(service.getUser());
        //passwdField.setText(service.getPass());
    }

    private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {
        Set<String> sectionNames = config.keySet(); // get all section names
        for(String section: sectionNames){
            String name, value;
            int sectionCnt = 0;
            for(int i=0; i< jTextFields.size();i++ ){
                if(sectionNames.contains(jLabels.get(i+sectionCnt).getText())){ //ignore section labels which have no textfield value
                    sectionCnt++;
                }
                name = jLabels.get(i+sectionCnt).getText();
                value = jTextFields.get(i).getText();
                config.get(section).put(name, value);
            }
        }
        dispose();
    }

    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(334, 122));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 334, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 122, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
