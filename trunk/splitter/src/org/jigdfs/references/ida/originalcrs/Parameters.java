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

package org.jigdfs.references.ida.originalcrs;

/**
 * Parameters are the parameters used to encode/decode an object.
 * 
 * @author Hakim Weatherspoon
 * @version $Id: Parameters.java,v 1.4 2004/05/14 00:46:01 hweather Exp $
 */
public class Parameters
{

   /**
    * Lfield is the log of the length of the field.
    */
   protected int _Lfield = 8;

   /**
    * Nsegs is the number of segments in a fragment. Length of fragment in bytes
    * is 4*Nsegs*Lfield.
    */
   protected int _Nsegs = 43;

   /**
    * TableLength is 2^{Lfield}
    */
   protected int _TableLength = 1 << _Lfield;

   /**
    * SMultField is the size of the multiplicative field (2^{Lfield}-1) ==
    * TableLength - 1.
    */
   protected int _SMultField = (1 << _Lfield) - 1;

   /**
    * Plen is the fragment length in words excluding the overhead for storing
    * the index.
    */
   protected int _Plen = _Nsegs * _Lfield;

   /**
    * Plentot is the fragment length in words including the overhead for storing
    * the index.
    */
   protected int _Plentot = _Plen + 1;

   /**
    * Mfragments is the number of message fragments
    * 
    * IMPORTANT: The max of Mfragments and Rfragments is at most 2^{Lfield-1}.
    * 
    * Lfield must be set large enough to make this true else the encoding and
    * decoding won't work
    */
   protected int _Mfragments;

   /**
    * Rfragments is the number of redundant fragments
    * 
    * IMPORTANT: The max of Mfragments and Rfragments is at most 2^{Lfield-1}.
    * 
    * Lfield must be set large enough to make this true else the encoding and
    * decoding won't work.
    */
   protected int _Rfragments;

   /**
    * Nfragments is the total number of fragments sent.
    */
   protected int _Nfragments;

   /**
    * Mlen is the length of the message in words.
    */
   protected int _Mlen;

   /**
    * Elen is the length of the encoding in words.
    */
   protected int _Elen;

   /**
    * CONSTRUCTOR
    * 
    * Initialize Parameters for a number of messages.
    * 
    * @param Mfragments =
    *           Mfragments is the number of message fragments.
    * @param Rfragments =
    *           Rfragments is the number of redundant fragments.
    */
   public Parameters(/* in */int Mfragments,
   /* in */int Rfragments)
   {
      _Mfragments = Mfragments;
      _Rfragments = Rfragments;

      _Nfragments = _Mfragments + _Rfragments;
      // _Mseglen = _Mfragments * _Lfield;
      _Mlen = _Plen * _Mfragments;
      _Elen = _Plen * (_Mfragments + _Rfragments);
   }

   /**
    * Lfield is the log of the length of the field.
    */
   public int Lfield()
   {
      return _Lfield;
   }

   /**
    * Nsegs is the number of segments in a fragment. Length of fragment in bytes
    * is 4*Nsegs*Lfield.
    */
   public int Nsegs()
   {
      return _Nsegs;
   }

   /**
    * TableLength is 2^{Lfield}
    */
   public int TableLength()
   {
      return _TableLength;
   }

   /**
    * SMultField is the size of the multiplicative field (2^{Lfield}-1) ==
    * TableLength - 1.
    */
   public int SMultField()
   {
      return _SMultField;
   }

   /**
    * Plen is the fragment length in words excluding the overhead for storing
    * the index.
    */
   public int Plen()
   {
      return _Plen;
   }

   /**
    * Plentot is the fragment length in words including the overhead for storing
    * the index.
    */
   public int Plentot()
   {
      return _Plentot;
   }

   /**
    * Mfragments is the number of message fragments
    * 
    * @return return == return the number of message fragments.
    * 
    * IMPORTANT: The max of Mfragments and Rfragments is at most 2^{Lfield-1}.
    * 
    * Lfield must be set large enough to make this true else the encoding and
    * decoding won't work
    */
   public int Mfragments()
   {
      return _Mfragments;
   }

   /**
    * Rfragments is the number of redundant fragments
    * 
    * @return return == return the number of message fragments.
    * 
    * IMPORTANT: The max of Mfragments and Rfragments is at most 2^{Lfield-1}.
    * 
    * Lfield must be set large enough to make this true else the encoding and
    * decoding won't work.
    */
   public int Rfragments()
   {
      return _Rfragments;
   }

   /**
    * Nfragments is the total number of fragments sent.
    */
   public int Nfragments()
   {
      return _Nfragments;
   }

   /**
    * Mlen is the length of the message in words.
    */
   public int Mlen()
   {
      return _Mlen;
   }

   /**
    * Elen is the length of the encoding in words.
    */
   public int Elen()
   {
      return _Elen;
   }

   /**
    * Nsegs is the number of segments in each fragment in which to perform the
    * Galois Field operations.
    * 
    * @param n == number to set Nsegs to, n > 0.
    * @return return==false if n <= 0, otherwise returns true.
    */
   public boolean setNsegs(int n)
   {
      if (n < 1)
         return false;
      _Nsegs = n;
      resetParam();

      return true;
   }

   /**
    * Lfield (length of field) must be (1 <= Lfield <= 16) otherwise function
    * returns false and Lfield stays default.
    * 
    * @param n ==
    *           number to set Lfield to, 1 <= Lfield <= 16.
    * @return return==Lfield must be (1 <= Lfield <= 16) otherwise function
    *         returns false and Lfield stays default
    */
   public boolean setLfield(int n)
   {
      if (n <= 16 && n >= 1)
         _Lfield = n;
      else
         return false;

      resetParam();

      return true;
   }

   public void resetParam()
   {
      _TableLength = 1 << _Lfield;
      _SMultField = (1 << _Lfield) - 1;
      _Plen = _Nsegs * _Lfield;
      _Plentot = _Plen + 1;
   }

}
