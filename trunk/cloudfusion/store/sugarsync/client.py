'''
Created on 04.05.2011

@author: joe
'''
import httplib
import os.path
from cloudfusion.util.xmlparser import DictXMLParser
from cloudfusion.util.string import *

#make thread safe by adding connection creation to every method call

class SugarsyncClient(object):
    def __init__(self, config):
        self.host = config["host"]
        self.server_url = config["server_url"]
        self.access_key_id = config["access_key_id"]
        self.private_access_key = config["private_access_key"] 
        self.username = config["user"]
        self.password = config["password"] 
        response = self.create_token()
        self.token = response.getheader("location")
        partial_tree = {"authorization": {"user": ""}}
        DictXMLParser().populate_dict_with_XML_leaf_textnodes(response.data, partial_tree)
        self.uid= regSearchString(self.server_url+'user/(.*)', partial_tree['authorization']['user'])
    
    def create_token(self):
        params = '<?xml version="1.0" encoding="UTF-8" ?><authRequest>    <username>%s</username>    <password>%s</password>    <accessKeyId>%s</accessKeyId>    <privateAccessKey>%s</privateAccessKey></authRequest>' % (self.username, self.password, self.access_key_id, self.private_access_key)
        headers = {"Host": self.host}#send application/xml; charset=UTF-8
        conn = httplib.HTTPSConnection(self.host)
        conn.request("POST", "/authorization", params, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def user_info(self):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("GET", "/user", None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def get_file_metadata(self, path_to_file):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("GET", "/file/:sc:%s:%s" % (self.uid, path_to_file), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
        """
       <?xml version="1.0" encoding="UTF-8"?>
<file>
    <displayName>Foo</displayName>
    <size>120233</size>
    <lastModified>2009-09-25T16:49:56.000-07:00</lastModified>
    <timeCreated>2009-09-25T16:49:56.000-07:00</timeCreated>
    <mediaType>image/jpeg</mediaType>
    <presentOnServer>true</presentOnServer>
    <parent>http://api.sugarsync.com/folder/xyzzy</parent>
    <fileData>http://api.sugarsync.com/file/abc123/data</fileData>
</file>

       """  
    def get_dir_listing(self, path):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("GET", "/folder/:sc:%s:%s/contents" % (self.uid, path), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    
    def get_folder_metadata(self, path):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("GET", "/folder/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
        """
<?xml version="1.0" encoding="UTF-8"?>
<folder>
   <displayName>folder1</displayName>
   <timeCreated>2009-06-25T11:31:05.000-07:00</timeCreated>
   <parent>https://api.sugarsync.com/folder/myfolderId</parent>
   <collections>https://api.sugarsync.com/folder/myfolderId/contents?type=folder
   </collections>
   <files>https://api.sugarsync.com/folder/myfolderId/contents?type=file</files>
   <contents>https://api.sugarsync.com/folder/myfolderId/contents</contents>
</folder>


       """
    
    def get_file(self, path_to_file):
        headers = {"Host": self.host, "Authorization: ": self.token}
        #metadata = self.get_metadata(path_to_file)
        #partial_tree = {"file": {"displayName": "", "size": "", "lastModified": "", "timeCreated": "", "mediaType": "", "presentOnServer": "", "parent": "", "fileData": ""}}
        #DictXMLParser().populate_dict_with_XML_leaf_textnodes(metadata.data, partial_tree)
        conn = httplib.HTTPSConnection(self.host)
        conn.request("GET", "/file/:sc:%s:%s/data" % (self.uid, path_to_file), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def put_file(self, fileobject, path_to_file):
        headers = {"Host": self.host, "Authorization: ": self.token}
        print self.token
        conn = httplib.HTTPSConnection(self.host)
        conn.request("PUT", "/file/:sc:%s:%s/data" % (self.uid, path_to_file), fileobject.read(), headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def create_file(self, directory, name, mime='text/x-cloudfusion'):
        headers = {"Host": self.host, "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><file><displayName>%s</displayName><mediaType>%s</mediaType></file>' % (name, mime)
        conn = httplib.HTTPSConnection(self.host)
        conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, directory), params, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
        
    def delete_file(self, path):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("DELETE", "/file/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def delete_folder(self, path):
        headers = {"Host": self.host, "Authorization: ": self.token}
        conn = httplib.HTTPSConnection(self.host)
        conn.request("DELETE", "/folder/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def create_folder(self, directory, name):
        headers = {"Host": self.host, "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><folder><displayName>%s</displayName></folder>' % name
        conn = httplib.HTTPSConnection(self.host)
        conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, directory), params, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
    def duplicate_file(self, path_to_src, path_to_dest, name):
        headers = {"Host": self.host, "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><fileCopy source="%sfile/:sc:%s:%s">   <displayName>%s</displayName></fileCopy>' % (self.server_url, self.uid, path_to_src, name)
        conn = httplib.HTTPSConnection(self.host)
        conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, path_to_dest), params, headers)
        response = HTTPResponse( conn.getresponse() )
        return response
    
        
    
class HTTPResponse(object):
    def __init__(self, connection_response):
        self.connection_response = connection_response
        self.data = connection_response.read()
        self.status = connection_response.status
        self.reason = connection_response.reason
    
    def getheaders(self):
        return self.connection_response.getheaders()
   
    def getheader(self, name, default=None):
        return self.connection_response.getheader(name, default)
