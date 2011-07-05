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

using Gee;
using Posix;

namespace NubiSave.IDA
{
	public class CauchyInformationDispersalDecoder : InformationDispersalDecoder, Object
	{
		/** Cauchy IDA parameters */
		private CauchyIDAParameters? params = null;
		
		private bool initialized = false;
		
		/**
		* The total number of slices
		*/
		public int NumSlices { get; protected set; }

		/**
		* The number of slices required to restore
		*/
		public int Threshold { get; protected set; }

		/**
		* Standard Message Length
		*/
		public int ChunkSize { get; protected set; default = InformationDispersalCodecBase.DEFAULT_CHUNK_SIZE; }


		public CauchyInformationDispersalDecoder (int numSlices, int threshold, int chunkSize = InformationDispersalCodecBase.DEFAULT_CHUNK_SIZE) throws IDAError
		{
			NumSlices = numSlices;
			Threshold = threshold;
			ChunkSize = chunkSize;
			
			initialize();
		}

		public void initialize () throws IDAError
		{
			// Configuration already calls initialize, but we need to reinitialize with the
			// calculated block size after datasource codecs are applied.	This is a safety 
			// measure which should be added back in the future, but is being removed as a quick
			// fix -- JKR
			//if (this.initialized == true) {
			//	throw IDAError.InvalidParameters ("Decoder may only be initialized once");
			//}
			if (NumSlices < 1)
				throw new IDAError.InvalidParameters ("Number of slices must be positive");
			
			if (Threshold <= 0)
				throw new IDAError.InvalidParameters ("Threshold must be greater than zero");

			if (ChunkSize < 1)
				throw new IDAError.InvalidParameters ("Chunk size must be positive");

			if (Threshold > NumSlices) 
				throw new IDAError.InvalidParameters ("Threshold must be less than or equal to number of slices");

			params = new CauchyIDAParameters (Threshold, NumSlices - Threshold, ChunkSize);

			initialized = true;
		}

		public ByteArray process (ArrayList<unowned ByteArray> encodedBuffers) throws IDAError
		{
			if (!initialized)
				throw new IDAError.NotInitialized ("IDA is not initialized, Call initialize() first");

			int fragmentSize = getFragmentSize();

			uint8[] data = new uint8[getMessageSize()];
			uint8[] fragments = new uint8[Threshold * fragmentSize];
			uint8[] output = new uint8[getMessageSize()];

			// Establish slice length
			int dataLength = -1;
			foreach (unowned ByteArray encodedBuffer in encodedBuffers) {
				if (dataLength == -1) {
					dataLength = (int)encodedBuffer.len;
				} else {
					if (dataLength == encodedBuffer.len)
						printf ("Inconsistent slice length: " + encodedBuffer.len.to_string () + " expected " + dataLength.to_string ());
				}
			}
			
			if (dataLength != -1)
				printf ("Data length can't be calculated");

			int outputBufferSize = dataLength / fragmentSize * getMessageSize ();

			if (outputBufferSize != output.length)
				output = new uint8[outputBufferSize];

			int outputPosition = 0;
			int encodedBufferPosition = 0;

			do {
				// Copy encoded data into fragments array
				int fragmentIdx = 0;
				foreach (unowned ByteArray encodedBuffer in encodedBuffers) {
					if (fragmentIdx >= Threshold)
						break;
					
					// Skip null buffers
					if (encodedBuffer != null) {
						int fragmentOffset = fragmentIdx * fragmentSize;
						
						//FIXME
						//System.arraycopy(encodedBuffer, encodedBufferPosition, fragments, fragmentOffset, fragmentSize);
						Memory.copy ((uint8*)fragments + fragmentOffset, (uint8*)encodedBuffer + encodedBufferPosition, fragmentSize);
						fragmentIdx++;
					}
				}

				if (fragmentIdx != Threshold)
					throw new IDAError.InvalidSliceCount ("Expected " + Threshold.to_string () + " but got only "
							+ fragmentIdx.to_string () + " slices");

				try {
					data = CauchyDecode.decode (fragments, Threshold, params);
				} catch (Error e) {
					throw new IDAError.Decode ("Decode error");
				}
	
				//FIXME
				//System.arraycopy(data, 0, output, outputPosition, data.length);
				Memory.copy ((uint8*)output + outputPosition, (uint8*)data, data.length);
				
				outputPosition += data.length;
				encodedBufferPosition += fragmentSize;
				
			} while (encodedBufferPosition < dataLength);

			// Truncate padding
			int outputSize = output.length;
			while (outputSize > 0 && output[outputSize - 1] == 0)
				outputSize--;

			outputSize--;

			ByteArray buffer = new ByteArray.sized (outputSize);
			buffer.append (output);
			//FIXME
			//System.arraycopy(output, 0, buffer, 0, outputSize);
			//Memory.copy ((uint8*)(buffer.data), (uint8*)output, outputSize);
			return buffer;
		}


		private int getMessageSize ()
		{
			return params.SliceLength * params.NumDataSlices;
		}

		private int getFragmentSize ()
		{
			return params.TotalSliceLength;
		}

		public string to_string ()
		{
			string str = "";
			str += "Slice count = " + NumSlices.to_string () + ", ";
			str += "threshold = " + Threshold.to_string () + ", ";
			str += "Message size: " + getMessageSize ().to_string () + ", ";
			str += "Fragment size: " + getFragmentSize ().to_string () + ", ";
			str += "Blowup = " + ((float) getFragmentSize () * (float) NumSlices / (float) getMessageSize ()).to_string () + ", ";
			str += "Ideal = " + ((float) NumSlices / (float) Threshold).to_string ();

			return str;
		}
	}
}