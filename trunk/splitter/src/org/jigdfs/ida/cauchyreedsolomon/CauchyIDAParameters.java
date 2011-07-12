//
// Cleversafe open-source code header - Version 1.2 - February 15, 2008
//
// Cleversafe Dispersed Storage(TM) is software for secure, private and
// reliable storage of the world's data using information dispersal.
//
// Copyright (C) 2005-2008 Cleversafe, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
// USA.
//
// Contact Information: Cleversafe, 224 North Desplaines Street, Suite 500 
// Chicago IL 60661
// email licensing@cleversafe.org
//
// END-OF-HEADER

/**
 * Parameters.java
 *
 * @author   Hakim Weatherspoon
 * @version  $Id: Parameters.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 *
 * Copyright (c) 2001 Regents of the University of California.
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. Neither the name of the University nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE
 *  FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 *  OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *  LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 **/

package org.jigdfs.ida.cauchyreedsolomon;

/**
 * Parameters are the parameters used to encode/decode an object.
 * 
 * @author Hakim Weatherspoon
 * @version $Id: Parameters.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 */
public class CauchyIDAParameters
{
   /**
    * chunkSize is the size of data that is will be sent to the IDA
    * for each encoding.  Should include the block size plus any size change 
    * introduced by the datasource codecs.
    */
   protected int chunkSize;

   /**
    * logOfFieldLength is the log of the length of the field.
    */
   protected int logOfFieldLength = 8;

   /**
    * segmentsPerFragment is the number of segments in a slice. Length of slice in bytes
    * is segmentsPerFragment*logOfFieldLength.
    */
   protected int segmentsPerSlice = 43;

   /**
    * finiteFieldTableLength is 2^{logOfFieldLength}
    */
   protected int finiteFieldTableLength = 1 << logOfFieldLength;

   /**
    * SMultField is the size of the multiplicative field (2^{logOfFieldLength}-1) ==
    * finiteFieldTableLength - 1.
    */
   protected int multiplicationFieldSize = (1 << logOfFieldLength) - 1;

   /**
    * sliceLength is the fragment length in words excluding the overhead for storing
    * the index.
    */
   protected int sliceLength = segmentsPerSlice * logOfFieldLength;

   /**
    * totalSliceLEngth is the slice length in words including the overhead for storing
    * the index.
    */
   protected int totalSliceLength = sliceLength + 1;

   /**
    * numDataSlices is the number of data slices
    * 
    * IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
    * 
    * logOfFieldLength must be set large enough to make this true else the encoding and
    * decoding won't work
    */
   protected int numDataSlices;

   /**
    * numCodeSlices is the number of redundant fragments
    * 
    * IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
    * 
    * logOfFieldLength must be set large enough to make this true else the encoding and
    * decoding won't work.
    */
   protected int numCodeSlices;

   /**
    * numSlices is the total number of slices sent.
    */
   protected int numSlices;

   /**
    * messageLength is the length of the message in bytes.
    */
   protected int messageLength;

   /**
    * dispersedMessageLength is the post-dispersal length of the message in words.
    */
   protected int dispersedMessageLength;

   /**
    * CONSTRUCTOR
    * 
    * Initialize Parameters for a number of messages.
    * 
    * @param numDataSlices =
    *           numDataSlices is the number of message fragments.
    * @param numCodeSlices =
    *           numCodeSlices is the number of redundant fragments.
    */
   public CauchyIDAParameters(int numDataSlices, int numCodeSlices, int chunkSize)
   {
      this.chunkSize = chunkSize;
      
      this.numDataSlices = numDataSlices;
      this.numCodeSlices = numCodeSlices;
      
      calculateOptimumSliceSize();

      this.numSlices = this.numDataSlices + this.numCodeSlices;
      this.messageLength = this.sliceLength * this.numDataSlices;
      this.dispersedMessageLength = this.sliceLength * (this.numDataSlices + this.numCodeSlices);
   }
   
   /**
    * Calculates the optimum segmentsPerSlice and sliceLength values based on
    * the amount of data the IDA is given to process at a time and the number
    * of data slices.
    */
   private void calculateOptimumSliceSize()
   {
      // We add one to chunkSize to avoid cases where the block size
      // exactly equals the message length, resulting in an entire additional
      // block being created and filled with padding bytes. - JKR
      final int tempChunkSize = this.chunkSize + 1;
      
      int sliceSize = tempChunkSize / this.numDataSlices;
      
      if (tempChunkSize % this.numDataSlices != 0)
      {
         sliceSize++;
      }

      // Round up to the nearest multiple of log of field length
      if (sliceSize % this.logOfFieldLength > 0)
      {
         sliceSize += ((this.logOfFieldLength) - (sliceSize % this.logOfFieldLength));
      }
      

      this.segmentsPerSlice = (sliceSize / this.logOfFieldLength);      
      this.sliceLength = this.segmentsPerSlice * this.logOfFieldLength;
      this.totalSliceLength = this.sliceLength + 1;
   }

   /**
    * Returns the chunk size.
    */
   public int getChunkSize()
   {
      return chunkSize;
   }

   /**
    * Returns the log of the length of the field.
    */
   public int getLogOfFieldLength()
   {
      return logOfFieldLength;
   }

   /**
    * Returns the number of segments in a slice. Length of slice in bytes
    * is segmentsPerSlice*logOfFieldLength.
    */
   public int getSegmentsPerSlice()
   {
      return segmentsPerSlice;
   }

   /**
    * Returns 2^{logOfFieldLength}
    */
   public int getFiniteFieldTableLength()
   {
      return finiteFieldTableLength;
   }

   /**
    * Returns the size of the multiplicative field (2^{logOfFieldLength}-1) ==
    * finiteFieldTableLength - 1.
    */
   public int getMultiplicationFieldSize()
   {
      return multiplicationFieldSize;
   }

   /**
    * Returns the fragment length in bytes excluding the overhead for storing
    * the index.
    */
   public int getSliceLength()
   {
      return sliceLength;
   }

   /**
    * Returns the fragment length in bytes including the overhead for storing
    * the index.
    */
   public int getTotalSliceLength()
   {
      return totalSliceLength;
   }

   /**
    * Returns the number of data slices
    * 
    * @return return == return the number of data slices.
    * 
    * IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
    * 
    * logOfFieldLength must be set large enough to make this true else the encoding and
    * decoding won't work
    */
   public int getNumDataSlices()
   {
      return numDataSlices;
   }

   /**
    * Returns the number of code slices
    * 
    * @return return == return the number of message fragments.
    * 
    * IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
    * 
    * logOfFieldLength must be set large enough to make this true else the encoding and
    * decoding won't work.
    */
   public int getNumCodeSlices()
   {
      return numCodeSlices;
   }

   /**
    * Returns the total number of slices sent.
    */
   public int getNumSlices()
   {
      return numSlices;
   }

   /**
    * Returns the length of the message in bytes.
    */
   public int getMessageLength()
   {
      return messageLength;
   }

   /**
    * Returns the length of the encoding in bytes.
    */
   public int getDispersedMessageLength()
   {
      return dispersedMessageLength;
   }

   /**
    * logOfFieldLength (length of field) must be (1 <= logOfFieldLength <= 16) otherwise function
    * returns false and logOfFieldLength stays default.
    * 
    * @param logOfFieldLength ==
    *           number to set logOfFieldLength to, 1 <= logOfFieldLength <= 16.
    * @return return==logOfFieldLength must be (1 <= logOfFieldLength <= 16) otherwise function
    *         returns false and logOfFieldLength stays default
    */
   public boolean setLogOfFieldLength(int logOfFieldLength)
   {
      if (logOfFieldLength <= 8 && logOfFieldLength >= 1)
         this.logOfFieldLength = logOfFieldLength;
      else
         return false;

      resetParam();

      return true;
   }

   public void setChunkSize(int chunkSize)
   {
      this.chunkSize = chunkSize;
      
      resetParam();
   }

   public void resetParam()
   {
      this.finiteFieldTableLength = 1 << this.logOfFieldLength;
      this.multiplicationFieldSize = (1 << this.logOfFieldLength) - 1;
      calculateOptimumSliceSize();
      this.sliceLength = this.segmentsPerSlice * this.logOfFieldLength;
      this.totalSliceLength = this.sliceLength + 1;
   }
   
   public String toString() {
	   return "number of segments: " + this.segmentsPerSlice + "; "
	   	+ "slice size:" + this.getSliceLength() + "; "
	   	+ "message size:" + this.messageLength;
   }

   public static void main(String args[])
   {
      CauchyIDAParameters params = new CauchyIDAParameters(12, 4, 4096);
      System.out.println("number of segments: " + params.segmentsPerSlice);
      System.out.println("slice size:" + params.getSliceLength());
      System.out.println("message size:" + params.messageLength);
   }
}
