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

package org.jigdfs.ida.base;

import org.jigdfs.exception.NotImplementedException;
import org.jigdfs.ida.exception.IDAInvalidParametersException;
import org.jigdfs.ida.exception.IDANotInitializedException;

/**
 * Base abstract class that implements {@link InformationDispersalCodec} and
 * contains common code for all codecs.
 * 
 * original from cleversafe, but revised to fit our need
 * 
 * @author Jiang Bian
 */
public abstract class InformationDispersalCodecBase implements
		InformationDispersalCodec {
	public static int DEFAULT_CHUNK_SIZE = 4096;

	protected String name = null;

	protected boolean isInitialized = false;
	
	// Number of slices
	protected int numSlices = 0;

	// Number of acceptable lost slices
	protected int threshold = 0;

	// Size of the chunk of data processed as input with each call to encode
	protected int chunkSize = DEFAULT_CHUNK_SIZE;

	// Encoder and decoder
	protected static InformationDispersalEncoder _encoder = null;
	protected static InformationDispersalDecoder _decoder = null;

	protected abstract InformationDispersalEncoder getNewEncoder() throws IDAInvalidParametersException;

	protected abstract InformationDispersalDecoder getNewDecoder() throws IDAInvalidParametersException;

	// should never be called without params
	protected InformationDispersalCodecBase() {
		throw new NotImplementedException(
				"This should never be called without params");
	}

	protected InformationDispersalCodecBase(int numSlices, int threshold) {
		this.numSlices = numSlices;
		this.threshold = threshold;
		this.chunkSize = DEFAULT_CHUNK_SIZE;
	}

	protected InformationDispersalCodecBase(int numSlices, int threshold,
			int chunkSize) {
		this.numSlices = numSlices;
		this.threshold = threshold;
		this.chunkSize = chunkSize;
	}

	public InformationDispersalEncoder getEncoder() throws IDANotInitializedException, IDAInvalidParametersException {

		if(!isInitialized) throw new IDANotInitializedException("the parameteres have not been initialized!");
		
		if (_encoder == null) {
			_encoder = getNewEncoder();
		}
		return _encoder;
	}

	public InformationDispersalDecoder getDecoder() throws IDANotInitializedException, IDAInvalidParametersException {

		if(!isInitialized) throw new IDANotInitializedException("the parameteres have not been initialized!");
		
		if (_decoder == null) {
			_decoder = getNewDecoder();
		}
		return _decoder;
	}

	/**
	 * Initializes IDA's encoder and decoder
	 * @throws IDAInvalidParametersException 
	 */
	protected void initialize() throws IDAInvalidParametersException {
		_encoder = getNewEncoder();
		_decoder = getNewDecoder();
		isInitialized = true;
	}

	public int getNumSlices() {
		return numSlices;
	}

	public int getThreshold() {
		return threshold;
	}

	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * @param numSlices
	 *            the numSlices to set
	 */
	public void setNumSlices(int numSlices) {
		this.numSlices = numSlices;
	}

	/**
	 * @param threshold
	 *            the threshold to set
	 */
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	/**
	 * @param chunkSize
	 *            the chunkSize to set
	 */
	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public float getBlowup() {
		return this.getNumSlices() / (float) (this.getThreshold());
	}

}
