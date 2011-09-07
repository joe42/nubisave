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
 * CauchyEncode.java
 *
 * @author   Hakim Weatherspoon
 * @version  $Id: CauchyEncode.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
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
 * CauchyEncode erasure encodes an object using cauchy reed solomon method.
 * 
 * @author Hakim Weatherspoon
 * @version $Id: CauchyEncode.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 */
public class CauchyEncode
{
   /**
    * encode uses the cauchy erasure encoding method to encode a message into
    * fragments.
    */

   /**
    * @param COLBIT == COLBIT is the bit that is used to make sure rows and
    * columns have distinct field elements associated with them.
    * @param BIT == The BIT array is used to mask out single bits in equations: bit
    * @param ExptoFE == ExptoFE is the table that goes from the exponent to the finite
    * field element.
    * @param FEtoExp == FEtoExp is the table that goes from the
    * finite field element to the exponent.
    * @param message == is the message in int array format.
    */
   public static byte[] encode(final byte[] message, final CauchyIDAParameters p)
   {
      //////////////////////////////////////////////////////////////////////////
      // Local variable declaration
      
      // Array to be filled with data and code slices
      byte slices[] = new byte[p.getTotalSliceLength() * p.getNumSlices()];

      // Finite Field Parameters
      final int[] COLBIT = InitField.getCOLBIT(p.getLogOfFieldLength());
      final int[] equationBitMask = InitField.getBIT(p.getLogOfFieldLength());
      final int[] exponentToFiniteFieldElement = InitField.getExptoFE(p.getLogOfFieldLength());
      final int[] finiteFieldElementToExponent = InitField.getFEtoExp(p.getLogOfFieldLength());

      // Constants taken from parameters
      final int numSegments = p.getSegmentsPerSlice();
      final int logOfField = p.getLogOfFieldLength();
      final int numDataSlices = p.getNumDataSlices();
      final int numCodeSlices = p.getNumCodeSlices();
      final int multFieldSize = p.getMultiplicationFieldSize();
      
      // For Loop Iterators
      int itr, row, col, rowEquation, columnEquation;
      
      // Miscellaneous variables used in code slice calculation
      int sliceArrayPosition, messageArrayPosition;
      int ExpFE, ExpFEplusRow;
      int arrayPositionDifference, sliceArrayPositionPlusNumSegments;
      int sliceArrayIterator;
      
      //////////////////////////////////////////////////////////////////////////

      // Set the slice index in the first byte of every slices      
      for (itr = 0; itr < p.getNumSlices(); itr++)
      {
         slices[itr * p.getTotalSliceLength()] = (byte) itr;
      }

      // Copy data slices from the message into position
      int sliceOffset = 0;
      for (itr = 0; itr < numDataSlices; itr++)
      {
         System.arraycopy(message, sliceOffset, slices, sliceOffset + itr + 1, p.getSliceLength());
         sliceOffset += p.getSliceLength();
      }

      /*
       * Pseudo code for Cauchy Reed-Solomon Encoding
       *
       *  for (codeSlice in CodeSlices) // 4
       *  {
       *     for (dataSlice in DataSlices) // 12
       *     {
       *        for (equationRow in FieldLength) // 8
       *        {
       *           for (equationCol in FieldLength) // 8
       *           {
       *              if (exponent[equationRow + exponent] & bit[equationCol] > 0)
       *              {
       *                 for (i = 0 to segments) // 43 (MessageLength / DataSlices / 8)
       *                 {
       *                    codeSlice[i + equationRow * segments] ^= dataSlice[i + equationCol * segments];
       *                 }
       *              }
       *           }
       *        }
       *     }
       *  }
       *
       */
      
      // Calculate code slices and put them into the correct position
      for (row = 0; row < numCodeSlices; row++)
      {
         /**
          * Compute values of equations applied to message and fill into
          * fragment(row+DataSlices).
          */
         final int rowOffset = ((row + numDataSlices) * p.getTotalSliceLength()) + 1;   
         
         /**
          * Second, fill in contents relevant portions of fragment
          */
         for (col = 0; col < numDataSlices; col++)
         {
            messageArrayPosition = col * p.getSliceLength();
            
            ExpFE = (multFieldSize - finiteFieldElementToExponent[row ^ col ^ COLBIT[0]])
                  % multFieldSize;

            for (rowEquation = 0; rowEquation < logOfField; rowEquation++)
            {
               ExpFEplusRow = ExpFE + rowEquation;
               sliceArrayPosition = rowOffset + (rowEquation * numSegments);
               
               for (columnEquation = 0; columnEquation < logOfField; columnEquation++)
               {
                  if ((exponentToFiniteFieldElement[ExpFEplusRow] & equationBitMask[columnEquation]) > 0)
                  {  
                     /*
                      * Warning: the following code is heavily optimized and difficult to read
                      * The following loop is the result of much optimization
                      * and is an attempt to use as few instructions as possible
                      * within this very deeply nested loop.
                      */
                     arrayPositionDifference = ((columnEquation * numSegments + messageArrayPosition) - sliceArrayPosition);
                     sliceArrayPositionPlusNumSegments = sliceArrayPosition + numSegments;
                     sliceArrayIterator = sliceArrayPosition;
                     
                     while (sliceArrayIterator < sliceArrayPositionPlusNumSegments)
                     {
                        slices[sliceArrayIterator] ^= message[sliceArrayIterator + arrayPositionDifference];
                        sliceArrayIterator++;
                     }
                     
                  }
               }
            }
         }
      }
      
      return slices;
   }
   
}
