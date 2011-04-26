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
        self.f.write( "getattr "+path)
        st = zstat()
        if path == "/":
            self.f.write( " isDir\n")
            st['st_mode']= 0777 | stat.S_IFDIR
            st['st_nlink']=2
            st['st_size'] = 1
            return st
        try:
            metadata = self.io_api._get_metadata(path)
        except: 
            raise FuseOSError(ENOENT)
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
        self.temp_file = tempfile.SpooledTemporaryFile()
        self.nonclosing_temp_file = NonclosingFile(self.temp_file)
        self.f.write( "open "+path+"\n")
        try:
            file = self.io_api.get_file(path)
            for line in file:
                self.nonclosing_temp_file.write(line)
            return self.nonclosing_temp_file
        except:
            return 0
    
    """def rmdir(self, path):
        self.files.pop(path)
        self.files['/']['st_nlink'] -= 1"""

    """def statfs(self, path):
        return dict(f_bsize=512, f_blocks=4096, f_bavail=2048) """
    """def rename(self, old, new):
        self.files[new] = self.files.pop(old)"""

    def create(self, path, mode):
        self.f.write( "create "+path+"\n")
        self.temp_file = tempfile.SpooledTemporaryFile()
        # use non closing file as parameter for next function since store_fileobject will close the file handle 
        self.nonclosing_temp_file = NonclosingFile(self.temp_file)
        self.io_api.store_fileobject(self.temp_file, path)
        return self.nonclosing_temp_file
        """       self.files[path] = dict(st_mode=(S_IFREG | mode), st_nlink=1,
            st_size=0, st_ctime=time(), st_mtime=time(), st_atime=time())
        self.fd += 1
        return self.fd
    def truncate(self, path, length, fh=None):
        self.data[path] = self.data[path][:length]
        self.files[path]['st_size'] = length"""
    
    """def unlink(self, path):
        self.files.pop(path)"""

    def read(self, path, size, offset, fh):
        self.f.write( "read "+path+"\n")
        file = self.io_api.get_file(path)
        #file.seek(offset)
        return file.read(size)

    def write(self, path, buf, offset, fh):
        self.f.write( "write "+path+"\n")
        #fh.seek(offset)
        fh.write(buf)
        return len(buf)
    
    def flush(self, path, fh):
        self.f.write( "flush "+path+"\n")
        self.io_api.store_fileobject(fh,path)
        return fh
    
    def release(self, path, fh):
        self.temp_file.close()
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

