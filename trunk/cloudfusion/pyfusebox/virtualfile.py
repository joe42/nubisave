'''
Created on 24.08.2011

@author: joe
'''
import os
from cloudfusion.pyfusebox.pyfusebox import zstat
import stat
  
class VirtualFile(object):
    INITIAL_TEXT="""
Some virtual Text.
"""
    def __init__(self, path):
        self.path = path
        self.stats = zstat()
        self.text = self.INITIAL_TEXT
        self.stats['st_mode'] = 0777 | stat.S_IFREG
    def getattr(self):
        self.stats['st_size'] = len(self.text)
        self.stats['st_blocks'] = (int) ((self.stats['st_size'] + 4095L) / 4096L);
        return self.stats
    def truncate(self):
        self.text = ''
    def get_text(self):
        return self.text
    def get_path(self):
        return self.path
    def __str__(self):
        return self.path
    def read(self, size, offset):
        return self.text[offset: offset+size]
    def write(self, buf, offset):
        self.text_tmp = self.text[:offset]+buf+self.text[len(buf)+offset:] 
        return len(buf)
    def get_dir(self):
        return os.path.dirname(self.path)
    def get_name(self):
        return os.path.basename(self.path)