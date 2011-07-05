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
	 * Cauchy Reed-Solomon IDA implementation, encoder should be a singlton
	 */
	public class CauchyInformationDispersalCodec : InformationDispersalCodecBase
	{
		/**
		* Construct a new Cauchy Reed-Solomon IDA
		* 
		* @param numSlices
		*			  Number of slices to produce
		* @param threshold
		*			  Number of recoverable slice losses
		* @param chunkSize
		*			  The size of data that the IDA will process at a time
		*/
		public CauchyInformationDispersalCodec (int numSlices, int threshold, int chunkSize = DEFAULT_CHUNK_SIZE) throws IDAError
		{
			base (numSlices, threshold, chunkSize);
			
			Name = "optimizedcauchy";
			
			if (numSlices < 1)
				throw new IDAError.InvalidParameters ("Number of slices must be positive");
			
			if (threshold <= 0)
				throw new IDAError.InvalidParameters ("Threshold must be greater than zero");
			
			if (chunkSize < 1)
				throw new IDAError.InvalidParameters ("Chunk size must be positive");
			
			if (threshold > numSlices)
				throw new IDAError.InvalidParameters ("Threshold must be less than or equal to number of slices");
			
			initialize ();
		}
		
		protected override InformationDispersalEncoder getNewEncoder () throws IDAError
		{		
			return new CauchyInformationDispersalEncoder (NumSlices, Threshold, ChunkSize);
		}
		
		protected override InformationDispersalDecoder getNewDecoder () throws IDAError
		{
			return new CauchyInformationDispersalDecoder (NumSlices, Threshold, ChunkSize);
		}

		public override long getDispersedSize (long inputSize)
		{
			var parameters = new CauchyIDAParameters (Threshold, NumSlices - Threshold, ChunkSize);
			
			int sliceLength = parameters.SliceLength;
			int messageLength = sliceLength * Threshold;
			int encodedLength = parameters.TotalSliceLength * parameters.NumSlices;
			
			return ((inputSize / messageLength) + 1) * encodedLength;
		}
	}
}