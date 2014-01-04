package com.fsync;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

import com.google.common.io.Files;


/**
 * This class represents the Http interface through which the application receives and broadcasts
 * changes in the directory structure to keep them synchronized.
 * @author shreyas shinde
 *
 */
public class Peer2PeerCommunicator implements DirectoryChangeListener {
	/** Default ports */
	public static final int DEFAULT_HTTP_PORT 		= 10080;
	public static final int DEFAULT_HTTPS_PORT 		= 10443;
	
	/** Http parameters */
	private static final String EVENT_PARAM     	= "event";
	private static final String FILE_PARAM         	= "file";
	
	/** HTTP listener ports */
	private int httpPort  							= DEFAULT_HTTP_PORT;
	private int httpsPort 							= DEFAULT_HTTPS_PORT;
	
	
	/** The HTTP server that will handle requests/operations */
	private Server httpServer 						= null;
	
	/** We will be using a file-system disk based uploads */
	private FileItemFactory fileItemFactory 		= new DiskFileItemFactory();
	
	/** The file upload handler */
	private ServletFileUpload upload 				= null;
	
	private static final Logger logger 				= Logger.getLogger(Peer2PeerCommunicator.class.getName());
	
	/** List of peers to which the service is connected */
	private List<String> peers                      = new ArrayList<String>();
	
	/**
	 * Constructs a new communicator that listens on a specific HTTP port.
	 * @param httpPort the port on which the communicator listens for HTTP requests.
	 */
	public Peer2PeerCommunicator(int httpPort) {
		// Uses custom HTTP port
		this.httpPort = httpPort;
		
		// Set a temporary directory for the file item factory
		//File tmpDir = Files.createTempDir();
		upload = new ServletFileUpload(fileItemFactory);
		
		// Get list of peers
		String initial = AppProperties.get(AppProperties.SYNC_PEERS);
		if(initial != null) {
			String[] values = initial.split(",");
			for(String value : values) {
				peers.add(value);
			}
		}
	}
	
	/**
	 * Constructs a new communicator that listens on default HTTP port.
	 */
	public Peer2PeerCommunicator() {
		this(DEFAULT_HTTP_PORT);
	}

	public void listen(DirectoryChangeEvent dirChangeEvent) {
		// TODO Auto-generated method stub
		
	}

	public String getName() {
		return Peer2PeerCommunicator.class.getName();
	}
	
	/**
	 * Starts the http server. Once started, the server is then ready
	 * to accept requests for connection and broadcasting of events.
	 * @throws Exception If the operation could not be completed successfully.
	 */
	public void start() throws Exception {
		httpServer = new Server(httpPort);
		
		// Create a context that handles the updates
		ContextHandler updateContext = new ContextHandler();
		updateContext.setHandler(new UpdateHandler());
		updateContext.setContextPath("/update");
		updateContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		updateContext.setResourceBase(".");
		logger.fine("Setting the update context.");
		
		// Create a context that handles stopping the service
		ContextHandler stopContext = new ContextHandler();
		stopContext.setContextPath("/stop");
		stopContext.setHandler(new StopHandler());
		stopContext.setClassLoader(Thread.currentThread().getContextClassLoader());
		stopContext.setResourceBase(".");
		logger.fine("Setting the update context.");
	
		// Create a collection of context each to match an operation
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.addHandler(updateContext);
		contexts.addHandler(stopContext);
		httpServer.setHandler(contexts);
		
		logger.info("Starting the http listener.");
		httpServer.start();
		httpServer.join();
	}
	
	/**
	 * Stops the http server. Once the server is stopped, it can no longer
	 * broadcasts events or listen to and file change events from its peers.
	 * @throws Exception If the operation could not be completed successfully.
	 */
	public void stop() throws Exception {
		logger.info("Stopping the http listener.");
		if(httpServer != null) {
			httpServer.stop();
		}
		logger.info("Http listener stopped.");
	}
	
	/**
	 * This method applies the change sent by the peers to the local file system under
	 * observation.
	 * @param dce
	 * @param data
	 */
	private void updateDirectory(DirectoryChangeEvent dce, InputStream data) {
		if(dce.getType() == null) {
			throw new NullPointerException("The directory change event must always have a type.");
		}
		switch(dce.getType()) {
		case CREATED:
		case MODIFIED:
			// The data needs to be written out to disk
			if(data == null) {
				throw new RuntimeException("The directory change event of type created or modified must be accompanied by file data.");
			}
			// Copy the file from input to its appropriate location
			String filepath = AppProperties.get(AppProperties.SYNC_DIR) + dce.getRelativeFilePath();
			FileOutputStream out = null;
			try {
				out = new FileOutputStream(filepath);
				logger.info("Updating file: " + filepath);
				Streams.copy(data, out, true);
				out.close();
			} catch (Exception e) {
				logger.severe("Failed to update file: " + filepath);
			} finally {
				if(data != null) {
					try {
						data.close();
					} catch (IOException ignore) {}
				}
				if(out != null) {
					try {
						out.close();
					} catch(IOException ignore) {}
				}
			}
			break;
		case DELETED:
			// Delete the file from disk
			File f = new File(dce.getRelativeFilePath());
			if(f.exists()) {
				if(!f.delete()) {
					logger.warning("Failed to delete file: " + f.getAbsolutePath());
				} else {
					logger.info("File deleted: " + f.getAbsolutePath());
				}
			}
			break;
		}
	}
	
	/**
	 * This class handles the HTTP update requests to the server.
	 * The update notification should typically contain the full file
	 * name and optional data associated with the change.
	 * @author shreyas shinde
	 *
	 */
	public class UpdateHandler extends AbstractHandler {
		public void handle(String target, Request baseRequest, HttpServletRequest request,
				HttpServletResponse response) throws IOException, ServletException {
			// Is the request a multi-part request
			if(!ServletFileUpload.isMultipartContent(request)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The request is not a multipart request.");
				return;
			}
			
			// Parse the request
			String event = null;
			InputStream data = null;
			try {
				List<FileItem> items = upload.parseRequest(request);
				if(items != null) {
					for(FileItem item : items) {
						// Handle event
						if(item.isFormField() && item.getFieldName().equalsIgnoreCase(EVENT_PARAM)) {
							event = item.getString();
							continue;
						}
						// Handle file
						if(item.getFieldName().equalsIgnoreCase(FILE_PARAM)) {
							data = item.getInputStream();
						}
					}
				}
				if(event == null) {
					response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The 'event' parameter not found in the request.");
					return;
				}
				
				// Update the local directory with the change prescribed in the event
				DirectoryChangeEvent dce = DirectoryChangeEvent.fromJSON(event);
				updateDirectory(dce, data);
			} catch (FileUploadException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
				return;
			}
		}
	}//UpdateHandler
	
	/**
	 * The stop handler stops the http server causing the application to eventually shutdown.
	 * @author shreyas shinde
	 *
	 */
	public class StopHandler extends AbstractHandler {
		public void handle(String arg0, Request arg1, HttpServletRequest arg2,
				HttpServletResponse arg3) throws IOException, ServletException {
			// TODO: Check security
			
			// Stop the http server
			try {
				stop();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
		}
		
	}
}
