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
	public class CauchyInformationDispersalEncoder : InformationDispersalEncoder, Object
	{
		/** Cauchy IDA parameters */
		private CauchyIDAParameters params = null;
		
		public int MessageSize { get; private set; }
		public int TotalSliceLength { get; private set; }

		private bool initialized = false;
		
		// private ArrayList<ByteArray> outputBuffers;
		
		/**
		* The total number of slices
		*/
		public int NumSlices { get; protected set; }

		/**
		* The number of slices required to restore
		*/
		public int Threshold { get; protected set; }

		/**
		* The chunk size
		*/
		public int ChunkSize { get; protected set; default = InformationDispersalCodecBase.DEFAULT_CHUNK_SIZE; }


		public CauchyInformationDispersalEncoder (int numSlices, int threshold, int chunkSize) throws IDAError
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
			//	throw new IDAError.InvalidParameters ("Encoder may only be initialized once");
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
			MessageSize = params.SliceLength * params.NumDataSlices;
			TotalSliceLength = params.TotalSliceLength;
			
			initialized = true;
		}

		public ArrayList<ByteArray> process(ByteArray buffer) throws IDAError
		{
			if (!initialized)
				throw new IDAError.NotInitialized ("IDA is not initialized, Call initialize() first");
			
			uint8[] message = new uint8[MessageSize];

			uint inputPosition = 0;
			uint outputPosition = 0;

			// Calculate the size of each output buffer
			uint outputSize = ((buffer.len + 1) / MessageSize) * TotalSliceLength;

			if ((buffer.len + 1) % MessageSize != 0)
				outputSize += TotalSliceLength;

			// Allocate new buffers for output
			var outputBuffers = new ArrayList<unowned ByteArray> ();	

			// Allocate the output buffers
			for (int fragmentIdx = 0; fragmentIdx < NumSlices; fragmentIdx++) {
				var array = new ByteArray ();
				var fragment = new uint8[TotalSliceLength];
				array.append (fragment);
				outputBuffers.add (array);
			}

			while (outputPosition < outputSize)
			{
				uint8 fillerByte = 0x01;
				
				// Copy data from the input buffer into the data buffer
				uint freeBufferSpace = buffer.len - inputPosition;
				uint maxWriteAmount = (message.length < freeBufferSpace ? message.length : freeBufferSpace);

				//FIXME
				//System.arraycopy(buffer, inputPosition, message, 0, maxWriteAmount);
				Memory.copy ((uint8*)message, (uint8*)(buffer.data) + inputPosition, sizeof(uint8) * maxWriteAmount);
				inputPosition += maxWriteAmount;
				
				// Add padding if needed
				if (maxWriteAmount < message.length) {
					message[maxWriteAmount] = fillerByte;
					
					//FIXME
					//Arrays.fill(message, maxWriteAmount+1, message.length, (uchar) 0x00);
					Memory.set ((uint8*)message + maxWriteAmount + 1, 0x00, sizeof(uint8) * (message.length - maxWriteAmount - 1));
				}

				// Perform encoding of the data buffer into slices array
				uint8[] slices = CauchyEncode.encode (message, params);

				// For each fragment
				for (int fragmentIdx = 0; fragmentIdx < NumSlices; fragmentIdx++) {
					unowned ByteArray fragmentArray = outputBuffers.get (fragmentIdx);
					int fragmentOffset = fragmentIdx * TotalSliceLength;
					
					//var fragment = new uint8[TotalSliceLength];

					//Logger.debug<CauchyEncode> (fragmentIdx.to_string ());
					//Logger.debug<CauchyEncode> (fragmentArray.data.length.to_string ());

					//FIXME
					//System.arraycopy(slices, fragmentOffset, fragment, outputPosition, totalSliceLength);
					Memory.copy ((uint8*)(fragmentArray.data), (uint8*)slices + fragmentOffset, sizeof(uint8) * TotalSliceLength);

					//fragmentArray.append (fragment);
				}

				outputPosition += TotalSliceLength;
			}

			return outputBuffers;
		}

		public string to_string ()
		{
			string str = "";
			str += "Slice count = " + NumSlices.to_string () + ", ";
			str += "threshold = " + Threshold.to_string () + ", ";
			str += "Message size: " + MessageSize.to_string () + ", ";
			str += "Fragment size: " + TotalSliceLength.to_string () + ", ";
			str += "Blowup = " + ((float)TotalSliceLength * (float)NumSlices / (float)MessageSize).to_string () + ", ";
			str += "Ideal = " + ((float) NumSlices / (float) Threshold).to_string ();
			
			return str;
		}
	}
}