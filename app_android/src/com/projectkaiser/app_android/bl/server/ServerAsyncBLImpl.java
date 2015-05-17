package com.projectkaiser.app_android.bl.server;

import android.os.AsyncTask;

import com.projectkaiser.app_android.async.AsyncCallback;
import com.projectkaiser.app_android.bl.AsyncTaskResult;
import com.projectkaiser.app_android.jsonrpc.JsonRPC;
import com.projectkaiser.app_android.settings.CreateFilesResultParser;
import com.projectkaiser.mobile.sync.BatchRequest;
import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MCreateRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeResponseEx;

public class ServerAsyncBLImpl implements IServerAsyncBL {
	
	JsonRPC m_rpc = new JsonRPC();
	public ServerAsyncBLImpl() {
	}
	
	@Override
	public void login(MBasicRequest request, final AsyncCallback<String> callback) {
		
		AsyncTask<MBasicRequest, Void, AsyncTaskResult<String>> task = 
				new AsyncTask<MBasicRequest, Void, AsyncTaskResult<String>>() {
			@Override
			protected AsyncTaskResult<String> doInBackground(MBasicRequest... params) {
				try {				
					return new AsyncTaskResult<String>(m_rpc.login(params[0]));
				} catch (Exception e) {
					return new AsyncTaskResult<String>(e);
				}	
			}
			@Override
			protected void onPostExecute(AsyncTaskResult<String> result) {
				if (result.getError()!=null)
					callback.onFailure(result.getError());
				else
					callback.onSuccess(result.getResult());
			}
		};
		task.execute(request);		
	}
	
	@Override
	public void synchronize(BatchRequest request,
			final AsyncCallback<MSynchronizeResponseEx> callback) {
		AsyncTask<BatchRequest, Void, AsyncTaskResult<MSynchronizeResponseEx>> task = 
				new AsyncTask<BatchRequest, Void, AsyncTaskResult<MSynchronizeResponseEx>>() {
			@Override
			protected AsyncTaskResult<MSynchronizeResponseEx> doInBackground(BatchRequest... params) {
				try {
					MSynchronizeResponseEx resp = new MSynchronizeResponseEx();
					MSynchronizeRequestEx sr = params[0].getSyncRequest();
					MCreateRequestEx cr = params[0].getCreateRequest();
					
					if (cr != null) {
						if (cr.getNewIssues().size() > 0 || cr.getNewComments().size() > 0) 
							CreateFilesResultParser.parseResponse(m_rpc.create(cr), resp.getCommentRes(), resp.getTaskRes());
					}
					
					resp.setData(m_rpc.syncronize(sr));
					
					return new AsyncTaskResult<MSynchronizeResponseEx>(resp);
				} catch (Exception e) {
					return new AsyncTaskResult<MSynchronizeResponseEx>(e);
				}	
			}
			@Override
			protected void onPostExecute(AsyncTaskResult<MSynchronizeResponseEx> result) {
				if (result.getError()!=null)
					callback.onFailure(result.getError());
				else
					callback.onSuccess(result.getResult());
			}
		};
		task.execute(request);		
		
	}
	
}
