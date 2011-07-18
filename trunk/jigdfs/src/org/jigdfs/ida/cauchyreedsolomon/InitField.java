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
 * InitField.java 
 *
 * @author   Hakim Weatherspoon
 * @version  $Id: InitField.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
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
 * InitField initializes the finite Field Element to Exponent (FEtoExp) table
 * and the Exponent to finite Field Element (ExptoFE) table.
 * 
 * SMultField is the size of the multiplicative field (2^{Lfield}-1) ==
 * TableLength - 1.
 * 
 * pCOLBIT is a pointer to the COLBIT, which is used to make sure rows and
 * columns have distinct field elements associated with them. The BIT array is
 * used to mask out single bits in equations: bit ExptoFE is the table that goes
 * from the exponent to the finite field element. FEtoExp == FEtoExp is the
 * table that goes from the finite field element to the exponent. Lfield is the
 * log of the length of the field.
 * 
 * @author Hakim Weatherspoon
 * @version $Id: InitField.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 */
public class InitField
{
   public static int MAXLFIELD = 16;

   public static int COLBIT[][];

   public static int BIT[][];

   public static int ExptoFE[][];

   public static int FEtoExp[][];

   static
   {
      generateTables(MAXLFIELD);
   }

   /**
    * Retrieve the COLBIT table for the input Lfield.
    * 
    * @param Lfield ==
    *           the Lfield to retrieve a COLBIT table for.
    */
   public static int[] getCOLBIT(int Lfield)
   {
      return COLBIT[Lfield];
   }

   /**
    * Retreve the BIT table for the input Lfield.
    * 
    * @param Lfield ==
    *           the Lfield to retrieve a BIT table for.
    */
   public static int[] getBIT(int Lfield)
   {
      return BIT[Lfield];
   }

   /**
    * Retreve the ExptoFE table for the input Lfield.
    * 
    * @param Lfield ==
    *           the Lfield to retrieve a ExptoFE table for.
    */
   public static int[] getExptoFE(int Lfield)
   {
      return ExptoFE[Lfield];
   }

   /**
    * Retreve the FEtoExp table for the input Lfield.
    * 
    * @param Lfield ==
    *           the Lfield to retrieve a FEtoExp table for.
    */
   public static int[] getFEtoExp(int Lfield)
   {
      return FEtoExp[Lfield];
   }

   /**
    * initField initializes the finite Field Element to Exponent (FEtoExp) table
    * and the Exponent to finite Field Element (ExptoFE) table.
    * 
    * Recall SMultField = TableLength - 1 is the number of elements in the
    * multiplicative group of the field.
    * 
    * @param pCOLBIT ==
    *           is a pointer to the COLBIT, which is used to make sure rows and
    *           columns have distinct field elements associated with them.
    * @param BIT ==
    *           The BIT array is used to mask out single bits in equations: bit
    * @param ExptoFE ==
    *           ExptoFE is the table that goes from the exponent to the finite
    *           field element.
    * @param FEtoExp ==
    *           FEtoExp is the table that goes from the finite field element to
    *           the exponent.
    * @param Lfield ==
    *           Lfield is the log of the length of the field..
    */
   public static void initField(
         int[] pCOLBIT,
         int[] BIT,
         int[] ExptoFE,
         int[] FEtoExp,
         int Lfield)
   {
      /**
       * Recall SMultField = TableLength - 1 is the number of elements in the
       * multiplicative group of the field.
       */
      int SMultField = (1 << Lfield) - 1;

      /**
       * CARRYMASK is used to see when there is a carry in the polynomial and
       * when it should be XOR'd with POLYMASK.
       */
      int CARRYMASK;

      /**
       * POLYMASK is the irreducible polynomial.
       */
      int POLYMASK[] = {
            0x0, 0x3, 0x7, 0xB, 0x13, 0x25, 0x43, 0x83, 0x11D, 0x211, 0x409,
            0x805, 0x1053, 0x201B, 0x402B, 0x8003, 0x1100B
      };
      int i;

      BIT[0] = 0x1;

      for (i = 1; i < Lfield; i++)
         BIT[i] = BIT[i - 1] << 1;

      pCOLBIT[0] = BIT[Lfield - 1];
      CARRYMASK = pCOLBIT[0] << 1;
      ExptoFE[0] = 0x1;

      for (i = 1; i < SMultField + Lfield - 1; i++)
      {
         ExptoFE[i] = ExptoFE[i - 1] << 1;
         if ((ExptoFE[i] & CARRYMASK) > 0)
            ExptoFE[i] ^= POLYMASK[Lfield];
      }

      FEtoExp[0] = -1;
      for (i = 0; i < SMultField; i++)
         FEtoExp[ExptoFE[i]] = i;
   }

   /**
    * This routine will generate tables for the input number of Lfields and
    * output them to stdout.
    * 
    * @param maxLfield ==
    *           maximum Lfield to produce fields for
    */
   public static void generateTables(int maxLfield)
   {
      /*
       * TableLength = 2^Lfield COLBIT = new int[2] BIT = new int[16] ExptoFE =
       * new int[TableLength + Lfield] FEtoExp = new int[TableLength]
       */

      COLBIT = new int[maxLfield + 1][];
      BIT = new int[maxLfield + 1][];
      ExptoFE = new int[maxLfield + 1][];
      FEtoExp = new int[maxLfield + 1][];

      for (int Lfield = 1; Lfield <= maxLfield; ++Lfield)
      {
         int TableLength = 1 << Lfield;
         COLBIT[Lfield] = new int[1];
         BIT[Lfield] = new int[16];
         ExptoFE[Lfield] = new int[TableLength + Lfield];
         FEtoExp[Lfield] = new int[TableLength];
         initField(COLBIT[Lfield], BIT[Lfield], ExptoFE[Lfield],
               FEtoExp[Lfield], Lfield);
      }
   }

}
