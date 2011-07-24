'''
Created on 25.04.2011

@author: joe
'''
'''
Created on 08.04.2011

@author: joe
'''
    
import tempfile
from cloudfusion.util.cache import Cache
from cloudfusion.store.dropbox.file_decorator import *
from cloudfusion.store.store import Store
import time

"""Wrapped store needs a logger as an attribute called logger """

class CachingStore(Store):
    def __init__(self, store):
        self.logger = store.logger
        self.logger.debug("creating CachingStore object")
        self.store = store
#        self.temp_file = tempfile.SpooledTemporaryFile()
        self.cache = Cache(240)
    
    def _is_valid_path(self, path):
        return self.store._is_valid_path(path)
    
    def _raise_error_if_invalid_path(self, path):
        self.store._raise_error_if_invalid_path(path)
        
    def get_name(self):
        return self.store.get_name()
    
    def _refresh_cache(self, path_to_file):
        """ Refreshes the cache entry with the key :param:`path_to_file` from the wrapped store.
        The modified time stamp of the cache is the wrapped stores modified time stamp.
        :raises: NoSuchFilesytemObjectError if file does not exist in wrapped store
        TODO: exception handling needs to be implemented
        """
        data = self.store.get_file(path_to_file)
        modified = self._get_actual_modified_date(path_to_file)
        self.cache.refresh(path_to_file, data, modified)
    
    def get_file(self, path_to_file):
        if not self.cache.exists(path_to_file):
            self._refresh_cache(path_to_file)
            return self.cache.get_value(path_to_file)
        if self.cache.is_expired(path_to_file): 
            if self.cache.is_dirty(path_to_file): #dirty&expired->flush
                self._flush()
            else: #not dirty&expired->update cache from store
                self.cache.update(path_to_file)
                actual_modified_date = self._get_actual_modified_date(path_to_file)
                cached_modified_date = self.cache[path_to_file]['modified']
                if actual_modified_date > cached_modified_date:
                    self._refresh_cache(path_to_file)
        return self.cache.get_value(path_to_file) #not expired->from cache
    
    def store_file(self, path_to_file, dest_dir="/", remote_file_name = None):
        fileobject = open(path_to_file)
        if not remote_file_name:
            remote_file_name = os.path.basename(path_to_file)
        self.store_fileobject(fileobject, dest_dir + "/" + remote_file_name)
    
    def _get_actual_modified_date(self, path):
        ret = 0
        if self.store.exists(path):
            ret = self.store._get_metadata(path)['modified']
        return ret
                
    def store_fileobject(self, fileobject, path):
        """ Stores a fileobject to the :class:`cloudfusion.util.cache.Cache` and if the existing fileobject has expired it is also stored to the wrapped store
        :param fileobject: The file object with the method read() returning data as a string 
        :param path: The path where the file object's data should be stored, including the filename
        """
        self.logger.debug("cached storing %s" % path)
        self.cache.write(path, fileobject.read())
        if self.cache.is_expired(path):
            self.cache.update(path)
            actual_modified_date = self._get_actual_modified_date(path)
            cached_modified_date = self.cache.get_modified(path)
            if not self.cache.is_dirty(path):
                if actual_modified_date > cached_modified_date:
                    self._refresh_cache(path)
                    self.logger.debug("refreshed fileobject")
            else: #dirty
                if actual_modified_date < cached_modified_date:
                    file = DataFileWrapper(self.cache.get_value(path))
                    self.store.store_fileobject(file, path)
                    self.cache.refresh(path, self.cache.get_value(path), time.time())
                else:
                    self.logger.warning("cannot write fileobject because it was modified on the server side")
                    #throw error since you don't want to override a file that was modified on the server
                

        
            
    def delete(self, path):#delete from metadata 
        self.cache.delete(path)
        if self.store.exists(path):  
            self.store.delete(path)
          
    def account_info(self):
        self.flush()
        return self.store.account_info()
    
    def get_free_space(self):
        self.flush()
        return self.store.get_free_space()
    
    def get_overall_space(self):
        self.flush()
        return self.store.get_overall_space()
    
    def get_used_space(self):
        self.flush()
        return self.store.get_used_space()

    def create_directory(self, directory):
        return self.store.create_directory(directory)
        
    def duplicate(self, path_to_src, path_to_dest):
        if self.exists(path_to_src) and not self.is_dir(path_to_src):
            self.cache.write(path_to_dest, self.cache.get_value(path_to_src))
            self.flush() 
        return self.store.duplicate(path_to_src, path_to_dest)
        
    def move(self, path_to_src, path_to_dest):
        self.flush()
        self.cache.delete(path_to_dest)
        self.store.move(path_to_src, path_to_dest)
 
    def get_modified(self, path):
        self.flush()
        return self.store.get_modified(path)
    
    def get_directory_listing(self, directory):
        self.flush()
        return self.store.get_directory_listing(directory)
    
    def get_bytes(self, path):
        self.flush()
        return self.store._get_metadata(path)['bytes']
    
    def exists(self, path):
        return self.cache.exists(path) or self.store.exists(path)
    
    def _get_metadata(self, path):
        self.flush()
        return self.store._get_metadata(path)

    def is_dir(self, path):
        self.flush()
        return self.store.is_dir(path)
    
    def flush(self):
        self.logger.debug("flushing cache")
        for path in self.cache.get_keys():
            self.__flush(path)
            
    def __flush(self, path):
        self.logger.debug("flushing %s ?" % path)
        self.cache.update(path)
        if self.cache.is_dirty(path):
            self.logger.debug("flushing %s ? -- it is dirty" % path)
            actual_modified_date = self._get_actual_modified_date(path)
            self.logger.debug("actual_modified_date: %s file: %s " % (actual_modified_date, path))
            cached_modified_date = self.cache.get_modified(path)
            self.logger.debug("cached_modified_date: %s file: %s " % (cached_modified_date, path))
            if actual_modified_date < cached_modified_date:
                self.logger.debug("flushing %s !" % path)
                file = DataFileWrapper(self.cache.get_value(path))
                file.fileno()#
                self.store.store_fileobject(file, path)
                self.cache.update(path)
                self.logger.debug("flushing %s with content starting with %s" % (path, self.cache.get_value(path)[0:10]))