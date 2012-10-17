package nubisave.component.graph.vertice.interfaces;

import java.awt.image.BufferedImage;


/**
 * Interface for accessing an ImageIcon representation of a component.
 * 
 * @author joe
 *
 */
public interface BufferedImageDelegator {
	
	/**
	 * @return a BufferedImage instance representing the current object
	 */
	public BufferedImage getBufferedImage();
	/**
	 * Set the visual representation of the component.
	 * @param bufferedImage
	 */
	public void setBufferedImage(BufferedImage bufferedImage);

}
