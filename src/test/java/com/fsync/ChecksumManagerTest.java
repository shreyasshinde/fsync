package com.fsync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fsync.ChecksumManager;

public class ChecksumManagerTest {
	public static String testDir = "/tmp/ChecksumManagerTest";
	
	@AfterClass
	public static void afterClass() throws Exception {
		// Delete the test directory
		Files.delete(Paths.get(testDir));
	}
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		// Create the test directory
		new File(testDir).mkdirs();
	}
	
	@Test
	public void testCreateChecksumOnDirectory() throws Exception {
		// Create two files in the test directory
		File f1 = createTestFileWithData("f1.dat", "abcefghijklmnopqrstuvwxyz");
		File f2 = createTestFileWithData("f2.dat", "1234567890");
		
		// Test
		ChecksumManager cm = new ChecksumManager(testDir);
		cm.createChecksumOnDirectory();
		assertEquals("", cm.getChecksum("f1.dat"));
		assertTrue(cm.isChecksumValid("", "f2.dat"));
		
		// Delete
		f1.delete();
		f2.delete();
	}

	@Test
	public void testUpdateChecksumOnFile() throws Exception {
		// Create two files in the test directory
		File f1 = createTestFileWithData("f1.dat", "abcefghijklmnopqrstuvwxyz");
		File f2 = createTestFileWithData("f2.dat", "1234567890");

		// Test
		ChecksumManager cm = new ChecksumManager(testDir);
		cm.createChecksumOnDirectory();
		cm.updateChecksumOnFile("testchecksum", "f1.dat");
		assertEquals("testchecksum", cm.getChecksum("f1.dat"));
		
		// Cleanup
		f1.delete();
		f2.delete();
	}

	@Test
	public void testIsChecksumValid() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChecksum() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChecksumOnDirectory() throws Exception {
		// Create two files in the test directory
		File f1 = createTestFileWithData("f1.dat", "abcefghijklmnopqrstuvwxyz");
		File f2 = createTestFileWithData("f2.dat", "1234567890");

		// Test
		ChecksumManager cm = new ChecksumManager(testDir);
		cm.createChecksumOnDirectory();
		Map<String,String> actual = cm.getChecksumOnDirectory();
		assertEquals(2, actual.size());
		
		// Cleanup
		f1.delete();
		f2.delete();
	}
	
	/**
	 * Creates a test file with some data.
	 * @param filename
	 * @param data
	 * @return
	 * @throws Exception
	 */
	private static File createTestFileWithData(String filename, String data) throws Exception {
		File f1 = Paths.get(testDir, "f1.dat").toFile();
		f1.createNewFile();
		FileWriter fw = new FileWriter(f1);
		fw.write(data);
		fw.close();
		return f1;
	}
}
