'''
Created on 23.04.2011

@author: joe
'''
class NamableFile(object):
    def __init__(self, file_object, name):
        self.file_object = file_object
        if hasattr(self.file_object, 'closed'):            
            self.closed = file_object.closed
        if hasattr(self.file_object, 'encoding'):            
            self.closed = file_object.encoding
        if hasattr(self.file_object, 'mode'):            
            self.closed = file_object.mode
        if hasattr(self.file_object, 'newlines'):            
            self.closed = file_object.newlines
        self.name = name
        self.softspace = file_object.softspace
    def close(self):
        self.file_object.close()
    def flush(self):
        self.file_object.flush()
    def fileno(self):
        return self.file_object.fileno()
    def isatty(self):
        return self.file_object.isatty()
    def next(self):
        return self.file_object.next()
    def read(self, size):
        return self.file_object.read(size)
    def readline(self, size):
        return self.file_object.readline(size)
    def readlines(self, sizehint):
        return self.file_object.readlines(sizehint)
    def xreadlines(self):
        return self.file_object.xreadlines()
    def seek(self, offset, whence):
        return self.file_object.seek(offset, whence)
    def tell(self):
        return self.file_object.tell()
    def truncate(self, size):
        self.file_object.truncate(size)
    def write(self, str):
        return self.file_object.write(str)
    def writelines(self, sequence):
        return self.file_object.writelines(sequence)
    
class NonclosingFile(object):
    def __init__(self, file_object):
        self.file_object = file_object
        if hasattr(self.file_object, 'closed'):            
            self.closed = file_object.closed
        if hasattr(self.file_object, 'encoding'):            
            self.closed = file_object.encoding
        if hasattr(self.file_object, 'mode'):            
            self.closed = file_object.mode
        if hasattr(self.file_object, 'name'):            
            self.closed = file_object.name
        if hasattr(self.file_object, 'newlines'):            
            self.closed = file_object.newlines
        self.softspace = file_object.softspace
    def close(self):
        pass;
    def flush(self):
        self.file_object.flush()
    def fileno(self):
        return self.file_object.fileno()
    def isatty(self):
        return self.file_object.isatty()
    def next(self):
        return self.file_object.next()
    def read(self, size):
        return self.file_object.read(size)
    def readline(self, size):
        return self.file_object.readline(size)
    def readlines(self, sizehint):
        return self.file_object.readlines(sizehint)
    def xreadlines(self):
        return self.file_object.xreadlines()
    def seek(self, offset, whence):
        return self.file_object.seek(offset, whence)
    def tell(self):
        return self.file_object.tell()
    def truncate(self, size):
        self.file_object.truncate(size)
    def write(self, str):
        return self.file_object.write(str)
    def writelines(self, sequence):
        return self.file_object.writelines(sequence)


