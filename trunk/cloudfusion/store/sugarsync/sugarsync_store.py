'''
Created on 08.04.2011

@author: joe
'''

import time
import datetime
from cloudfusion.store.store import *
import logging
import logging.config
import os.path
from cloudfusion.store.sugarsync.sugarsync_rest import *
from cloudfusion.store.sugarsync.client import SugarsyncClient
from cloudfusion.util.xmlparser import DictXMLParser
from cloudfusion.util.string import *
import tempfile
import httplib
#import elementtree.ElementTree as ET
import xml.dom.minidom as dom
        
logging.config.fileConfig('cloudfusion/config/logging.conf')
logger = logging.getLogger('sugarsync')
"""
config = auth.Authenticator.load_config("cloudfusion/config/testing.ini")
dba = auth.Authenticator(config)
access_token = dba.obtain_trusted_access_token(config['testing_user'], config['testing_password'])
db_client = client.DropboxClient(config['server'], config['content_server'], config['port'], dba, access_token)
root = config['root']
"""

 
class SugarsyncStore(Store):
    WORKSPACE = 2    
    def __init__(self, username, password):
        #self.dir_listing_cache = {}
        self.path_cache = {}
        self.client = SugarsyncClient(username, password)
        self.time_difference = self._get_time_difference()
        logger.debug("sugarsync store initialized")
        super(SugarsyncStore, self).__init__() 
        
    def get_name(self):
        logger.debug("getting name")
        return "Sugarsync"
    
    """def __get_path_prefix(self):
        info = self.client.user_info()
        tree = XML(info.data)
        workspace_url = tree.get("user").get("workspace").text
        return regSearchString('.*user/(.*)/workspaces.*', workspace_url)"""
    
    def _translate_path(self, path):
        logger.debug("translating path: "+path)
        if path in self.path_cache:
            return self.path_cache[path]
        if path == "/":
            return self.WORKSPACE
        else:
            parent_dir = os.path.dirname(path)
            self.path_cache[parent_dir] = self._translate_path(parent_dir)
            collection = self._parse_collection(self.path_cache[parent_dir])
            for item in collection:
                if parent_dir[-1] != "/":
                    parent_dir += "/"
                self.path_cache[ parent_dir+item["name"] ] = item["reference"]
            if not path in  self.path_cache:
                logger.warn("could not translate path: " +path)
                raise NoSuchFilesytemObjectError(path)
            return self.path_cache[path]
            
            
    def _parse_collection(self, translated_path):
        """:returns: dict a dictionary with all paths of the collection at :param:`translated_path` as keys and the corresponding nested dictionaries with the key/value pair for is_dir and reference."""
        ret = []
        resp = self.client.get_dir_listing(translated_path)
        if resp.status <200 or resp.status >300:
            logger.warn("could not get direcory listing: " +translated_path+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
            raise NoSuchFilesytemObjectError(translated_path)
        xml_tree = dom.parseString(resp.data)
        for collection in xml_tree.documentElement.getElementsByTagName("collection"): 
            item = {}
            item["is_dir"] = collection.getAttribute("type") == "folder"
            item["name"] = collection.getElementsByTagName("displayName")[0].firstChild.nodeValue
            reference_url = collection.getElementsByTagName("ref")[0].firstChild.nodeValue
            reference_uid = regSearchString('.*:(.*)', reference_url)
            item["reference"] = reference_uid
            ret.append(item)
        for collection in xml_tree.documentElement.getElementsByTagName("file"): 
            item = {}
            item["is_dir"] = False
            item["name"] = collection.getElementsByTagName("displayName")[0].firstChild.nodeValue
            item["size"] = collection.getElementsByTagName("size")[0].firstChild.nodeValue
            item["lastModified"] = collection.getElementsByTagName("lastModified")[0].firstChild.nodeValue
            item["presentOnServer"] = collection.getElementsByTagName("lastModified")[0].firstChild.nodeValue
            reference_url = collection.getElementsByTagName("ref")[0].firstChild.nodeValue
            reference_uid = regSearchString('.*:(.*)', reference_url)
            item["reference"] = reference_uid
            ret.append(item)
        return ret
    
    
    def account_info(self):
        logger.debug("retrieving account info")
        info = self.client.user_info()
        partial_tree = {"user": {"quota": {"limit": "limit", "usage": "usage"}}}
        DictXMLParser().populate_dict_with_XML_leaf_textnodes(info.data, partial_tree)
        #print response.status, response.reason, response.getheaders()
        ret = {}
        ret['overall_space'] = int(partial_tree['user']['quota']['limit'])
        ret['used_space'] = int(partial_tree['user']['quota']['usage'])
        return "Sugarsync overall space: %s, used space: %s" % (ret['overall_space'], ret['used_space']) 
    
    def get_overall_space(self):
        logger.debug("retrieving all space")
        info = self.client.user_info()
        partial_tree = {"user": {"quota": {"limit": "limit", "usage": "usage"}}}
        DictXMLParser().populate_dict_with_XML_leaf_textnodes(info.data, partial_tree)
        #print response.status, response.reason, response.getheaders()
        if info.status <200 or info.status >300:
            logger.warn("could not retrieve overall space"+"\nstatus: %s reason: %s" % (info.status, info.reason))
        return int(partial_tree['user']['quota']['limit'])
    
    def get_used_space(self):
        logger.debug("retrieving used space")
        info = self.client.user_info()
        partial_tree = {"user": {"quota": {"limit": "limit", "usage": "usage"}}}
        DictXMLParser().populate_dict_with_XML_leaf_textnodes(info.data, partial_tree)
        #print response.status, response.reason, response.getheaders()
        if info.status <200 or info.status >300:
            logger.warn("could not retrieve used space"+"\nstatus: %s reason: %s" % (info.status, info.reason))
        return int(partial_tree['user']['quota']['usage'])
    
    def get_file(self, path_to_file): 
        logger.debug("getting file: " +path_to_file)
        self._raise_error_if_invalid_path(path_to_file)
        file = self.client.get_file( self._translate_path(path_to_file) )
        if file.status <200 or file.status >300:
            logger.warn("could not get file: %s\nstatus: %s reason: %s" % (path_to_file, file.status, file.reason))
        return file.data 
    
    def store_fileobject(self, fileobject, path_to_file):
        logger.debug("storing file object to "+path_to_file)
        if not self.exists(path_to_file):
            self._create_file(path_to_file)
        resp = self.client.put_file( fileobject, self._translate_path(path_to_file) ) 
        if resp.status <200 or resp.status >300:
            logger.warn("could not store file to " +path_to_file+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
            
    def _create_file(self, path, mime='text/x-cloudfusion'):
        logger.debug("creating file object "+path)
        name = os.path.basename(path)
        directory = os.path.dirname(path)
        translated_dir = self._translate_path(directory)
        resp = self.client.create_file(translated_dir, name)
        if resp.status <200 or resp.status >300:
            logger.warn("could not create file " +path+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
    
    def delete(self, path):
        logger.debug("deleting " +path)
        if path == "/":
            return
        if path[-1] == "/":
            path = path[0:-1]
        self._raise_error_if_invalid_path(path)
        resp = self.client.delete_file( self._translate_path(path) )
        if resp.status <200 or resp.status >300:
            resp = self.client.delete_folder( self._translate_path(path) )
        if resp.status <200 or resp.status >300:
            logger.warn("could not delete " +path+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
        del self.path_cache[path]
    
    def create_directory(self, path):
        logger.debug("creating directory " +path)
        self._raise_error_if_invalid_path(path)
        if path == "/":
            return
        if path[-1] == "/":
            path = path[0:-1]
        if self.exists(path):
            return -1;
        name = os.path.basename(path)
        directory = os.path.dirname(path)
        resp = self.client.create_folder( self._translate_path(directory), name ) 
        if resp.status <200 or resp.status >300:
            logger.warn("could not create directory: " +path+"\nstatus: %s reason: %s" % (resp.status, resp.reason))

    def get_directory_listing(self, directory):
        logger.debug("getting directory listing for "+directory)
        ret = []
        translated_dir = self._translate_path(directory)
        collection = self._parse_collection(translated_dir)
        for item in collection:
            ret.append( directory+"/"+item['name'] )
        return ret 
    
    def duplicate(self, path_to_src, path_to_dest): #src might be a directory
        logger.debug("duplicating " +path_to_src+" to "+path_to_dest)
        self._raise_error_if_invalid_path(path_to_src)
        self._raise_error_if_invalid_path(path_to_dest)
        if path_to_src[-1] == "/":
            path_to_src = path_to_src[0:-1]
        if path_to_dest[-1] == "/":
            path_to_dest = path_to_dest[0:-1]
        dest_name = os.path.basename(path_to_dest)
        dest_dir  = os.path.dirname(path_to_dest)
        if path_to_dest.startswith(path_to_src) and dest_name == os.path.basename(path_to_src):
            logger.warning("cannot copy folder to itself")
            return #DBG raise error
        translated_dest_dir = self._translate_path( dest_dir )
        translated_src = self._translate_path(path_to_src)
        if self.is_dir(path_to_src):
            #make destination directory: (might exist)
            if not self.exists(path_to_dest):
                self.create_directory(path_to_dest)
            #copy all files from original directory:
            for item in self._parse_collection(translated_src):
                if item['is_dir']:#copy all folders form original directory
                    self.duplicate(path_to_src+"/"+item['name'], path_to_dest+"/"+item['name'])
                else:
                    resp = self.client.duplicate_file(item['reference'], translated_dest_dir, dest_name)
                    if resp.status != 200:
                        logger.warn("could not duplicate " +path_to_src+" to "+path_to_dest+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
        else:
            #if dest exists raise error
            if self.exists(path_to_dest):
                logger.warn("could not duplicate " +path_to_src+" to "+path_to_dest+"\nfile already exists")
                return
            resp = self.client.duplicate_file(translated_src, translated_dest_dir, dest_name)
            if resp.status != 200:
                logger.warn("could not duplicate " +path_to_src+" to "+path_to_dest+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
        #ssert_all_in(resp.data.keys(), [u'thumb_exists', u'bytes', u'modified',u'path', u'is_dir', u'size', u'root', u'mime_type', u'icon'])

    def _get_metadata(self, path):
        logger.debug("getting metadata for "+path)
        self._raise_error_if_invalid_path(path)
        if path == "/": # workaraund for root metadata necessary for sugarsync?
            ret = {}
            ret["bytes"] = 0
            ret["modified"] = time.time()
            ret["path"] = "/"
            ret["is_dir"] = True
            return ret;
        if path[-1] == "/":
            path = path[0:-1]
        is_file = True
        resp = self.client.get_file_metadata( self._translate_path(path) )
        if resp.status <200 or resp.status >300:
            is_file = False
            resp = self.client.get_folder_metadata( self._translate_path(path) )
        if resp.status <200 or resp.status >300:
            logger.warn("could not get metadata: " +path+"\nstatus: %s reason: %s" % (resp.status, resp.reason))
            raise NoSuchFilesytemObjectError(path)
        
        print path, resp.data
        ret = {}
        if is_file:
            partial_tree = {"file": {"size": "", "lastModified": "", "timeCreated": ""}}
            DictXMLParser().populate_dict_with_XML_leaf_textnodes(resp.data, partial_tree)
            ret["bytes"] = int(partial_tree['file']['size'])
            
            try:#"Sat, 21 Aug 2010 22:31:20 +0000"#2011-05-10T06:18:33.000-07:00
                ret["modified"] = time.mktime( time.strptime( partial_tree['file']['lastModified'], "%Y-%m-%dT%H:%M:%S.000-07:00") ) - self.time_difference
            except Exception as x:
                logger.warn("Time conversion error: %s" % str(partial_tree['file']['lastModified']))
                raise DateParseError("Error parsing modified attribute: %s" % str(x));

            ret["created"] = partial_tree['file']['timeCreated']
            ret["path"] = path
            ret["is_dir"] = False
        else:
            partial_tree = {"folder": {"timeCreated": ""}}
            DictXMLParser().populate_dict_with_XML_leaf_textnodes(resp.data, partial_tree)
            ret["bytes"] = 0
            ret["modified"] = time.time()
            ret["created"] = partial_tree['folder']['timeCreated']
            ret["path"] = path
            ret["is_dir"] = True
        return ret;
            
    def _get_time_difference(self):
        resp =  self.client.user_info()
        return time.mktime( time.strptime(resp.getheader('date'), "%a, %d %b %Y %H:%M:%S GMT") ) - time.time()
            
    """     
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
            logger.warn("Time conversion error: %s" % str(x))
            raise DateParseError("Error parsing modified attribute: %s" % str(x));
        ret["path"] = data["path"]
        ret["is_dir"] = data["is_dir"]
        return ret;
        
    def _handleError(self, status):
        pass;
    
    def _get_time_difference(self):
        resp =  db_client.account_info()
        return time.mktime( time.strptime(resp.headers['date'], "%a, %d %b %Y %H:%M:%S GMT") ) - time.time()
    
        

"""









