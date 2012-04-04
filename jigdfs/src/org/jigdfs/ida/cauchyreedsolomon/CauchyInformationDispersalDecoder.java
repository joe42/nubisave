
package org.jigdfs.ida.cauchyreedsolomon;

import java.util.List;

import org.apache.log4j.Logger;
import org.jigdfs.ida.base.InformationDispersalCodecBase;
import org.jigdfs.ida.base.InformationDispersalDecoder;
import org.jigdfs.ida.exception.*;


public class CauchyInformationDispersalDecoder implements InformationDispersalDecoder
{
   /** Cauchy IDA parameters */
   private CauchyIDAParameters params = null;

   /** Standard Message Length */
   private int chunkSize = InformationDispersalCodecBase.DEFAULT_CHUNK_SIZE;
   
   /** The total number of slices */
   private int numSlices;

   /** The number of slices required to restore */
   private int threshold;

   private boolean initialized = false;

   /** Buffer for unencoded data */
   // private int data[];
   /** Pre-allocated output buffer */
   // private byte output[];
   // private byte buffer[];
   /** Buffer for encoded data */
   // private int fragments[];
   private static Logger logger = Logger.getLogger(CauchyInformationDispersalDecoder.class);

   public CauchyInformationDispersalDecoder()
   {
   }

   public CauchyInformationDispersalDecoder(int numSlices, int threshold, int chunkSize) throws IDAInvalidParametersException
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
      //   throw new IDAInvalidParametersException("Decoder may only be initialized once");
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

      this.params = new CauchyIDAParameters(numSlices - threshold, threshold, chunkSize);

      if (logger.isTraceEnabled())
      {
         logger.trace(toString());
      }
      
      this.initialized = true;
   }

   public byte[] process(List<byte[]> encodedBuffers) throws IDADecodeException,
         IDANotInitializedException
   {
      if (!this.initialized)
      {
         throw new IDANotInitializedException("IDA is not initialized, Call initialize() first");
      }

      int fragmentSize = getFragmentSize();

      byte data[];
      byte fragments[] = new byte[encodedBuffers.size() * fragmentSize];
      byte output[] = new byte[getMessageSize()];

      // Establish slice length
      int dataLength = -1;
      for (int i = 0; i < encodedBuffers.size(); i++)
      {
         if (encodedBuffers.get(i) != null)
         {
            if (dataLength == -1)
            {
               dataLength = encodedBuffers.get(i).length;
            }
            else
            {
               assert dataLength == encodedBuffers.get(i).length : "Inconsistent slice length: "
                     + encodedBuffers.get(i).length + " expected " + dataLength;
            }
         }
      }
      assert dataLength != -1 : "Data length can't be calculated";
  
      int outputBufferSize = dataLength / fragmentSize * getMessageSize();
      if (outputBufferSize != output.length)
      {
         output = new byte[outputBufferSize];
      }


      int outputPosition = 0;

      int encodedBufferPosition = 0;
      do
      {
         // Copy encoded data into fragments array
         int fragmentIdx = 0;
         for (int i = 0; i < encodedBuffers.size() && (fragmentIdx < encodedBuffers.size()); i++)
         {
            // Skip null buffers
            if (encodedBuffers.get(i) != null)
            {
               byte encodedBuffer[] = encodedBuffers.get(i);
               int fragmentOffset = fragmentIdx * fragmentSize;
               System.arraycopy(encodedBuffer, encodedBufferPosition, fragments, fragmentOffset,
            		   fragmentSize);
               fragmentIdx++;
            }
         }

         if (fragmentIdx < numSlices - threshold)
         {
            throw new IDAInvalidSliceCountException("Expected " + (numSlices - threshold) + " but got only "
                  + fragmentIdx + " slices");
         }
         try
         {//treshold=redundant fragments < data slices?
        	 data = CauchyDecode.decode(fragments, fragmentIdx, params);
         }
         catch (Exception e)
         {
        	 e.printStackTrace();
            throw new IDAInvalidSliceFormatException("Decode error");
         }

         System.arraycopy(data, 0, output, outputPosition, data.length);
         outputPosition += data.length;

         encodedBufferPosition += fragmentSize;
      } while (encodedBufferPosition < dataLength);

      // Truncate padding
      int outputSize = output.length;
      while (outputSize > 0 && output[outputSize - 1] == 0)
      {
         outputSize--;
      }

      outputSize--;

      byte buffer[] = new byte[outputSize];
      System.arraycopy(output, 0, buffer, 0, outputSize);

      return buffer;
   }


   private int getMessageSize()
   {
      return params.getSliceLength() * params.getNumDataSlices();
   }

   private int getFragmentSize()
   {
      return params.getTotalSliceLength();
   }

   public int getNumSlices()
   {
      return numSlices;
   }

   public int getThreshold()
   {
      return threshold;
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
   
   public void setChunkSize(int chunkSize)
   {
      this.chunkSize = chunkSize;
   }

   @Override
   public String toString()
   {
      StringBuffer stringBuff = new StringBuffer("");
      
      stringBuff.append("Slice count = " + numSlices + ", ");
      stringBuff.append("threshold = " + threshold + ", ");
      stringBuff.append("Message size: " + this.getMessageSize() + ", ");
      stringBuff.append("Fragment size: " + this.getFragmentSize() + ", ");

      stringBuff.append("Blowup = "
            + ((float) getFragmentSize() * (float) numSlices / (float) getMessageSize()) + ", ");

      stringBuff.append("Ideal = " + ((float) numSlices / (float) threshold));

      return stringBuff.toString();
   }
   
   
}

