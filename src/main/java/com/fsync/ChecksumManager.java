package com.fsync;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The class is responsible for keeping track of the checksum of all the files
 * in the synchronized directory.
 * @author shreyas shinde
 *
 */
public class ChecksumManager {
	/** To keep the checksum of each of the files in the shared directory */
	private Map<String, String> checksums = new HashMap<String, String>();
	
	/** The path to the sync folder so that this class can keep relative paths */
	private String syncDirectory = null;
	
	/** The logger */
	private Logger logger = Logger.getLogger(ChecksumManager.class.getName());
	
	/**
	 * Constructs a new checksum manager with a reference to the shared/synchronized
	 * directory.
	 * @param syncDirectory the path to the shared directory.
	 */
	public ChecksumManager(String syncDirectory) {
		if(syncDirectory == null) {
			throw new NullPointerException("The path to sync directory cannot be null or empty.");
		}
		this.syncDirectory = syncDirectory;
	}
	
	/**
	 * This method creates a checksum of every file in the directory and
	 * caches the checksum in memory. The checksums are then ready for
	 * consultation. This method may take some time as every file in the
	 * directory is visited.
	 */
	public void createChecksumOnDirectory() throws IOException {
		logger.info("Creating checksum on directory: " + syncDirectory);
		FileVisitor fv = new FileVisitor();
		Files.walkFileTree(Paths.get(syncDirectory), fv);
		List<Path> files = fv.getAllFiles();
		for(Path f : files) {
			String checksum = ChecksumUtil.computeChecksumForFile(f.toFile().getAbsolutePath());
			checksums.put(f.toFile().getAbsolutePath().replace(syncDirectory, ""), checksum);
		}
		logger.info("Checksum created for " + files.size() + " files.");
	}
	
	/**
	 * Updates the checksum on a file. This method does not validate the checkum but
	 * simply updates it.
	 * @param checksum the new checksum for the file
	 * @param filepath the full path to the file for which to update the checksum.
	 */
	public void updateChecksumOnFile(String checksum, String filepath) {
		checksums.put(filepath.replace(syncDirectory, ""), checksum);
	}
	
	/**
	 * Validates the checksum of a file with the expected value.
	 * @param expected the expected checksum of the file
	 * @param filepath the absolute path to the file
	 * @return true if the checksum is valid, false otherwise
	 */
	public boolean isChecksumValid(String expected, String filepath) {
		String key = filepath.replace(syncDirectory, "");
		if(!checksums.containsKey(key)) {
			return false;
		}
		return checksums.get(key).equalsIgnoreCase(expected);
	}
	
	/**
	 * Returns the checksum of a file in the shared directory.
	 * @param filepath the absolute path to the file whose checksum needs to be returned
	 * @return the checksum if the file is present, null otherwise.
	 */
	public String getChecksum(String filepath) {
		String key = filepath.replace(syncDirectory, "");
		return checksums.get(key);
	}
	
	/**
	 * Returns the checksums on all the files in the shared directory.
	 * @return a map of the relative file names in the shared directory and 
	 *         their checksums.
	 */
	public Map<String,String> getChecksumOnDirectory() {
		return new HashMap<String,String>(checksums);
		
	}
	
	/**
	 * This class returns the list of files traversed in 
	 * a directory.
	 * @author shreyas shinde
	 *
	 */
	private static class FileVisitor extends SimpleFileVisitor<Path> {
		List<Path> paths = new ArrayList<Path>();
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if(attrs.isRegularFile()) {
				paths.add(file);
			}
			return super.visitFile(file, attrs);
		}
		
		/**
		 * Returns a list of all the files under a given path.
		 * @return
		 */
		public List<Path> getAllFiles() {
			return paths;
		}
	}
}
