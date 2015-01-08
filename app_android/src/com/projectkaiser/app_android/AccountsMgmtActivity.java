package com.projectkaiser.app_android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.projectkaiser.app_android.adapters.ConnectionsAdapter;
import com.projectkaiser.app_android.settings.SessionManager;

public class AccountsMgmtActivity extends ActionBarActivity {

	List<String> m_connections = new ArrayList<String>();
	
	SessionManager m_sessionManager;
	
	private void deleteAccount(int position) {
		m_sessionManager.removeConnectionId(m_connections.get(position));
		update();
	}
	
	private void update() {
		m_connections.clear();
		m_connections.add(getString(R.string.add_account));
		m_connections.addAll(m_sessionManager.getConnections());
		final Activity _this = this;
		ConnectionsAdapter adapter = new ConnectionsAdapter(getApplicationContext(), m_connections);
		
		ListView lvAccounts = (ListView)findViewById(R.id.lvAccounts);
		lvAccounts.setAdapter(adapter);
		lvAccounts.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int position,
					long arg3) {
				if (position == 0) {
					Intent i = new Intent(getApplicationContext(),
							SigninActivity.class);
			        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					finish();
					getApplicationContext().startActivity(i);
				} else {
					new AlertDialog.Builder(_this)
					.setMessage(R.string.delete_account_confirm)
					.setCancelable(false)
					.setPositiveButton(R.string.yes,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									deleteAccount(position);
								}
							}).setNegativeButton(R.string.no, null).show();
				}				
			}
		});
		
	}
	
	@Override
	public void onBackPressed() {
    	Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(i);
        finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		m_sessionManager = SessionManager.get(this);
		setContentView(R.layout.activity_accounts);
		update();
	}

}
