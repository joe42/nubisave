from cloudfusion.pyfusebox.pyfusebox import *
import threading

class StoreFlusher(threading.Thread):
    def __init__(self, store, lock, logger=None):
        self.logger = logger
        self.store = store
        self.lock = lock
        threading.Thread.__init__(self)
 
    def __log_debug(self, msg):
        if self.logger:
            self.logger.debug(msg)
            
    def __log_info(self, msg):
        if self.logger:
            self.logger.info(msg)
    
    def run(self):
        with self.lock:
            self.__log_debug("StoreFlusher: no ongoing file operation")
            if self.store:  # store is initialized
                self.__log_debug("StoreFlusher: store is properly initialized")
                if(time.time() > self.store.get_cache_expiration_time() + self.store.get_time_of_last_flush()):
                    self.__log_debug("StoreFlusher: cache expiration date is overdue; flushing store")
                    self.store.flush()
                    self.__log_info("StoreFlusher: store flushed")

class FlushingPyFuseBox(PyFuseBox):
    def __init__(self, root, store):
        super( FlushingPyFuseBox, self ).__init__(root, store)
        self.fileoperation_is_pending = threading.Lock()
        StoreFlusher(store, self.fileoperation_is_pending, self.logger).start()
    
    def getattr(self, path, fh=None):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).getattr(path, fh)
    
    def truncate(self, path, length, fh=None):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).truncate(path, length, fh)
    
    def rmdir(self, path):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).rmdir(path)
        
    def mkdir(self, path, mode):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).mkdir(path, mode)
    
    def statfs(self, path):#add size of vtf
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).statfs(path)
    
    def rename(self, old, new):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).rename(old, new)

    def create(self, path, mode):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).create(path, mode)
    
    def unlink(self, path):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).unlink(path)

    def read(self, path, size, offset, fh):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).read(path, size, offset, fh)

    def write(self, path, buf, offset, fh):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).write(path, buf, offset, fh)
    
    def flush(self, path, fh):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).flush(path, fh)
    
    def release(self, path, fh):
        with self.fileoperation_is_pending:
            super( FlushingPyFuseBox, self ).release(path, fh) 
       
    def readdir(self, path, fh):
        with self.fileoperation_is_pending:
            return super( FlushingPyFuseBox, self ).readdir(path, fh) 