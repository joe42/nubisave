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
		
		public ArrayList<CloudFilePart> FileParts;
		CloudFilePart current_read_filepart;
		CloudFilePart current_write_filepart;
		
		uint64 new_size;

		Core core = Core.get_instance ();

		construct
		{
			FileParts = new ArrayList<CloudFilePart> ();
		}
		
		public CloudFile (string name)
		{
			base ();
			load (name);
			
			foreach (string uuid in Parts.split (";"))
				if (uuid.length > 0)
					FileParts.add (new CloudFilePart (Name, uuid));
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
				
				FileParts.add (filepart);
				Parts = cloudparts_to_string ();
			}
		}

		public int open ()
		{
			if (current_read_filepart != null || current_write_filepart != null)
				return -EIO;

			Logger.error<CloudFile> ("Open: %s".printf (Name));
			
			// Dispersion pattern definition:
			//	byte pattern: 8 bits with its target dispersion part
			//	i.e. 2 files : 12121212 , 11221122, 11122212
			//	i.e. 4 files : 12341234 , 11223344, 13142324
			
			//TODO Dispersion here? open fileparts which needs to be processed parallel
			
			var current_filepart = FileParts.first ();
			current_filepart.open ();

			current_read_filepart = current_write_filepart = current_filepart;
			new_size = Size;
			
			return 0;
		}

		public int read (char* buffer, size_t size, off_t offset)
		{
			if (current_read_filepart == null)
				return -EIO;
			
			Logger.error<CloudFile> ("Read: %s %i at %i".printf (Name, (int)size, (int)offset));
			
			int ret = 0;
			size_t read_size = 0;
			char* read_buffer = buffer;
			
			// check if current filepart is the right one and if not search for the proper one
			if (!(offset >= current_read_filepart.Offset 
				&& offset < current_read_filepart.Offset + current_read_filepart.Size)) {
				
				if (current_read_filepart != current_write_filepart)
					current_read_filepart.close ();
				
				current_read_filepart = null;
				foreach (var filepart in FileParts)
					if (offset >= filepart.Offset && offset < filepart.Offset + filepart.Size)
						current_read_filepart = filepart;
				
				if (current_read_filepart == null)
					return -EIO;
				
				if (current_read_filepart != current_write_filepart)
					ret = current_read_filepart.open ();
			}
			
			do {
				if (ret >= 0)
					ret = current_read_filepart.read (read_buffer, size - read_size, offset + read_size);
				
				if (ret >= 0) {
					read_size += ret;
					read_buffer += ret;
				}
				
				if (size <= read_size)
					break;
				
				//FIXME This is a nasty hack
				if (size - read_size < 4096)
					break;
				
				if (current_read_filepart != current_write_filepart)
					current_read_filepart.close ();
				
				var tmp_filepart = current_read_filepart;
				current_read_filepart = null;
				
				// find next part
				foreach (var filepart in FileParts)
					if (tmp_filepart.Offset + tmp_filepart.Size == filepart.Offset)
						current_read_filepart = filepart;
				
				if (current_read_filepart == null)
					return -EIO;
				
				if (current_read_filepart != current_write_filepart)
					ret = current_read_filepart.open ();

			} while (ret >= 0 && read_size < size);
			
			if (ret >= 0)
				Logger.error<CloudFile> ("Read: %s %i OK".printf (Name, (int)read_size));
			else
				return -EIO;
			
			return (int)read_size;
		}

		public int write (char* buffer, size_t size, off_t offset)
		{
			if (current_write_filepart == null)
				return -EIO;
			
			// Only sequential writing!
			if (Size > offset || new_size > offset)
				return -EIO;
			
			int ret = 0;
			
			//TODO Encryption, Dispersion here?
			
			//TODO Handle Error
			// use another target? -> fix filepart settings

			// Check if filepart is "full"
			if (ChunkSize >= current_write_filepart.current_size () + size) {
				ret = current_write_filepart.write (buffer, size, offset);
				
			} else {
				if (current_read_filepart != current_write_filepart)
					current_write_filepart.close ();
				
				current_write_filepart = null;
				
				foreach (var filepart in FileParts)
					if (offset >= filepart.Offset && offset < filepart.Offset + filepart.Size)
						current_write_filepart = filepart;
				
				if (current_write_filepart == null) {
					string uuid = Uuid.generate_random ();
					CloudStorage target = core.get_next_target (this);
					if (target == null)
						return -EIO;
					
					current_write_filepart = new CloudFilePart (Name, uuid);
					//TODO RAIC here? choose the proper target storages for this part
					current_write_filepart.Storages = target.Name;
					current_write_filepart.Offset = offset;
					FileParts.add (current_write_filepart);
					Parts = cloudparts_to_string ();
				}
				
				if (current_read_filepart != current_write_filepart)
					ret = current_write_filepart.open ();
				
				if (ret >= 0)
					ret = current_write_filepart.write (buffer, size, offset);
			}
				
			if (ret < 0)
				return ret;
			
			if ((new_size == 0 || new_size > Size)
				&& new_size < offset + size)
				new_size = offset + size;
			
			return (int) size;
		}
		
		public int close ()
		{
			if (current_read_filepart == null && current_write_filepart == null)
				return -EIO;

			if (current_read_filepart == current_write_filepart) {
				if (current_read_filepart != null)
					current_read_filepart.close ();
			} else {
				if (current_read_filepart != null)
					current_read_filepart.close ();
				if (current_write_filepart != null)
					current_write_filepart.close ();
			}
			
			Logger.error<CloudFile> ("Close: %s".printf (Name));

			current_read_filepart = current_write_filepart = null;
			if (new_size > Size)
				Size = new_size;
			
			//save ();
			
			return 0;
		}
		
		public override void delete ()
		{
			close ();
			
			foreach (var filepart in FileParts)
				filepart.delete ();
				
			FileParts.clear ();
			
			base.delete ();
		}

		string cloudparts_to_string ()
		{
			string parts = "";
			
			foreach (var filepart in FileParts)
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
				
				FileParts.add (filepart);
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
