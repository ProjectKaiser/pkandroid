package com.projectkaiser.app_android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.mobile.sync.MTeamMember;

public class TeamMembersAdapter extends ArrayAdapter<MTeamMember> {

	Context m_ctx;

	List<MTeamMember> m_users;

	public TeamMembersAdapter(Context ctx, List<MTeamMember> folders) {
		super(ctx, R.layout.teammembers_row, folders);
		m_ctx = ctx;
		m_users = folders;
	}

	private final static int COLOR_NOTSTARTED = 0xffbbbbbb;
	
	@Override
	public long getItemId(int position) {
		return m_users.get(position).getId();
	}
	
	private View initView(int position, View convertView, ViewGroup parent, int resource) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) m_ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}
		TextView lblUserName = (TextView) convertView.findViewById(R.id.lblUserName);
		TextView lblRoleName = (TextView) convertView.findViewById(R.id.lblRoleName);

		MTeamMember user = m_users.get(position);
		lblUserName.setText(user.getName());
		lblRoleName.setText(user.getRoleName());
		lblRoleName.setTextColor(COLOR_NOTSTARTED);
		
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.teammembers_row);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.teammembers_row);
	}

}
