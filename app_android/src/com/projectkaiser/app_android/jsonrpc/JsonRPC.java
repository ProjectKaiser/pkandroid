package com.projectkaiser.app_android.jsonrpc;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.projectkaiser.app_android.bl.obj.MNewComment;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.jsonrpc.auth.AuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.GoogleAuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.PlainAuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.SessionAuthScheme;
import com.projectkaiser.app_android.jsonrpc.errors.EAppException;
import com.projectkaiser.app_android.jsonrpc.errors.EAuthError;
import com.projectkaiser.app_android.jsonrpc.errors.EJsonException;
import com.projectkaiser.app_android.rpc.IAppRPC;
import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;

public class JsonRPC implements IAppRPC {

	private final Logger log = Logger.getLogger(JsonRPC.class);

	String getRpcUrl(String serverUrl) {
		if (!serverUrl.endsWith("/"))
			serverUrl += "/";
		return serverUrl + "soap";
	}
	
	private JsonObjectBuilder getComments(MCreateRequestEx request) {

		JsonObjectBuilder jb = new JsonObjectBuilder(false);
		jb.openArray();
		for (MNewComment c: request.getNewComments()) {
			jb.openObject();
			jb.addProp("id", c.getId());
			jb.addProp("ns", c.isNonSyncedTask());
			jb.addProp("tId", c.getTaskId());
			jb.addProp("des", c.getDescription());
			jb.closeObject();
		}
		jb.closeArrayProp();
		return jb;
		
	}
	
	private JsonObjectBuilder getTasks(MCreateRequestEx request) {

		JsonObjectBuilder jb = new JsonObjectBuilder(false);
		jb.openArray();
		for (MRemoteNotSyncedIssue i: request.getNewIssues()) {
			
			jb.openObject();
			jb.addProp("id", i.getId());
			jb.addProp("name", i.getName());
			
			if (i.getDescription()!=null)
				jb.addProp("des", i.getDescription());
			if (i.getAssigneeId()!=null && i.getAssigneeId().longValue()>0)
				jb.addProp("aId", i.getAssigneeId());
			if (i.getResponsibleId()!=null && i.getResponsibleId().longValue()>0) 
				jb.addProp("rId", i.getResponsibleId());
			if (i.getBudget()!=null && i.getBudget().intValue()!=0)
				jb.addProp("bud", i.getBudget());
			if (i.getDueDate()!=null && i.getDueDate().longValue()>0)
				jb.addProp("due", i.getDueDate());
			if (i.getPriority()!=null && i.getPriority().longValue()>0) 
				jb.addProp("pri", i.getPriority());

			jb.addProp("sta", i.getState());
			jb.addProp("fId", i.getFolderId());			
			jb.closeObject();
		}
		jb.closeArrayProp();
		return jb;
		
	}

	@Override
	public String create(MCreateRequestEx request) {
		
		return request(request, 
				"create", 
				getTasks(request), 
				getComments(request), 
				request.getLocale().getLanguage());
		
	}
	
	private String request(MBasicRequest request, Object... args) {
		JsonObjectBuilder j = new JsonObjectBuilder(false); 
		
		j.openObject();
		j.addProp("jsonrpc", "2.0");
		j.addProp("method", "doCollectionView");
		
		j.openArrayProp("params");
		j.openObject();
		
		// <scheme>
		j.openObjectProp("scheme");
		AuthScheme scheme = request.getAuthScheme();
		if (scheme instanceof PlainAuthScheme) {
			j.addProp("type", "PlainAuthScheme");
			j.addProp("userName", ((PlainAuthScheme)scheme).getUserName());
			j.addProp("password", ((PlainAuthScheme)scheme).getPassword());
		} else if (scheme instanceof SessionAuthScheme) {
			j.addProp("type", "SessionAuthScheme");
			j.addProp("session", ((SessionAuthScheme)scheme).getSessionId());
		} else if (scheme instanceof GoogleAuthScheme) {
			j.addProp("type", "GoogleTokenScheme");
			j.addProp("token", ((GoogleAuthScheme)scheme).getToken());
			j.addProp("email", ((GoogleAuthScheme)scheme).getEmail());
			j.addProp("displayName", ((GoogleAuthScheme)scheme).getDisplayName());
			j.addProp("pictureUrl", ((GoogleAuthScheme)scheme).getPictureUrl());
		};
		j.closeObject();
		// </scheme>
		
		// <headers>
		j.openArrayProp("headers");
		j.closeArrayProp();
		// </headers>
		
		// <args>
		j.openArrayProp("args");
		for (Object arg:args) {
			if (arg instanceof String)
				j.addString((String)arg);
			else if (arg instanceof JsonObjectBuilder)
				j.addEscapedJson((JsonObjectBuilder)arg);
			else
				throw new RuntimeException("Not supported class "+arg.getClass().getName()+" at JsonRPC.request");
		}
		j.closeArrayProp();
		// </args>
		
		j.addProp("target", "com.triniforce.server.plugins.webclient.CDMobileClient");
		j.addProp("parentOf", 0L);
		
		// <columns>
		j.openArrayProp("columns");
		j.addString("result");
		j.closeArrayProp();
		// </columns>
		
		// <where>
		j.openArrayProp("where");
		j.closeArrayProp();
		// </where>
		
		// <orderBy>
		j.openArrayProp("orderBy");
		j.closeArrayProp();
		// </orderBy>

		// <whereExprs>
		j.openArrayProp("whereExprs");
		j.closeArrayProp();
		// </whereExprs>

		// <functions>
		j.openArrayProp("functions");
		j.closeArrayProp();
		// </functions>

		j.addProp("dbValue", false);

		j.closeObject();
		j.closeArrayProp();
		j.closeObject();
		
		try {
			JSONObject o = JsonClient.makeRequest(getRpcUrl(request.getServerUrl()), j);
	
			if (!o.isNull("error")) {
				JSONObject error = o.getJSONObject("error");
				String message;
				if (error.isNull("message"))
					message = error.getString("code");
				else
					message = error.getString("message");
				String stack;
				if (error.isNull("stackTrace"))
					stack = "unknown";
				else
					stack = error.getString("stackTrace");						
				if (stack.contains(".EAuth$"))
					throw new EAuthError();
				throw new EAppException(message);
			} 		

			if (!o.isNull("result")) {
				JSONObject result = o.getJSONObject("result");
				if (!result.isNull("values")) {
					JSONArray values = result.getJSONArray("values");
					if (values.length()<1)
						throw new EJsonException("Unexpected JSON format: no values returned");
					return values.getString(0);
				} else				
					throw new EJsonException("Unexpected JSON format: values not found");
			} else
				throw new EJsonException("Unexpected JSON format: result not found");
			
		} catch (JSONException e) {
			log.error( "Unable to parse json", e);
			throw new EJsonException(e);
		}
	}

	@Override
	public String syncronize(MSynchronizeRequestEx request) {

		return request(request, 
				"synchronize", 
				request.getLocale().getLanguage(),
				request.getWorkingSetsDigest()==null?"":request.getWorkingSetsDigest(),
				request.getIssuesDigest()==null?"":request.getIssuesDigest(),
				request.getProjectsDigest()==null?"":request.getProjectsDigest()
						);
		
	}
	
	@Override
	public String login(MBasicRequest request) {

		return request(request, 
				"login", 
				request.getLocale().getLanguage()
				);
		
	}

}
