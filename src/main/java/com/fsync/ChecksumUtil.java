package com.fsync;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.util.Streams;


/**
 * This class can be used to compute the checksum on a sequence of data.
 * @author shreyas shinde
 *
 */
public class ChecksumUtil {
	/** Default algorithm */
	public static final String MESSAGE_DIGEST_ALGORITHM = "SHA-1";
	
	/** Currently configured algorithm */
	private static String messageDigestAlgorithm        = MESSAGE_DIGEST_ALGORITHM;
	
	/**
	 * Returns the currently configured message digest algorithm.
	 * @return the name of the message digest algorithm.
	 */
	public static String getMessageDigestAlgorithm() {
		return messageDigestAlgorithm;
	}

	/**
	 * Changes the current message digest algorithm to a new algorithm. The change will
	 * apply to all invocations henceforth until set or unset method is invoked.
	 * @param messageDigestAlgorithm the name of the algorithm. Supported values : MD5, SHA-1, SHA-256
	 */
	public static void setMessageDigestAlgorithm(String messageDigestAlgorithm) {
		ChecksumUtil.messageDigestAlgorithm = messageDigestAlgorithm;
	}
	
	/**
	 * Reverts the algorithm to its default value of SHA-1.
	 */
	public static void resetMessageDigestAlgorithm() {
		messageDigestAlgorithm = MESSAGE_DIGEST_ALGORITHM; //revert to default
	}

	/**
	 * Computes the MD5 checksum on a file.
	 * @param filepath the path to the file whose checksum needs to be computed. 
	 * @return a base64 encoded string checksum of the contents of the file.
	 */
	public static String computeChecksumForFile(String filepath) {
		InputStream is = null;
		try {
			// We use Java's message digester to simply read all the bytes
			// from the file and then compute the digest.
			MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm);
			is = Files.newInputStream(Paths.get(filepath));
			DigestInputStream dis = new DigestInputStream(is, md);
			while(dis.read() != -1) {
				;
			}
			byte[] digest = md.digest();
			
			// The digest is then Base64 encoded
			return new Base64().encodeAsString(digest).toLowerCase();
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
			}
		}
	}
	
	/**
	 * Computes the checksum of a binary sequence of data.
	 * @param data the byte array containing data that needs to be checksumed
	 * @return a base64 encoded string checksum of the data
	 */
	public static String computeChecksumForData(byte[] data) {
		ByteArrayInputStream bais = null;
		try {
			// Just run the bytes through the DigestInputStream
			MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm);
			bais = new ByteArrayInputStream(data);
			DigestInputStream dis = new DigestInputStream(bais, md);
			while(dis.read() != -1) {
				;
			}
			byte[] digest = md.digest();
			return new Base64().encodeAsString(digest).toLowerCase();
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				bais.close();
			} catch(IOException ignore) {
			}
		}
	}
	
	/**
	 * Copies the bytes from the input stream to output while computing the
	 * checksum.
	 * @param in the source of the bytes 
	 * @param out the destination where the bytes need to be copied
	 * @return the checksum of the bytes transferred from input to output.
	 */
	public static String computeChecksumAndCopy(InputStream in, OutputStream out) {
		try {
			// Just run the bytes through the DigestInputStream
			MessageDigest md = MessageDigest.getInstance(messageDigestAlgorithm);
			DigestInputStream dis = new DigestInputStream(in, md);
			Streams.copy(dis, out, true);
			byte[] digest = md.digest();
			dis.close();
			return new Base64().encodeAsString(digest);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
