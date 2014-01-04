package com.fsync;

import java.io.InputStream;

import org.json.JSONObject;

/**
 * This class represents the change in the directory structure as observed
 * by the directory observer.
 * @author shreyas shinde
 *
 */
public class DirectoryChangeEvent {
	/** JSON keys */
	public static final String ABSOLUTE_FILE_PATH = "absoluteFilePath";
	public static final String RELATIVE_FILE_PATH = "relativeFilePath";
	public static final String EVENT_TYPE         = "type";
	public static final String TIME               = "time";
	
			
	private String absoluteFilePath;
	private String relativeFilePath;
	private DirectoryChangeEventType type;
	private long time;

	public String getAbsoluteFilePath() {
		return absoluteFilePath;
	}

	public DirectoryChangeEvent setAbsoluteFilePath(String filePath) {
		this.absoluteFilePath = filePath;
		return this;
	}
	
	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}

	public DirectoryChangeEventType getType() {
		return type;
	}

	public DirectoryChangeEvent setType(DirectoryChangeEventType type) {
		this.type = type;
		return this;
	}

	public long getTime() {
		return time;
	}

	public DirectoryChangeEvent setTime(long time) {
		this.time = time;
		return this;
	}

	public enum DirectoryChangeEventType {
		CREATED, MODIFIED, DELETED
	}
	
	@Override
	public String toString() {
		// Return the event in the JSON format
		return toJSON().toString();
	}
	
	/**
	 * Returns the JSON representation of the object.
	 * @return a JSON object representing the DirectoryChangeEvent object.
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.putOpt(ABSOLUTE_FILE_PATH, absoluteFilePath);
		json.putOpt(RELATIVE_FILE_PATH, relativeFilePath);
		json.putOpt(EVENT_TYPE, type);
		json.putOpt(TIME, time);
		return json;
	}
	
	/**
	 * This factory method creates a new DirectoryChangeEvent object from its
	 * JSON representation.
	 * @param jsonObject the JSON string that represents the DirectoryChangeEvent object.
	 * @return a new DirectoryChangeEvent object created from the JSON string.
	 */
	public static DirectoryChangeEvent fromJSON(String jsonObject) {
		JSONObject json = new JSONObject(jsonObject);
		String absFilePath = json.optString(ABSOLUTE_FILE_PATH);
		String relFilePath = json.optString(RELATIVE_FILE_PATH);
		long time = json.optLong(TIME, -1);
		DirectoryChangeEvent dce = new DirectoryChangeEvent();
		dce.setAbsoluteFilePath(absFilePath);
		dce.setRelativeFilePath(relFilePath);
		dce.setTime(time);
		String type = json.optString(EVENT_TYPE);
		if(type != null) {
			dce.setType(DirectoryChangeEventType.valueOf(type));
		}
		return dce;
	}
}
