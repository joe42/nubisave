'''
Created on 12.05.2011

@author: joe
'''
from cloudfusion.pyfusebox.pyfusebox import PyFuseBox
from cloudfusion.fuse import FUSE
from cloudfusion.store.dropbox.dropbox_store import DropboxStore
from cloudfusion.store.sugarsync.sugarsync_store import SugarsyncStore
from cloudfusion.store.caching_store import CachingStore
import os, sys, stat,  time
from dropbox import auth
import getpass
from ConfigParser import SafeConfigParser
from cloudfusion.store.metadata_caching_store import MetadataCachingStore

def get_password():
    print "Enter Password for the account:"
    password =  getpass.getpass()
    return password

def get_dropbox_config():
    return auth.Authenticator.load_config("cloudfusion/config/dropbox.ini")

def get_sugarsync_config():
    config = SafeConfigParser()
    config_file = open("cloudfusion/config/sugarsync.ini", "r")
    config.readfp(config_file)
    return dict(config.items('auth'))

def get_store(service, key, secret, username, password=None):
    if password == None:
        password = get_password()
    if service.lower() == "sugarsync":
        config = get_sugarsync_config()
        config['password'] = password
        config['user'] = username
        config['access_key_id'] = key
        config['private_access_key'] = secret
        store = SugarsyncStore(config)
    else: # default
        config = get_dropbox_config()
        config['password'] = password
        config['user'] = username
        config['consumer_key'] = key
        config['consumer_secret'] = secret
        store = DropboxStore(config)
    return store

def check_arguments(args):
    if not len(args) in [6,7,8]:
        print 'usage: %s mountpoint service username key secret [password | password [cache]]' % args[0]
        exit(1)

def main():
    check_arguments(sys.argv)
    service = "dropbox"
    password  = None
    username  = None
    key  = None
    secret  = None
    if len(sys.argv) in [6,7,8]:
        service = sys.argv[2]
        username= sys.argv[3]
        key= sys.argv[4]
        secret= sys.argv[5]
    if len(sys.argv) in [7,8]:
        password= sys.argv[6]
    store = get_store(service, key, secret, username, password)
    if len(sys.argv) == 8 and "cache" == sys.argv[7]:
        store = MetadataCachingStore( CachingStore( MetadataCachingStore( store ) ) )
    fuse_operations = PyFuseBox(sys.argv[1], store)
    FUSE(fuse_operations, sys.argv[1], foreground=False, nothreads=True)
    
if __name__ == '__main__':
    main()