'''
Created on 16.03.2012

@author: joe
'''
#!/usr/bin/env python

from __future__ import with_statement
from time import sleep
import time
from errno import EACCES
from os.path import realpath
from sys import argv, exit
from threading import Lock

import os

from fuse import FUSE, FuseOSError, Operations, LoggingMixIn


class Loopback(LoggingMixIn, Operations):    
    def __init__(self, root, read_bandwidth, write_bandwidth, bit_error_per_GB):
        self.root = realpath(root)
        self.rwlock = Lock()
        self.bit_error_per_GB = bit_error_per_GB
        if bit_error_per_GB != 0:
            self.one_error_every_x_KB = 1000000.0  / bit_error_per_GB #one error every x KB
        self.read_bandwidth  =read_bandwidth #KB/s
        self.write_bandwidth = write_bandwidth #KB/s
        self.bytes_without_error_written = 0 #the number of bytes that was written since the last error was introduced
        self.bytes_written = 0
        self.bytes_read = 0
        self.write_next_second = 0 
        self.read_next_second = 0 
    
    def __call__(self, op, path, *args):
        return super(Loopback, self).__call__(op, self.root + path, *args)
    
    def access(self, path, mode):
        if not os.access(path, mode):
            raise FuseOSError(EACCES)
    
    chmod = os.chmod
    chown = os.chown
    
    def create(self, path, mode):
        return os.open(path, os.O_WRONLY | os.O_CREAT, mode)
    
    def flush(self, path, fh):
        return os.fsync(fh)

    def fsync(self, path, datasync, fh):
        return os.fsync(fh)
                
    def getattr(self, path, fh=None):
        st = os.lstat(path)
        return dict((key, getattr(st, key)) for key in ('st_atime', 'st_ctime',
            'st_gid', 'st_mode', 'st_mtime', 'st_nlink', 'st_size', 'st_uid'))
    
    getxattr = None
    
    def link(self, target, source):
        return os.link(source, target)
    
    listxattr = None
    mkdir = os.mkdir
    mknod = os.mknod
    open = os.open
        
    def read(self, path, size, offset, fh):
        with self.rwlock:
            if self.read_next_second == 0:
                self.read_next_second=time.time()+0.01
            self.bytes_read += size
            if self.bytes_read >= self.read_bandwidth*1000:
                self.bytes_read -= self.read_bandwidth*1000
                while time.time()<self.read_next_second:
                    sleep(0.0001)
                self.read_next_second=time.time()+0.01
            os.lseek(fh, offset, 0)
            return os.read(fh, size)
    
    def readdir(self, path, fh):
        return ['.', '..'] + os.listdir(path)

    readlink = os.readlink
    
    def release(self, path, fh):
        return os.close(fh)
        
    def rename(self, old, new):
        return os.rename(old, self.root + new)
    
    rmdir = os.rmdir
    
    def statfs(self, path):
        stv = os.statvfs(path)
        return dict((key, getattr(stv, key)) for key in ('f_bavail', 'f_bfree',
            'f_blocks', 'f_bsize', 'f_favail', 'f_ffree', 'f_files', 'f_flag',
            'f_frsize', 'f_namemax'))
    
    def symlink(self, target, source):
        return os.symlink(source, target)
    
    def truncate(self, path, length, fh=None):
        with open(path, 'r+') as f:
            f.truncate(length)
    
    unlink = os.unlink
    utimens = os.utime
    
    def write(self, path, data, offset, fh):
        with self.rwlock:
            if self.write_next_second == 0:
                self.write_next_second=time.time()+0.01
            if self.bit_error_per_GB != 0:
                self.bytes_without_error_written += len(data)
                if self.bytes_without_error_written >= self.one_error_every_x_KB*1000:
                    data = data[:-1]+self.toggle_a_bit(data[-1])
                    self.bytes_without_error_written -= self.one_error_every_x_KB*1000
            self.bytes_written += len(data)
            if self.bytes_written >= self.write_bandwidth*10:
                self.bytes_written -= self.write_bandwidth*10
                #actual time needed for write is neglected
                while time.time()<self.write_next_second:
                    sleep(0.0001)
                self.write_next_second=time.time()+0.01
            os.lseek(fh, offset, 0)
            return os.write(fh, data)
    
    def toggle_a_bit(self, character):
        return chr(ord(character) ^ 1)

if __name__ == "__main__":
    if len(argv) != 6:
        print 'usage: %s <root> <mountpoint> <read_bandwidth_in_KBs> <write_bandwidth_in_KBs> <bit_error_per_GB>' % argv[0]
        exit(1)
    print argv[3]
    fuse = FUSE(Loopback(argv[1], int(argv[3]), int(argv[4]), int(argv[5])), argv[2], foreground=False)
