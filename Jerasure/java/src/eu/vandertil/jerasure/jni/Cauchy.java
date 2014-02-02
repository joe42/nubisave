package eu.vandertil.jerasure.jni;

/**
 * This class provides access to the functions described in cauchy.h.
 *
 * See the documentation of Jerasure for a description of functionality and parameters.
 *
 * @author Jos van der Til
 * @version 1.0
 * @since 1.0
 */
public class Cauchy {

    public static native int[] cauchy_original_coding_matrix(int k, int m, int w);

    public static native int[] cauchy_xy_coding_matrix(int k, int m, int w, int[] x, int[] y);

    public static native void cauchy_improve_coding_matrix(int k, int m, int w, int[] matrix);

    public static native int[] cauchy_good_general_coding_matrix(int k, int m, int w);

    public static native int cauchy_n_ones(int n, int w);
}
