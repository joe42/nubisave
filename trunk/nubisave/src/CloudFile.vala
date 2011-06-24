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
	public enum CLOUDFILE_STATE
	{
		SAVED = 0,
		DELETED,
		UPLOADING,
		ABORTED,
		CORRUPTED,
	}

	public class CloudFile : Preferences
	{
		[Description(nick = "filename", blurb = "The real name of the file.")]
		public string Name { get; set; default = ""; }
		
		[Description(nick = "relativepath", blurb = "The relative path to the file.")]
		public string RelativePath { get; set; default = ""; }

		[Description(nick = "sourcepath", blurb = "The path to the source file.")]
		public string SourcePath { get; set; default = ""; }

		[Description(nick = "parts", blurb = "Ordered list of the uuids representing the file parts.")]
		public string Parts { get; set; default = ""; }
		
		[Description(nick = "ctime", blurb = "The time of creation for this file.")]
		public uint64 CTime { get; set; default = new DateTime.now_utc ().to_unix (); }
		
		//[Description(nick = "status", blurb = "Status of this file")]
		//public CLOUDFILE_STATE Status { get; set; default = CLOUDFILE_STATE.UPLOADING; }
		
		[Description(nick = "filesize", blurb = "The original size of the file.")]
		public uint64 Size { get; set; default = 0; }
		
		[Description(nick = "chunksize", blurb = "The maximum size of a FilePart.")]
		public uint64 ChunkSize { get; set; default = 1024*1024; }
		
		public ArrayList<CloudFilePart> fileparts;
		HashMap<uint32, CloudFilePart> handlers;
		
		uint64 new_size;

		Core core = Core.get_instance ();

		construct
		{
			fileparts = new ArrayList<CloudFilePart> ();
			handlers = new HashMap<uint32, CloudFilePart> ();
		}
		
		public CloudFile (string name)
		{
			base ();
			load (name);
			
			foreach (string uuid in Parts.split (";"))
				if (uuid.length > 0)
					fileparts.add (new CloudFilePart (Name, uuid));
		}

		public CloudFile.from_file (File file)
		{
			base ();
			
			load (file.get_basename ());

			Name = file.get_basename ();
			SourcePath = file.get_path ();
			try {
				var file_info = file.query_info ("*", FileQueryInfoFlags.NONE);
				Size = file_info.get_size ();
			} catch (Error e) {
				Logger.error<CloudFile> (e.message);
			}
			
			Parts = create_cloudparts ();
		}
		
		public CloudFile.from_stream (string name, uint64 chunksize = 1024*1024, uint64 size = 0)
		{
			base ();
			
			load (name);

			Name = name;
			if (Size != size)
				Size = size;
			if (ChunkSize != chunksize)
				ChunkSize = chunksize;
			
			if (size > 0) {
				Parts = create_cloudparts ();
			} else {
				string uuid = Uuid.generate_random ();
				CloudStorage target = core.get_next_target (null);
				if (target == null)
					return;
				
				var filepart = new CloudFilePart (Name, uuid);
				
				//TODO strategy to choose the target storage
				filepart.Storages = target.Name;
				filepart.Offset = 0;
				
				fileparts.add (filepart);
				Parts = cloudparts_to_string ();
			}
		}

		public int open (uint32 fh)
		{
			var filepart = handlers.get (fh);
			if (filepart != null) {
				Logger.error<CloudFile> ("Already Opened (do nothing): %s (%u)".printf (Name, fh));
				return 0;
			}

			Logger.error<CloudFile> ("Open: %s (%u)".printf (Name, fh));
			
			// Dispersion pattern definition:
			//	byte pattern: 8 bits with its target dispersion part
			//	i.e. 2 files : 12121212 , 11221122, 11122212
			//	i.e. 4 files : 12341234 , 11223344, 13142324
			
			//TODO Dispersion here? open fileparts which needs to be processed parallel
			
			filepart = fileparts.first ();
			filepart.open (fh);
			handlers.set (fh, filepart);

			new_size = Size;
			
			return 0;
		}

		public int read (uint32 fh, char* buffer, size_t size, off_t offset)
		{
			var filepart = handlers.get (fh);
			if (filepart == null)
				return -EIO;

			Logger.error<CloudFile> ("Read: %s (%u) %i bytes at %i".printf (Name, fh, (int)size, (int)offset));
			
			int ret = 0;
			size_t read_size = 0;
			char* read_buffer = buffer;
			
			// check if current filepart is the right one and if not search for the proper one
			if (!(offset >= filepart.Offset 
				&& offset < filepart.Offset + filepart.Size)) {
				
				filepart.close (fh);
				handlers.unset (fh, null);
				
				filepart = null;
				foreach (var fp in fileparts)
					if (offset >= fp.Offset && offset < fp.Offset + fp.Size)
						filepart = fp;
				
				if (filepart == null)
					return -EIO;
				
				ret = filepart.open (fh);
				handlers.set (fh, filepart);
			}
			
			do {
				if (ret >= 0)
					ret = filepart.read (fh, read_buffer, size - read_size, offset + read_size);
				
				if (ret >= 0) {
					read_size += ret;
					read_buffer += ret;
				}
				
				if (size <= read_size)
					break;
				
				//FIXME This is a nasty hack
				if (size - read_size < 4096)
					break;
				
				filepart.close (fh);
				handlers.unset (fh, null);
				
				if (ret < 0)
					break;
				
				// find next part
				uint64 next_offset = filepart.Offset + filepart.Size;
				filepart = null;
				foreach (var fp in fileparts)
					if (fp.Offset == next_offset)
						filepart = fp;
				
				if (filepart == null)
					return -EIO;
				
				ret = filepart.open (fh);
				handlers.set (fh, filepart);

			} while (ret >= 0 && read_size < size);
			
			if (ret < 0) {
				Logger.error<CloudFile> ("Failed Read: %s (%u) %i".printf (Name, fh, (int)read_size));
				return -EIO;
			}
			
			return (int)read_size;
		}

		public int write (uint32 fh, char* buffer, size_t size, off_t offset)
		{
			var filepart = handlers.get (fh);
			if (filepart == null)
				return -EIO;

			// Only sequential writing!
			if (Size > offset || new_size > offset)
				return -EIO;
			
			int ret = 0;
			
			//TODO Encryption, Dispersion here?
			
			//TODO Handle Error
			// use another target? -> fix filepart settings

			// Check if filepart is "full"
			if (ChunkSize >= filepart.current_size () + size) {
				ret = filepart.write (fh, buffer, size, offset);
				
			} else {
				filepart.close (fh);
				handlers.unset (fh, null);
				
				filepart = null;
				foreach (var fp in fileparts)
					if (offset >= fp.Offset && offset < fp.Offset + fp.Size)
						filepart = fp;
				
				if (filepart == null) {
					string uuid = Uuid.generate_random ();
					CloudStorage target = core.get_next_target (this);
					if (target == null)
						return -EIO;
					
					filepart = new CloudFilePart (Name, uuid);
					//TODO RAIC here? choose the proper target storages for this part
					filepart.Storages = target.Name;
					filepart.Offset = offset;
					fileparts.add (filepart);
					Parts = cloudparts_to_string ();
				}
				
				ret = filepart.open (fh);
				handlers.set (fh, filepart);
				
				if (ret >= 0) {
					ret = filepart.write (fh, buffer, size, offset);
				}
			}
				
			if (ret < 0) {
				Logger.error<CloudFile> ("Failed Write: %s (%u) %i".printf (Name, fh, (int)size));
				return ret;
			}
			
			if ((new_size == 0 || new_size > Size)
				&& new_size < offset + size)
				new_size = offset + size;
			
			return (int) size;
		}
		
		public int close (uint32 fh)
		{
			var filepart = handlers.get (fh);
			if (filepart == null)
				return -EIO;

			Logger.error<CloudFile> ("Close: %s (%u)".printf (Name, fh));

			filepart.close (fh);
			handlers.unset (fh, null);

			if (new_size > Size)
				Size = new_size;

			//save ();
			
			return 0;
		}
		
		void close_all ()
		{
			Logger.error<CloudFile> ("Close All: %s".printf (Name));

			foreach (var fh in handlers.keys)
				close (fh);
			
			if (handlers.size > 0)
				Logger.error<CloudFile> ("Inconsitent Close All: %s".printf (Name));
			handlers.clear ();
		}
		
		public override void delete ()
		{
			close_all ();
			
			foreach (var filepart in fileparts)
				filepart.delete ();
				
			fileparts.clear ();
			
			base.delete ();
		}

		string cloudparts_to_string ()
		{
			string parts = "";
			
			foreach (var filepart in fileparts)
				parts += filepart.Uuid + ";";
			
			return parts;
		}

		string create_cloudparts ()
		{
			string parts = "";
			
			uint64 length = Size;
			while (length > 0) {
				string uuid = Uuid.generate_random ();
				var filepart = new CloudFilePart (Name, uuid);
				filepart.Offset = Size - length;
				if (length > ChunkSize) {
					filepart.Size = ChunkSize;
					length -= ChunkSize;
					parts += uuid + ";";
				} else {
					filepart.Size = length;
					length = 0;
					parts += uuid;
				}
				
				fileparts.add (filepart);
			}
			
			return parts;
		}
		
		public void load (string name)
		{
			if (name.has_suffix (".cloudfile"))
				init_from_file ("files/" + name);
			else
				init_from_file ("files/" + name + ".cloudfile");
		}
		
		public string to_string ()
		{
			return "Name: " + Name + "\nSize: " + Size.to_string () + "\nParts: " + Parts + "\n";
		}		
	}
}
