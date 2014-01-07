package com.esri.arcgis.fsync;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fsync.ChecksumUtil;

public class ChecksumUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testComputeChecksumForFile() throws Exception {
		// Create a temporary file with some well known data
		File testFile = File.createTempFile("ChecksumUtilTest", String.valueOf(System.currentTimeMillis()));
		BufferedWriter bw = new BufferedWriter(new FileWriter(testFile));
		bw.write("0123456789abcdefghijklmnopqurstuvwxyz");
		bw.flush();
		bw.close();
		
		// Get the checksum of this file
		String expected = "ehv3bjje6hgv53//ck9h9n6pzti="; //sha-1
		String observed = ChecksumUtil.computeChecksumForFile(testFile.getAbsolutePath());
		assertTrue(observed.equalsIgnoreCase(expected));
	}
	
	@Test
	public void testComputeChecksumForData() throws Exception {
		// Create a temporary file with some well known data
		byte[] data = "0123456789abcdefghijklmnopqurstuvwxyz".getBytes();
		
		// Get the checksum of this data
		String expected = "ehv3bjje6hgv53//ck9h9n6pzti="; //sha-1
		String observed = ChecksumUtil.computeChecksumForData(data);
		assertTrue(observed.equalsIgnoreCase(expected));
	}
}
