#!/usr/bin/python
import os
import ConfigParser
import time
import shutil

HOME = os.environ['HOME']
nubisave_config_path = HOME+'/.nubisave/nubisavemount/config/config'
redundancy = 0

new_storage = 999
previous_availability = 0

def get_config():
    config = ConfigParser.RawConfigParser()
    config.read(nubisave_config_path)
    return config

def set_redundancy(redundancy):
    config = get_config()
    config.set('splitter', 'redundancy', str(redundancy))
    with open(nubisave_config_path, 'wb') as configfile:
        config.write(configfile)

def availability_increased():
    global previous_availability
    config = get_config()
    availability = config.getfloat('splitter', 'availability')
    ret = availability != previous_availability
    previous_availability = availability
    return ret

def get_redundancy():
    config = get_config()
    return config.getint('splitter', 'redundancy')

def increase_availability():
    global redundancy
    while True:
        redundancy = get_redundancy()
        if redundancy == 100:
            break
        if availability_increased():
            break
        set_redundancy(redundancy+1)
    
def add_new_storage_and_reset_redundancy():
    global new_storage
    new_storage += 1
    shutil.copy('../../splitter/mountscripts/directory.ini', HOME+'/.nubisave/nubisavemount/config/'+str(new_storage))
    set_redundancy(0)
    

def initialize_nubisave():
    config = get_config()
    config.set('splitter', 'storagestrategy', 'UseAllInParallel')
    with open(nubisave_config_path, 'wb') as configfile:
        config.write(configfile)
    add_new_storage_and_reset_redundancy()

def main():
    switch = 0
    initialize_nubisave()
    while True:
        os.system('../scripts/test_streaming_files_simple.py %s/.nubisave/nubisavemount/data . "1 2 3 4 5 6 7 8 9 10" 10000;' %HOME)
        increase_availability()
        time.sleep(1)
        if redundancy == 100:
            if switch == 0:
                switch = 1
                continue
            switch = 0
            add_new_storage_and_reset_redundancy()
            
if __name__ == '__main__':
    main()