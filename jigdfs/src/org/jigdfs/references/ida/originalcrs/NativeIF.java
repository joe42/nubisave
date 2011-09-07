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

package org.jigdfs.references.ida.originalcrs;

public class NativeIF
{

   /**
    * encode uses the cauchy erasure encoding method to encode a message into
    * fragments.
    * 
    * @return return == time in microseconds to encode the msg into fragments.
    */

   /*
    * @param COLBIT == COLBIT is the bit that is used to make sure rows and
    * columns have distinct field elements associated with them. @param BIT ==
    * The BIT array is used to mask out single bits in equations: bit @param
    * ExptoFE == ExptoFE is the table that goes from the exponent to the finite
    * field element. @param FEtoExp == FEtoExp is the table that goes from the
    * finite field element to the exponent. @param fragments == fragments are
    * the array of fragments as output. @param message == is the message in int
    * array format.
    */
   public static native long cauchy_encode_using_c_helper(
         byte[] message /* in */,
         int Mfragments /* in */,
         int Rfragments /* in */,
         int NSegs /* in */,
         int Lfield /* in */,
         byte[] fragments /* out */);

   /**
    * encode uses the cauchy erasure encoding method to encode a message into
    * fragments.
    * 
    * FIXME!!!
    * 
    * @return return == time in microseconds to encode the msg into fragments.
    */

   /*
    * @param COLBIT == COLBIT is the bit that is used to make sure rows and
    * columns have distinct field elements associated with them. @param BIT ==
    * The BIT array is used to mask out single bits in equations: bit @param
    * ExptoFE == ExptoFE is the table that goes from the exponent to the finite
    * field element. @param FEtoExp == FEtoExp is the table that goes from the
    * finite field element to the exponent. @param fragments == fragments are
    * the array of fragments as output. @param message == is the message in int
    * array format.
    */
   public static native long cauchy_encode_and_verify_using_c_helper(
         byte[] message /* in */,
         int Mfragments /* in */,
         int Rfragments /* in */,
         int NSegs /* in */,
         int Lfield /* in */,
         int[] fragments /* out */,
         int[] intFrags2,
         byte[] frags3,
         int[] message2,
         byte[] message3);

   /**
    * decode uses the cauchy erasure coding method to decode an array of
    * fragments back into a msg.
    * 
    * FIXME
    * 
    * @return return == time in milleseconds to decode fragments into a msg.
    */

   /*
    * @param COLBIT == COLBIT is the bit that is used to make sure rows and
    * columns have distinct field elements associated with them. @param BIT ==
    * The BIT array is used to mask out single bits in equations: bit @param
    * ExptoFE == ExptoFE is the table that goes from the exponent to the finite
    * field element. @param FEtoExp == FEtoExp is the table that goes from the
    * finite field element to the exponent. @param fragments == fragments are
    * the array of fragments as output. @param message == is the message in int
    * array format.
    */
   public static native long cauchy_decode_using_c_helper(
         byte[] rec_fragments /* in */,
         int Nrec /* in */,
         int Mfragments /* in */,
         int Rfragments /* in */,
         int NSegs /* in */,
         int Lfield /* in */,
         byte[] rec_message /* out */);

   public static boolean available = false;

   static
   {
      try
      {
         System.loadLibrary( "cauchy_ida" );
         available = true;   
      } catch (UnsatisfiedLinkError e1)
      {         
         available = false;
      }
   }
}
