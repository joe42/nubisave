from cloudfusion.store.dropbox.file_decorator import NonclosingFile
import os, sys, stat,  time
from errno import ENOENT
from cloudfusion.fuse import FUSE, FuseOSError, Operations, LoggingMixIn
from cloudfusion.store.dropbox.dropbox_store import DropboxStore
import tempfile

# Specify what Fuse API use: 0.2
#fuse.fuse_python_api = (0, 2)

def zstat():
    now = time.time()
    st = {}
    st['st_mode'] = 0
    st['st_ino']  = 0
    st['st_dev']  = 0
    st['st_nlink']= 1
    st['st_uid']  = os.getuid()
    st['st_gid']  = os.getgid()
    st['st_size'] = 0
    st['st_atime']= now
    st['st_mtime']= now
    st['st_ctime']= now
    return st

class PyFuseBox(Operations):
    def __init__(self, path):
        self.root = path
        self.temp_file = None
        self.io_api = DropboxStore()
        self.f = open('fuselog', 'w')

        #io_api.store_file(file, root_dir)
    def getattr(self, path, fh=None):
        self.f.write( "getattr "+path+"\n")
        st = zstat()
        try:
            metadata = self.io_api._get_metadata(path)
        except: 
            raise FuseOSError(ENOENT)
        st['st_atime']= metadata['modified']
        st['st_mtime']= metadata['modified']
        st['st_ctime']= metadata['modified']
        if metadata['is_dir']:
            self.f.write( " isDir\n")
            st['st_mode'] = 0777 | stat.S_IFDIR
            st['st_nlink']=2
            st['st_size'] = 1
        else:
            self.f.write( " isFile\n")
            st['st_mode'] = 0777 | stat.S_IFREG
            st['st_size'] = metadata['bytes']
        return st
    
    def open(self, path, flags):
        self.f.write( "open "+path+"\n")
        self.temp_file = tempfile.SpooledTemporaryFile()
        if self.io_api.exists(path):
            file = self.io_api.get_file(path)
            self.temp_file.write(file)
            self.temp_file.seek(0)
        self.io_api.store_fileobject(self.temp_file,path)
        return 0
    
    def truncate(self, path, length, fh=None):
        self.f.write( "truncate %s to %s\n" % (path, length))
        self.io_api.delete(path)
        self.temp_file = tempfile.SpooledTemporaryFile()
        self.io_api.store_fileobject(self.temp_file,path)
        return 0
    
    def rmdir(self, path):
        self.f.write( "rmdir %s\n" % (path))
        self.io_api.delete(path)
        
    def mkdir(self, path, mode):
        self.f.write( "mkdir %s with mode: %s\n" % (path, str(mode)))
        self.io_api.create_directory(path)

    def statfs(self, path):
        """ This implementation should be looked at by a linux guru, since I have little experience concerning filesystems. """
        ret = {}
        ret['f_bsize'] = 4096 #Preferred file system block size.
        ret['f_bavail'] = int( self.io_api.get_free_space() / ret['f_bsize'] ) #Free blocks available to non-super user.
        ret['f_bfree'] = int( self.io_api.get_free_space() / ret['f_bsize'] ) #Total number of free blocks.
        ret['f_blocks'] = int( self.io_api.get_overall_space() / ret['f_bsize'] ) #Total number of blocks in the filesystem.
        ret['f_favail'] = 810280 #Free nodes available to non-super user -- not sure about this
        ret['f_ffree'] = ret['f_favail'] #Total number of free file nodes.
        ret['f_files'] = 810280 #Total number of file nodes -- not sure about this
        ret['f_flag'] = 4096 #Flags. System dependent: see statvfs() man page.
        ret['f_frsize'] = 4096 #Fundamental file system block size.
        ret['f_namemax'] = 255 #Maximum file name length.
        return ret
    
    def rename(self, old, new):
        self.f.write( "rename %s to %s\n" % (old, new))
        self.io_api.move(old, new)

    def create(self, path, mode):
        self.f.write( "create %s with mode %s\n" % (path, str(mode)))
        self.temp_file = tempfile.SpooledTemporaryFile()
        self.io_api.store_fileobject(self.temp_file, path)
        return 0
        """       self.files[path] = dict(st_mode=(S_IFREG | mode), st_nlink=1,
            st_size=0, st_ctime=time(), st_mtime=time(), st_atime=time())
        self.fd += 1
        return self.fd
    def truncate(self, path, length, fh=None):
        self.data[path] = self.data[path][:length]
        self.files[path]['st_size'] = length"""
    
    def unlink(self, path):
        self.f.write( "unlink %s\n" % path)
        self.io_api.delete(path)

    def read(self, path, size, offset, fh):
        self.f.write( "read %s bytes from %s at %s - fh %s\n" % (size, path, offset, fh))
        file = self.io_api.get_file(path)
        #file.seek(offset)
        return  file[offset: offset+size]

    def write(self, path, buf, offset, fh):
        self.f.write( "write %s ... from %s at %s - fh: %s\n" % (path, buf[0:10], offset, fh))
        self.temp_file = tempfile.SpooledTemporaryFile()
        file = self.io_api.get_file(path)
        self.temp_file.write(file)
        self.temp_file.seek(offset)
        self.temp_file.write(buf)
        self.temp_file.seek(0)
        self.io_api.store_fileobject(self.temp_file,path)
        return len(buf)
    
    def flush(self, path, fh):
        self.f.write( "flush %s - fh: %s\n" % (path, fh))
        #self.io_api.store_fileobject(fh,path)
        return 0
    
    def release(self, path, fh):
        self.f.write( "release %s - fh: %s\n" % (path, fh))
        #self.temp_file.close()
        return 0
       
    def readdir(self, path, fh):
        self.f.write( "readdir "+path+"\n")
        directories = self.io_api.get_directory_listing(path)
        #self.f.write( "readdir -> "+str(directories)+"\n")
        file_objects = [".", ".."]
        for file_object in directories:
            if file_object != "/":
                file_object = os.path.basename(file_object)
                file_objects.extend( [os.path.basename(file_object)] )
        return file_objects;

def main():
    if len(sys.argv) != 2:
        print 'usage: %s <mountpoint>' % sys.argv[0]
        exit(1)
    FUSE(PyFuseBox(sys.argv[1]), sys.argv[1], foreground=False, nothreads=True)
    
if __name__ == '__main__':
    main()

