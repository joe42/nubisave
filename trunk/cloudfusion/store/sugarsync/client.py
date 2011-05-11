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
    def __init__(self, username, password):
        self.conn = httplib.HTTPSConnection("api.sugarsync.com")
        response = self.create_token(username, password)
        self.token = response.getheader("location")
        partial_tree = {"authorization": {"user": ""}}
        DictXMLParser().populate_dict_with_XML_leaf_textnodes(response.data, partial_tree)
        self.uid= regSearchString('https://api.sugarsync.com/user/(.*)', partial_tree['authorization']['user'])
        
    
    def create_token(self, username, password):
        params = '<?xml version="1.0" encoding="UTF-8" ?><authRequest>    <username>%s</username>    <password>%s</password>    <accessKeyId>MTIzNzYzNjEzMDQ0MTE2ODQ2MDk</accessKeyId>    <privateAccessKey>NDM0NDRkZmU4OTExNDZiYjk5OTlmYWVlY2I4M2EzZjM</privateAccessKey></authRequest>' % (username, password)
        headers = {"Host": "api.sugarsync.com"}#send application/xml; charset=UTF-8
        self.conn.request("POST", "/authorization", params, headers)
        response = HTTPResponse(self. conn.getresponse() )
        return response
    
    def user_info(self):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("GET", "/user", None, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def get_file_metadata(self, path_to_file):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("GET", "/file/:sc:%s:%s" % (self.uid, path_to_file), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
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
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("GET", "/folder/:sc:%s:%s/contents" % (self.uid, path), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    
    def get_folder_metadata(self, path):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("GET", "/folder/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
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
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        #metadata = self.get_metadata(path_to_file)
        #partial_tree = {"file": {"displayName": "", "size": "", "lastModified": "", "timeCreated": "", "mediaType": "", "presentOnServer": "", "parent": "", "fileData": ""}}
        #DictXMLParser().populate_dict_with_XML_leaf_textnodes(metadata.data, partial_tree)
        self.conn.request("GET", "/file/:sc:%s:%s/data" % (self.uid, path_to_file), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def put_file(self, fileobject, path_to_file):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        print self.token
        self.conn.request("PUT", "/file/:sc:%s:%s/data" % (self.uid, path_to_file), fileobject.read(), headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def create_file(self, directory, name, mime='text/x-cloudfusion'):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><file><displayName>%s</displayName><mediaType>%s</mediaType></file>' % (name, mime)
        self.conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, directory), params, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
        
    
    def delete_file(self, path):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("DELETE", "/file/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def delete_folder(self, path):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        self.conn.request("DELETE", "/folder/:sc:%s:%s" % (self.uid, path), None, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def create_folder(self, directory, name):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><folder><displayName>%s</displayName></folder>' % name
        self.conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, directory), params, headers)
        response = HTTPResponse( self.conn.getresponse() )
        return response
    
    def duplicate_file(self, path_to_src, path_to_dest, name):
        headers = {"Host": "api.sugarsync.com", "Authorization: ": self.token}
        params = '<?xml version="1.0" encoding="UTF-8"?><fileCopy source="http://api.sugarsync.com/file/:sc:%s:%s">   <displayName>%s</displayName></fileCopy>' % (self.uid, path_to_src, name)
        self.conn.request("POST", "/folder/:sc:%s:%s" % (self.uid, path_to_dest), params, headers)
        response = HTTPResponse( self.conn.getresponse() )
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
