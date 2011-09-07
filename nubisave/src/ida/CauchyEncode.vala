//  
//  Copyright (C) 2011 Rico Tzschichholz
// 
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
// 
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
// 
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
// 

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

using Posix;
using Math;

namespace NubiSave.IDA
{
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
		public static uint8[] encode (uint8[] message, CauchyIDAParameters p)
		{
			//////////////////////////////////////////////////////////////////////////
			// Local variable declaration
			
			// Constants taken from parameters
			int numSegments = p.SegmentsPerSlice;
			int logOfField = p.LogOfFieldLength;
			int numDataSlices = p.NumDataSlices;
			int numCodeSlices = p.NumCodeSlices;
			int multFieldSize = p.MultiplicationFieldSize;
			int sliceLength = p.SliceLength;
			int totalSliceLength = p.TotalSliceLength;
			
			// Array to be filled with data and code slices
			var slices = new uint8[p.TotalSliceLength * p.NumSlices];
			
			Logger.debug<CauchyEncode> ("message.length = " + message.length.to_string () + " > slices.length = " + slices.length.to_string ());
			
			// Finite Field Parameters
			int64[] COLBIT = InitField.getCOLBIT (logOfField);
			int64[] BIT = InitField.getBIT (logOfField);
			int64[] ExptoFE = InitField.getExptoFE (logOfField);
			int64[] FEtoExp = InitField.getFEtoExp (logOfField);
			
			// For Loop Iterators
			int itr, row, col, rowEquation, columnEquation;
			
			// Miscellaneous variables used in code slice calculation
			int sliceArrayPosition, messageArrayPosition;
			int ExpFE, ExpFEplusRow;
			int arrayPositionDifference, sliceArrayPositionPlusNumSegments;
			int sliceArrayIterator;

			//////////////////////////////////////////////////////////////////////////

			// Set the slice index in the first uint8 of every slices  
			for (itr = 0; itr < p.NumSlices; itr++)
				slices[itr * totalSliceLength] = (uint8) itr;
			
			// Copy data slices from the message into position
			int sliceOffset = 0;
			for (itr = 0; itr < numDataSlices; itr++) {
			
				//FIXME
				//System.arraycopy(message, sliceOffset, slices, sliceOffset + itr + 1, p.getSliceLength());
				Memory.copy ((uint8*)slices + sliceOffset + itr + 1, (uint8*)message + sliceOffset, sliceLength);
				
				sliceOffset += sliceLength;
			}

			//foreach (uint8 u1 in message)
			//	Logger.debug<CauchyEncode> (u1.to_string ());

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
			for (row = 0; row < numCodeSlices; row++) {
				/**
				* Compute values of equations applied to message and fill into
				* fragment(row+DataSlices).
				*/
				int rowOffset = ((row + numDataSlices) * totalSliceLength) + 1;	

				/**
				* Second, fill in contents relevant portions of fragment
				*/
				for (col = 0; col < numDataSlices; col++) {
					messageArrayPosition = col * sliceLength;

					ExpFE = (multFieldSize - (int)FEtoExp[row ^ col ^ COLBIT[0]]) % multFieldSize;

					for (rowEquation = 0; rowEquation < logOfField; rowEquation++) {
						ExpFEplusRow = ExpFE + rowEquation;
						sliceArrayPosition = rowOffset + (rowEquation * numSegments);

						for (columnEquation = 0; columnEquation < logOfField; columnEquation++)	{
							if ((ExptoFE[ExpFEplusRow] & BIT[columnEquation]) > 0) {  
								/*
								* Warning: the following code is heavily optimized and difficult to read
								* The following loop is the result of much optimization
								* and is an attempt to use as few instructions as possible
								* within this very deeply nested loop.
								*/
								arrayPositionDifference = ((columnEquation * numSegments + messageArrayPosition) - sliceArrayPosition);
								sliceArrayPositionPlusNumSegments = sliceArrayPosition + numSegments;
								sliceArrayIterator = sliceArrayPosition;

								while (sliceArrayIterator < sliceArrayPositionPlusNumSegments) {
									slices[sliceArrayIterator] ^= message[sliceArrayIterator + arrayPositionDifference];
									sliceArrayIterator++;
								}
							}
						}
					}
				}
			}

			//foreach (uint8 u2 in slices)
			//	Logger.debug<CauchyEncode> (u2.to_string ());

			return slices;
		}
	}
}