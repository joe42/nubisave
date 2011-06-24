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
		
		HashMap<uint32, FileIOStream> streams;
		uint64 new_size = 0;
		
		construct
		{
			streams = new HashMap<uint32, FileIOStream> ();
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
		
		public int open (uint32 fh)
		{
			if (streams.get (fh) != null)
				return 0;
			
			Logger.error<CloudFilePart> ("Open: %s (%u)".printf (Uuid, fh));
			
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
		
					streams.set (fh, stream);
					
					// use first storage and bail
					break;
				}
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Open Error: %s (%u) %s".printf (Uuid, fh, e.message));
				return -EIO;
			}

			new_size = Size;

			return 0;
		}
		
		public int read (uint32 fh, char* buffer, size_t size, off_t offset)
		{
			var stream = streams.get (fh);
			if (stream == null)
				return -EIO;
			
			int ret = 0;
			
			try {
				if (stream.can_seek () && stream.tell () != offset - Offset) {
					Logger.error<CloudFilePart> ("Read: %s at %i seek to %i".printf (Uuid, (int)stream.tell (), (int)offset - (int)Offset));
					stream.seek (offset - (int64)Offset, SeekType.SET, null);
				}
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Read Seek Error: %s".printf (e.message));
				return -EIO;
			}
			
			try {
				Logger.error<CloudFilePart> ("Read: %s %i bytes at %i".printf (Uuid, (int)size, (int)offset - (int)Offset));
			
				//TODO move this to CloudStorage
				//TODO choose better source storage
				var b = new uint8[size];
				ret = (int)stream.input_stream.read (b);
				Memory.copy (buffer, b, size);
			
				if (ret < 0)
					Logger.error<CloudFilePart> ("Failed Read: %s (%u)".printf (Uuid, fh));
			
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Read Error: %s".printf (e.message));
				return -EIO;
			}
			
			return ret;
		}
		
		public int write (uint32 fh, char* buffer, size_t size, off_t offset)
		{
			var stream = streams.get (fh);
			if (stream == null)
				return -EIO;
			
			// Only sequential writing!
			//FIXME they even should be equal
			//if (new_size > offset)
			//	return -EIO;
			
			try {
				Logger.error<CloudFilePart> ("Write: %s %i bytes at %i".printf (Uuid, (int)size, (int)offset - (int)Offset));
				if (stream.can_seek ()) {
					if (stream.tell () != offset - Offset)
						stream.seek (offset - (int64)Offset, SeekType.SET, null);
				}
				//TODO move this to CloudStorage
				var b = new uint8[size];
				Memory.copy (b, buffer, size);
				stream.output_stream.write (b);
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Write Error: %s (%u) %s".printf (Name, fh, e.message));
				return -EIO;
			}
			
			if ((new_size < offset + size) && (new_size == 0 || new_size > Size))
				new_size = offset + size;
			return (int)size;
		}
		
		public int close (uint32 fh)
		{
			int ret = 0;
			
			var stream = streams.get (fh);
			if (stream == null)
				return -EIO;
			
			Logger.error<CloudFilePart> ("Close: %s (%u)".printf (Uuid, fh));
			
			try {
				stream.output_stream.flush ();
				stream.close ();
			} catch (Error e) {
				Logger.error<CloudFilePart> ("Close Error: %s".printf (e.message));
				ret = -EIO;
			}
			
			if (Size != new_size)
				Size = new_size;
			
			streams.unset (fh, null);

			return ret;
		}

		void close_all ()
		{
			foreach (var fh in streams.keys)
				close (fh);
			
			if (streams.size > 0)
				Logger.error<CloudFile> ("Inconsitent Close All: %s".printf (Name));
			streams.clear ();
		}

		public override void delete ()
		{
			close_all ();
			
			foreach (string storage in Storages.split (";")) {
				if (storage.length == 0)
					continue;
				
				try {
					var targetfile = Paths.UserCacheFolder.get_child ("storages").get_child (storage).get_child (Uuid);
					if (targetfile.query_exists (null))
						targetfile.delete (null);
			
				} catch (Error e) {
					Logger.error<CloudFilePart> ("Delete Error: %s %s".printf (Name, e.message));
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
