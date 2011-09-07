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
using NubiSave.IDA;
using Posix;

namespace NubiSave
{
	public class Core : GLib.Object
	{
		public ArrayList<CloudFile> files = new ArrayList<CloudFile> ();
		public ArrayList<CloudFilePart> fileparts = new ArrayList<CloudFilePart> ();
		public ArrayList<CloudStorage> storages = new ArrayList<CloudStorage> ();

		int last_storage = 0;

		private static Core instance = null;
		public static Core get_instance ()
		{
			return instance;
		}
		
		public static void create_instance ()
		{
			if (instance == null) {
				instance = new Core ();
			}
		}

		private Core ()
		{
			//Initialize IDA stuff
			InitField.init ();
			//test_ida ();

			// Check folders
			// Configuration
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("files"));
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("fileparts"));
			Paths.ensure_directory_exists (Paths.UserConfigFolder.get_child ("storages"));
			// Temporary stuff
			Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages"));
			
			// Load existing storage and file information
			// do storages first !!!
			load_storage_information ();
			load_file_information ();
			
			debug_output ();
		}

		public void apply_config (uint8[] data)
		{
			try {
				KeyFile file = new KeyFile ();
				file.load_from_data ((string) data, data.length, 0);

				var group_name = "CloudStorage";
				if (!file.has_group (group_name))
					return;
					
				var keys = file.get_keys (group_name);
				foreach (var key in keys)	
					Logger.debug<Core> (key + " = " + file.get_value (group_name, key));
				
			} catch (KeyFileError e) {
			
			}			
		}

		// simple round robbin storage chooser
		//TODO RAIC here? choose the proper target storages for this part
		public CloudStorage? get_next_target (CloudFile? file)
		{
			if (storages.size == 0)
				return null;
			
			int next_storage = (last_storage + 1) % storages.size;
			last_storage = next_storage;
			return storages.get (next_storage);
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
					fileparts.add_all (file.fileparts);
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
		
		public void test_config ()
		{
			wipe ();
			
			try {
				//TODO add real credentials for testing
				var storage = new CloudStorage ("storage01");
				storage.Name = "storage01";
				storage.Module = "dropbox";
				storage.Username = "";
				storage.Password = "";
				Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages").get_child (storage.Name));
				storages.add (storage);

				storage = new CloudStorage ("storage02");
				storage.Name = "storage02";
				storage.Module = "sugarsync";
				storage.Username = "";
				storage.Password = "";
				Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages").get_child (storage.Name));
				storages.add (storage);

				storage = new CloudStorage ("storage03");
				storage.Name = "storage03";
				storage.Module = "";
				storage.Username = "";
				storage.Password = "";
				Paths.ensure_directory_exists (Paths.UserCacheFolder.get_child ("storages").get_child (storage.Name));
				storages.add (storage);

				Logger.debug<Core> ("Sample storage configuration was created...");
				
			} catch (Error e) {
				Logger.error<Core> (e.message);
			}
		}

		public void wipe ()
		{
			foreach (var f in files)
				f.delete ();
			files.clear ();

			foreach (var fp in fileparts)
				fp.delete ();
			fileparts.clear ();

			foreach (var s in storages)
				s.delete ();
			storages.clear ();
			
			Logger.debug<Core> ("All data has been wiped!");
		}
		
		void test_ida ()
		{
			try {
				Logger.debug<Core> ("IDA Test");

				var ida = new CauchyInformationDispersalCodec (6, 5, 256);
				
				var input = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

				Logger.debug<Core> (input);

				Logger.debug<Core> (input.length.to_string () + " > " + ida.getDispersedSize (input.length).to_string ());
							
				var data = new ByteArray ();
				data.append (input.data);
	
				var encode = ida.getEncoder ().process (data);
				Logger.debug<Core> (encode.size.to_string ());
				var decode = ida.getDecoder ().process (encode);
				Logger.debug<Core> ((string)decode.data);
				
			} catch (IDAError e) {
				Logger.error<Core> ("%s".printf (e.message));
			}
		}
	}
}
