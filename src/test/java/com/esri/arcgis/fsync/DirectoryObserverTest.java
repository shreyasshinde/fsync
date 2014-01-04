package com.esri.arcgis.fsync;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fsync.DirectoryChangeEvent;
import com.fsync.DirectoryChangeListener;
import com.fsync.DirectoryObserver;

public class DirectoryObserverTest {
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
	public void testStart() throws Exception {
		DirectoryObserver dirObs = new DirectoryObserver();
		
		// Start the observer
		dirObs.start();
		
		// Register the listener
		TestDirectoryChangeListener testListener = new TestDirectoryChangeListener();
		dirObs.registerListener(testListener);
		
		// Register a path
		String testDir = "/tmp/DirectoryObserverTest";
		new File(testDir).mkdirs();
		dirObs.registerDirectory(testDir);
		
		// Create a new file in the test directory
		OutputStream os = new FileOutputStream(testDir + File.separator + "testA.dat");
		os.flush();
		os.close();
		
		// Sleep a bit
		Thread.sleep(20000);
		
		// The listener should have been invoked
		assertEquals(1, testListener.getAllReceivedEvents().size()); //created
		testListener.clearAllReceivedEvents();
		
		// Delete the file
		new File(testDir + File.separator + "testA.dat").delete();
		
		// Sleep a bit
		Thread.sleep(20000);
		
		// The listener should have been invoked
		assertEquals(1, testListener.getAllReceivedEvents().size()); //deleted
		testListener.clearAllReceivedEvents();
		dirObs.stop();
	}

	@Test
	public void testStop() throws Exception {
		DirectoryObserver dirObs = new DirectoryObserver();
		
		// Start the observer
		dirObs.start();

		// Register the listener
		TestDirectoryChangeListener testListener = new TestDirectoryChangeListener();
		dirObs.registerListener(testListener);

		// Register a path
		String testDir = "/tmp/DirectoryObserverTest";
		new File(testDir).mkdirs();
		dirObs.registerDirectory(testDir);
		
		// Stop the observer
		dirObs.stop();

		// Create a new file in the test directory
		OutputStream os = new FileOutputStream(testDir + File.separator
				+ "testA.dat");
		os.flush();
		os.close();

		// Sleep a bit
		Thread.sleep(20000);

		// The listener should NOT have been invoked
		assertEquals(0, testListener.getAllReceivedEvents().size());
		testListener.clearAllReceivedEvents();

		// Delete the file
		new File(testDir + File.separator + "testA.dat").delete();

		// Sleep a bit
		Thread.sleep(20000);

		// The listener should NOT have been invoked
		assertEquals(0, testListener.getAllReceivedEvents().size());
		testListener.clearAllReceivedEvents();
	}

	/**
	 * A test listener of directory change events.
	 * @author shreyas shinde
	 *
	 */
	public class TestDirectoryChangeListener implements DirectoryChangeListener {
		private ArrayList<DirectoryChangeEvent> list = new ArrayList<DirectoryChangeEvent>();

		public void listen(DirectoryChangeEvent dirChangeEvent) {
			list.add(dirChangeEvent);
			System.out.println(dirChangeEvent);
		}

		public String getName() {
			return "TestDirectoryChangeListener";
		}
		
		public List<DirectoryChangeEvent> getAllReceivedEvents() {
			return list;
		}
		
		public void clearAllReceivedEvents() {
			list.clear();
		}
	}
}
