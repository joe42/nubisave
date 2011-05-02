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

namespace NubiSave
{
	public class CloudFilePart : Preferences
	{
		[Description(nick = "uuid", blurb = "The uuid of the filepart.")]
		public string Uuid { get; set; default = ""; }
		
		[Description(nick = "filename", blurb = "The real name of the file.")]
		public string Name { get; set; default = ""; }
		
		[Description(nick = "storages", blurb = "The list of cloud storages which contain this filepart.")]
		public string Storages { get; set; default = ""; }
		
		[Description(nick = "offset", blurb = "The offset of this filepart in the original file.")]
		public uint64 Offset { get; set; default = 0; }
		
		[Description(nick = "ctime", blurb = "The time of creation for this filepart.")]
		public uint64 CTime { get; set; default = new DateTime.now_utc ().to_unix (); }
		
		[Description(nick = "filesize", blurb = "The size of the filepart.")]
		public uint64 Size { get; set; default = 0; }
		
		public CloudFilePart Next = null;
		public CloudFilePart Previous = null;
		
		HashMap<string, FileIOStream> Streams;
		FileIOStream current_iostream = null;
		uint64 new_size = 0;
		bool has_written;
		bool has_read;
		
		construct
		{
			Streams = new HashMap<string, FileIOStream> ();
		}
		
		public CloudFilePart (string name, string uuid)
		{
			base ();
			load (uuid);
			if (Uuid != uuid)
				Uuid = uuid;
			if (Name != name)
				Name = name;
		}
		
		public uint64 current_size ()
		{
			if (new_size > Size)
				return new_size;
			return Size;
		}
		
		public int open ()
		{
			if (Streams.size > 0)
				return -EIO;
			
			current_iostream = null;
			has_written = false;
			has_read = false;

			Logger.error<CloudFilePart> ("Open: %s".printf (Uuid));
			
			try {
				//TODO RAIC here? choose fastest source
				foreach (var storage in Storages.split (";")) {
					//TODO move this to CloudStorage
					var targetfile = Paths.UserCacheFolder.get_child ("storages").get_child (storage).get_child (Uuid);
					FileIOStream stream = null;
					if (targetfile.query_exists ())
						stream = targetfile.open_readwrite (null);
					else
						stream = targetfile.create_readwrite (FileCreateFlags.REPLACE_DESTINATION, null);
					Streams.set (storage, stream);
					
					// just pick first stream
					if (current_iostream == null)
						current_iostream = stream;
				}
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Open Error and Force Close: %s".printf (e.message));
				
				close ();
				
				return -EIO;
			}

			new_size = Size;

			return 0;
		}
		
		public int read (char* buffer, size_t size, off_t offset)
		{
			if (Streams.size == 0)
				return -EIO;
				
			if (current_iostream == null)
				return -EIO;
			
			//if (current_iostream.can_seek ())
			//	if (current_iostream.tell () != offset)
			//		current_iostream.seek (offset, SeekType.SET, null);
			//	else 
			//		return -EIO;
			
			int ret = 0;
			
			try {
				Logger.error<CloudFilePart> ("Read: %s %i at %i".printf (Uuid, (int)size, (int)offset));
				
				//TODO move this to CloudStorage
				//TODO choose better source storage
				var b = new uint8[size];
				ret = (int)current_iostream.input_stream.read (b);
				Memory.copy (buffer, b, size);
				
				if (ret >= 0)
					Logger.error<CloudFilePart> ("Read: %s %i OK".printf (Uuid, ret));
				
				has_read = true;
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Read Error: %s".printf (e.message));
				return -EIO;
			}
			
			return ret;
		}
		
		public int write (char* buffer, size_t size, off_t offset)
		{
			if (Streams.size == 0)
				return -EIO;
			
			// Only sequential writing!
			if (new_size > offset)
				return -EIO;
			
			try {
				Logger.error<CloudFilePart> ("Write: %s %i at %i".printf (Uuid, (int)size, (int)offset));
				foreach (var stream in Streams.values) {
					//TODO move this to CloudStorage
					var b = new uint8[size];
					Memory.copy (b, buffer, size);
					stream.output_stream.write (b);
				}
				
				Logger.error<CloudFilePart> ("Write: %s OK");

				has_written = true;
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Write Error: %s".printf (e.message));
				return -EIO;
			}
			
			if (new_size == 0 || new_size > Size)
				new_size += size;
			return (int)size;
		}
		
		public int close ()
		{
			if (Streams.size == 0)
				return -EIO;
			
			int ret = 0;
			
			current_iostream = null;
			
			Logger.error<CloudFilePart> ("Close: %s".printf (Uuid));
			
			//TODO move this to CloudStorage
			foreach (var stream in Streams.values) {
				try {
					if (has_written)
						stream.output_stream.flush ();
					stream.close ();
				} catch (Error e) {
					Logger.error<CloudFilePart> ("Close Error: %s".printf (e.message));
					ret = -EIO;
				}
			}
			
			if (has_written && Size != new_size)
				Size = new_size;
			
			Streams.clear ();

			has_written = false;
			has_read = false;
			
			return ret;
		}

		public override void delete ()
		{
			close ();
			
			foreach (string storage in Storages.split (";")) {
				if (storage.length == 0)
					continue;
				
				try {
					var targetfile = Paths.UserCacheFolder.get_child ("storages").get_child (storage).get_child (Uuid);
					if (targetfile.query_exists (null))
						targetfile.delete (null);
			
				} catch (Error e) {
					Logger.error<CloudFilePart> ("Error: %s".printf (e.message));
				}
			}
			
			base.delete ();
		}

		public void load (string name)
		{
			if (name.has_suffix (".cloudfilepart"))
				init_from_file ("fileparts/" + name);
			else
				init_from_file ("fileparts/" + name + ".cloudfilepart");
		}
		
		public string to_string ()
		{
			return "UUID: " + Uuid + "\nSize: " + Size.to_string () + "\nStorages: " + Storages + "\nFile: " + Name + "\n";
		}		
	}
}
