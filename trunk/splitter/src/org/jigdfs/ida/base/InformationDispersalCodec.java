package org.jigdfs.ida.base;

import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;

/**
 * IDA interface - combines an encoder and decoder
 */
public interface InformationDispersalCodec
{
   
   /**
    * 
    * @return a name of codec
    */
   String getName();
   
   /**
    * Sets the name of the codec
    * @param name
    */
   void setName(String name);
   
   
   /**
    * @return Number of slices produced by this codec
    */
   int getNumSlices();
       
   
   /**
    * @return Number of slices needed to restore data
    */
   int getThreshold();
   
   /**
    * @return The chunk size on which the IDA will operate
    */
   int getChunkSize();

   /**
    * Sets number of slices, needed for instantiating from configuration
    * @param numSlices
    */
   void setNumSlices(int numSlices);
   
   /**
    * Number of slices needed to restore data
    * @param treshold
    */
   void setThreshold(int threshold);
   
   /**
    * Chunk size for which the IDA will operate, typically the disk block size
    * plus overhead due to datasource codecs
    * @param inputSize
    */
   void setChunkSize(int inputSize);
   
   /**
    * 
    * @return
    */
   InformationDispersalEncoder getEncoder() throws IDANotInitializedException, IDAInvalidParametersException;

   /**
    * 
    * @return
    */
   InformationDispersalDecoder getDecoder() throws IDANotInitializedException, IDAInvalidParametersException;
   
   /**
    * Returns the blowup of this IDA.  For example, if encoded data is 30% larger than 
    * the original input, this would return 1.3.
    * @return Blowup of this IDA as a factor of the original data size (eg. 1.3)
    */
   float getBlowup();
   
   /**
    * Returns the post-dispersed upper-bound size in bytes for data with a given input size.
    * 
    * @param inputDataSize
    * @return
    */
   long getDispersedSize(long inputSize);


}


