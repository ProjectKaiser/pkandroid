package com.projectkaiser.app_android.jsonapi.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.projectkaiser.app_android.jsonrpc.errors.EJsonException;

public abstract class AbstractJsonParser {

	protected static JSONObject required(JSONObject obj, String name) throws JSONException {
		if (obj.isNull(name)) 
			throw new EJsonException("Unexpected JSON format: "+name+" not found");
		else
			return obj;
	}

	protected static String _rstring(JSONObject obj, String name) throws JSONException {
		return required(obj, name).getString(name);
	}
	
	protected static Long _rlong(JSONObject obj, String name) throws JSONException {
		return required(obj, name).getLong(name);
	}
	
	protected static JSONArray _rarray(JSONObject obj, String name) throws JSONException {
		return required(obj, name).getJSONArray(name);
	}

	protected static Long _long(JSONObject obj, String name) throws JSONException {
		if (obj.isNull(name))
			return null;
		else
			return Long.valueOf(obj.getLong(name));
	}
	
	protected static String _string(JSONObject obj, String name) throws JSONException {
		if (obj.isNull(name))
			return null;
		else
			return obj.getString(name);
	}
	
	protected static Integer _int(JSONObject obj, String name) throws JSONException {
		if (obj.isNull(name))
			return null;
		else
			return Integer.valueOf(obj.getInt(name));
	}
	
	
}
