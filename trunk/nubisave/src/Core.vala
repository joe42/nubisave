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

using GLib;
using Gee;

namespace NubiSave
{
	public class Core : GLib.Object
	{
		public ArrayList<CloudFile> files = new ArrayList<CloudFile> ();
		public ArrayList<CloudFilePart> fileparts = new ArrayList<CloudFilePart> ();
		public ArrayList<CloudStorage> storages = new ArrayList<CloudStorage> ();

		private static Core instance = null;
		public static Core get_instance ()
		{
			if (instance == null)
				return new Core ();
			return instance;
		}
		
		private Core ()
		{
			// Check folders
			// Configuration
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("files"));
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("fileparts"));
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("storages"));
			// Temporary stuff
			Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages"));
			
			// Load existing storage and file information
			load_storage_information ();
			load_file_information ();
			
			debug_output ();
		}

		void load_storage_information ()
		{
			storages.clear ();
			try {
				var directory = Paths.UserConfigFolder.get_child ("storages");
				var enumerator = directory.enumerate_children (FILE_ATTRIBUTE_STANDARD_NAME, 0);

				FileInfo fileinfo;
				while ((fileinfo = enumerator.next_file ()) != null) {
					var storage = new CloudStorage (fileinfo.get_name ());
					Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages").get_child (storage.Name));
					storages.add (storage);
				}
			} catch (Error e) {
				Logger.error<Core> (e.message);
			}
		}
		
		void load_file_information ()
		{
			if (storages.size <= 0)
				return;
			
			files.clear ();
			fileparts.clear ();
			
			try {
				var directory = Paths.UserConfigFolder.get_child ("files");
				var enumerator = directory.enumerate_children (FILE_ATTRIBUTE_STANDARD_NAME, 0);
				
				FileInfo fileinfo;
				while ((fileinfo = enumerator.next_file ()) != null) {
					var file = new CloudFile (fileinfo.get_name ());
					files.add (file);
					fileparts.add_all (file.FileParts);
				}
				
			} catch (Error e) {
				Logger.error<Core> (e.message);
			}
		}
		
		void debug_output ()
		{
			Logger.debug<Core> ("Current Storagelist");
			foreach (var storage in storages)
				print ("%s\n", storage.to_string ());
			
			Logger.debug<Core> ("Current Filelist");
			foreach (var file in files)
				print ("%s\n", file.to_string ());
			foreach (var filepart in fileparts)
				print ("%s\n", filepart.to_string ());
		}
	}
}
