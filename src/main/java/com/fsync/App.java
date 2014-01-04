package com.fsync;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main entry point for the fsync service.
 * @author shreyas shinde
 */
public class App {
	private static final Logger logger          = Logger.getLogger(App.class.getName());
	
    public static void main( String[] args ) throws Exception {
    	DirectoryObserver observer= null;
    	try {
			// This communicator will broadcast directory updates to all
    		// peers while listening on an HTTP protocol for changes broadcasted
    		// by peers.
			Peer2PeerCommunicator p2p = new Peer2PeerCommunicator();
			
			// New directory observer
			observer = new DirectoryObserver();
			observer.start();
			observer.registerListener(p2p);

			// Register the directories with the observer
			observer.registerDirectory(AppProperties.get(AppProperties.SYNC_DIR));
			
			// Loop till we are asked to stop
			p2p.start(); //this should block till a stop is invoked
			
			// Stop has been called - program will shutdown
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} finally {
			if(observer != null) {
				observer.stop();
			}
		}
    }
}
      