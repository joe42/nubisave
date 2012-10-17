package nubisave.component.graph.tranformer;

import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;

import nubisave.component.graph.vertice.interfaces.BufferedImageDelegator;

/**
 * Transforms vertices implementing BufferedImageDelegator to Icon instances.
 */
public class BufferedImageDelegatorVertexIconTransformer<V> implements Transformer<V, Icon> {

	public Icon transform(V v) {
		if (v instanceof BufferedImageDelegator) {
			Icon icon = new ImageIcon(((BufferedImageDelegator) v).getBufferedImage());
			return icon;
		}
		throw new RuntimeException(
				"MyVertexShapeTransformer: vertex is no BufferedImageDelegator");
	}

}
