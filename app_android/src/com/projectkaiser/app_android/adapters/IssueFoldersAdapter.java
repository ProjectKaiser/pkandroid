package com.projectkaiser.app_android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.obj.SelectedIssuesFolder;

public class IssueFoldersAdapter extends ArrayAdapter<SelectedIssuesFolder> {

	Context m_ctx;

	ArrayList<SelectedIssuesFolder> m_folders;

	public IssueFoldersAdapter(Context ctx, ArrayList<SelectedIssuesFolder> folders) {
		super(ctx, R.layout.folders_row, folders);
		m_ctx = ctx;
		m_folders = folders;
	}

	private final static int COLOR_NOTSTARTED = 0xffbbbbbb;
	
	@Override
	public long getItemId(int position) {
		return m_folders.get(position).getId();
	}
	
	private View initView(int position, View convertView, ViewGroup parent, int resource) {
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) m_ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}
		TextView lblProjectName = (TextView) convertView.findViewById(R.id.lblProjectName);
		TextView lblFolderName = (TextView) convertView.findViewById(R.id.lblFolderName);

		SelectedIssuesFolder folder = m_folders.get(position);

		if (folder.getProject()!=null) {
			lblProjectName.setText(folder.getProject().getName());
			lblProjectName.setTextColor(COLOR_NOTSTARTED);
		} else {
			lblProjectName.setVisibility(View.GONE);
		}		
		
		lblFolderName.setText(folder.getName());
		return convertView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.folders_row);
	}
	
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return initView(position, convertView, parent, R.layout.folders_row);
	}

}
