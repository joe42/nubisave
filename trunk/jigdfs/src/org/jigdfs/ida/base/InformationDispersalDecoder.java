package org.jigdfs.ida.base;

import java.util.List;

import org.jigdfs.ida.exception.IDADecodeException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;



public interface InformationDispersalDecoder
{

   /**
    * Prepares the decoder to begin processing encoded data.
    * 
    * @throws IDAInvalidParametersException
    */
   public void initialize() throws IDAInvalidParametersException;


   /**
    * Performs a complete decoding operation or finishes a multiple-part
    * decoding operation.
    * 
    * @param encodedBuffers A list of encoded buffers
    * 
    * @return Decoded data
    * @throws IDADecodeException 
    */
   public byte[] process(List<byte[]> encodedBuffers) 
      throws IDADecodeException, IDANotInitializedException;

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
