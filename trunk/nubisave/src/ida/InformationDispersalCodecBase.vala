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
//-----------------------
// Author: mmotwani
//
// Date: May 30, 2007
//---------------------


using Gee;

namespace NubiSave.IDA
{
	/**
	 * Base abstract class that implements {@link InformationDispersalCodec} and
	 * contains common code for all codecs.
	 * 
	 * original from cleversafe, but revised to fit our need
	 * 
	 * @author Jiang Bian
	 */
	public abstract class InformationDispersalCodecBase : InformationDispersalCodec, Object
	{
		public static int DEFAULT_CHUNK_SIZE = 4096;

		public string Name { get; set; default = null; }
		
		// Number of slices
		public int NumSlices { get; set; default = 0; }

		// Number of acceptable lost slices !?!?
		public int Threshold { get; set; default = 0; }

		// Size of the chunk of data processed as input with each call to encode
		public int ChunkSize { get; set; default = DEFAULT_CHUNK_SIZE; }

		protected bool isInitialized = false;
	
		// Encoder and decoder
		protected static InformationDispersalEncoder? _encoder = null;
		protected static InformationDispersalDecoder? _decoder = null;

		protected abstract InformationDispersalEncoder getNewEncoder () throws IDAError;

		protected abstract InformationDispersalDecoder getNewDecoder () throws IDAError;

		protected InformationDispersalCodecBase (int numSlices, int threshold, int chunkSize = DEFAULT_CHUNK_SIZE) 
		{
			NumSlices = numSlices;
			Threshold = threshold;
			ChunkSize = chunkSize;
		}

		public InformationDispersalEncoder getEncoder() throws IDAError
		{
			if(!isInitialized) 
				throw new IDAError.NotInitialized ("the parameteres have not been initialized!");
		
			if (_encoder == null)
				_encoder = getNewEncoder ();
			
			return _encoder;
		}

		public InformationDispersalDecoder getDecoder() throws IDAError
		{
			if(!isInitialized)
				throw new IDAError.NotInitialized ("the parameteres have not been initialized!");
		
			if (_decoder == null)
				_decoder = getNewDecoder ();
			
			return _decoder;
		}

		/**
		 * Initializes IDA's encoder and decoder, cannot be changed thereafter
		 * @throws IDAInvalidParametersException 
		 */
		protected void initialize () throws IDAError
		{
			if (_encoder == null)
				_encoder = getNewEncoder ();

			if (_decoder == null)
				_decoder = getNewDecoder ();
		
			isInitialized = true;
		}

		public float getBlowup ()
		{
			return NumSlices / (float) Threshold;
		}

		public abstract long getDispersedSize (long inputSize);
	}
}