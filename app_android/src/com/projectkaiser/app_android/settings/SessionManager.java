package com.projectkaiser.app_android.settings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.Fragment;

import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.obj.MRemoteSyncedIssue;
import com.projectkaiser.app_android.jsonapi.parser.ResponseParser;
import com.projectkaiser.app_android.jsonrpc.auth.AuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.GoogleAuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.PlainAuthScheme;
import com.projectkaiser.mobile.sync.MDigestedArray;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MWorkingSets;

public class SessionManager {
	
	private final Logger log = Logger.getLogger(SessionManager.class);

	SharedPreferences pref;
     
    Context _context;
     
    int PRIVATE_MODE = 0;
     
    private static final String PREF_NAME = "session_pref"; // user preferences

    private static final String KEY_USER_EMAIL = "user_email"; // prefixed

    private static final String KEY_SERVER_URL = "server_url"; // prefixed
    
    private static final String KEY_LOGIN_DATA = "login_data"; // prefixed

    private static final String KEY_WORKING_SETS_DATA = "working_sets_data"; // prefixed

    private static final String KEY_PROJECTS_DATA = "projects_data"; // prefixed

    private static final String KEY_ISSUES_DATA = "issues_data"; // prefixed

    private static final String KEY_LAST_FOLDER_DLG_WORKING_SET = "last_folder_working_set"; // prefixed

    private static final String KEY_LAST_FOLDER_DLG_CONNECTION_ID = "last_folder_connection";

    private static final String KEY_CONNECTIONS = "connections";
    
    private static final String KEY_LAST_SYNC_DATE = "last_sync_date";

    public static final String KEY_SYNC_INTERVAL = "sync_frequency";

    protected SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
    }
    
    public static SessionManager get(Context context) {
    	return new SessionManager(context);
    }
    
    public static SessionManager get(Activity activity) {
    	return new SessionManager(activity.getApplicationContext());
    }
    
    public static SessionManager get(Fragment fragment) {
    	return new SessionManager(fragment.getActivity().getApplicationContext());
    }
    
    public List<String> getConnections() {
    	ArrayList<String> conns = new ArrayList<String>();
        String cc = pref.getString(KEY_CONNECTIONS, null);
    	
        if (cc != null) 
        	for (String c:cc.split("\n"))
        		if (c.trim().length()>0)
        			conns.add(c);
        return conns;
    }
    
    private String connectionsToString(List<String> connections) {
		StringBuilder cclist = new StringBuilder();
		for (String c:connections) {
			if (cclist.length()>0) cclist.append("\n");
			cclist.append(c);
		}
		return cclist.toString();
    }
        
    private void setConnectionId(SrvConnectionId id, String email, String serverUrl, String loginData) {
    	Editor editor = pref.edit();

        String cc = pref.getString(KEY_CONNECTIONS, null);
        if (cc != null) {
        	List<String> connections = new ArrayList<String>();
        	
        	for (String c:cc.split("\n"))
        		connections.add(c);
        	
        	if (!connections.contains(id.toString())) {
        		connections.add(id.toString());
                editor.putString(KEY_CONNECTIONS, connectionsToString(connections));
        	}
        } else
            editor.putString(KEY_CONNECTIONS, id.toString());
        
        editor.putString(id.prefixed(KEY_LOGIN_DATA), loginData);
        editor.remove(id.prefixed(KEY_PROJECTS_DATA));
        editor.remove(id.prefixed(KEY_ISSUES_DATA));
        editor.remove(id.prefixed(KEY_WORKING_SETS_DATA));
        editor.putString(id.prefixed(KEY_USER_EMAIL), email);
        editor.putString(id.prefixed(KEY_SERVER_URL), serverUrl);

        editor.commit();
    }
    
    public SrvConnectionId loggedOn(String serverUrl, AuthScheme scheme, String loginData) {
		    	
		log.debug("loggedOn: "+serverUrl);

		String email = null;
    	
    	if (scheme instanceof PlainAuthScheme) 
    		email = ((PlainAuthScheme)scheme).getUserName();
    	else if (scheme instanceof GoogleAuthScheme) 
    		email = ((GoogleAuthScheme)scheme).getEmail();
    	else
    		throw new RuntimeException(scheme.getClass().getName() + " not supported by SessionManager.loggedOn");
    	
    	SrvConnectionId auth = new SrvConnectionId(serverUrl, email);
    	
    	setConnectionId(auth, email, serverUrl, loginData);
    	
    	return auth;
    	
    }
    
//    public boolean loggedOn(SrvConnectionId id) {
//    	
//    	String serverUrl = pref.getString(id.prefixed(KEY_SERVER_URL), null);
//    	String email = pref.getString(id.prefixed(KEY_USER_EMAIL), null);
//    	String syncData = pref.getString(id.prefixed(KEY_SYNCED_DATA), null); 
//    	
//    	if (serverUrl!=null && syncData != null && email != null) {
//	    	Intent i = new Intent(_context, MainActivity.class);
//	        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//	        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	        _context.startActivity(i);	        
//	        return true;
//    	} else
//    		return false;
//    		
//    }

    public void removeConnectionId(String connectionId) {
    	
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	
    	ArrayList<String> connections = new ArrayList<String>();
    	connections.addAll(getConnections());
    	
    	connections.remove(connectionId);
    	
    	Editor editor = pref.edit();
        editor.remove(id.prefixed(KEY_SERVER_URL));
        editor.remove(id.prefixed(KEY_USER_EMAIL));
        editor.remove(id.prefixed(KEY_LOGIN_DATA));
        editor.remove(id.prefixed(KEY_PROJECTS_DATA));
        editor.remove(id.prefixed(KEY_ISSUES_DATA));
        editor.remove(id.prefixed(KEY_WORKING_SETS_DATA));
        editor.remove(id.prefixed(KEY_LAST_FOLDER_DLG_WORKING_SET));
        
        editor.putString(KEY_CONNECTIONS, connectionsToString(connections));
        
        editor.commit();
        
        BL.getLocal(_context).deleteNonSyncedTasks(connectionId);

    }
    
    public SrvConnectionBaseData getBaseData(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	String json = pref.getString(id.prefixed(KEY_LOGIN_DATA), null);
    	return ResponseParser.getBase(json);
    }
    
    public MDigestedArray<MMyProject> getMyProjects(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	String json = pref.getString(id.prefixed(KEY_PROJECTS_DATA), null);
   		return ResponseParser.getProjects(json);
    }
    
    public MDigestedArray<MRemoteSyncedIssue> getIssues(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	String json = pref.getString(id.prefixed(KEY_ISSUES_DATA), null);
   		return ResponseParser.getIssues(json, connectionId);
    }
    
    public MWorkingSets getWorkingSets(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	String json = pref.getString(id.prefixed(KEY_WORKING_SETS_DATA), null);
   		return ResponseParser.getWorkingSets(json);
    }
    
    public String getServerUrl(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	return pref.getString(id.prefixed(KEY_SERVER_URL), null);
    }
    
    public void updateProjectsData(String connectionId, String json) {
    	Editor editor = pref.edit();
    	SrvConnectionId id = new SrvConnectionId(connectionId);
        editor.putString(id.prefixed(KEY_PROJECTS_DATA), json);
        editor.commit();
    }

    public void updateIssuesData(String connectionId, String json) {
    	Editor editor = pref.edit();
    	SrvConnectionId id = new SrvConnectionId(connectionId);
        editor.putString(id.prefixed(KEY_ISSUES_DATA), json);
        editor.commit();
    }

    public void updateWorkingSetsData(String connectionId, String json) {
    	Editor editor = pref.edit();
    	SrvConnectionId id = new SrvConnectionId(connectionId);
        editor.putString(id.prefixed(KEY_WORKING_SETS_DATA), json);
        editor.commit();
    }

    public void updateLastSyncDate() {
    	Editor editor = pref.edit();    	
        editor.putLong(KEY_LAST_SYNC_DATE, System.currentTimeMillis());
        editor.commit();
    }
    
    public String getLatestWorkingSet(String connectionId) {
    	SrvConnectionId id = new SrvConnectionId(connectionId);
    	return pref.getString(id.prefixed(KEY_LAST_FOLDER_DLG_WORKING_SET), null);
    }
    
    public String getLatestFolderDlgConnectionId() {
    	return pref.getString(KEY_LAST_FOLDER_DLG_CONNECTION_ID, null);
    }
    
    public int getSyncIntervalMin() {
    	return pref.getInt(KEY_SYNC_INTERVAL, 5);
    }
    
    public void setSyncIntervalMin(int value) {
    	Editor editor = pref.edit();    	
        editor.putInt(KEY_SYNC_INTERVAL, value);
        editor.commit();
    }

    public void updateLastWorkingSet(String connectionId, String workingSet) {
    	Editor editor = pref.edit();  
    	SrvConnectionId id = new SrvConnectionId(connectionId);        
        editor.putString(id.prefixed(KEY_LAST_FOLDER_DLG_WORKING_SET), workingSet);
        editor.commit();
    }
    
    public void updateLastFolderDlgConnectionId(String connectionId) {
    	Editor editor = pref.edit();  
        editor.putString(KEY_LAST_FOLDER_DLG_CONNECTION_ID, connectionId);
        editor.commit();
    }

    public Date getLastSyncDate() {
    	long l = pref.getLong(KEY_LAST_SYNC_DATE, 0);
    	if (l > 0)
    		return new Date(l);
    	else
    		return null;
    	
    }
	
}
