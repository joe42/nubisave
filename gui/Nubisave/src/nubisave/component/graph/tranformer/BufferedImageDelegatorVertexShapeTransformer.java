package nubisave.component.graph.tranformer;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;

import nubisave.component.graph.vertice.interfaces.BufferedImageDelegator;

import edu.uci.ics.jung.visualization.FourPassImageShaper;

/**
 * Get Shapes from vertices implementing BufferedImageDelegator.
 * Also applies a shaping function to images to extract the shape of the opaque
 * part of a transparent image.
 */
public class BufferedImageDelegatorVertexShapeTransformer<V> implements Transformer<V, Shape> {

	/**
	 * Get the shape from the image. If not available, get the shape from the
	 * delegate VertexShapeFunction.
	 */
	public Shape transform(V v) {
		if (v instanceof BufferedImageDelegator) {
			BufferedImage bufferedImage = ((BufferedImageDelegator)v).getBufferedImage();
			Shape shape = FourPassImageShaper.getShape(bufferedImage, 30);
			if (shape.getBounds().getWidth() > 0
					&& shape.getBounds().getHeight() > 0) {
				int width = bufferedImage.getWidth(null);
				int height = bufferedImage.getHeight(null);
				AffineTransform transform = AffineTransform
						.getTranslateInstance(-width / 2, -height / 2);
				shape = transform.createTransformedShape(shape);
			}
			return shape;
		} 
		throw new RuntimeException("MyVertexShapeTransformer: vertex is no ImageIconDelegater");
	}

}
