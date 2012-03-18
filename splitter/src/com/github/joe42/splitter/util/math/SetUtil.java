package com.github.joe42.splitter.util.math;

import java.util.*;
import java.util.Set;

/**
 * Utility class for set operations.
 */
public class SetUtil {
	
	/**
	 * Get the power set of originalSet.
	 * The power set is the set of all of its subsets
	 * @param <T>
	 * @param originalSet
	 * @return  the power set of originalSet
	 */
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new HashSet<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }		
	    return sets;
	}
}
