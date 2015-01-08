package com.projectkaiser.app_android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.projectkaiser.app_android.R;

public class ConnectionsAdapter extends ArrayAdapter<String> {

	Context m_ctx;

	List<String> m_connections;

	public ConnectionsAdapter(Context ctx, List<String> connections) {
		super(ctx, R.layout.connections_row, connections);
		m_ctx = ctx;
		m_connections = connections;
	}

	private View initView(int position, View convertView, ViewGroup parent, int resource) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) m_ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}
		TextView lblServerName = (TextView) convertView.findViewById(R.id.lblServerName);
		TextView lblEmail = (TextView) convertView.findViewById(R.id.lblEmail);

		String id = m_connections.get(position);
		
		int pos = id.indexOf('!');
		if (pos > -1) {
			String server = id.substring(0, pos);
			String email = id.substring(pos+1);
			
			lblServerName.setText(server);
			lblEmail.setText(email);
		} else {
			lblServerName.setText(id);
			lblEmail.setText("");
		}
		
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.connections_row);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.connections_row);
	}

}
