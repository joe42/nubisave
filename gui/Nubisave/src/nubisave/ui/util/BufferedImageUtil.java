/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nubisave.ui.util;

import java.awt.*;
import java.awt.image.*;;

/**
 *
 * @author joe
 */
public class BufferedImageUtil {
/**
     * Turn the {@link BufferedImage} instance color components from source to destination for highlighting.
     * Does not modify img.
     * @param img
     * @param source
     * @param dest
     * @return a copy of the highlighted img instance
     */
    public static BufferedImage highlight(BufferedImage img, Color source, Color dest) {
        img = deepCopy(img);
        short[] r = new short[256];
        short[] g = new short[256];
        short[] b = new short[256];
        short[] a = new short[256];
        for (int i = 0; i < 256; i++) {
            r[i] = (short) i;
            g[i] = (short) i;
            b[i] = (short) i;
            a[i] = (short) i;
        }
        r[source.getRed()] = (short) dest.getRed();
        g[source.getGreen()] = (short) dest.getGreen();
        b[source.getBlue()] = (short) dest.getBlue();
        short[][] blueInvert = new short[][]{r, g, b, a};
        BufferedImageOp blueInvertOp = new LookupOp(new ShortLookupTable(0, blueInvert), null);
        Graphics2D gr = img.createGraphics();
        gr.drawImage(img, 0, 0, null);
        gr.dispose();
        blueInvertOp.filter(img, img);
        return img;
    }

    public static BufferedImage deepCopy(BufferedImage bi) {
         ColorModel cm = bi.getColorModel();
         boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
         WritableRaster raster = bi.copyData(null);
         return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }
}
