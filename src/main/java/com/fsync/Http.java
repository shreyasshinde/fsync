package com.fsync;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.fileupload.util.Streams;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * This class contains static utility functions to
 * invoke HTTP operations.
 * @author shreyas shinde
 *
 */
public class Http {
	
	/**
	 * This method returns the resource identified by the {@code url} parameter.
	 * If there are parameters, they are URL encoded before they are sent to the server.
	 * @param url the resource that is requested
	 * @param optParams the optional bag of query parameters
	 * @return the content returned in byte[] format
	 */
	public static byte[] get(String url, Map<String, String> optParams) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		InputStream in = null;
		try {
			// Using a URIBuilder to make it easy to set parameters
			URIBuilder uriBuilder = new URIBuilder(url);
			if(optParams != null && optParams.size() > 0) {
				for(Entry<String, String> param: optParams.entrySet()) {
					uriBuilder.setParameter(param.getKey(), param.getValue());
				}
			}
			
			// Create the get request
			HttpGet httpGet = new HttpGet(uriBuilder.build());
			
			// Executing get
			response = httpClient.execute(httpGet);
			
			// Status code check
			int status = response.getStatusLine().getStatusCode();
			if(status != 200) {
				throw new IOException("The server returned error code: " + status);
			}
			
			// Convert the response to byte[]
			in = response.getEntity().getContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Streams.copy(in, baos, true);
			return baos.toByteArray();
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ignore) {
			}
			try {
				response.close();
			} catch (IOException ignore) {
			}
		}
	}
	
	/**
	 * This method performs an HTTP POST operation on the give URL.
	 * @param url the URL to which we need to post parameters
	 * @param optParams an optional bag of parameters
	 * @return the response of the server in byte[] form
	 */
	public static byte[] post(String url, Map<String,String> optParams) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		InputStream in = null;
		try {
			// Constructing a UrlEncodedFormEntity to process text
			// parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			UrlEncodedFormEntity entity = null;
			if(optParams != null && optParams.size() > 0) {
				for(Entry<String, String> param: optParams.entrySet()) {
					params.add(new BasicNameValuePair(param.getKey(), param.getValue()));
				}
				entity = new UrlEncodedFormEntity(params, Consts.UTF_8);
			}
			
			// Create the post request
			HttpPost httpPost = new HttpPost(url);
			if(entity != null) {
				httpPost.setEntity(entity);
			}
			
			// Executing post
			response = httpClient.execute(httpPost);
			
			// Status code check
			int status = response.getStatusLine().getStatusCode();
			if(status != 200) {
				throw new IOException("The server returned error code: " + status);
			}
			
			// Convert the response to byte[]
			in = response.getEntity().getContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Streams.copy(in, baos, true);
			return baos.toByteArray();
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ignore) {
			}
			try {
				response.close();
			} catch (IOException ignore) {
			}
		}
	}
	
	/**
	 * This method creates a multipart form request and uses HTTP POST to deliver
	 * it to a server.
	 * @param url the URL to which we need to post parameters
	 * @param optParams an optional bag of parameters
	 * @param optFiles an optional bag of files that need to be sent to the server
	 * @return the response of the server in byte[] form
	 */
	public static byte[] post(String url, Map<String, String> optParams, Map<String,File> optFiles) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = null;
		InputStream in = null;
		try {
			// Using a MultipartEntityBuilder to set text and file params
			MultipartEntityBuilder meb = MultipartEntityBuilder.create();
			if(optParams != null && optParams.size() > 0) {
				for(Entry<String, String> param: optParams.entrySet()) {
					meb.addTextBody(param.getKey(), param.getValue());
				}
			}
			// Process files
			if(optFiles != null && optFiles.size() > 0) {
				for(Entry<String,File> file : optFiles.entrySet()) {
					meb.addBinaryBody(file.getKey(), file.getValue());
				}
			}
			
			HttpEntity entity = meb.build();
			
			// Create the post request
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(entity);
			
			// Executing post
			response = httpClient.execute(httpPost);
			
			// Status code check
			int status = response.getStatusLine().getStatusCode();
			if(status != 200) {
				throw new IOException("The server returned error code: " + status);
			}
			
			// Convert the response to byte[]
			in = response.getEntity().getContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Streams.copy(in, baos, true);
			return baos.toByteArray();
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				httpClient.close();
			} catch (IOException ignore) {
			}
			try {
				response.close();
			} catch (IOException ignore) {
			}
		}
	}
}
