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

namespace NubiSave
{
	public class Uuid
	{
		[CCode (cname = "uuid_generate", cheader_filename = "uuid/uuid.h")]
		internal extern static void uuid_generate ([CCode (array_length = false)]
		                                           uchar[] uuid);
		[CCode (cname = "uuid_unparse", cheader_filename = "uuid/uuid.h")]
		internal extern static void uuid_unparse ([CCode (array_length = false)]
		                                          uchar[] uuid,
		                                          [CCode (array_length = false)]
		                                          uchar[] output);

		public static string generate_random ()
		{
			var udn = new uchar[50];
			var id = new uchar[16];
			
			/* Generate new UUID */
			uuid_generate (id);
			uuid_unparse (id, udn);
			
			return (string) udn;
		}		
	}
}
