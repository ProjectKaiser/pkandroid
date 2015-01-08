package com.projectkaiser.app_android.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.mobile.sync.MMyProject;

public class FoldersDlgAdapter extends BaseExpandableListAdapter  {
	
	ArrayList<MMyProject> m_projects;
	
	Context m_context;
	
	public FoldersDlgAdapter(Context context, ArrayList<MMyProject> projects) {
		m_projects = projects;
		m_context = context;
	}
	
	@Override
    public boolean hasStableIds() {
        return true;
    }

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return m_projects.get(groupPosition).getFolders().get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return m_projects.get(groupPosition).getFolders().get(childPosition).getId();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {

		if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.folders_group_item, null);
        }

        if (isExpanded){
        }
        else{
        }

        TextView lbl = (TextView) convertView.findViewById(R.id.lblProjectName);
        lbl.setText(m_projects.get(groupPosition).getName());

        return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.folders_item, null);
        }

        TextView textChild = (TextView) convertView.findViewById(R.id.lblFolderName);
        textChild.setText(m_projects.get(groupPosition).getFolders().get(childPosition).getName());

//        Button button = (Button)convertView.findViewById(R.id.buttonChild);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Toast.makeText(mContext,"button is pressed",5000).show();
//            }
//        });

        return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return m_projects.get(groupPosition).getFolders().size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return m_projects.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return m_projects.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return m_projects.get(groupPosition).getId();
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	
}
