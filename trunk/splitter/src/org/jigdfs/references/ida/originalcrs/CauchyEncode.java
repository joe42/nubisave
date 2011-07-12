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

package org.jigdfs.references.ida.originalcrs;

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
   public static void encode(
         int[] fragments /* out */,
         int[] message /* in */,
         Parameters p /* in */)
   {
      int i, j, k, l, m, ind_seg, col_eqn, row_eqn, ind_eqn;
      int row, col, ExpFE;

      int[] COLBIT = InitField.getCOLBIT(p.Lfield());
      int[] BIT = InitField.getBIT(p.Lfield());
      int[] ExptoFE = InitField.getExptoFE(p.Lfield());
      int[] FEtoExp = InitField.getFEtoExp(p.Lfield());

      /**
       * Set the identifier in all the fragments to be sent
       */
      for (i = 0; i < p.Nfragments(); i++)
         fragments[i * p.Plentot()] = i;

      k = 0;
      j = 0;
      for (i = 0; i < p.Mfragments(); i++)
      {
         k++;
         for (ind_eqn = 0; ind_eqn < p.Lfield(); ind_eqn++)
         {
            for (ind_seg = 0; ind_seg < p.Nsegs(); ind_seg++)
            {
               fragments[k] = message[j];
               j++;
               k++;
            }
         }
      }

      /**
       * Fill in values for remaining Rfragments fragments
       */
      for (row = 0; row < p.Rfragments(); row++)
      {
         /**
          * Compute values of equations applied to message and fill into
          * fragment(row+Mfragments).
          * 
          * First, zero out contents relevant portions of fragment
          */
         j = (row + p.Mfragments()) * p.Plentot();
         for (i = 1; i < p.Plentot(); i++)
            fragments[j + i] = 0;

         /**
          * Second, fill in contents relevant portions of fragment
          */
         for (col = 0; col < p.Mfragments(); col++)
         {
            m = col * p.Lfield() * p.Nsegs();
            ExpFE = (p.SMultField() - FEtoExp[row ^ col ^ COLBIT[0]])
                  % p.SMultField();

            for (row_eqn = 0; row_eqn < p.Lfield(); row_eqn++)
            {
               k = row_eqn * p.Nsegs();
               for (col_eqn = 0; col_eqn < p.Lfield(); col_eqn++)
               {
                  if ((ExptoFE[ExpFE + row_eqn] & BIT[col_eqn]) > 0)
                  {
                     l = col_eqn * p.Nsegs() + m;
                     for (ind_seg = 0; ind_seg < p.Nsegs(); ind_seg++)
                     {
                        fragments[j + 1 + ind_seg + k] ^= message[ind_seg + l];
                     }
                  }
               }
            }
         }
      }

   }
}
