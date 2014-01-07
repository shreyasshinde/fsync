package com.fsync;

/**
 * The class is responsible for keeping track of the checksum of all the files
 * in the synchronized directory.
 * @author shreyas shinde
 *
 */
public class ChecksumManager {
	
	/** Implements singleton */
	private ChecksumManager(){}
	private static class SingletonHolder {
		public static final ChecksumManager INSTANCE = new ChecksumManager();
	}
	
	/**
	 * Returns the singleton instance of the ChecksumManager class.
	 * @return the reference to the ChecksumManager
	 */
	public ChecksumManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

}
