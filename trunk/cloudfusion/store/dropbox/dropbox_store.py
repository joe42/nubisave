'''
Created on 08.04.2011

@author: joe
'''

import time
import datetime
from dropbox import client, auth
from cloudfusion.store.store import *
import logging
import logging.config
import os.path
from cloudfusion.store.dropbox.file_decorator import NameableFile
import tempfile

logging.config.fileConfig('cloudfusion/config/logging.conf')
logger = logging.getLogger('dropbox')

config = auth.Authenticator.load_config("cloudfusion/config/testing.ini")
dba = auth.Authenticator(config)
access_token = dba.obtain_trusted_access_token(config['testing_user'], config['testing_password'])
db_client = client.DropboxClient(config['server'], config['content_server'], config['port'], dba, access_token)
root = config['root']

CALLBACK_URL = 'http://printer.example.com/request_token_ready'
RESOURCE_URL = 'http://' + config['server'] + '/0/oauth/echo'

class ServerError(StoreAccessError):
    def __init__(self, msg):
        super(ServerError, self).__init__(msg) 
class DropboxError(object):
    def __init__(self, status, operation_name):
        if status == 507:
            msg = "User over quota."
        elif status == 503:
            msg = "Too many requests."
        elif status == 403:
            msg = "Operation forbidden (path exists, wrong token, expired timestamp?)."
        super(ServerError, self).__init__(msg) 



class DropboxStore(Store):
    def __init__(self):
        self.dir_listing_cache = {}
        self.time_difference = self._get_time_difference()
        logger.debug("api initialized")
        super(DropboxStore, self).__init__() 
        
    def get_name(self):
        logger.debug("getting name")
        return "Dropbox"
    
    def get_file(self, path_to_file): 
        logger.debug("getting file: " +path_to_file)
        self._raise_error_if_invalid_path(path_to_file)
        resp = db_client.get_file(root, path_to_file)
        if resp.status != 200:
            logger.warn("could not get file: " +path_to_file+"\ndata: "+resp.data['error'])
        return resp
    
    def store_fileobject(self, fileobject, path):
        logger.debug("storing file object to "+path)
        remote_file_name = os.path.basename(path)
        dest_dir = os.path.dirname(path);
        namable_file = NameableFile( fileobject, remote_file_name )
        resp = db_client.put_file(root, dest_dir, namable_file) 
        if resp.status != 200:
            logger.warn("could not store file: " +dest_dir+remote_file_name+"\ndata: "+resp.data['error'])
    
    def delete(self, path):
        logger.debug("deleting " +path)
        self._raise_error_if_invalid_path(path)
        resp = db_client.file_delete(root, path)
        if resp.status != 200:
            logger.warn("could not delete " +path+"\ndata: "+resp.data['error'])
            #assert_all_in(resp.data.keys(), [u'is_deleted', u'thumb_exists', u'bytes', u'modified',u'path', u'is_dir', u'size', u'root', u'mime_type', u'icon'])
        
    def account_info(self):
        logger.debug("retrieving account info")
        resp =  db_client.account_info()
        if resp.status != 200:
            logger.warn("could not retrieve account data"+"\ndata: "+resp.data['error'])
            #assert_all_in(resp.data.keys(), [u'country', u'display_name', u'uid', u'quota_info'])
        return str(resp.data)

    def create_directory(self, directory):
        logger.debug("creating directory " +directory)
        self._raise_error_if_invalid_path(directory)
        resp = db_client.file_create_folder(root, directory)
        if resp.status != 200:
            logger.warn("could not create directory: " +directory+"\ndata: "+resp.data['error'])
        return resp.status
        #assert_all_in(resp.data.keys(), [u'thumb_exists', u'bytes', u'modified', u'path', u'is_dir', u'size', u'root', u'icon'])
        
    def duplicate(self, path_to_src, path_to_dest):
        logger.debug("duplicating " +path_to_src+" to "+path_to_dest)
        self._raise_error_if_invalid_path(path_to_src)
        self._raise_error_if_invalid_path(path_to_dest)
        resp = db_client.file_copy(root, path_to_src, path_to_dest)
        if resp.status != 200:
            logger.warn("could not duplicate " +path_to_src+" to "+path_to_dest+"\ndata: "+resp.data['error'])
        #ssert_all_in(resp.data.keys(), [u'thumb_exists', u'bytes', u'modified',u'path', u'is_dir', u'size', u'root', u'mime_type', u'icon'])
    
    def move(self, path_to_src, path_to_dest):
        logger.debug("moving " +path_to_src+" to "+path_to_dest)
        self._raise_error_if_invalid_path(path_to_src)
        self._raise_error_if_invalid_path(path_to_dest)
        resp = db_client.file_move(root, path_to_src, path_to_dest)
        if resp.status != 200:
            logger.warn("could not move " +path_to_src+" to "+path_to_dest+"\ndata: "+resp.data['error'])
    
    def get_overall_space(self):
        logger.debug("retrieving all space")
        resp =  db_client.account_info()
        if resp.status != 200:
            logger.warn("could not retrieve all space"+"\ndata: "+resp.data['error'])
        return resp.data[u'quota_info']["quota"]

    def get_used_space(self):
        logger.debug("retrieving used space")
        resp =  db_client.account_info()
        if resp.status != 200:
            logger.warn("could not retrieve used space"+"\ndata: "+resp.data['error'])
        return resp.data[u'quota_info']["shared"] + resp.data[u'quota_info']["normal"]
        
    def get_directory_listing(self, directory):
        logger.debug("getting directory listing for "+directory)
        self._raise_error_if_invalid_path(directory)
        hash = None
        if directory in self.dir_listing_cache:
            hash = self.dir_listing_cache[directory]['hash']
        resp = db_client.metadata(root, directory, hash=hash, list=True)
        if resp.status != 200: 
            if resp.status == 304: 
                logger.debug("retrieving listing from cache " +directory)
                ret = self.dir_listing_cache[directory]['dir_listing']
            else:
                logger.warn("could not get directory listing for " +directory+"\ndata: "+resp.data['error'])
        else:
            ret = self._parse_dir_list(resp.data)
            self.dir_listing_cache[directory] = {}
            self.dir_listing_cache[directory]['hash'] = resp.data["hash"]
            self.dir_listing_cache[directory]['dir_listing'] = ret
        return ret 
    
    def _parse_dir_list(self, data):
        #OverflowError or ValueError
        ret = {}
        for obj in data["contents"]:
            file_sys_obj = self._parse_filesys_obj(obj)
            path = file_sys_obj['path']
            ret[path] = file_sys_obj
        return ret
      
    def _log_error(self, method_name, path, resp, msg = None):
        log = method_name + " failed with status: "+str(resp.status)
        if 'error' in resp.data:
            log = "\n"+resp.data['error']
        if msg:
            log = "\n"+msg 
        logger.warn(log)
        
    def _get_metadata(self, path):
        logger.debug("getting metadata for "+path)
        self._raise_error_if_invalid_path(path)
        if path == "/": # workaraund for root metadata
            ret = {}
            ret["bytes"] = 0
            ret["modified"] = time.time()
            ret["path"] = "/"
            ret["is_dir"] = True
            return ret;
        resp = db_client.metadata(root, path, list=False)
        object_is_deleted = 'is_deleted' in resp.data and resp.data['is_deleted']
        if resp.status == 404 or object_is_deleted:
            msg = None
            if object_is_deleted:
                msg = "filesystem object has been deleted"
            self._log_error("_get_metadata", path, resp, msg)
            raise NoSuchFilesytemObjectError(path)
        elif resp.status != 200:
            self._log_error("_get_metadata", path, resp)
            raise RetrieveMetadataError(path, resp.data['error'])
        elif resp.status == 200:
            return self._parse_filesys_obj(resp.data)
    
    def _parse_filesys_obj(self, data):
        #OverflowError or ValueError
        ret = {}
        ret["bytes"] = data["bytes"]
        try:
            ret["modified"] = time.mktime( time.strptime(data["modified"], "%a, %d %b %Y %H:%M:%S +0000") ) - self.time_difference
        except Exception as x:
            logger.warn("Time conversion error: %s" % str(data["modified"]))
            raise DateParseError("Error parsing modified attribute: %s" % str(x));
        ret["path"] = data["path"]
        ret["is_dir"] = data["is_dir"]
        return ret;
        
    def _handleError(self, status):
        pass;
    
    def _get_time_difference(self):
        resp =  db_client.account_info()
        return time.mktime( time.strptime(resp.headers['date'], "%a, %d %b %Y %H:%M:%S GMT") ) - time.time()
    
        












