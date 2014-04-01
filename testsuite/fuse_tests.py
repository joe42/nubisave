#!/usr/bin/python
import sys, os, shutil
short="short"
long="long"*1050

def check_arguments(args): 
    if not len(args) == 3:
        print 'Basic file system tests.'
        print 'usage: %s directory testfile_name' % args[0]
        print 'example: %s mount mytestfile' % args[0]
        exit(1)

def touch(path, times=None):
    with file(path, 'a'):
        os.utime(path, times)
    assert_file_exists(path)

def write(filename, content):
    fh = open(filename,"w")
    fh.write(content)
    fh.close()
    
def get_content(filename):
    fh = open(filename)
    content = fh.read()
    fh.close() 
    return content

def append(filename, content):
    fh = open(filename, "a")
    fh.write(content)
    fh.close()
        
def assert_file_exists(path):
    try:
        with open(path): pass
    except IOError:
        print "Error: File does not exists."    
       
def assert_file_not_exists(path):
    try:
        with open(path): 
            print "Error: File does exists."
    except IOError:
        pass            

def main():
    check_arguments(sys.argv)
    path = "%s/%s" % (sys.argv[1], sys.argv[2])
    create_new_empty_file(path)
    remove_empty_file(path)
    read_empty_file(path)
    append_to_empty_file(path)
    truncat_empty_file(path)
    append_to_file(path)
    truncate_file(path)
    move_empty_file(path)
    move_file(path)
    move_to_existing_file(path)
    copy_empty_file(path)
    copy_file(path)
    copy_to_existing_file(path)
    create_folder(path)
    remove_folder(path)
    create_nested_folder(path)
    remove_nested_folder(path)
    list_directory(path)
        
def create_new_empty_file(path):
    print "Creating new empty file"
    touch(path)

def remove_empty_file(path):
    print "Removing empty file"
    os.remove(path)
    assert_file_not_exists(path)

def read_empty_file(path):
    print "Reading empty file"
    touch(path)
    if get_content(path) != "":
        print "Error: File is not empty."
    os.remove(path)

def append_to_empty_file(path):
    print "Appending to empty file and reading"
    append(path, short)
    if get_content(path) != short:
        print "Error: Appending short text was unsuccessful."
    os.remove(path)
    append(path, long)
    if get_content(path) != long:
        print "Error: Appending long text was unsuccessful."
    os.remove(path)

def truncat_empty_file(path):
    print "Truncating empty file and reading"
    write(path, short)
    if get_content(path) != short:
        print "Error: Truncating to short text was unsuccessful."
    os.remove(path)
    write(path, long)
    if get_content(path) != long:
        print "Error: Truncating to long text was unsuccessful."
    os.remove(path)

def append_to_file(path):
    print "Appending to non empty file and reading"
    assert_file_not_exists(path)
    append(path, short)
    if get_content(path) != short:
        print "Error: Appending short text was unsuccessful."
    append(path, long)
    if get_content(path) != short+long:
        print "Error: Appending long text was unsuccessful."
    os.remove(path) 


def truncate_file(path):
    print "Truncating non empty file and reading"
    append(path, short)
    write(path, short)
    if get_content(path) != short:
        print "Error: Truncating to short text was unsuccessful."
    os.remove(path) 
    append(path, long)
    write(path, long)
    if get_content(path) != long:
        print "Error: Truncating to long text was unsuccessful."
    os.remove(path) 

def move_empty_file(path):
    print "Moving empty file"
    touch(path)
    os.rename(path, path+" renamed")
    assert_file_exists(path+" renamed")
    assert_file_not_exists(path)
    if get_content(path+" renamed") != "":
        print "Error: Moved emtpy file is not empty."
    os.remove(path+" renamed") 


def move_file(path):
    print "Moving non empty file"
    write(path, long)
    os.rename(path, path+" renamed")
    assert_file_exists(path+" renamed")
    assert_file_not_exists(path)
    if get_content(path+" renamed") != long:
        print "Error: Moved file has wrong content."
    os.remove(path+" renamed") 

def move_to_existing_file(path):
    print "Moving to existing file"
    write(path+" 1", long)
    assert_file_exists(path+" 1")
    write(path+" 2", short)
    assert_file_exists(path+" 2")
    os.rename(path+" 1", path+" 2")
    assert_file_exists(path+" 2")
    assert_file_not_exists(path+" 1") 
    if get_content(path+" 2") != long:
        print "Error: Moved file has wrong content."
    os.remove(path+" 2") 

def copy_empty_file(path):
    print "Copying empty file"
    touch(path)
    shutil.copyfile(path, path+" copy")
    assert_file_exists(path+" copy")
    assert_file_exists(path)  
    if get_content(path) != "":
        print "Error: Source file has wrong content."
    if get_content(path+" copy") != "":
        print "Error: Copied file has wrong content."
    os.remove(path+" copy") 
    os.remove(path) 

def copy_file(path):
    print "Copying non empty file"
    write(path, long)
    shutil.copyfile(path, path+" copy")
    assert_file_exists(path+" copy")
    assert_file_exists(path)  
    if get_content(path+" copy") != long:
        print "Error: Copied file has wrong content."
    os.remove(path+" copy") 
    os.remove(path) 

def copy_to_existing_file(path):
    print "Copying to existing file"
    write(path+" 1", long)
    write(path+" 2", short) 
    assert_file_exists(path+" 1")
    assert_file_exists(path+" 2")  
    shutil.copyfile(path+" 1", path+" 2") 
    assert_file_exists(path+" 1")
    assert_file_exists(path+" 2")  
    if get_content(path+" 1") != long:
        print "Error: Source file has wrong content."
    if get_content(path+" 2") != long:
        print "Error: Copied file has wrong content."
    os.remove(path+" 1")
    os.remove(path+" 2")

def create_folder(path):
    print "Create Folder"
    os.makedirs(path)
    if not os.path.exists(path):
        print "Error: Directory does not exists."

def remove_folder(path):
    print "Remove Folder"
    os.rmdir(path) 
    if os.path.exists(path):
        print "Error: Directory does still exists."

def create_nested_folder(path):
    print "Create Nested Folder"
    os.makedirs(path)
    os.makedirs(path+"/nested")
    if not os.path.exists(path+"/nested"):
        print "Error: Nested directory does not exists."

def remove_nested_folder(path):
    print "Remove Nested Folder"
    os.rmdir(path+"/nested")
    os.rmdir(path)
    if os.path.exists(path+"/nested"):
        print "Error: Nested directory does still exists."
    if os.path.exists(path):
        print "Error: Nested directory does still exists."

def list_directory(path):
    print "Listing directory?"
    os.mkdir(path)
    if os.listdir(path) != []:
        print "Error: New directory is not empty."
    os.makedirs(path+"/nested dir")
    if os.listdir(path) != ['nested dir']:
        print "Error: New nested directory is not listed."
    touch(path+"/nested file")    
    assert_file_exists(path+"/nested file")   
    if set(os.listdir(path)) != set(['nested dir','nested file']):
        print "Error: New nested file is not listed."


if __name__ == '__main__':
    main()
