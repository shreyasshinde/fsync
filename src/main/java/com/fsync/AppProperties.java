package com.fsync;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class returns properties for the fsync application.
 * @author shreyas shinde
 *
 */
public class AppProperties {
	/** Name of the properties file */
	public static final String FSYNC_PROPS_FILE = "fsync.properties";
	
	/** The properties object that contains the read properties */
	public static Properties props = null;
	
	/** Names of properties */
	public static final String SYNC_PEERS       = "sync.peers";
	public static final String SYNC_DIR         = "sync.dir";
	
	static {
		InputStream is;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(FSYNC_PROPS_FILE);
			if(is != null) {
				props = new Properties();
				props.load(is);
			} else {
				throw new FileNotFoundException(FSYNC_PROPS_FILE);
			}
		} catch(Exception e) {
			throw new RuntimeException("Failed to load properties file: " + FSYNC_PROPS_FILE);
		}
	}

	/**
	 * Returns the value of the property if it exists. Null otherwise.
	 * @param propName the name of the property.
	 * @return the value of the property or null otherwise
	 */
	public static String get(String propName) {
		return props.getProperty(propName);
	}
}
