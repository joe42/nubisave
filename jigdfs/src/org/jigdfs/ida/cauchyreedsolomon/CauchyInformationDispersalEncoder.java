
package org.jigdfs.ida.cauchyreedsolomon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.jigdfs.ida.base.InformationDispersalCodecBase;
import org.jigdfs.ida.base.InformationDispersalEncoder;
import org.jigdfs.ida.exception.IDAEncodeException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;



public class CauchyInformationDispersalEncoder
   implements InformationDispersalEncoder
{
   /** Cauchy IDA parameters */
   private CauchyIDAParameters params = null;
   
   /** Chunk size */
   private int chunkSize = InformationDispersalCodecBase.DEFAULT_CHUNK_SIZE;;
 
   private int messageSize;
   private int totalSliceLength;

   private boolean initialized = false;
  
   /** Buffers for unencoded data */
   // private int message[];

   /** Buffer for encoded data */
   // private int fragments[];

   // private List<byte[]> outputBuffers;
   
   /** The total number of slices */
   private int numSlices;
   
   /** The number of slices required to restore */
   private int threshold;

   private static Logger logger = Logger.getLogger(CauchyInformationDispersalEncoder.class);
   
   public CauchyInformationDispersalEncoder(int numSlices, int threshold, int chunkSize) throws IDAInvalidParametersException
   {
      this.numSlices = numSlices;
      this.threshold = threshold;
      this.chunkSize = chunkSize;
      
      initialize();
   }

   public synchronized void initialize() throws IDAInvalidParametersException
   {
      // Configuration already calls initialize, but we need to reinitialize with the
      // calculated block size after datasource codecs are applied.  This is a safety 
      // measure which should be added back in the future, but is being removed as a quick
      // fix -- JKR
      //if (this.initialized == true)
      //{
      //   throw new IDAInvalidParametersException("Encoder may only be initialized once");
      //}
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
      
      this.params = new CauchyIDAParameters(this.numSlices - this.threshold, this.threshold, this.chunkSize);
      this.messageSize = params.getSliceLength() * params.getNumDataSlices();
      this.totalSliceLength = params.getTotalSliceLength();
      
      if (logger.isTraceEnabled())
      {
         logger.trace(toString());
      }
      
      this.initialized = true;
   }



   public List<byte[]> process(byte buffer[])
      throws IDAEncodeException, IDANotInitializedException
   {
      if (!this.initialized)
      {
         throw new IDANotInitializedException(
               "IDA is not initialized, Call initialize() first");
      }
      
      byte message[] = new byte[getMessageSize()];

      int inputPosition = 0;
      int outputPosition = 0;

      // Calculate the size of each output buffer
      int outputSize = 
         ((buffer.length + 1) / getMessageSize()) * getTotalSliceLength();

      if ( (buffer.length + 1) % getMessageSize() != 0 )
      {
         outputSize += getTotalSliceLength();
      }

      // Allocate new buffers for output        
      List<byte[]> outputBuffers  = new ArrayList<byte[]>(this.getNumSlices());   

      // Allocate the output buffers
      for (int fragmentIdx = 0; fragmentIdx < getNumSlices(); fragmentIdx++)
      {
         outputBuffers.add(new byte[outputSize]);
      }
      

      while (outputPosition < outputSize)
      {
         final byte fillerByte = 1;
         
         // Copy data from the input buffer into the data buffer
         int freeBufferSpace = buffer.length - inputPosition;
         int maxWriteAmount = (message.length < freeBufferSpace ? message.length : freeBufferSpace);

         System.arraycopy(buffer, inputPosition, message, 0, maxWriteAmount);
         inputPosition += maxWriteAmount;
         
         // Add padding if needed
         if (maxWriteAmount < message.length)
         {
            message[maxWriteAmount] = fillerByte;
            Arrays.fill(message, maxWriteAmount+1, message.length, (byte) 0x00);
         }

         // Perform encoding of the data buffer into slices array
         byte[] slices;
         slices = CauchyEncode.encode(message, params);

         // For each fragment
         for (int fragmentIdx = 0; fragmentIdx < numSlices; fragmentIdx++)
         {
            byte fragment[] = outputBuffers.get(fragmentIdx);

            int fragmentOffset = fragmentIdx * totalSliceLength;
            
            System.arraycopy(slices, fragmentOffset, fragment, outputPosition, totalSliceLength);
         }

         outputPosition += totalSliceLength;
      }

      return outputBuffers;
   }

   public int getNumSlices()
   {
      return numSlices;
   }

   public int getThreshold()
   {
      return threshold;
   }
      
   private int getMessageSize()
   {
      return messageSize;
   }

   private int getTotalSliceLength()
   {
      return totalSliceLength;
   }
   
   public int getChunkSize()
   {
      return chunkSize;
   }
   
   public void setNumSlices(int numSlices)
   {
      this.numSlices = numSlices;
   }
   
   public void setThreshold(int threshold)
   {
      this.threshold = threshold;
   }
   
   public void setChunkSize(int inputMessageLength)
   {
      this.chunkSize = inputMessageLength;
   }
   
   @Override
   public String toString()
   {
      StringBuffer stringBuff = new StringBuffer("");
      
      stringBuff.append( "Slice count = " + numSlices + ", " );
      stringBuff.append( "threshold = " + threshold + ", " );
      stringBuff.append( "Message size: " + this.getMessageSize() + ", " );
      stringBuff.append( "Fragment size: " + this.getTotalSliceLength() + ", " );

      stringBuff.append( "Blowup = " + ( (float)getTotalSliceLength() *
         (float)numSlices / (float)getMessageSize()) + ", " );
      
      stringBuff.append( "Ideal = " + ((float) numSlices / (float) threshold) );
      
      return stringBuff.toString();
   }


}
