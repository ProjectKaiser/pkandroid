package com.projectkaiser.app_android.jsonapi.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.jsonrpc.errors.EJsonException;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.mobile.sync.MComment;
import com.projectkaiser.mobile.sync.MDigestedArray;
import com.projectkaiser.mobile.sync.MFolder;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MAttachment;
import com.projectkaiser.mobile.sync.MTeamMember;
import com.projectkaiser.mobile.sync.MWorkingSet;
import com.projectkaiser.mobile.sync.MWorkingSets;


public class ResponseParser extends AbstractJsonParser {
	
	private final Logger log = Logger.getLogger(ResponseParser.class);
	
	private static List<MTeamMember> parseTeam(JSONArray j) throws JSONException {
		ArrayList<MTeamMember> items = new ArrayList<MTeamMember>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MTeamMember p = new MTeamMember();
			p.setId(_rlong(o, "id"));
			p.setName(_rstring(o, "name"));
			p.setRoleName(_rstring(o, "roleName"));
			items.add(p);			
		}
		return items;
	}
	
	private static List<MFolder> parseFolders(JSONArray j) throws JSONException {
		ArrayList<MFolder> items = new ArrayList<MFolder>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MFolder p = new MFolder();
			p.setId(_rlong(o, "id"));
			p.setName(_rstring(o, "name"));
			items.add(p);			
		}
		return items;
	}
	
	
	private static List<MMyProject> parseProjects(JSONArray j) throws JSONException {
		ArrayList<MMyProject> projects = new ArrayList<MMyProject>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MMyProject p = new MMyProject();
			p.setId(_rlong(o, "id"));
			p.setName(_rstring(o, "name"));
			p.setTeam(parseTeam(_rarray(o, "team")));
			p.setFolders(parseFolders(_rarray(o, "folders")));
			projects.add(p);
		}
		return projects;
	}
	
	public static List<MWorkingSet> parseWorkingSets(JSONArray j) throws JSONException {
		ArrayList<MWorkingSet> items = new ArrayList<MWorkingSet>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MWorkingSet p = new MWorkingSet();
			p.setName(_rstring(o, "name"));
			
			JSONArray jp = _rarray(o, "projects");
			for (int k=0; k<jp.length(); k++) 
				p.getProjects().add(jp.getLong(k));				 
			
			items.add(p);			
		}
		return items;
	}
	
	private static List<MComment> parseComments(JSONArray j) throws JSONException {
		ArrayList<MComment> items = new ArrayList<MComment>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MComment p = new MComment();
			p.setCreated(_rlong(o, "created"));
			p.setCreator(_rlong(o, "creator"));
			p.setCreatorName(_rstring(o, "creatorName"));
			p.setDescription(_string(o, "description"));
			p.setId(_rlong(o, "id"));
			
			items.add(p);			
		}
		return items;
	}
	
	public static List<MRemoteSyncedIssue> parseIssues(JSONArray j, String srvConnId) throws JSONException {
		ArrayList<MRemoteSyncedIssue> items = new ArrayList<MRemoteSyncedIssue>();
		for (int i=0; i<j.length(); i++) {
			JSONObject o = j.getJSONObject(i);
			MRemoteSyncedIssue p = new MRemoteSyncedIssue();
			p.setId(_rlong(o, "id"));
			p.setName(_rstring(o, "name"));
			p.setAssigneeName(_string(o, "assigneeName"));
			p.setBudget(_int(o, "budget"));
			p.setCreated(_rlong(o, "created"));
			p.setDescription(_string(o, "description"));
			p.setDueDate(_long(o, "dueDate"));
			p.setModified(_rlong(o, "modified"));
			p.setModifier(_rlong(o, "modifier"));
			p.setPath(_string(o, "path"));
			p.setPriority(_int(o, "priority"));
			p.setResponsibleName(_string(o, "responsibleName"));
			p.setState(_int(o, "state"));
			p.setStatusName(_string(o, "statusName"));	
			p.setSrvConnId(srvConnId);
			
			p.setComments(parseComments(_rarray(o, "comments")));
			
			items.add(p);			
		}
		return items;
	}
	
	public static MAttachment parseAttachment(JSONObject o, String srvConnId) throws JSONException {
		MAttachment attachment = new MAttachment();
		
		attachment.setId(_rlong(o, "id"));
		attachment.setName(_rstring(o, "name"));
		attachment.setCreated(_rlong(o, "created"));
		return attachment;
	}

	public static SrvConnectionBaseData getBase(String json) {
		SrvConnectionBaseData base = new SrvConnectionBaseData();
		try {
			JSONObject j = new JSONObject(json);
			
			base.setSessionId(_rstring(j, "sessionId"));
			base.setUserId(_rlong(j, "userId"));
			base.setUserName(_rstring(j, "userName"));
			base.setServerName(_string(j, "serverName"));
			
			return base;

		} catch (JSONException e) {
			(new ResponseParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}
	
	public static MWorkingSets getWorkingSets(String json) {
		try {
			MWorkingSets ws = new MWorkingSets();
			if (json != null && json.trim().length()>0) {
				JSONObject j = new JSONObject(json);	
				ws.setDefaultWorkingSet(_string(j, "defaultWorkingSet"));
				ws.setDigest(_string(j, "digest"));
				ws.setItems(parseWorkingSets(_rarray(j, "items")));
			}
			return ws;
		} catch (JSONException e) {
			(new ResponseParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}
	
	public static MDigestedArray<MMyProject> getProjects(String json) {
		try {
			MDigestedArray<MMyProject> projects = new MDigestedArray<MMyProject>();
			if (json != null && json.trim().length()>0) {
				JSONObject j = new JSONObject(json);			
				projects.setDigest(_string(j, "digest"));
				projects.setItems(parseProjects(_rarray(j, "items")));
			}
			return projects;
		} catch (JSONException e) {
			(new ResponseParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}

	public static MDigestedArray<MRemoteSyncedIssue> getIssues(String json, String srvConnId) {
		try {
			MDigestedArray<MRemoteSyncedIssue> issues = new MDigestedArray<MRemoteSyncedIssue>();
			if (json != null && json.trim().length()>0) {
				JSONObject j = new JSONObject(json);	
				issues.setDigest(_string(j, "digest"));
				issues.setItems(parseIssues(_rarray(j, "items"), srvConnId));
			}
			return issues;
		} catch (JSONException e) {
			(new ResponseParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}
	
	public static void parseSyncResponse(SessionManager sm, String connectionId, String json) {
		if (json != null && !json.equalsIgnoreCase("null")) {

			try {
				JSONObject j = new JSONObject(json);
				if (!j.isNull("w")) 
					sm.updateWorkingSetsData(connectionId, j.getJSONObject("w").toString()); 
	
				if (!j.isNull("p")) 
					sm.updateProjectsData(connectionId, j.getJSONObject("p").toString()); 
					
				if (!j.isNull("i")) 
					sm.updateIssuesData(connectionId, j.getJSONObject("i").toString());
			} catch (JSONException e) {
				(new ResponseParser()).log.error("Unable to parse json", e);
				throw new RuntimeException(e);
			}
				
		}
	}
		
	public static MAttachment getAttachment(String json, String srvConnId, long fileId, long attachmentId) {
		try {
			MAttachment attachment = new MAttachment();
			if (json != null && json.trim().length()>0) {
				JSONObject j = new JSONObject(json);	
				attachment.setName("sss"); 
			}
			return attachment;
		} catch (JSONException e) {
			(new ResponseParser()).log.error("Unable to parse json", e);
			throw new EJsonException(e);
		}
	}
}
