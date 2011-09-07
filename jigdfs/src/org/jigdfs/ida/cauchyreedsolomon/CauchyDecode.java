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
 * CauchyDecode.java
 *
 * @author   Hakim Weatherspoon
 * @version  $Id: CauchyDecode.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
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

import java.util.*;

/**
 * CauchyEncode decodes a msg that was previously encoded using Cauchy
 * Reed-Solomon method.
 * 
 * Move information from fragments into received message. Fill in parts of
 * received message that requires no processing and figure out how many of the
 * redundant fragments are needed. Nfirstrec is the number of fragments received
 * from among the first Mfragments that carry portions of the unprocessed
 * original message. Rec_index is an array that indicates which parts of the
 * message are received. The pattern is the same within all Nsegs segments,
 * 
 * @author Hakim Weatherspoon
 * @version $Id: CauchyDecode.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 */
public class CauchyDecode
{
   private static final int STACK_SIZE = 32;

   private static Map<Integer, FiniteStack> _decodeTable = new HashMap<Integer, FiniteStack>();

   /**
    * decode uses the cauchy erasure coding method to decode an array of
    * fragments back into a msg.
    */

   /*
    * @param COLBIT == COLBIT is the bit that is used to make sure rows and
    * columns have distinct field elements associated with them. @param BIT ==
    * The BIT array is used to mask out single bits in equations: bit @param
    * ExptoFE == ExptoFE is the table that goes from the exponent to the finite
    * field element. @param FEtoExp == FEtoExp is the table that goes from the
    * finite field element to the exponent. @param fragments == fragments are
    * the array of fragments as output. @param message == is the message in int
    * array format.
    */
   public static byte[] decode(
         final byte[] rec_fragments,
         final int Nrec,
         final CauchyIDAParameters p) throws Exception
   {
      byte[] rec_message = new byte[p.getMessageLength()];
      
      int i, j, k, l, m, index, seg_ind;
      int col_ind, row_ind, col_eqn, row_eqn;
      int Nfirstrec, Nextra;
      int ExpFE;

      int diff;
      
      final int numSegments = p.getSegmentsPerSlice();
      final int logOfField = p.getLogOfFieldLength();

      final int[] COLBIT = InitField.getCOLBIT(logOfField);
      final int[] BIT = InitField.getBIT(logOfField);
      final int[] ExptoFE = InitField.getExptoFE(logOfField);
      final int[] FEtoExp = InitField.getFEtoExp(logOfField);

      Integer key = new Integer(p.getNumDataSlices() + numSegments * p.getNumCodeSlices()
            * logOfField);
      FiniteStack stack = (FiniteStack) _decodeTable.get(key);

      if (stack == null)
      {
         synchronized (_decodeTable)
         {
            if ((stack = (FiniteStack) _decodeTable.get(key)) == null)
            {
               Object bucket = null;
               stack = new FiniteStack(STACK_SIZE);
               if ((bucket = _decodeTable.put(key, stack)) != null)
                  System.err.println("CauchyDecode.decode: bucket=" + bucket
                        + " for key=" + key + " stack=" + stack);
            }
         }
      }

      ArrayObj arrayObj = (ArrayObj) stack.pop();
      if (arrayObj == null)
         arrayObj = new ArrayObj(p.getNumDataSlices(), p.getNumCodeSlices(), numSegments, logOfField);

      int[] Rec_index = arrayObj.Rec_index;
      int[] Col_Ind = arrayObj.Col_Ind;
      int[] Row_Ind = arrayObj.Row_Ind;
      byte[] M = arrayObj.M;
      int[] C = arrayObj.C;
      int[] D = arrayObj.D;
      int[] E = arrayObj.E;
      int[] F = arrayObj.F;

      if (Nrec < p.getNumDataSlices())
      {
         throw new Exception("Decode error");
      }

      /**
       * Move information from fragments into received message. Fill in parts of
       * received message that requires no processing and figure out how many of
       * the redundant fragments are needed. Nfirstrec is the number of
       * fragments received from among the first Mfragments that carry portions
       * of the unprocessed original message. Rec_index is an array that
       * indicates which parts of the message are received. The pattern is the
       * same within all Nsegs segments,
       */

      Nfirstrec = 0;

      m = 0;
      for (i = 0; i < Nrec; i++)
      {
         index = rec_fragments[m];
         if (index < p.getNumDataSlices())
         {
            j = index * p.getSliceLength();
            Rec_index[index] = 1;

            diff = (m + 1) - j;
            
            System.arraycopy(rec_fragments, j + diff, rec_message, j, numSegments * logOfField);
            
            Nfirstrec++;
         }
         m += p.getTotalSliceLength();
      }

      /**
       * Nextra is the number of redundant fragments that need to be processed.
       */
      Nextra = p.getNumDataSlices() - Nfirstrec;

      /**
       * Compute the indices of the missing words in the message
       */
      col_ind = 0;
      for (i = 0; i < p.getNumDataSlices(); i++)
      {
         if (Rec_index[i] == 0)
            Col_Ind[col_ind++] = i;
      }

      /**
       * Keep track of indices of extra fragments in Row_Ind array and
       * initialize M array from the received extra fragments
       */
      row_ind = 0;
      m = 0;
      for (i = 0; i < Nrec; i++)
      {
         if (rec_fragments[m] >= p.getNumDataSlices())
         {
            k = numSegments * row_ind * logOfField;
            Row_Ind[row_ind] = rec_fragments[m] - p.getNumDataSlices();
            
            System.arraycopy(rec_fragments, m + 1, M, k, numSegments * logOfField);
            
            row_ind++;
            if (row_ind >= Nextra)
               break;
         }
         m += p.getTotalSliceLength();
      }

      /**
       * Adjust M array according to the equations and the contents of
       * rec_message.
       */
      for (row_ind = 0; row_ind < Nextra; row_ind++)
      {
         for (col_ind = 0; col_ind < p.getNumDataSlices(); col_ind++)
         {
            if (Rec_index[col_ind] == 1)
            {
               ExpFE = (p.getMultiplicationFieldSize() - FEtoExp[Row_Ind[row_ind] ^ col_ind
                     ^ COLBIT[0]])
                     % p.getMultiplicationFieldSize();
               for (row_eqn = 0; row_eqn < logOfField; row_eqn++)
               {
                  j = numSegments * (row_eqn + row_ind * logOfField);
                  for (col_eqn = 0; col_eqn < logOfField; col_eqn++)
                  {
                     k = numSegments * (col_eqn + col_ind * logOfField);
                     if ((ExptoFE[ExpFE + row_eqn] & BIT[col_eqn]) > 0)
                     {
                        
                        diff = (k - j);
                        int stop = j + numSegments;
                        
                        for (seg_ind = j; seg_ind < stop; seg_ind++)
                        {
                           M[seg_ind] ^= rec_message[seg_ind + diff];
                        }

                     }
                  }
               }
            }
         }
      }

      /**
       * Compute the determinant of the matrix in the finite field and then
       * compute the inverse matrix
       */
      for (row_ind = 0; row_ind < Nextra; row_ind++)
      {
         for (col_ind = 0; col_ind < Nextra; col_ind++)
         {
            if (col_ind != row_ind)
            {
               C[row_ind] += FEtoExp[Row_Ind[row_ind] ^ Row_Ind[col_ind]];
               D[col_ind] += FEtoExp[Col_Ind[row_ind] ^ Col_Ind[col_ind]];
            }
            E[row_ind] += FEtoExp[Row_Ind[row_ind] ^ Col_Ind[col_ind]
                  ^ COLBIT[0]];
            F[col_ind] += FEtoExp[Row_Ind[row_ind] ^ Col_Ind[col_ind]
                  ^ COLBIT[0]];
         }
      }

      /**
       * Fill in the recovered information in the message from the inverted
       * matrix and from M.
       */
      for (row_ind = 0; row_ind < Nextra; row_ind++)
      {
         for (col_ind = 0; col_ind < Nextra; col_ind++)
         {
            ExpFE = E[col_ind] + F[row_ind] - C[col_ind] - D[row_ind]
                  - FEtoExp[Row_Ind[col_ind] ^ Col_Ind[row_ind] ^ COLBIT[0]];
            if (ExpFE < 0)
               ExpFE = p.getMultiplicationFieldSize() - ((-ExpFE) % p.getMultiplicationFieldSize());
            ExpFE = ExpFE % p.getMultiplicationFieldSize();
            j = Col_Ind[row_ind] * logOfField * numSegments;
            for (row_eqn = 0; row_eqn < logOfField; row_eqn++)
            {
               k = row_eqn * numSegments + j;
               for (col_eqn = 0; col_eqn < logOfField; col_eqn++)
               {
                  l = numSegments * (col_eqn + col_ind * logOfField);
                  if ((ExptoFE[ExpFE + row_eqn] & BIT[col_eqn]) > 0)
                  {
                     diff = (l - k);
                     int stop = k + numSegments;
                     
                     for (seg_ind = k; seg_ind < stop; seg_ind++)
                     {
                        rec_message[seg_ind] ^= M[seg_ind + diff];
                     }
                     
                  }
               }
            }
         }
      }

      return rec_message;
   }

   private static class ArrayObj
   {
      public int[] Rec_index;

      public int[] Col_Ind;

      public int[] Row_Ind;

      public int[] C;

      public int[] D;

      public int[] E;

      public int[] F;

      public byte[] M;

      public ArrayObj(
            int numMsgFrags,
            int numRedundantFrags,
            int nSegs,
            int Lfield)
      {
         Rec_index = new int[numMsgFrags];
         Col_Ind = new int[numMsgFrags];
         Row_Ind = new int[numRedundantFrags];
         C = new int[numRedundantFrags];
         D = new int[numMsgFrags];
         E = new int[numMsgFrags];
         F = new int[numRedundantFrags];
         M = new byte[nSegs * numRedundantFrags * Lfield];
      }
   }
}
