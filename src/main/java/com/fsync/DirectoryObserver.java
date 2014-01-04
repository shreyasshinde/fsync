package com.fsync;

import java.io.File;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fsync.DirectoryChangeEvent.DirectoryChangeEventType;

/**
 * This class observes one or more file system directories and notifies
 * the interested listeners of any change in the directory.
 * @author shreyas shinde
 *
 */
public class DirectoryObserver {

	/** Contains list of listeners registered with the observer */
	private Map<String, DirectoryChangeListener> listeners = new HashMap<String, DirectoryChangeListener>();
	
	/** Contains list of directories that need to be observed */
	private Set<String> directories = new HashSet<String>();
	
	/** The service that watches directories and notifys of change */
	private WatchService watcher = null;
	
	/** A boolean flag to indicate to the observer thread to stop observing */
	private boolean stopObserverThread = false;
	
	/** To log events */
	private Logger logger = Logger.getLogger(DirectoryObserver.class.getName());
	
	/** To keep track of all the paths and keys that are being observed */
	private Map<WatchKey, Path> keys = new HashMap<WatchKey, Path>();
	
	/**
	 * Starts a new instance of the observer. For the observer to watch
	 * directories they need to be registered using the {@code} registerDirectory
	 * operation.
	 * @throws IOException
	 */
	public void start() throws IOException {
		// Initialize a watcher
		watcher = FileSystems.getDefault().newWatchService();
		
		// Start the observer thread
		new ObserverThread().start();
		
		// If directories have been registered before the start was called,
		// register them again with the watcher service.
		if(directories.size() > 0) {
			for(String dir : directories) {
				registerDirectory(dir, false);
			}
		}
		
		logger.info("Directory observer started.");
	}
	
	/**
	 * Stops the observer. Once stopped, it needs to be started again to observe
	 * changes on the directory.
	 * @throws IOException
	 */
	public void stop() throws IOException {
		stopObserverThread = true;
		if(watcher != null) {
			watcher.close();
		}
		logger.info("Directory observer stopped.");
	}

	/**
	 * Register a listener with the observer.
	 * 
	 * @param listener
	 *            the new listener to be registered with the observer.
	 */
	public void registerListener(DirectoryChangeListener listener) {
		if (listeners.containsKey(listener.getName())) {
			throw new RuntimeException("A listener by name '"
					+ listener.getName()
					+ "' is already registered with the observer.");
		}
		listeners.put(listener.getName(), listener);
	}

	/**
	 * Unregisters a listener from the observer. Once unregistered, the listener
	 * no longer receives any updates from the observer.
	 * 
	 * @param name
	 *            the name of the listener that needs to be unregistered
	 * @return the unregistered DirectoryChangeListener or null if the observer
	 *         does not find any listener by the name
	 */
	public DirectoryChangeListener unregisterListener(String name) {
		return listeners.remove(name);
	}
	
	public void registerDirectory(String dir) throws IOException {
		registerDirectory(dir, true);
	}
	
	/**
	 * Register a new directory that needs to be observed for any changes.
	 * @param dir the full path to the directory that needs to be observed
	 * @param store a boolean to indicate that the method needs to store the directory
	 */
	private void registerDirectory(String dir, boolean store) throws IOException {
		if(!new File(dir).exists()) {
			throw new RuntimeException("Path not found: " + dir);
		}
		Path p = Paths.get(dir);
		if(p == null) {
			throw new IOException("Could not build a Path object for dir: " + dir);
		}
		if(store) {
			directories.add(dir);
		}
		registerPath(p);
	}
 	
	/**
	 * Registers a path with the watcher service. The create, delete and modify
	 * events on the registered directory are observed.
	 * @param path
	 * @throws IOException
	 */
	private void registerPath(Path path) throws IOException {
		if(watcher != null) {
			// Register the directory path
			WatchKey key = path.register(watcher,
					StandardWatchEventKinds.ENTRY_CREATE,
					StandardWatchEventKinds.ENTRY_DELETE,
					StandardWatchEventKinds.ENTRY_MODIFY);
			keys.put(key, path);
			logger.info("Path registered: " + path);
		}
	}
	
	/**
	 * Removes a directory from observation.
	 * @param path
	 * @return a boolean to indicate if the directory was removed
	 */
	public boolean unregisterDirectory(String path) {
		return directories.remove(path);
	}
	
	private class ObserverThread extends Thread {

		@Override
		public void run() {
			logger.info("Observer thread started.");
			while(!stopObserverThread) {
				WatchKey watchKey = null;
				try {
					// Wait for an event - continue if timed out
					watchKey = watcher.poll(10, TimeUnit.SECONDS);
					if(watchKey == null) {
						continue;
					}
					
					if(!keys.containsKey(watchKey)) {
						logger.warning("Watch key '" + watchKey + "' not recognized.");
						continue;
					}
					
					// Process all the events on the key
					for(WatchEvent<?> event : watchKey.pollEvents()) {
						WatchEvent.Kind<?> kind = event.kind();
						if(kind == StandardWatchEventKinds.OVERFLOW) {
							continue;
						}
						
						@SuppressWarnings("unchecked")
						WatchEvent<Path> pathEvent = (WatchEvent<Path>)event; //cast
						Path path = pathEvent.context();
						logger.fine("Event observed:" + path + ", type:" + kind.name() + ".");
						
						// Create an event to notify
						final DirectoryChangeEvent dce = new DirectoryChangeEvent();
						dce.setAbsoluteFilePath(path.toFile().getAbsolutePath()); //absolute
						dce.setRelativeFilePath(path.toFile().getAbsolutePath().replace(keys.get(watchKey).toString(), "")); //relative
						dce.setTime(System.currentTimeMillis());
						if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
							dce.setType(DirectoryChangeEventType.CREATED); 
						} else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
							dce.setType(DirectoryChangeEventType.MODIFIED);
						} else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
							dce.setType(DirectoryChangeEventType.DELETED);
						} else {
							logger.warning("Unknown event type '" + kind.name() + "'.");
							continue;
						}
						
						// Run the notifications in another thread
						Thread t = new Thread(new Runnable() {
							public void run() {
								// Notify all the listeners of the change
								for(DirectoryChangeListener listener : listeners.values()) {
									try {
										listener.listen(dce);
									} catch(Exception e) {
										logger.log(Level.WARNING, "Failed to notify listener of change. " + e.getLocalizedMessage(), e);
									}
								}
							}
						});
						t.start();
					}
				} catch(ClosedWatchServiceException e) {
					logger.log(Level.INFO, "Closing the file observer.");
				} catch(Exception e) {
					logger.log(Level.WARNING, e.getLocalizedMessage(), e);
				} finally {
					// Important!: Reset the watch key
					if(watchKey != null) {
						watchKey.reset();
					}
				}
			}//while
			
			logger.info("Observer thread stopped.");
		}
	}
}
