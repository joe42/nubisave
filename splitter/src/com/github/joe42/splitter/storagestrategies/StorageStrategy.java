package com.github.joe42.splitter.storagestrategies;

import java.util.List;
import java.util.Map;
/**
 * Interface used by the splitter to determine how a file should be stored.
 * First, it is used to obtain the paths to write the file fragments to. 
 * Further, by the number of returned directories it determines when a complete file is stored successfully.
 */
public interface StorageStrategy {
	/**
	 * Get the directories to store the file fragments to
	 * The paths must not end in "/". Determines the number and location of fragments for each complete file. It is possible to have the same paths multiple times 
	 * to increase the number of file parts stored to this location.
	 * @return a list of directory paths 
	 */
	public List<String> getFragmentDirectories();
	
	/**
	 * Get the number of file fragments that must be stored successfully. 
	 * This is used to determine when a complete file is stored successfully.
	 * For instance, an implementation might return the number of locations returned by {@link #getFragmentDirectories() getFragmentDirectories()}.
	 * Therefore the splitter will wait until all files are written or report an error otherwise. 
	 * On the other hand, returning a number less than the number of fragment locations, might cause the splitter to report success more quickly and  
	 * to either abort writing file fragments before all fragments are written or continue writing in the background.
	 * This strategy can improve throughput.
	 * @return the number of file fragments that must be stored
	 */
	public int getNrOfRequiredSuccessfullyStoredFragments();
	
	/**
	 * Get the number of fragments that can be lost or corrupted before the file cannot be restored.
	 * A higher value means that the file fragments are larger. On the other hand, the complete file  
	 * might be retrieved more quickly in the following scenario. The file parts are stored on different devices with 
	 * varying response times. Each of the devices are read in parallel. Since the file to retrieve is stored with a 
	 * high redundancy, it can be restored as soon as enough stores have sent the file. The other file parts from the stores, 
	 * which were slower to respond can be ignored. This scenario depends on a large bandwidth capacity.
	 * A lower value reduces redundancy, which leads to smaller file size, lower bandwidth load and therefore maybe to lower costs.
	 * @return the number of redundant fragments 
	 */
	public int getNrOfRedundantFragments();
	
	
	/**
	 * Returns true iff files that are already stored should be stored using this storage strategy instead of other.
	 * @param other the StorageStrategy  object to compare 
	 * @return
	 */
	public boolean changeToCurrentStrategy(StorageStrategy other);
	
	/**
	 * Get the minimal availability of files achieved by this StorageStrategy instance.
	 * The calculation considers the availability of the storages on which the {@link #getFragmentDirectories() fragment directories} are located.
	 * The availability of NubiSave itself does not need to be considered.
	 * @return the availability in percent
	 */
	public double getStorageAvailability();

	/**
	 * Get estimated redundancy factor for the data stored.
	 * @return the redundancy factor, which is one if the size of the data stored remains the same as the original data's size
	 */
	public double getStorageRedundancy();

	public Map<String, String> getCodecInfo();
}
