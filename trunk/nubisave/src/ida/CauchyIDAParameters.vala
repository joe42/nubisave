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

namespace NubiSave.IDA
{
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
		public int ChunkSize { get; private set; }

		/**
		* logOfFieldLength is the log of the length of the field.
		*/
		public int LogOfFieldLength { get; private set; default = 8; }

		/**
		* segmentsPerFragment is the number of segments in a slice. Length of slice in bytes
		* is segmentsPerFragment*logOfFieldLength.
		*/
		public int SegmentsPerSlice  { get; private set; default = 43; }

		/**
		* finiteFieldTableLength is 2^{logOfFieldLength}
		*/
		public int FiniteFieldTableLength { get; private set; }

		/**
		* SMultField is the size of the multiplicative field (2^{logOfFieldLength}-1) ==
		* finiteFieldTableLength - 1.
		*/
		public int MultiplicationFieldSize { get; private set; }

		/**
		* sliceLength is the fragment length in words excluding the overhead for storing
		* the index.
		*/
		public int SliceLength { get; private set; }

		/**
		* totalSliceLEngth is the slice length in words including the overhead for storing
		* the index.
		*/
		public int TotalSliceLength { get; private set; }

		/**
		* numDataSlices is the number of data slices
		* 
		* IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
		* 
		* logOfFieldLength must be set large enough to make this true else the encoding and
		* decoding won't work
		*/
		public int NumDataSlices { get; private set; }

		/**
		* numCodeSlices is the number of redundant fragments
		* 
		* IMPORTANT: The max of numDataSlices + numCodeSlices is at most 2^{logOfFieldLength-1}.
		* 
		* logOfFieldLength must be set large enough to make this true else the encoding and
		* decoding won't work.
		*/
		public int NumCodeSlices { get; private set; }

		/**
		* numSlices is the total number of slices sent.
		*/
		public int NumSlices { get; private set; }

		/**
		* messageLength is the length of the message in bytes.
		*/
		public int MessageLength { get; private set; }

		/**
		* dispersedMessageLength is the post-dispersal length of the message in words.
		*/
		public int DispersedMessageLength { get; private set; }

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
		public CauchyIDAParameters (int numDataSlices, int numCodeSlices, int chunkSize)
		{
			LogOfFieldLength = 8;
			SegmentsPerSlice = 43;
			FiniteFieldTableLength = 1 << LogOfFieldLength;
			MultiplicationFieldSize = (1 << LogOfFieldLength) - 1;
			SliceLength = SegmentsPerSlice * LogOfFieldLength;
			TotalSliceLength = SliceLength + 1;

			ChunkSize = chunkSize;
			NumDataSlices = numDataSlices;
			NumCodeSlices = numCodeSlices;

			calculateOptimumSliceSize();

			NumSlices = numDataSlices + numCodeSlices;
			MessageLength = SliceLength * numDataSlices;
			DispersedMessageLength = SliceLength * (numDataSlices + numCodeSlices);
		}

		/**
		* Calculates the optimum segmentsPerSlice and sliceLength values based on
		* the amount of data the IDA is given to process at a time and the number
		* of data slices.
		*/
		private void calculateOptimumSliceSize ()
		{
			// We add one to chunkSize to avoid cases where the block size
			// exactly equals the message length, resulting in an entire additional
			// block being created and filled with padding bytes. - JKR
			int tempChunkSize = ChunkSize + 1;

			int sliceSize = tempChunkSize / NumDataSlices;

			if (tempChunkSize % NumDataSlices != 0)
				sliceSize++;

			// Round up to the nearest multiple of log of field length
			if (sliceSize % LogOfFieldLength > 0)
				sliceSize += (LogOfFieldLength - (sliceSize % LogOfFieldLength));


			SegmentsPerSlice = sliceSize / LogOfFieldLength;      
			SliceLength = SegmentsPerSlice * LogOfFieldLength;
			TotalSliceLength = SliceLength + 1;
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
/*		public bool setLogOfFieldLength (int logOfFieldLength)
		{
			if (logOfFieldLength <= 8 && logOfFieldLength >= 1)
				this.logOfFieldLength = logOfFieldLength;
			else
				return false;

			resetParam();

			return true;
		}
*/
		public void setChunkSize (int chunkSize)
		{
			ChunkSize = chunkSize;

			resetParam ();
		}
		
		public void resetParam ()
		{
			LogOfFieldLength = 8;
			SegmentsPerSlice = 43;
			FiniteFieldTableLength = 1 << LogOfFieldLength;
			MultiplicationFieldSize = (1 << LogOfFieldLength) - 1;
			SliceLength = SegmentsPerSlice * LogOfFieldLength;
			TotalSliceLength = SliceLength + 1;

			calculateOptimumSliceSize();
			
			SliceLength = SegmentsPerSlice * LogOfFieldLength;
			TotalSliceLength = SliceLength + 1;
		}
		
		public string to_string()
		{
			return "number of segments: " + SegmentsPerSlice.to_string () + "; "
				+ "slice size:" + SliceLength.to_string () + "; "
				+ "message size:" + MessageLength.to_string ();
		}
	}
}