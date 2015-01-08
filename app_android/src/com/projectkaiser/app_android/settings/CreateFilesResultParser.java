package com.projectkaiser.app_android.settings;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.projectkaiser.app_android.jsonapi.parser.AbstractJsonParser;
import com.projectkaiser.app_android.jsonrpc.errors.EJsonException;

public class CreateFilesResultParser extends AbstractJsonParser {
	
	private final Logger log = Logger.getLogger(CreateFilesResultParser.class);

	public static void parseResponse(String value, List<Object> commentsRes, List<Object> tasksRes) {
		try {
			JSONObject j = new JSONObject(value);
			JSONArray cc = _rarray(j, "commentRes");
			for (int i=0; i<cc.length(); i++) 
				commentsRes.add(cc.get(i));
					
			JSONArray tt = _rarray(j, "taskRes"); 
			for (int i=0; i<tt.length(); i++) 
				tasksRes.add(tt.get(i));

		} catch (JSONException e) {
			(new CreateFilesResultParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}

}
