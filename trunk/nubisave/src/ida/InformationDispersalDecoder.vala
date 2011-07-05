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

namespace NubiSave.IDA
{
	public interface InformationDispersalDecoder : Object
	{
		/**
		* The number of slices
		*/
		public abstract int NumSlices { get; protected set; }

		/**
		* The number of slices which are required to restore data.
		*/
		public abstract int Threshold { get; protected set; }

		/**
		* The chunk size
		*/
		public abstract int ChunkSize { get; protected set; }

		/**
		* Prepares the decoder to begin processing encoded data.
		*/
		public abstract void initialize () throws IDAError;

		/**
		* Performs a complete decoding operation or finishes a multiple-part
		* decoding operation.
		* 
		* @param encodedBuffers A list of encoded buffers
		* 
		* @return Decoded data
		* @throws IDAError
		*/
		public abstract ByteArray process (ArrayList<ByteArray> encodedBuffers) throws IDAError;
	}
}