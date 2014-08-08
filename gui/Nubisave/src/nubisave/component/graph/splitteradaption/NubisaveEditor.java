package nubisave.component.graph.splitteradaption;

/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 *
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 *
 */
import nubisave.component.graph.mouseplugins.extension.AbstractNubisaveComponentEdgeCreator;
import nubisave.component.graph.mouseplugins.extension.PreventEdgeCreationForRestrictedPorts;
import nubisave.component.graph.mouseplugins.extension.ToggleActivateNubisaveComponentOnDoubleClick;
import nubisave.component.graph.mouseplugins.StorageServiceSelector;
import nubisave.component.graph.mouseplugins.PopupEditor;
import nubisave.component.graph.vertice.NubiSaveComponent;
import nubisave.component.graph.vertice.GenericNubiSaveComponent;
import nubisave.component.graph.vertice.CloudEntranceComponent;
import nubisave.component.graph.mouseplugins.extension.DataVertexEdgeCreator;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
import edu.uci.ics.jung.visualization.renderers.Renderer;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import net.contentobjects.jnotify.*;
import nubisave.Nubisave;
import nubisave.StorageService;
import nubisave.MatchmakerService;
import nubisave.component.graph.mouseplugins.ExtensibleNubisaveComponentMousePlugin;
import nubisave.component.graph.mouseplugins.VertexSelector;
import nubisave.component.graph.pickedvertexlistener.BufferedImageDelegatorHighlighter;
import nubisave.component.graph.SortedSparseMultiGraph;
import nubisave.component.graph.tranformer.GenericComponentLabeller;
import nubisave.component.graph.edge.DataVertexEdge;
import nubisave.component.graph.splitteradaption.ActionKeyAdapter;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.ui.AddServiceDialog;
import nubisave.ui.CustomServiceDlg;
import nubisave.ui.util.SystemIntegration;
import nubisave.web.NubiSaveWeb;
import nubisave.web.test.NativeSwingTest;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.NativeSwing;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import java.awt.HeadlessException;
import nubisave.component.graph.mouseplugins.MagneticMousePointer;
import org.ini4j.Ini;

/**
 * The visual graph editor for Nubisave, which allows to add, configure, and monitor
 * the components in real time.
 */
public class NubisaveEditor extends JApplet {

    private static final long serialVersionUID = -2023243689258876721L;

    private String storage_directory;

    public static NubiSaveWeb browser;

    Graph<NubiSaveVertex, NubiSaveEdge> graph;

    AbstractLayout<NubiSaveVertex, NubiSaveEdge> layout;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer<NubiSaveVertex, NubiSaveEdge> graphDisplay;

    String instructions = "<html>"
            + "<h3>Create a Component:</h3>"
            + "<ul>"
            + "<li>Click the <b>Local Component</b> button to select a deployment descriptor in the opening dialog"
            + "<li>Click on an empty area of the editor for the component to be added"
            + "</ul>"
            + "<h3>Transform the Display:</h3>"
            + "<ul>"
            + "<li>Press the mouse on an empty area to drag it and then release the mouse to change the viewpoint"
            + "<li>Turn the mouse wheel inside the editor to zoom in and out"
            + "</ul>"
            + "<h3>Move a Component:</h3>"
            + "<ul>"
            + "<li>Press the mouse on a component to drag it and then release the mouse to move it to a new location"
            + "</ul>"
            + "<h3>Delete a Component:</h3>"
            + "<ul>"
            + "<li>Right-click on the component to open a popup dialog and select <b>Delete Component</b>"
            + "<li>This action will open a dialog asking if the component should be deleted"
            + "<li>A shortcut is to select a component by clicking onto it. This will highlight the component"
            + "<li>Then press the <b>Del</b> key on your keyboard, which deletes the selected component instantly"
            + "</ul>"
            + "<h3>Configure a Component:</h3>"
            + "<ul>"
            + "<li>Right-click on the component to open a popup dialog and select <b>Configure Component</b>"
            + "<li>Set the desired parameters in the opening dialog and click <b>Apply</b> to confirm the configuration or abort it by clicking the <b>Cancel</b> button"
            + "<li>To increase the <b>weight</b> of a connection between components, connect the ports of these components again"
            + "<li>The weight is displayed as a label of the connecting arrows. It determines the fraction of data being stored at the component offering the <b>Provided Port</b>"
            + "</ul>"
            + "<h3>Configure the Component Architecture:</h3>"
            + "<ul>"
            + "<li>Press the mouse on the <b>Provided Port<b> or the <b>Required Port</b> of a component"
            + "<li>Drag the mouse to the corresponding port of an other component and release the mouse"
            + "<li>This will connect the ports by an arrow"
            + "<li>The connection means that data of the component instance with the <b>Required Port</b> is stored at the component instance offering the provided port"
            + "<li>A port might support a restricted number of connections. Once this number is satisfied, no more connections can be made to this port"
            + "</ul>" + "<h3>Instantiate a Component:</h3>" + "<ul>" + "<li>Double-click a component to mount it and wait until a green checkmark appears"
            + "<li>At first, the <b>NubiSave<b> component should be instantiated if it is not already"
            + "<li>To remove an instance, double-click the component again and wait until the checkmark disappears or remove the component" + "</ul>"
            + "<h3>Transfer Data:</h3>" + "<ul>" + "<li>Press the mouse on the <b>Data Icon</b> of a component and drag it to an other <b>Data Icon</b>"
            + "<li>Releasing the mouse will display a transfer connection between the <b>Data Icons</b>"
            + "<li>All data stored at the component instance with the <b>Data Icon</b> first selected will be transfered to the second component instance"
            + "<li>The transfer progress is displayed in the label of the connection, which disappears after the transfer is completed"
            + "<li>Note that the transfer can only be done between two instantiated components" + "</ul>" + "</html>";

    private final DataVertexEdgeFactory dataVertexEdgeFactory;
    private final Factory<? extends NubiSaveEdge> edgeFactory;
    private final PluggableGraphMouse graphMouse;
    private MagneticMousePointer magneticMousePointer;

    /**
     * Create an instance of a simple graph with popup controls to create a
     * graph.
     *
     */
    @SuppressWarnings("unchecked")
    public NubisaveEditor() {
        edgeFactory = new WeightedNubisaveVertexEdgeFactory();
        dataVertexEdgeFactory = new DataVertexEdgeFactory();
        Container content = this;
        graph = new SortedSparseMultiGraph<NubiSaveVertex, NubiSaveEdge>();
        this.layout = new StaticLayout<NubiSaveVertex, NubiSaveEdge>(graph, new Dimension(600, 600));
        graphDisplay = createGraphDisplay(content);
        graphMouse = createPluggableGraphMouse(graphDisplay.getRenderContext(), edgeFactory, dataVertexEdgeFactory);
        graphDisplay.setGraphMouse(graphMouse);
        //create a graph that can have undirected and directed edges
        storage_directory = Nubisave.properties.getProperty("storage_configuration_directory");
        content.add(new GraphZoomScrollPane(graphDisplay));
        addServicesToGraph();
        //create filtered graph with only nubisave components in it
        Graph<AbstractNubisaveComponent, Object> nubisaveComponentGraph = new VertexPredicateFilter(new Predicate() {
            @Override
            public boolean evaluate(Object vertex) {
                return vertex instanceof AbstractNubisaveComponent;
            }
        }).transform(graph);
        addCloudEntrance();
        interconnectNubisaveComponents(nubisaveComponentGraph, edgeFactory);
        JPanel controls = createControls();
        content.add(controls, BorderLayout.SOUTH);
        startMonitoring();
    }
    
    protected void startMonitoring(){
        // Immediate Adaption to external changes
        int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED | JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
        boolean watchSubtree = false;
            // try {
        // JNotify.addWatch(Nubisave.mainSplitter.getConfigDir(), mask,
        // watchSubtree, new JNotifyConfigUpdater(dataVertexEdgeFactory, graph,
        // vv));
        // } catch (Exception ex) {
        // Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE,
        // null, ex);
        // }
    }

    /**
     * Add {@link Nubisave#services} to graph if they are not yet displayed
     * there.
     */
    private void addServicesToGraph() {
        for (int i = 0; i < nubisave.Nubisave.services.size(); i++) {
            StorageService persistedService = nubisave.Nubisave.services.get(i);
            AbstractNubisaveComponent vertex = null;
            try{
                if (persistedService.getName().toLowerCase().equals("nubisave")) {
                    vertex = new NubiSaveComponent(persistedService);
                } else if (persistedService.getName().toLowerCase().equals("cloudentrance")) {
                    vertex = new CloudEntranceComponent(persistedService);
                } else {
                    vertex = new GenericNubiSaveComponent(persistedService);
                }
            } catch (IOException ex) {
                Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
            Point pos = persistedService.getGraphLocation();
            if (pos == null) {
                pos = new Point(new java.awt.Point((int) layout.getSize().getHeight() / 2, (int) layout.getSize().getWidth() / 2 - i * 10 - 10));
            }
            if (!graph.containsVertex(vertex)) {
                vertex.addToGraph(graphDisplay, pos);
                layout.setLocation(vertex, graphDisplay.getRenderContext().getMultiLayerTransformer().inverseTransform(pos));
            }
        }
    }

    /**
     * Create controls at the bottom of the editor like buttons for help,
     * selecting a module, or to start a configuration wizard.
     *
     * @return
     */
    private JPanel createControls() {
        JPanel controls = new JPanel();
        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, instructions);
            }
        });
        JButton chooseLocalComponent = new JButton("Custom Storage/Modification/Splitter Module");
        chooseLocalComponent.addActionListener(new ChooseLocalComponentActionListener());
        controls.add(chooseLocalComponent);
        JButton searchServiceComponent = new JButton("Storage Service Directory");
        searchServiceComponent.addActionListener(new ChooseServiceDirectoryComponent());
        controls.add(searchServiceComponent);
        JButton browserButton = new JButton("Visualization");
        browserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                SystemIntegration.openLocationbyBrowser("http://localhost/nubivis/index.html");
            }
        });
        controls.add(browserButton);
        controls.add(help);
        return controls;
    }

    private void addCloudEntrance() {
        StorageService service = new StorageService("Cloud Entrance");
        Ini cloudEntranceConfig = new Ini();
        cloudEntranceConfig.add("module", "name", "Cloud Entrance");
        cloudEntranceConfig.add("module", "desc", "The access point of the cloud storage.");
        cloudEntranceConfig.add("parameter", "path", "~/.nubisave/nubisavemount/data");
        cloudEntranceConfig.add("gui", "graphlocationx", 800);
        cloudEntranceConfig.add("gui", "graphlocationy", 150);
        cloudEntranceConfig.get("gui").putComment("graphlocationx", "hidden");
        cloudEntranceConfig.get("gui").putComment("graphlocationy", "hidden");
        service.setConfig(cloudEntranceConfig);
        CloudEntranceComponent cloudEntrance = null;
        try {
            cloudEntrance = new CloudEntranceComponent(service);
        } catch (IOException ex) {
            Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Point pos = service.getGraphLocation();
        if (!graph.containsVertex(cloudEntrance)) {
            cloudEntrance.addToGraph(graphDisplay, pos);
            layout.setLocation(cloudEntrance, graphDisplay.getRenderContext().getMultiLayerTransformer().inverseTransform(pos));
        }
    }
    /**
     * Create new Nubisave component from the chosen service and create it
     * at the center of the Nubisave editor. Let it move with the mouse
     * pointer until the user clicks in the editor.
     */
    protected class ChooseServiceDirectoryComponent implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae) {
            AddServiceDialog addServiceDlg = new AddServiceDialog(null, true);
            addServiceDlg.setVisible(true);
            for (MatchmakerService newService : addServiceDlg.getSelectedServices()) {
                //Create the component in the middle of the editor,
                //and move it with the mouse pointer until clicked
                GenericNubiSaveComponent newComponent = null;
                try {
                    newComponent = new GenericNubiSaveComponent(newService);
                } catch (IOException ex) {
                    Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
                nubisave.Nubisave.services.add(newService);
                newComponent.addToGraph(graphDisplay,
                        new java.awt.Point((int) layout.getSize().getHeight() / 2,
                        (int) layout.getSize().getWidth() / 2));
                magneticMousePointer.setMagneticComponent(newComponent);
            }
        }
    }
    
    /**
     * Create new Nubisave component from chosen file and create it
     * at the center of the Nubisave editor. Let it move with the mouse
     * pointer until the user clicks in the editor.
     */
    protected class ChooseLocalComponentActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent ae) {
            if (magneticMousePointer.getMagneticComponent() != null) { 
                return;
            }
            CustomServiceDlg cusDlg = new CustomServiceDlg();
            cusDlg.setVisible(true);
            String module = (String) cusDlg.getItemName();
            if (magneticMousePointer.getMagneticComponent() == null) {
                AbstractNubisaveComponent newComponent = null;
                try {
                    if (module != null) {
                        //a module has been selected
                        module = module.split("\\.")[0];
                        if (module.toLowerCase().equals("nubisave")) {
                            StorageService newService = new StorageService(module);
                            newComponent = new NubiSaveComponent(newService);
                            nubisave.Nubisave.services.addNubisave(newService);
                        } else {
                            StorageService newService = new StorageService(module);
                            newComponent = new GenericNubiSaveComponent(newService);
                            nubisave.Nubisave.services.add(newService);
                        }
                    } else { 
                        //No module has been selected, so open a file chooser 
                        //to select a custom module
                        newComponent = chooseCustomModule();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(NubisaveEditor.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(newComponent != null){
                    //Create the component in the middle of the editor,
                    //and move it with the mouse pointer until clicked
                    newComponent.addToGraph(graphDisplay, 
                                        new java.awt.Point((int)layout.getSize().getHeight()/2,
                                                (int)layout.getSize().getWidth()/2));
                    magneticMousePointer.setMagneticComponent(newComponent);
                }
            }
        }

        /**
         * Choose a custom module from the file system.
         * @return the Nubisave component created from the selected ini file
         * @throws HeadlessException
         * @throws IOException 
         */
        public AbstractNubisaveComponent chooseCustomModule() throws HeadlessException, IOException {
            AbstractNubisaveComponent newComponent = null;
            JFileChooser customStorageserviceChooser = new javax.swing.JFileChooser();
            customStorageserviceChooser.setCurrentDirectory(new java.io.File(nubisave.Nubisave.mainSplitter.getMountScriptDir()));
            customStorageserviceChooser.setDialogTitle("Custom Service");
            customStorageserviceChooser.setFileFilter(new IniFileFilter());
            int returnVal = customStorageserviceChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = customStorageserviceChooser.getSelectedFile();
                if (file.getName().toLowerCase().equals("nubisave.ini")) {
                    StorageService newService = new StorageService(file);
                    newComponent = new NubiSaveComponent(newService);
                    nubisave.Nubisave.services.addNubisave(newService);
                } else {
                    StorageService newService = new StorageService(file);
                    newComponent = new GenericNubiSaveComponent(newService);
                    nubisave.Nubisave.services.add(newService);
                }
            } 
            return newComponent;
        }

    }

    private VisualizationViewer<NubiSaveVertex, NubiSaveEdge> createGraphDisplay(Container content) {
        graphDisplay = new VisualizationViewer<NubiSaveVertex, NubiSaveEdge>(layout);
        graphDisplay.setBackground(Color.white);
        graphDisplay.getRenderContext().setEdgeStrokeTransformer(new EdgeWeightStrokeFunction<NubiSaveEdge>());
        graphDisplay.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<NubiSaveEdge>());
        graphDisplay.getRenderContext().setVertexShapeTransformer(new BufferedImageDelegatorVertexShapeTransformer<NubiSaveVertex>());
        graphDisplay.getRenderContext().setVertexIconTransformer(new BufferedImageDelegatorVertexIconTransformer<NubiSaveVertex>());
        graphDisplay.setVertexToolTipTransformer(graphDisplay.getRenderContext().getVertexLabelTransformer());
        graphDisplay.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
        graphDisplay.getRenderContext().setVertexLabelTransformer(new GenericComponentLabeller<NubiSaveVertex>());
        graphDisplay.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<NubiSaveVertex, NubiSaveEdge>());
        new BufferedImageDelegatorHighlighter(graphDisplay.getPickedVertexState());
        graphDisplay.addKeyListener(new ActionKeyAdapter(graphDisplay.getPickedVertexState(), graph));
        return graphDisplay;
    }

    /**
     * Creates edges of type WeightedNubisaveVertexEdge, which connect the outgoing
     * port of one Nubisave component to the ingoing port of another Nubisave component.
     */
    public class WeightedNubisaveVertexEdgeFactory implements Factory<WeightedNubisaveVertexEdge> {

        public WeightedNubisaveVertexEdge create() {
            return new WeightedNubisaveVertexEdge();
        }
    }
    
    /**
     * Creates edges of type DataVertexEdge, which connect two cloud drives
     * symbolizing the ongoing data migration from one drive to the other.
     */
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

    /**
     * Provides the width for edges of class NubisaveEdge in the graph,
     * according to the edges' weight.
     * @param <NubisaveEdge> 
     */
    private final static class EdgeWeightStrokeFunction<NubisaveEdge> implements Transformer<NubisaveEdge, Stroke> {
        public Stroke transform(NubisaveEdge e) {
            int weight = 1;
            if (e instanceof WeightedNubisaveVertexEdge) {
                weight = ((WeightedNubisaveVertexEdge) e).getWeight();
            }
            return new BasicStroke(1 + weight);
        }
    }

    protected PluggableGraphMouse createPluggableGraphMouse(RenderContext<NubiSaveVertex, NubiSaveEdge> rc,
            Factory<? extends NubiSaveEdge> edgeFactory, DataVertexEdgeFactory dataVertexEdgeFactory) {
        PluggableGraphMouse graphMouse = new PluggableGraphMouse();
        TranslatingGraphMousePlugin translatingPlugin = new TranslatingGraphMousePlugin(InputEvent.BUTTON1_MASK);
        ScalingGraphMousePlugin scalingPlugin = new ScalingGraphMousePlugin(new ViewScalingControl(), 0, 1.1f, 1 / 1.1f);
        PopupEditor popupEditingPlugin = new PopupEditor(edgeFactory);
        StorageServiceSelector<NubiSaveVertex, NubiSaveEdge> storageServiceSelector;
        storageServiceSelector = new StorageServiceSelector<NubiSaveVertex, NubiSaveEdge>();
        magneticMousePointer = new MagneticMousePointer();

        ExtensibleNubisaveComponentMousePlugin extendablePlugin = new ExtensibleNubisaveComponentMousePlugin(null, edgeFactory);
        extendablePlugin.addEventListener(new ToggleActivateNubisaveComponentOnDoubleClick(graphDisplay, graph));
        extendablePlugin.addEventListener(new PreventEdgeCreationForRestrictedPorts(graphDisplay, graph));
        extendablePlugin.addEventListener(new DataVertexEdgeCreator(graphDisplay, graph, dataVertexEdgeFactory));
        extendablePlugin.addEventListener(new AbstractNubisaveComponentEdgeCreator(graphDisplay, graph, (WeightedNubisaveVertexEdgeFactory) edgeFactory));

        graphMouse.add(scalingPlugin);
        graphMouse.add(extendablePlugin);
        graphMouse.add(storageServiceSelector);
        graphMouse.add(translatingPlugin);
        graphMouse.add(popupEditingPlugin);
        graphMouse.add(magneticMousePointer);
        return graphMouse;
    }

    /**
     * Tries to read the file in the storage directory called connections.txt,
     * to reconstruct the edges between all components.
     * The file consists of lines, whereas each line contains the unique names
     * of two connected components, which are separated by a space character.
     * @param nubisaveComponentGraph
     * @param edgeFactory 
     */
    protected void interconnectNubisaveComponents(Graph<AbstractNubisaveComponent, Object> nubisaveComponentGraph, Factory<? extends NubiSaveEdge> edgeFactory) {
        WeightedNubisaveVertexEdge edge;
        File file = new File(storage_directory + "/" + "connections.txt");
        HashMap startToEndVertexMap = new HashMap();
        ArrayList<AbstractNubisaveComponent> myNodeList = new ArrayList<AbstractNubisaveComponent>(nubisaveComponentGraph.getVertices());
        ArrayList str = new ArrayList();
        for (int i = 0; i < nubisaveComponentGraph.getVertices().size(); i++) {
            str.add(myNodeList.get(i).getUniqueName());
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String strLine;
            try {
                // Read the text file Line By Line
                while ((strLine = reader.readLine()) != null) {
                    String startVertex = strLine.split(" ")[0];
                    String endVertex = strLine.split(" ")[1];
                    startToEndVertexMap.put(startVertex, endVertex);
                    for (AbstractNubisaveComponent component : nubisaveComponentGraph.getVertices()) {
                        if (component.getUniqueName() != null && component.getUniqueName().equals(startVertex)) {
                            String endpoint = (String) startToEndVertexMap.get(startVertex);
                            int pos = str.indexOf(endpoint);
                            if (pos >= 0) {
                                AbstractNubisaveComponent endcomponent = myNodeList.get(pos);
                                edge = (WeightedNubisaveVertexEdge) edgeFactory.create();
                                edge.setWeight(component.getNrOfFilePartsToStore());
                                graph.addEdge(edge, component.getRequiredPorts().iterator().next(), endcomponent.getProvidedPorts().iterator().next(),
                                        EdgeType.DIRECTED);
                            }
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
