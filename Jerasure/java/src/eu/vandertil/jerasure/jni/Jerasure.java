package eu.vandertil.jerasure.jni;

/**
 * This class provides access to the functions described in jerasure.h.
 *
 * See the documentation of Jerasure for a description of functionality and parameters.
 *
 * Not all functions from jerasure.h are supported.
 *
 * @author Jos van der Til
 * @version 1.0
 * @since 1.0
 */
public class Jerasure {
    /* ---------------------------------------------------------------  */
/* Bitmatrices / schedules ---------------------------------------- */

    /**
     * This function turns a \f$m \times k\f$ matrix in \f$GF(2^w)\f$ into a \f$wm \times wk\f$ bitmatrix (in \f$GF(2)\f$).
     * For a detailed explanation see: J. Blomer, M. Kalfane, M. Karpinski, R. Karp, M. Luby and D. Zuckerman:
     * An XOR-based erasure-resilient coding scheme. Technical Report TR-95-048, International Computer Science
     * Institute, August 1995
     *
     * @param k      Number of data devices
     * @param m      Number of coding devices
     * @param w      Word size
     * @param matrix Array of k*m integers. It represents an m by k matrix. Element i,j is in matrix[i*k+j]
     *
     * @return bit-matrix (int*)
     *
     * @todo example code
     */
    public static native int[] jerasure_matrix_to_bitmatrix(int k, int m, int w, int[] matrix);

/* ------------------------------------------------------------ */
/* Encoding - these are all straightforward.  jerasure_matrix_encode only
   works with w = 8|16|32.  */

    /**
     * This function calculates the parity of size bytes of data from each of k regions of memory accessed by data_ptrs. It put the result into the size pointed to by parity_ptr.
     *
     * @param k         Number of data devices
     * @param data_ptrs Array of k pointers to data which is size bytes. Size must be a multiple of sizeof(long). Pointers must also be longword aligned.
     * @param size      Size of memory allocated by data_ptrs in bytes.
     *
     * @todo fix
     * @todo example code
     */
    public static native void jerasure_do_parity(int k, byte[][] data_ptrs, byte[] parity_ptr, int size);

    /**
     * This function encodes a matrix in \f$GF(2^w)\f$. \f$w\f$ must be either 8, 16 or 32.
     *
     * @param k           Number of data devices
     * @param m           Number of coding devices
     * @param w           Word size
     * @param matrix      Array of k*m integers. It represents an m by k matrix. Element i,j is in matrix[i*k+j]
     * @param data_ptrs   Array of k pointers to data which is size bytes. Size must be a multiple of sizeof(long). Pointers must also be longword aligned.
     * @param coding_ptrs Array of m pointers to coding data which is size bytes
     * @param size        Size of memory allocated by coding_ptrs in bytes.
     *
     */
    public static native void jerasure_matrix_encode(int k, int m, int w, int[] matrix,
                                                     byte[][] data_ptrs, byte[][] coding_ptrs, int size);

    /**
     * This function encodes a matrix with a bit-matrix in \f$GF(2^w)\f$. \f$w\f$ my be any number between 1 and 32.
     *
     * @param k           Number of data devices
     * @param m           Number of coding devices
     * @param w           Word size
     * @param bitmatrix   Array of k*m*w*w integers. It represents an mw by kw matrix. Element i,j is in matrix[i*k*w+j]
     * @param data_ptrs   Array of k pointers to data which is size bytes. Size must be a multiple of sizeof(long). Pointers must also be longword aligned.
     * @param coding_ptrs Array of m pointers to coding data which is size bytes
     * @param size        Size of memory allocated by data_ptrs in bytes.
     * @param packetsize  The size of a coding block with bitmatrix coding. When you code with a bitmatrix, you will use w packets of size packetsize.
     *
     * @todo example code
     */
    public static native void jerasure_bitmatrix_encode(int k, int m, int w, int[] bitmatrix,
                                                        byte[][] data_ptrs, byte[][] coding_ptrs, int size, int packetsize);

/* ------------------------------------------------------------ */
/* Decoding. -------------------------------------------------- */

/* These return integers, because the matrix may not be invertible.

   The parameter row_k_ones should be set to 1 if row k of the matrix
   (or rows kw to (k+1)w+1) of th distribution matrix are all ones
   (or all identity matrices).  Then you can improve the performance
   of decoding when there is more than one failure, and the parity
   device didn't fail.  You do it by decoding all but one of the data
   devices, and then decoding the last data device from the data devices
   and the parity device.

 */

    /**
     * This function decodes unsing a matrix in \f$GF(2^w)\f$, only when \f$w\f$ = 8|16|32.
     *
     * @param k           Number of data devices
     * @param m           Number of coding devices
     * @param w           Word size
     * @param matrix      Array of k*m integers. It represents an m by k matrix. Element i,j is in matrix[i*k+j]
     * @param erasures    Array of id's of erased devices. Id's are integers between 0 and k+m-1. Id's 0 to k-1 are id's of data devices. Id's k to k+m-1 are id's of coding devices: Coding device id = id-k. If there are e erasures, erasures[e] = -1.
     * @param data_ptrs   Array of k pointers to data which is size bytes. Size must be a multiple of sizeof(long). Pointers must also be longword aligned.
     * @param coding_ptrs Array of m pointers to coding data which is size bytes
     * @param size        Size of memory allocated by data_ptrs/coding_ptrs in bytes.
     *
     * @return 0 if it worked, -1 if it failed
     *
     * @todo crossreferences
     * @todo example code
     */
    public static native boolean jerasure_matrix_decode(int k, int m, int w,
                                                    int[] matrix, boolean row_k_ones, int[] erasures,
                                                    byte[][] data_ptrs, byte[][] coding_ptrs, int size);

    /**
     * This function
     *
     * @param k           Number of data devices
     * @param m           Number of coding devices
     * @param w           Word size
     * @param bitmatrix   Array of k*m*w*w integers. It represents an mw by kw matrix. Element i,j is in matrix[i*k*w+j]
     * @param erasures    Array of id's of erased devices. Id's are integers between 0 and k+m-1. Id's 0 to k-1 are id's of data devices. Id's k to k+m-1 are id's of coding devices: Coding device id = id-k. If there are e erasures, erasures[e] = -1.
     * @param data_ptrs   Array of k pointers to data which is size bytes. Size must be a multiple of sizeof(long). Pointers must also be longword aligned.
     * @param coding_ptrs Array of m pointers to coding data which is size bytes
     * @param size        Size of memory allocated by coding_ptrs/data_ptrs in bytes.
     * @param packetsize  The size of a coding block with bitmatrix coding. When you code with a bitmatrix, you will use w packets of size packetsize.
     *
     * @todo return data
     * @todo formula
     * @todo fix
     * @todo example code
     * @todo description
     * @todo references
     */
    public static native boolean jerasure_bitmatrix_decode(int k, int m, int w,
                                                       int[] bitmatrix, boolean row_k_ones, int[] erasures,
                                                       byte[][] data_ptrs, byte[][] coding_ptrs, int size, int packetsize);

    /**
     * This function makes the k*k decoding matrix (or wk*wk bitmatrix) by taking the rows corresponding to k non-erased devices of the distribution matrix, and then inverting that matrix.
     * You should already have allocated the decoding matrix and dm_ids, which is a vector of k integers.
     * These will be filled in appropriately.  dm_ids[i] is the id of element i of the survivors vector.
     * I.e. row i of the decoding matrix times dm_ids equals data drive i.
     * Both of these routines take "erased" instead of "erasures".
     * Erased is a vector with k+m elements, which has 0 or 1 for each device's id, according to whether the device is erased.
     *
     * @param k Number of data devices
     * @param m Number of coding devices
     * @param w Word size
     *
     * @todo fix
     * @todo return data
     * @todo example code
     */
    public static native boolean jerasure_make_decoding_matrix(int k, int m, int w, int[] matrix, boolean[] erased,
                                                           int[] decoding_matrix, int[] dm_ids);

    /**
     * This function makes the k*k decoding matrix (or wk*wk bitmatrix) by taking the rows corresponding to k non-erased devices of the distribution matrix, and then inverting that matrix.
     * You should already have allocated the decoding matrix and dm_ids, which is a vector of k integers.
     * These will be filled in appropriately.
     * dm_ids[i] is the id of element i of the survivors vector.
     * I.e. row i of the decoding matrix times dm_ids equals data drive i.
     * Both of these routines take "erased" instead of "erasures".
     * Erased is a vector with k+m elements, which has 0 or 1 for each device's id, according to whether the device is erased.
     *
     * @param k      Number of data devices
     * @param m      Number of coding devices
     * @param w      Word size
     * @param matrix Array of k*m integers. It represents an m by k matrix. Element i,j is in matrix[i*k+j]
     *
     * @todo fix
     * @todo example code
     */
    public static native boolean jerasure_make_decoding_bitmatrix(int k, int m, int w, int[] matrix, boolean[] erased,
                                                              int[] decoding_matrix, int[] dm_ids);

    /**
     * This function allocates and returns erased from erasures.
     *
     * @param k        Number of data devices
     * @param m        Number of coding devices
     * @param erasures Array of id's of erased devices.
     *                 Id's are integers between 0 and k+m-1.
     *                 Id's 0 to k-1 are id's of data devices.
     *                 Id's k to k+m-1 are id's of coding devices: Coding device id = id-k.
     *                 If there are e erasures, erasures[e] = -1.
     *
     * @todo return data
     * @todo usage example
     */
    public static native boolean[] jerasure_erasures_to_erased(int k, int m, int[] erasures);
}
