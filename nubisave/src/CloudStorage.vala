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

namespace NubiSave
{
	public enum CLOUDSTORAGE_STATE
	{
		CONNECTED = 0,
		DISCONNECTED,
	}

	public class CloudStorage : Preferences
	{
		[Description(nick = "name", blurb = "Unique name of this storage.")]
		public string Name { get; set; default = ""; }
		
		[Description(nick = "module", blurb = "Cloudfusion module to use.")]
		public string Module { get; set; default = ""; }
		
		//[Description(nick = "mountpath", blurb = "The path to the folder of this storage.")]
		//public string MountPath { get; set; default = ""; }
		
		[Description(nick = "username", blurb = "Username.")]
		public string Username { get; set; default = ""; }
		
		[Description(nick = "password", blurb = "Password.")]
		public string Password { get; set; default = ""; }
		
		//[Description(nick = "status", blurb = "Status of this storage")]
		//public CLOUDSTORAGE_STATE Status { get; set; default = ""; }
		
		[Description(nick = "size", blurb = "Capacity of this Storage.")]
		public uint64 Size { get; set; default = 0; }

		[Description(nick = "freesize", blurb = "Free Capacity of this Storage.")]
		public uint64 FreeSize { get; set; default = 0; }
		
		[Description(nick = "deleted", blurb = "Mark this Storages as deleted for account removal.")]
		public uint64 Deleted { get; set; default = 0; }
		
		[Description(nick = "priority", blurb = "Usage priority of this Storage.")]
		public uint64 Priority { get; set; default = 0; }
		
		construct
		{
		}
		
		public CloudStorage (string name)
		{
			base ();
			load (name);
		}
		
		public void load (string name)
		{
			if (name.has_suffix (".cloudstorage"))
				init_from_file ("storages/" + name);
			else
				init_from_file ("storages/" + name + ".cloudstorage");
		}
		
		public string to_string ()
		{
			return "Name: " + Name + "\n";
		}		
	}
}
