'''
Created on 25.04.2011

@author: joe
'''
'''
Created on 08.04.2011

@author: joe
'''
    
import tempfile
import time

class CachingStore(object):
    def __init__(self, store):
        self.store = store
#        self.temp_file = tempfile.SpooledTemporaryFile()
        self.cache = {}
        self.expire = 2
    
    def _is_valid_path(self, path):
        return self.store._is_valid_path(path)
    
    def _raise_error_if_invalid_path(self, path):
        self.store._raise_error_if_invalid_path(path)
        
    def get_name(self):
        return self.store.get_name()
    
    def _refresh_cache(self, path_to_file):
        cache_fileobject = {}
        cache_fileobject['data'] = self.store.get_file(path_to_file)
        cache_fileobject['updated'] = time.time()
        cache_fileobject['modified'] = self._get_metadata(path_to_file)['modified']
        self.cache[path_to_file] = cache_fileobject
            
    def get_file(self, path_to_file):
        if not path_to_file in self.cache:
            self._refresh_cache(path_to_file)
        if time.time() > self.cache[path_to_file]['updated'] + self.expire:
            actual_modified_date = self._get_metadata(path_to_file)['modified']
            cached_modified_date = self.cache[path_to_file]['modified']
            if actual_modified_date > cached_modified_date:
                self._refresh_cache(path_to_file)
        return self.cache[path_to_file]['data']
    
    def store_file(self, path_to_file, dest_dir="/", remote_file_name = None):
        self.store.store_file(path_to_file, dest_dir, remote_file_name)
        
    def store_fileobject(self, fileobject, path):
        self.store.store_fileobject(fileobject, path)
            
    def delete(self, path):
        self.store.delete(path)
          
    def account_info(self):
        return self.store.account_info()
    
    def get_free_space(self):
        return self.store.get_free_space()
    
    def get_overall_space(self):
        return self.store.get_overall_space()
    
    def get_used_space(self):
        return self.store.get_used_space()

    def create_directory(self, directory):
        return self.store.create_directory(directory)
        
    def duplicate(self, path_to_src, path_to_dest):
        return self.store.duplicate(path_to_src, path_to_dest)
        
    def move(self, path_to_src, path_to_dest):
        self.store.move(path_to_src, path_to_dest)
 
    def get_modified(self, path):
        return self.store.get_modified(path)
    
    def get_directory_listing(self, directory):
        return self.store.get_directory_listing(directory)
    
    def get_bytes(self, path):
        return self.store.get_bytes(path)
    
    def exists(self, path):
        return self.store.exists(path)
    
    def _get_metadata(self, path):
        return self.store._get_metadata(path)

    def is_dir(self, path):
        return self.store.is_dir(path)
