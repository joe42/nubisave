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

namespace NubiSave.IDA
{
	/**
	 * IDA interface - combines an encoder and decoder
	 */
	public interface InformationDispersalCodec : Object
	{
		/**
		* Name of the codec
		*/
		public abstract string Name { get; set; }
		
		/**
		* Number of slices produced by this codec
		*/
		public abstract int NumSlices { get; set; }

		/**
		* Number of slices needed to restore data !?!?
		*/
		public abstract int Threshold { get; set; }

		/**
		* Chunk size for which the IDA will operate, typically the disk block size
		* plus overhead due to datasource codecs
		*/
		public abstract int ChunkSize { get; set; }

		/**
		* 
		* @return
		*/
		public abstract InformationDispersalEncoder getEncoder () throws IDAError;

		/**
		* 
		* @return
		*/
		public abstract InformationDispersalDecoder getDecoder () throws IDAError;

		/**
		* Returns the blowup of this IDA.  For example, if encoded data is 30% larger than 
		* the original input, this would return 1.3.
		* @return Blowup of this IDA as a factor of the original data size (eg. 1.3)
		*/
		public abstract float getBlowup ();

		/**
		* Returns the post-dispersed upper-bound size in bytes for data with a given input size.
		* 
		* @param inputDataSize
		* @return
		*/
		public abstract long getDispersedSize (long inputSize);
	}
}