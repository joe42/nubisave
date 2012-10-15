package nubisave.component.graph.pickedvertexlistener;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.Color;
import java.awt.image.LookupOp;
import java.awt.image.PixelGrabber;
import java.awt.image.ShortLookupTable;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import nubisave.component.graph.vertice.interfaces.BufferedImageDelegator;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.picking.PickedState;
import nubisave.component.graph.DontHighlight;
import nubisave.component.graph.vertice.interfaces.NubiSaveVertex;
import nubisave.ui.util.BufferedImageUtil;

/**
 * Turn blue values of vertex from 255 to 1 if picked and vice versa if unpicked,
 * if vertex instanceof {@link BufferedImageDelegator} && ! (vertex instanceof {@link DontHighlight}).
 * @param <V>
 */
public class BufferedImageDelegatorHighlighter<V> implements ItemListener {
	protected PickedState<V> pickedVertexState;
	protected Set<V> previously_picked = new HashSet<V>();
        Color sourceColor = new Color(0, 0, 255);
        Color highlightedColor = new Color(0, 0, 1);
	/**
	 * Create a new instance and registers it with the {@link PickedState}
	 * @param pickedVertexState
	 */
	public BufferedImageDelegatorHighlighter(PickedState<V> pickedVertexState) {
            this.pickedVertexState = pickedVertexState;
            pickedVertexState.addItemListener(this);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
        BufferedImage img;
            if(e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED){

                for(V p: previously_picked){
                    if( p instanceof BufferedImageDelegator && ! (p instanceof DontHighlight)){
                        img = ((BufferedImageDelegator) p).getBufferedImage();
                        img = BufferedImageUtil.highlight(img, highlightedColor, sourceColor);
                        ((BufferedImageDelegator) p).setBufferedImage(img);
                    }
                }
                
                for(V p: pickedVertexState.getPicked()){
                    if( p instanceof BufferedImageDelegator && ! (p instanceof DontHighlight)){
                        img = ((BufferedImageDelegator) p).getBufferedImage();
                        img = BufferedImageUtil.highlight(img, sourceColor, highlightedColor);
                        ((BufferedImageDelegator) p).setBufferedImage(img);
                    }
                }

                previously_picked.clear();
                previously_picked.addAll(pickedVertexState.getPicked());
            }
	}
}
