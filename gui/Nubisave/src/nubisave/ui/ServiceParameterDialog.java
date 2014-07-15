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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map.Entry;

import nubisave.StorageService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import nubisave.ui.backendconfig.*;
import org.ini4j.Ini;


public class ServiceParameterDialog extends javax.swing.JDialog {

    protected StorageService service;
    private javax.swing.JButton applyBtn;
    private javax.swing.JButton cancelBtn;
    protected Ini config;
    private List<JPanel> sectionGroupBoxes;

    /**
     * Factory method to create an appropriate instance of a graphical configuration dialogu.
     * If service has a name that corresponds to a class in the backendconfig subpackage
     * it is used. Otherwise this class is used. I.e. if service.getName() equals directory.ini,
     * Directory.java is used instead of this class.
     * @param parent
     * @param modal
     * @param service
     * @return An instance of ServiceParameterDialog
     */
    public static JDialog getInstance(java.awt.Frame parent, boolean modal,StorageService service)  {
        System.out.println("getinstance");
        try {
            String serviceName = service.getName();
            String targetClassName = serviceName.substring(0, 1).toUpperCase() + serviceName.substring(1); //Capitalize first character
            Class targetClass = Class.forName("nubisave.ui.backendconfig."+targetClassName); //create the class through reflection
            Constructor constructor = targetClass.getConstructor(java.awt.Frame.class, boolean.class, StorageService.class);
            return (JDialog) constructor.newInstance(parent, modal, service);
        } catch (ClassNotFoundException ex) {} 
          catch (NoSuchMethodException ex) {}
          catch (InstantiationException ex) {}
          catch (IllegalAccessException ex) {}
          catch (IllegalArgumentException ex) {}
          catch (InvocationTargetException ex) {}
        return new ServiceParameterDialog(parent, modal, service); //if an exception occured, use this class instead
    }
                
    /** Creates new generic ServiceParameterDialog form */
    public ServiceParameterDialog(java.awt.Frame parent, boolean modal,StorageService service) {
        super(parent, modal);
        this.service = service;
        initComponents();
        sectionGroupBoxes = new ArrayList<JPanel>();

        JPanel mainGrid = new JPanel();
        setContentPane(mainGrid);
        mainGrid.setLayout(new GridBagLayout());
        mainGrid.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        GridBagConstraints c;
        JLabel label;
        JPanel groupBox;
        JTextField textfield;
        int cntMainGridLine = 0;
        config = service.getConfig();
        Set<String> sectionNames = config.keySet(); // get all section names
        for(String section: sectionNames){
            if(isHiddenSection(section)){
                continue;
            }
            //add section name
            groupBox = new JPanel();
            groupBox.setBorder(BorderFactory.createTitledBorder(section));
            groupBox.setLayout(new GridBagLayout());
            groupBox.setName(section);
            //label.setFont(new Font("Courier New", Font.BOLD, 16));
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 4;
            c.weightx = 1;
            c.gridx = 0;
            c.gridy = cntMainGridLine;
            mainGrid.add(groupBox, c);
            sectionGroupBoxes.add(groupBox);

            cntMainGridLine++;

            int cntGroupBoxLine = 0;
            for (Map.Entry<String, String> parameter : config.get(section).entrySet()){
                if(isHiddenParameter(parameter, section)){
                    continue;
                }
                label = new JLabel(parameter.getKey());
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                c.gridx = 0;
                c.gridy = cntGroupBoxLine;
                groupBox.add(label, c);

                if(parameter.getKey().equals("password")){
                    textfield = new JPasswordField(parameter.getValue());
                } else {
                    textfield = new JTextField(parameter.getValue());
                }
                c = new GridBagConstraints();
                c.fill = GridBagConstraints.HORIZONTAL;
                c.weightx = 1;
                c.gridx = 1;
                c.gridy = cntGroupBoxLine;
                groupBox.add(textfield, c);

                cntGroupBoxLine++;

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
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = cntMainGridLine;
        mainGrid.add(applyBtn, c);

        c = new GridBagConstraints();
        c.weightx = 0.5;
        c.gridx = 2;
        c.gridy = cntMainGridLine;
        mainGrid.add(cancelBtn, c);
        pack();
        //usernameField.setText(service.getUser());
        //passwdField.setText(service.getPass());
        //usernameField.setText(service.getUser());
        //passwdField.setText(service.getPass());
    }

    private void applyBtnActionPerformed(java.awt.event.ActionEvent evt) {
        
        System.out.println("apply button called");
        for(JPanel sectionGroupBox: sectionGroupBoxes){
            String name, value;
            for(int i=0; i< sectionGroupBox.getComponentCount();i+=2 ){
                name = ((JLabel)sectionGroupBox.getComponent(i)).getText();
                value = ((JTextField)sectionGroupBox.getComponent(i+1)).getText();
                config.get(sectionGroupBox.getName()).put(name, value);
                nubisave.Nubisave.services.update(service);
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

    /**
     * Tell if this parameter should be hidden
     * The parameter should be hidden if it is preceeded with a comment "hidden" or if the parameter is one of "backendservice" or "isbackendmodule",
     * which are configured automatically.
     * @param parameter
     * @param section
     * @return
     */
    private boolean isHiddenParameter(Entry<String, String> parameter, String section) {
        boolean isHidden = false;
        if(parameter.getKey().equals("backendservice") || parameter.getKey().equals("isbackendmodule")){
            isHidden = true;
        }
        String comment = config.get(section).getComment(parameter.getKey());
        if("hidden".equals(comment)){
            isHidden = true;
        }
        return isHidden;
    }

    /**
     * Tell if this section should be hidden
     * The section should be hidden if it is preceeded with a comment "hidden" or if all of its parameters are hidden
     * @param section
     * @return true iff this section should not be displayed
     */
    private boolean isHiddenSection(String section) {
        boolean isHidden;
        String comment = config.getComment(section);
        if("hidden".equals(comment)){
            return true;
        }
        isHidden = true;
        for (Map.Entry<String, String> parameter : config.get(section).entrySet()){
            if(! isHiddenParameter(parameter, section) ){
                isHidden = false;
            }
        }
        return isHidden;
    }

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
