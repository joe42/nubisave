package com.github.joe42.splitter.storagestrategies;

import java.util.List;

/**
 * Uses all potential storage directories. 
 * Storing a file is always successful. Therefore, the splitter does not have to wait until all fragments are stored. 
 * Since the failure of storing some fragments may be ignored, this strategy can lower the possibility of recreating the file.
 * Also, there is no guarantee that the file can be accessed after writing it, even though the file may eventually be accessible.
 * On the other hand, storing the fragments can be done in the background, which might increase the speed.
 * So this storage strategy is interesting for data that needs to be stored quickly and looses importance over time.
 */
public class FastUseAllInParallelStorageStrategy extends
		UseAllInParallelStorageStrategy {
	/**
	 * Creates a FastUseAllParallelStorageStrategy object using all potentialStorageDirectories in parallel with half the number of #potentialStorageDirectories - 1 as expendable stores.
	 * @param potentialStorageDirectories
	 */
	public FastUseAllInParallelStorageStrategy(List<String> potentialStorageDirectories) {
		super(potentialStorageDirectories);
	}

	/**
	 * Get the number of file fragments that must be stored successfully.
	 * Return the number of elements needed to reconstruct the complete file. 
	 * @return the number of file fragments that must be stored
	 */
	@Override
	public int getNrOfRequiredSuccessfullyStoredFragments() {
		return potentialStorageDirectories.size()-getNrOfRedundantFragments();
	}
}
