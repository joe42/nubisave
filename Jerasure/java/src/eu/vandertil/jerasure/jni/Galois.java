package eu.vandertil.jerasure.jni;

/**
 * This class provides access to the functions described in galois.h.
 *
 * See the documentation of Jerasure for a description of functionality and parameters.
 *
 * IMPORTANT:
 * THESE FUNCTIONS CAN AND WILL ALLOCATE TABLES FOR LOGARITHMS ETC. IN NATIVE MEMORY.
 * THIS IS NOT A MEMORY LEAK, HOWEVER I HAVE NO CONTROL OVER THIS.
 *
 * ALSO, SOME FUNCTIONS IN galois.c WILL CALL exit(1) ON FAILURE CAUSING A CRASH.
 * THESE ARE MOSTLY RELATED TO THE WORD SIZE, SO BE CAREFUL WITH THAT PARAMETER.
 * STICK TO 8/16/32 TO BE SAFE.
 *
 * YOU HAVE BEEN WARNED, PLEASE BE CAREFUL.
 *
 * @author Jos van der Til
 * @version 1.0
 * @since 1.0
 */
public class Galois {
    public static native int galois_single_multiply(int a, int b, int w);
    public static native int galois_single_divide(int a, int b, int w);
    public static native int galois_log(int value, int w);
    public static native int galois_ilog(int value, int w);

    public static native boolean galois_create_log_tables(int w);   /* Returns 0 on success, -1 on failure */
    public static native int galois_logtable_multiply(int x, int y, int w);
    public static native int galois_logtable_divide(int x, int y, int w);

    public static native boolean galois_create_mult_tables(int w);   /* Returns 0 on success, -1 on failure */
    public static native int galois_multtable_multiply(int x, int y, int w);
    public static native int galois_multtable_divide(int x, int y, int w);

    public static native int galois_shift_multiply(int x, int y, int w);
    public static native int galois_shift_divide(int x, int y, int w);

    public static native boolean galois_create_split_w8_tables(); /* Returns 0 on success, -1 on failure */
    public static native int galois_split_w8_multiply(int x, int y);

    public static native int galois_inverse(int x, int w);
    public static native int galois_shift_inverse(int y, int w);

    public static native void galois_region_xor(           byte []r1,         /* Region 1 */
                                      byte []r2,         /* Region 2 */
                                      byte []r3,         /* Sum region (r3 = r1 ^ r2) -- can be r1 or r2 */
                                      int nbytes);      /* Number of bytes in region */

/* These multiply regions in w=8, w=16 and w=32.  They are much faster
   than calling galois_single_multiply.  The regions must be long word aligned. */

    public static native void galois_w08_region_multiply(byte []region,       /* Region to multiply */
                                    int multby,       /* Number to multiply by */
                                    int nbytes,       /* Number of bytes in region */
                                    byte []r2,         /* If r2 != NULL, products go here.
                                                       Otherwise region is overwritten */
                                    boolean add);         /* If (r2 != NULL && add) the produce is XOR'd with r2 */

    public static native void galois_w16_region_multiply(byte[]region,       /* Region to multiply */
                                    int multby,       /* Number to multiply by */
                                    int nbytes,       /* Number of bytes in region */
                                    byte []r2,         /* If r2 != NULL, products go here.
                                                       Otherwise region is overwritten */
                                    boolean add);         /* If (r2 != NULL && add) the produce is XOR'd with r2 */

    public static native void galois_w32_region_multiply(byte []region,       /* Region to multiply */
                                    int multby,       /* Number to multiply by */
                                    int nbytes,       /* Number of bytes in region */
                                    byte []r2,         /* If r2 != NULL, products go here.
                                                       Otherwise region is overwritten */
                                    boolean add);         /* If (r2 != NULL && add) the produce is XOR'd with r2 */
}
