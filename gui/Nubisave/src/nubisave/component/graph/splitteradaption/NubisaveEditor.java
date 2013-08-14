package nubisave.component.graph.splitteradaption;

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 */

import com.github.joe42.splitter.util.file.PropertiesUtil;
import nubisave.component.graph.mouseplugins.extension.AbstractNubisaveComponentEdgeCreator;
import nubisave.component.graph.mouseplugins.extension.PreventEdgeCreationForRestrictedPorts;
import nubisave.component.graph.mouseplugins.extension.ToggleActivateNubisaveComponentOnDoubleClick;
import nubisave.component.graph.mouseplugins.StorageServicePicker;
import nubisave.component.graph.mouseplugins.PopupEditor;
import nubisave.component.graph.vertice.NubiSaveComponent;
import nubisave.component.graph.vertice.GenericNubiSaveComponent;
import nubisave.component.graph.mouseplugins.extension.DataVertexEdgeCreator;
import nubisave.component.graph.mouseplugins.extension.AbstractNubiSaveComponentCreator;
import nubisave.component.graph.edge.WeightedNubisaveVertexEdge;
import nubisave.component.graph.edge.NubiSaveEdge;
import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.collections15.Factory;

import nubisave.component.graph.tranformer.BufferedImageDelegatorVertexIconTransformer;
import nubisave.component.graph.tranformer.BufferedImageDelegatorVertexShapeTransformer;
import nubisave.component.graph.vertice.AbstractNubisaveComponent;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.ViewScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.BasicStroke;
import java.awt.Dialog;
import java.awt.Dialog.ModalityType;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import net.contentobjects.jnotify.*;
import nubisave.Nubisave;
import nubisave.Services;
import nubisave.StorageService;
import nubisave.component.graph.mouseplugins.ExtensibleNubisaveComponentMousePlugin;
import nubisave.component.graph.mouseplugins.VertexPicker;
import nubisave.component.graph.pickedvertexlistener.BufferedImageDelegatorHighlighter;
import nubisave.component.graph.SortedSparseMultiGraph;
import nubisave.component.graph.tranformer.GenericComponentLabeller;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.splitteradaption.ActionKeyAdapter;
import nubisave.component.graph.splitteradaption.ActionKeyAdapter;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.component.graph.vertice.interfaces.VertexGroup;
import nubisave.ui.AddServiceDialog;
import nubisave.ui.CustomServiceDlg;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.ini4j.Ini;


/**
 *
 */
public class NubisaveEditor extends JApplet {

    private static final long serialVersionUID = -2023243689258876721L;
    private String storage_directory;
    Graph<NubiSaveVertex,NubiSaveEdge> graph;
    NubiSaveComponent nubiSaveComponent;
    AbstractLayout<NubiSaveVertex,NubiSaveEdge> layout;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<NubiSaveVertex,NubiSaveEdge> vv;

    String instructions =
        "<html>"+
        "<h3>Create a Component:</h3>"+
        "<ul>"+
        "<li>Click the <b>Local Component</b> button to select a deployment descriptor in the opening dialog"+
        "<li>Click on an empty area of the editor for the component to be added"+

        "</ul>"+
        "<h3>Transform the Display:</h3>"+
        "<ul>"+
        "<li>Press the mouse on an empty area to drag it and then release the mouse to change the viewpoint"+
        "<li>Turn the mouse wheel inside the editor to zoom in and out"+
        "</ul>"+

        "<h3>Move a Component:</h3>"+
        "<ul>"+
        "<li>Press the mouse on a component to drag it and then release the mouse to move it to a new location"+
        "</ul>"+

        "<h3>Delete a Component:</h3>"+
        "<ul>"+
        "<li>Right-click on the component to open a popup dialog and select <b>Delete Component</b>"+
        "<li>This action will open a dialog asking if the component should be deleted"+
        "<li>A shortcut is to select a component by clicking onto it. This will highlight the component"+
        "<li>Then press the <b>Del</b> key on your keyboard, which deletes the selected component instantly"+
        "</ul>"+

        "<h3>Configure a Component:</h3>"+
        "<ul>"+
        "<li>Right-click on the component to open a popup dialog and select <b>Configure Component</b>"+
        "<li>Set the desired parameters in the opening dialog and click <b>Apply</b> to confirm the configuration or abort it by clicking the <b>Cancel</b> button"+
        "<li>To increase the <b>weight</b> of a connection between components, connect the ports of these components again"+
        "<li>The weight is displayed as a label of the connecting arrows. It determines the fraction of data being stored at the component offering the <b>Provided Port</b>"+
        "</ul>"+

        "<h3>Configure the Component Architecture:</h3>"+
        "<ul>"+
        "<li>Press the mouse on the <b>Provided Port<b> or the <b>Required Port</b> of a component"+
        "<li>Drag the mouse to the corresponding port of an other component and release the mouse"+
        "<li>This will connect the ports by an arrow"+
        "<li>The connection means that data of the component instance with the <b>Required Port</b> is stored at the component instance offering the provided port"+
        "<li>A port might support a restricted number of connections. Once this number is satisfied, no more connections can be made to this port"+
        "</ul>"+

        "<h3>Instantiate a Component:</h3>"+
        "<ul>"+
        "<li>Double-click a component to mount it and wait until a green checkmark appears"+
        "<li>At first, the <b>NubiSave<b> component should be instantiated if it is not already"+
        "<li>To remove an instance, double-click the component again and wait until the checkmark disappears or remove the component"+
        "</ul>"+

        "<h3>Transfer Data:</h3>"+
        "<ul>"+
        "<li>Press the mouse on the <b>Data Icon</b> of a component and drag it to an other <b>Data Icon</b>"+
        "<li>Releasing the mouse will display a transfer connection between the <b>Data Icons</b>"+
        "<li>All data stored at the component instance with the <b>Data Icon</b> first selected will be transfered to the second component instance"+
        "<li>The transfer progress is displayed in the label of the connection, which disappears after the transfer is completed"+
        "<li>Note that the transfer can only be done between two instantiated components"+
        "</ul>"+
        "</html>";
    private final DataVertexEdgeFactory dataVertexEdgeFactory;

    /**
     * create an instance of a simple graph with popup controls to
     * create a graph.
     *
     */
    public NubisaveEditor() {
        
        // create a simple graph for the demo
        graph = new SortedSparseMultiGraph<NubiSaveVertex,NubiSaveEdge>();
        this.layout = new StaticLayout<NubiSaveVertex,NubiSaveEdge>(graph,
                new Dimension(600,600));
        vv =  new VisualizationViewer<NubiSaveVertex, NubiSaveEdge>(layout);
        dataVertexEdgeFactory = new DataVertexEdgeFactory();
        storage_directory= new PropertiesUtil("nubi.properties").getProperty("storage_configuration_directory");

        //Immediate Adaption to external changes
        int mask = JNotify.FILE_CREATED  |
               JNotify.FILE_DELETED  |
               JNotify.FILE_MODIFIED |
               JNotify.FILE_RENAMED;
        boolean watchSubtree = false;
//        try {
//            JNotify.addWatch(Nubisave.mainSplitter.getConfigDir(), mask, watchSubtree, new JNotifyConfigUpdater(dataVertexEdgeFactory, graph, vv));
//        } catch (Exception ex) {
//            Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
//        }


        vv.setBackground(Color.white);
        vv.getRenderContext().setEdgeStrokeTransformer(new EdgeWeightStrokeFunction<NubiSaveEdge>());
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<NubiSaveEdge>());
        vv.getRenderContext().setVertexShapeTransformer(new BufferedImageDelegatorVertexShapeTransformer<NubiSaveVertex>());
        vv.getRenderContext().setVertexIconTransformer(new BufferedImageDelegatorVertexIconTransformer<NubiSaveVertex>());
        vv.setVertexToolTipTransformer(vv.getRenderContext().getVertexLabelTransformer());
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        vv.getRenderContext().setVertexLabelTransformer(new GenericComponentLabeller<NubiSaveVertex>());
	vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<NubiSaveVertex,NubiSaveEdge>());

        new BufferedImageDelegatorHighlighter(vv.getPickedVertexState());

        Container content = this;
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        content.add(panel);
        final StatefulNubiSaveComponentFactory vertexFactory = new StatefulNubiSaveComponentFactory();
        Factory<? extends NubiSaveEdge> edgeFactory = new WeightedNubisaveVertexEdgeFactory();
        final PluggableGraphMouse graphMouse = createPluggableGraphMouse(vv.getRenderContext(), vertexFactory, edgeFactory, dataVertexEdgeFactory);
//        try {
//            // the EditingGraphMouse will pass mouse event coordinates to the
//            // vertexLocations function to set the locations of the vertices as
//            // they are created
            	        //graphMouse.setVertexLocations(vertexLocations);
//            nubiSaveComponent = new NubiSaveComponent();
//            nubiSaveComponent.addToGraph(vv, new java.awt.Point((int)layout.getSize().getHeight()/2,(int)layout.getSize().getWidth()/2));
//        } catch (IOException ex) {
//            Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
//        }
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(new ActionKeyAdapter(vv.getPickedVertexState(), graph));

            JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(vv, instructions);
            }
        });
        addServicesToGraph();
        Graph<AbstractNubisaveComponent,Object> nubisaveComponentGraph = new VertexPredicateFilter(new Predicate() {
            @Override
            public boolean evaluate(Object vertex){
                return vertex instanceof AbstractNubisaveComponent;
            }
        }).transform(graph);
        interconnectNubisaveComponents(nubisaveComponentGraph, edgeFactory);
        //interconnectNubisaveComponents();

        JPanel controls = new JPanel();
        JButton chooseLocalComponent = new JButton("Custom Storage/Modification/Splitter Module");
        chooseLocalComponent.addActionListener(new ActionListener() {
            /**
             * Create new {@link StorageService} from chosen file and set it as the next Vertex to create in {@link StatefulNubiSaveComponentFactory}
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                CustomServiceDlg cusDlg=new CustomServiceDlg();
                cusDlg.pack();
                cusDlg.setLocationRelativeTo(null);
                cusDlg.setTitle("Module Selection");
                cusDlg.setModalityType(Dialog.ModalityType.APPLICATION_MODAL); 
                cusDlg.setVisible(true);
                String module=(String) cusDlg.getItemName();
                if (module!=null){
                    module=module.split("\\.")[0];  
                }
                if (module!=null){
                    if(cusDlg.okstatus=="True"){
                        if( module.toLowerCase().equals("nubisave")){
                            StorageService newService = new StorageService(module);
                            try {
                                vertexFactory.setNextInstance(new NubiSaveComponent(newService));
                                //nubiSaveComponent.addToGraph(vv, new java.awt.Point((int)layout.getSize().getHeight()/2,(int)layout.getSize().getWidth()/2));
                            } catch (IOException ex) {
                                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            nubisave.Nubisave.services.addNubisave(newService);  
                        } else {
                            StorageService newService = new StorageService(module);
                            try {
                                vertexFactory.setNextInstance(new GenericNubiSaveComponent(newService));
                            } catch (IOException ex) {
                                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            nubisave.Nubisave.services.add(newService); 
                        }
                    }
                } else {
                      JFileChooser customStorageserviceChooser = new javax.swing.JFileChooser();
                        customStorageserviceChooser.setCurrentDirectory(new java.io.File(nubisave.Nubisave.mainSplitter.getMountScriptDir()));
                        customStorageserviceChooser.setDialogTitle("Custom Service");
                        customStorageserviceChooser.setFileFilter(new IniFileFilter());
                        int returnVal = customStorageserviceChooser.showOpenDialog(null);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = customStorageserviceChooser.getSelectedFile();
                            if (file.getName().toLowerCase().equals("nubisave.ini")){
                                StorageService newService = new StorageService(file);
                            try {
                                vertexFactory.setNextInstance(new NubiSaveComponent(newService));
                                //nubiSaveComponent.addToGraph(vv, new java.awt.Point((int)layout.getSize().getHeight()/2,(int)layout.getSize().getWidth()/2));
                            } catch (IOException ex) {
                                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            nubisave.Nubisave.services.addNubisave(newService);
                            } else {
                            StorageService newService = new StorageService(file);
                            try {
                                vertexFactory.setNextInstance(new GenericNubiSaveComponent(newService));
                            } catch (IOException ex) {
                                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            nubisave.Nubisave.services.add(newService);
                        }
                    }
                }
              }
        });
        controls.add(chooseLocalComponent);
        JButton searchServiceComponent = new JButton("Storage Service Directory");
        searchServiceComponent.addActionListener(new ActionListener() {
            /**
             * Create new {@link StorageService} from chosen file and set it as the next Vertex to create in {@link StatefulNubiSaveComponentFactory}
             */
            @Override
            public void actionPerformed(ActionEvent ae) {
                AddServiceDialog addServiceDlg = new AddServiceDialog(null, true);
                addServiceDlg.setVisible(true);
            }
        });
        controls.add(searchServiceComponent);
        controls.add(help);
        content.add(controls, BorderLayout.SOUTH);
    }
    
    /**
     * Add {@link Nubisave#services} to graph if they are not yet displayed there.
     */
    private void addServicesToGraph() {
         for (int i = 0; i < nubisave.Nubisave.services.size(); i++) {
            StorageService persistedService = nubisave.Nubisave.services.get(i);
            AbstractNubisaveComponent vertex=null;
            if(persistedService.getName().toLowerCase().equals("nubisave")){
                try {
                    vertex = new NubiSaveComponent(persistedService);
                } catch (IOException ex) {
                    Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    vertex = new GenericNubiSaveComponent(persistedService);
                } catch (IOException ex) {
                    Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
                Point pos = persistedService.getGraphLocation();
                if (pos == null) {
                    pos = new Point(new java.awt.Point((int) layout.getSize().getHeight() / 2, (int) layout.getSize().getWidth() / 2 - i * 10-10));
                }
                if(!graph.containsVertex(vertex)){
                    System.out.println("add vertice "+vertex.getUniqueName());
                    vertex.addToGraph(vv, pos);
                    layout.setLocation(vertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(pos));
                }
        }
    }

    class StatefulNubiSaveComponentFactory implements Factory<AbstractNubisaveComponent> {
        private AbstractNubisaveComponent next;
        /**
         * @return the last instance of AbstractNubiSaveComponent passed in by {@link #setNextInstance()} or null
         */
        @Override
        public AbstractNubisaveComponent create() {
            AbstractNubisaveComponent ret = next;
            next = null;
            return ret;
        }
        /**
         * Set the next value to return by this factory.
         * @param next
         */
        public void setNextInstance(AbstractNubisaveComponent next){
            this.next = next;
        }
    }

    public class WeightedNubisaveVertexEdgeFactory implements Factory<WeightedNubisaveVertexEdge> {
        public WeightedNubisaveVertexEdge create() {
            return new WeightedNubisaveVertexEdge();
        }
    }

    class DataVertexEdgeFactory implements Factory<DataVertexEdge> {
        public DataVertexEdge create() {
            return new DataVertexEdge();
        }
    }

   
    /**
     * Allow only directories, or files with ".ini" extension
     */
    class IniFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File file) {
            return file.isDirectory() || file.getAbsolutePath().endsWith(".ini");
        }
        @Override
        public String getDescription() {
            return "*.ini";
        }
    }

    private final static class EdgeWeightStrokeFunction<NubisaveEdge> implements Transformer<NubisaveEdge, Stroke> {
        public Stroke transform(NubisaveEdge e) {
            int weight = 1;
            if(e instanceof WeightedNubisaveVertexEdge)
                weight = ((WeightedNubisaveVertexEdge)e).getWeight();
            return new BasicStroke(1+weight);
        }
    }



    protected PluggableGraphMouse createPluggableGraphMouse(RenderContext<NubiSaveVertex,NubiSaveEdge> rc,
                    Factory<AbstractNubisaveComponent> vertexFactory, Factory<? extends NubiSaveEdge> edgeFactory, DataVertexEdgeFactory dataVertexEdgeFactory) {
        PluggableGraphMouse graphMouse = new PluggableGraphMouse();
        VertexPicker<NubiSaveVertex, NubiSaveEdge> pickingPlugin = new VertexPicker<NubiSaveVertex, NubiSaveEdge>();
        TranslatingGraphMousePlugin translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        ScalingGraphMousePlugin scalingPlugin = new ScalingGraphMousePlugin(new ViewScalingControl(), 0, 1.1f, 1/1.1f);
        PopupEditor popupEditingPlugin = new PopupEditor(edgeFactory);
        StorageServicePicker<NubiSaveVertex, NubiSaveEdge> storageServicePickingPlugin = new StorageServicePicker<NubiSaveVertex, NubiSaveEdge>();

        ExtensibleNubisaveComponentMousePlugin extendablePlugin = new ExtensibleNubisaveComponentMousePlugin(vertexFactory, edgeFactory);
        extendablePlugin.addEventListener(new AbstractNubiSaveComponentCreator(vv, graph, vertexFactory));
        extendablePlugin.addEventListener(new ToggleActivateNubisaveComponentOnDoubleClick(vv, graph));
        extendablePlugin.addEventListener(new PreventEdgeCreationForRestrictedPorts(vv, graph));
        extendablePlugin.addEventListener(new DataVertexEdgeCreator(vv, graph, dataVertexEdgeFactory));
        extendablePlugin.addEventListener(new AbstractNubisaveComponentEdgeCreator(vv, graph, (WeightedNubisaveVertexEdgeFactory) edgeFactory));

        graphMouse.add(scalingPlugin);
        graphMouse.add(extendablePlugin);
        graphMouse.add(storageServicePickingPlugin);
        graphMouse.add(translatingPlugin);
        graphMouse.add(popupEditingPlugin);
        return graphMouse;
    }

    protected void interconnectNubisaveComponents(Graph<AbstractNubisaveComponent, Object> nubisaveComponentGraph, Factory<? extends NubiSaveEdge> edgeFactory) {
        boolean connected = false;
        WeightedNubisaveVertexEdge edge;
        File file = new File(storage_directory+"/"+ "connections.txt");
        HashMap hh= new HashMap();
        ArrayList<AbstractNubisaveComponent> myNodeList = new ArrayList<AbstractNubisaveComponent>(nubisaveComponentGraph.getVertices());
        ArrayList str=new ArrayList();
        for(int i=0;i<nubisaveComponentGraph.getVertices().size();i++){
           str.add(myNodeList.get(i).getUniqueName());
        }
        if(file.exists()){
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String strLine;
                try {
                    //Read the text file Line By Line
                    while ((strLine = reader.readLine()) != null) {
                    String startVertex=strLine.split(" ")[0];
                    String endVertex=strLine.split(" ")[1];
                    hh.put(startVertex,endVertex);
                                     
                    for(AbstractNubisaveComponent component:nubisaveComponentGraph.getVertices()){
                       if(component.getUniqueName().equals(startVertex)){                            
                           String endpoint=(String) hh.get(startVertex);
                           int pos=str.indexOf(endpoint);
                           AbstractNubisaveComponent endcomponent=myNodeList.get(pos);
                           edge = (WeightedNubisaveVertexEdge) edgeFactory.create();
                           edge.setWeight(component.getNrOfFilePartsToStore());
                           graph.addEdge(edge, component.getRequiredPorts().iterator().next(), endcomponent.getProvidedPorts().iterator().next(), EdgeType.DIRECTED);               
                       }
                   }
                  }
                reader.close();   
                } catch (IOException ex) {
                    Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
            } 
            }      
    }

}

