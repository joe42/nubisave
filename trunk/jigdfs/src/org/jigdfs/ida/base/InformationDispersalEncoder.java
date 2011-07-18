
package org.jigdfs.ida.base;

import java.util.List;

import org.jigdfs.ida.exception.IDAEncodeException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;


public interface InformationDispersalEncoder
{
   /**
    * Prepares the encoder to begin processing data.
    */
   public void initialize() throws IDAInvalidParametersException;

   /**
    * Performs a complete encoding operation or finishes a multiple-part
    * encoding operation.
    * 
    * @param buffer
    *           The data to encoded
    * 
    * @return A list of encoded data buffers
    */
   public List<byte[]> process(byte buffer[])
         throws IDAEncodeException, IDANotInitializedException;

   /**
    * Returns the number of slices
    * 
    * @return The number of slices
    */
   public int getNumSlices();

   /**
    * Returns the number of slices which are required to restore data.
    * 
    * @return The number of slices which are required to restore data
    */
   public int getThreshold();
   
   /**
    * Returns the chunk size
    * 
    * @return The chunk size
    */
   public int getChunkSize();
   
   public void setNumSlices(int numSlices);
   public void setThreshold(int threshold);
   public void setChunkSize(int chunkSize);
}
