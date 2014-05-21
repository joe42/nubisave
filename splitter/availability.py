#from itertools import *
from sys import exit
import sys
import signal
import shelve
import datetime
#from multiprocessing import Process
#from multiprocessing import Pool
            
stop = False

def signal_handler(signal, frame):
    global stop
    stop = True
    print "stopping"
    
def choose(n, k):
    """
    A fast way to calculate binomial coefficients by Andrew Dalke (contrib).
    """
    if 0 <= k <= n:
        ntok = 1
        ktok = 1
        for t in xrange(1, min(k, n - k) + 1):
            ntok *= n
            ktok *= t
            n -= 1
        return ntok // ktok
    else:
        return 0
    
def combinations(iterable, r):
    # combinations('ABCD', 2) --> AB AC AD BC BD CD
    # combinations(range(4), 3) --> 012 013 023 123
    pool = tuple(iterable)
    n = len(pool)
    if r > n:
        return
    indices = range(r)
    yield tuple(pool[i] for i in indices)
    while True:
        for i in reversed(range(r)):
            if indices[i] != i + n - r:
                break
        else:
            return
        indices[i] += 1
        for j in range(i+1, r):
            indices[j] = indices[j-1] + 1
        yield tuple(pool[i] for i in indices)
      
def powerset(some_list, k, m):
    '''Subset of the powerset of some_list.
    The subset contains all elements of the powerset that have at least k elements.
    But if m>k, then the subset contains the other elements in the powerset.
    some_list is a multiset'''
    n = k+m
    if m>k: # k is small, so there are many combinations with at least k elements - instead consider combinations with less than k elements
        rng = range(0,k+1) # 0,1,2...k-1 stores (with less than k elements)
    else: 
        rng = range(1,len(some_list)+1) # 1,2...n stores (with more than k elements)
    for r in rng:
        for subset in combinations(some_list, r):
            if m>k and not has_k_elements(k, subset): 
                yield map(lambda x: x[0], subset)
            elif m<=k and has_k_elements(k, subset):
                yield map(lambda x: x[0], subset)

def has_k_elements(k, store_list):
    '''return: True iff at least #k elements are in store_list'''
    nr_of_elements = 0
    ids = []
    dependent_stores = [] 
    for a, e, id, dependency in store_list:
        ids.append(id)
        if dependency:
            dependent_stores.append((dependency,e))
        else:
            nr_of_elements += e
        if nr_of_elements >= k:
            return True
    for dependency, e in dependent_stores:
        if dependency in ids:
            nr_of_elements += e
        if nr_of_elements >= k:
            return True
    return False
        

def sequence_generator(start_from_list=None):
    ret = [0.01]
    while True:
        idx = 0
        while not idx+1 == len(ret) and not ret[idx] < ret[idx+1]:
            ret[idx] = 0.01
            idx += 1
        if ret[idx] == 0.99:
            ret[idx] = 0.01
            ret.append(0.01)
        else:
            ret[idx] += 0.01
        yield ret
        
def get_availability(k, availabilities_list):
    result = 0 # availability
    for i in range(len(availabilities_list)):
        value = availabilities_list[i]
        if len(value) == 2:
            availabilities_list[i] = (value[0],value[1],i+1,0)
        if len(value) == 3:
            availabilities_list[i] = (value[0],value[1],i+1,value[2])
    m = sum(e for a, e, id, dependency in availabilities_list) - k #number of coding elements
    n = k + m
    store_list = map(lambda x: x[0], availabilities_list)
    if len(availabilities_list) == 0:
        return 0
    elif len(availabilities_list) == 1:
        return availabilities_list[0][0]
    elif n == len(availabilities_list): #if each store has one element
        if len(set(availabilities_list)) <= 1: #if each store has the same availability
            a = availabilities_list[0][0] #average availability
            return sum([choose(n,x)*pow(a,x)*pow((1-a),n-x) for x in range(k,n+1)])
    for S in powerset(availabilities_list, k, m):
        av = uv = 1
        for a in S:
            av *= a
        unavailabilities_list = list(store_list) # Stores not in set S
        for e in S:
            unavailabilities_list.remove(e)
        #print "uavset "+repr(unavailabilities_list)
        for u in unavailabilities_list:
            uv *= 1-u
        result += av * uv
    if m>k:
        result = 1 - result
    return result
       
    
def main():        
    signal.signal(signal.SIGINT, signal_handler)
    k = int(sys.argv[1]) #number of elements sufficient for object availability
    availabilities_list = eval(sys.argv[2]) #[(0.5,3), (0.5,1,0), (0.6,1,1)] # third store depends on first store; 0 as a third element of the tuple or only two elements in a tuple mean that they do not depend on another store
    
    print get_availability(k, availabilities_list)
    
     
#        print "k: "+str(k)+" av: "+str(result)
            

if __name__ == "__main__":
    main()
    
