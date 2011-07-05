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
using GLib;
using Fuse;
using Posix;

namespace NubiSave
{
	private Core core = null;
	
	private string mountpath;
	
	private HashMap<uint32, CloudFile> handles;
	private HashMap<uint32, CloudFilePart> parthandles;
	
	public const string CONFIG_PATH = ".config";
	public const string PARTS_PATH = ".parts";
	public const string STATS_PATH = ".stats";
	public const string STORAGES_PATH = ".storages";

	public int getattr (string path, Posix.Stat *stbuf)
	{
		if (core == null || path == null)
			return -ENOENT;
		
		Logger.debug<Object> ("getattr: %s".printf (path));

		time_t now = (time_t) new DateTime.now_utc ().to_unix ();
		Memory.set ((void *) stbuf, 0, sizeof (Posix.Stat));

		// Set common attributes
		stbuf->st_ctime = stbuf->st_mtime = stbuf->st_atime = now;
		stbuf->st_uid = getuid ();
		stbuf->st_gid = getgid ();
		stbuf->st_nlink = 1;
		
		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];
		
		if (path == "") {
			stbuf->st_mode = S_IFDIR | 0755;
			stbuf->st_nlink = 6; // subfolder-count + . + ..
			return 0;
			
		} else if (path == CONFIG_PATH || path == PARTS_PATH || path == STATS_PATH || path == STORAGES_PATH) {
			// Virtual hidden subfolders
			stbuf->st_mode = S_IFDIR | 0555;
			stbuf->st_nlink = 2;
			return 0;
			
		} else if (path.has_prefix (CONFIG_PATH)) {
			stbuf->st_mode = S_IFIFO | 0444;
			return 0;
			
		} else if (path.has_prefix (STATS_PATH)) {
			return 0;
			
		} else if (path.has_prefix (PARTS_PATH)) {
			// Search in Parts
			// TODO sorting with folders for every connected file 
			//      or link directly to webspace file
			foreach (var f in core.fileparts)
				if (path == f.Uuid) {
					stbuf->st_mode = S_IFREG | 0444;
					stbuf->st_size = (size_t) f.Size;
					stbuf->st_ctime = stbuf->st_mtime = stbuf->st_atime = (time_t) f.CTime;
					return 0;
				}
		} else if (path.has_prefix (STORAGES_PATH)) {
			// Search in Storages
			foreach (var f in core.storages)
				if (path.has_suffix (f.Name)) {
					stbuf->st_mode = S_IFREG | 0444;
					stbuf->st_size = (size_t) 0;
					return 0;
				}
				
		} else {
			// Search in Files
			foreach (var f in core.files)
				if (path == f.Name) {
					stbuf->st_mode = S_IFREG | 0666;
					stbuf->st_size = (size_t) f.Size;
					stbuf->st_ctime = stbuf->st_mtime = stbuf->st_atime = (time_t) f.CTime;
					return 0;
				}
		}	
		
		// Path doesn't exist
		return -ENOENT;
	}

	public int readdir (string path, void *buf, FillDir filler, off_t offset, ref Fuse.FileInfo fi)
	{
		if (core == null || path == null)
			return -ENOENT;
		
		Logger.debug<Object> ("readdir: %s".printf (path));

		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];
		
		filler (buf, ".", null, 0);
		filler (buf, "..", null, 0);

		if (path == "") {
			filler (buf, CONFIG_PATH, null, 0);
			filler (buf, PARTS_PATH, null, 0);
			filler (buf, STATS_PATH, null, 0);
			filler (buf, STORAGES_PATH, null, 0);
			foreach (var file in core.files)
				filler (buf, file.Name, null, 0);
				
		} else if (path == CONFIG_PATH) {
			
		} else if (path == PARTS_PATH) {
			foreach (var filepart in core.fileparts)
				filler(buf, filepart.Uuid, null, 0);
				
		} else if (path == STATS_PATH) {
			
		} else if (path == STORAGES_PATH) {
			foreach (var storage in core.storages)
				filler(buf, storage.Name, null, 0);
		}
		
		return 0;
	}
/*
	public int mknod (string path, mode_t mode, dev_t rdev)
	{
		Logger.debug<Object> ("mknod: %s".printf (path));

		int retstat = 0;
		
		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];

		if (S_ISREG (mode)) {
			//var file = new CloudFile.from_stream (path);
			//core.files.add (file);
		} else if (S_ISFIFO (mode)) {
			
		}
		
		return retstat;
	}
*/
	public int create (string path, mode_t mode, ref Fuse.FileInfo fi)
	{
		Logger.debug<Object> ("create: %s".printf (path));
		
		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];

		if (S_ISREG (mode)) {
			var file = new CloudFile.from_stream (path);
			core.files.add (file);
			
			do {
				fi.fh = GLib.Random.int_range (0, int32.MAX);
			} while (handles.has_key ((uint32)fi.fh));
			Logger.debug<Object> ("create: fh = %I".printf (fi.fh));

			handles.set ((uint32)fi.fh, file);
			
			return open (path, ref fi);
			
		} else if (S_ISFIFO (mode)) {
			
		}
		
		return -EIO;
	}

	public int unlink (string path)
	{
		Logger.debug<Object> ("unlink (delete): %s".printf (path));
		
		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];

		// Only files can be deleted
		foreach (var f in core.files)
			if (path == f.Name) {
				core.fileparts.remove_all (f.fileparts);
				core.files.remove (f);
				f.delete ();
				return 0;
			}
		
		return -EIO;
	}

	public int utimens (string path, timespec[] ts)
	{
		Logger.debug<Object> ("utimens: %s".printf (path));
		return -ENOSYS;
	}

	public int open (string path, ref Fuse.FileInfo fi)
	{
		Logger.debug<Object> ("open: %s".printf (path));
		
		string[] pparts = path.split ("/", 2);
		if (pparts.length > 1)
			path = pparts[1];

		var file = handles.get ((uint32)fi.fh);
		if (file == null) {
			foreach (var f in core.files) {
				if (f.Name == path) {
					file = f;
					fi.fh = GLib.Random.int_range (0, int32.MAX);		
					handles.set ((uint32)fi.fh, file);
					break;
				}
			}
		}
		
		file.open ((uint32)fi.fh);
		
		return 0;
	}

	public int read (string path, char *buf, size_t size, off_t offset, ref Fuse.FileInfo fi)
	{
		Logger.debug<Object> ("read: %s %i at %i".printf (path, (int)size, (int)offset));
		
		var file = handles.get ((uint32)fi.fh);
		if (file == null)
			return -EIO;
		
		int ret = file.read ((uint32)fi.fh, buf, size, offset);

		if (ret >= 0)
			return ret;
		return -EIO;
	}

	public int write (string path, char *buf, size_t size, off_t offset, ref Fuse.FileInfo fi)
	{
		Logger.debug<Object> ("write: %s %i".printf (path, (int)size));

		var file = handles.get ((uint32)fi.fh);
		if (file == null)
			return -EIO;
		
		int ret = file.write ((uint32)fi.fh, buf, size, offset);

		if (ret >= 0)
			return ret;
		return -EIO;
	}

	public int release (string path, ref Fuse.FileInfo fi)
	{
		Logger.debug<Object> ("release: %s".printf (path));
		
		CloudFile file;
		if (!handles.unset ((uint32)fi.fh, out file) || file == null)
			return -EIO;
			
		file.close ((uint32)fi.fh);
		
		return 0;
	}
	
	protected static bool DEBUG = true;
	protected static bool WIPE = false;
	protected static bool TEST = false;
			
	protected const OptionEntry[] options = {
		{ "debug", 'd', 0, OptionArg.NONE, out DEBUG, "Enable debug logging", null },
		{ "test", 't', 0, OptionArg.NONE, out TEST, "Create sample configuration includes a WIPE", null },
		{ "wipe", 'w', 0, OptionArg.NONE, out WIPE, "Delete configuration files", null },
		{ null }
	};
	
	static int main (string [] args)
	{
		Logger.initialize ("nubisave");
		
		var context = new OptionContext ("");
		context.set_summary ("NubiSave - Secure usage of Cloud-Storage-Providers\nDEVELOPMENT VERSION - HIGHLY UNSTABLE");
		context.add_main_entries (options, null);
		
		try {
			context.parse (ref args);
		} catch { }
		
		if (DEBUG)
			Logger.DisplayLevel = LogLevel.DEBUG;
		else
			Logger.DisplayLevel = LogLevel.INFO;
			
		Logger.debug<Object> ("Start up");

		Logger.debug<Core> ("Initialize folders");
		Paths.initialize ("nubisave");
		
		Core.create_instance ();
		core = Core.get_instance ();

		if (WIPE) {
			core.wipe ();
			return 0;
		}

		if (TEST) {
			core.test_config ();
			return 0;
		}
		
		mountpath = Paths.UserMountFolder.get_path ();
		handles = new HashMap<uint32, CloudFile> ();
		parthandles = new HashMap<uint32, CloudFilePart> ();
 		
		var ops = Operations ();
		ops.readdir = readdir;
		ops.getattr = getattr;
		//ops.mknod = mknod;
		ops.create = create;
		ops.utimens = utimens;
		ops.open = open;
		ops.read = read;
		ops.write = write;
		ops.release = release;
		ops.unlink = unlink;

		return Fuse.main (new string [] {args[0], mountpath}, ops, null);
	}

}
