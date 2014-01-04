package com.fsync;

/**
 * This interface listens for changes in the file system
 * directory structure.
 * @author shreyas shinde
 *
 */
public interface DirectoryChangeListener {
	/**
	 * This method is invoked by any directory observer when there is a 
	 * change in the directory structure. The {@value}dirChangeEvent
	 * object contains information about the change.
	 * @param dirChangeEvent the DirectoryChangeEvent object that contains information
	 *                       about the change
	 */
	public void listen(DirectoryChangeEvent dirChangeEvent);
	
	/**
	 * Returns the name of the listener. This name should be unique to each
	 * listener.
	 * @return
	 */
	public String getName();
}