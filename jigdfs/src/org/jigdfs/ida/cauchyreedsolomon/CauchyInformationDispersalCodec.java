
package org.jigdfs.ida.cauchyreedsolomon;

import org.jigdfs.ida.base.InformationDispersalCodec;
import org.jigdfs.ida.base.InformationDispersalCodecBase;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.exception.*;



/**
 * Cauchy Reed-Solomon IDA implementation, encoder should be a singlton
 */
public class CauchyInformationDispersalCodec extends InformationDispersalCodecBase
      implements
         InformationDispersalCodec
{
  
   /*
    * this should never be called, this should be created with params...
    */
   protected CauchyInformationDispersalCodec()
   {
	   super();	   
   }
   
   protected InformationDispersalEncoder getNewEncoder() throws IDAInvalidParametersException
   {	   
      CauchyInformationDispersalEncoder encoder = new CauchyInformationDispersalEncoder(this.numSlices, this.threshold, this.chunkSize);
      return encoder;
   }
   
   protected InformationDispersalDecoder getNewDecoder() throws IDAInvalidParametersException
   {
      CauchyInformationDispersalDecoder decoder = new CauchyInformationDispersalDecoder(this.numSlices, this.threshold, this.chunkSize);
      return decoder;
   }

  
   
   /**
    * Construct a new Cauchy Reed-Solomon IDA
    * 
    * @param numSlices
    *           Number of slices to produce
    * @param threshold
    *           Number of recoverable slice losses
    * @param chunkSize
    *           The size of data that the IDA will process at a time
    */
   public CauchyInformationDispersalCodec(int numSlices, int threshold, int chunkSize) throws
         IDAInvalidParametersException
   {
      super(numSlices, threshold, chunkSize);
      this.setName("optimizedcauchy");

      if (this.numSlices < 1) 
      {
         throw new IDAInvalidParametersException("Number of slices must be positive");
      }
      if (this.threshold <= 0)
      {
         throw new IDAInvalidParametersException("Threshold must be greater than zero");
      }
      if (this.chunkSize < 1) 
      {
         throw new IDAInvalidParametersException("Chunk size must be positive");
      }
      if (this.threshold > this.numSlices) 
      {
         throw new IDAInvalidParametersException("Threshold must be less than or equal to number of slices");
      }
      
      initialize();
   }

   
   public long getDispersedSize(long inputSize)
   {
      CauchyIDAParameters parameters = new CauchyIDAParameters(this.threshold, this.numSlices - this.threshold, this.chunkSize);
      
      int sliceLength = parameters.getSliceLength();
      
      int messageLength = sliceLength * this.threshold;
      
      int encodedLength = parameters.getTotalSliceLength() * parameters.getNumSlices();
      
      return ((inputSize / messageLength) + 1) * encodedLength;
   }



}
