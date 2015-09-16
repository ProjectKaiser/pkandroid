package com.projectkaiser.app_android;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.projectkaiser.app_android.settings.SessionManager;

public class InfoActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_info);
		SessionManager sm = SessionManager.get(this);
		TextView lblVersion = (TextView)findViewById(R.id.lblVersion);
		TextView lblLasySync = (TextView)findViewById(R.id.lblLastSync);

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			lblVersion.setText(getString(R.string.version, pInfo.versionName));
		} catch (NameNotFoundException e) {
			lblVersion.setText("error");
		}
		
		SimpleDateFormat df = new SimpleDateFormat(getString(R.string.short_time), Locale.getDefault());
		Date lastSyncDate = sm.getCommonLastSyncDate();
		if (lastSyncDate != null){
			SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
			Date noOneDate;
			try {
				noOneDate = formatter.parse("12-12-2012");
				if (lastSyncDate.compareTo(noOneDate)==0) {
					lblLasySync.setText(getString(R.string.no_last_sync));
				} else {
					lblLasySync.setText(getString(R.string.last_sync,
						df.format(lastSyncDate)));
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			lblLasySync.setText("");
	}
}
