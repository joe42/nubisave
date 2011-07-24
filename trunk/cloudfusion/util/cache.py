'''
Created on 03.06.2011

@author: joe
'''
import time

class Cache(object):
    def __init__(self, expiration_time):
        self.cache = {}
        self.expire = expiration_time
        
    def refresh(self, key, disk_value, modified):
        """ Refreshes a cache entry, if :param:`modified` is bigger than the cache entry's modified date. """
        if key in self.cache:
            disk_entry_is_newer = modified > self.cache[key]['modified']
            if not disk_entry_is_newer:
                return
        entry = {}
        entry['value'] = disk_value
        entry['updated'] = time.time()
        entry['modified'] = modified
        entry['dirty'] = False
        self.cache[key] = entry
        
    def write(self, key, value):
        entry = {}
        entry['value'] = value
        entry['updated'] = time.time()
        entry['modified'] = time.time()
        entry['dirty'] = True
        self.cache[key] = entry
        
    def get_keys(self):
        return self.cache
    
    def get_modified(self, key):
        return self.cache[key]['modified']
    
    def get_size_of_dirty_data(self):
        ret = 0
        for entry in self.cache:
            if self.is_dirty(entry):
                ret += len(str(self.get_value(entry)))
        return ret
    
    def get_size_of_cached_data(self):
        ret = 0
        for entry in self.cache:
            ret += len(str(self.get_value(entry)))
        return ret
    
    def exists(self, key):
        if key in self.cache:
            return True
        return False

    def is_expired(self, key):
        return time.time() > self.cache[key]['updated'] + self.expire
    
    def update(self, key):
        self.cache[key]['updated'] = time.time() 
    
    def get_value(self, key):
        return self.cache[key]['value']
    
    def is_dirty(self, key):
        return self.cache[key]['dirty']
    
    def delete(self, key):
        try:
            del self.cache[key]
        except KeyError:
            pass
