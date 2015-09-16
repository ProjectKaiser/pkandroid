package com.projectkaiser.app_android.adapters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.obj.MRemoteNonSyncedComment;
import com.projectkaiser.mobile.sync.MComment;

public class CommentsArrayAdapter extends ArrayAdapter<MComment> {

	Context m_ctx;

	List<MComment> m_comments;

	public CommentsArrayAdapter(Context ctx, List<MComment> comments) {
		super(ctx, R.layout.comments_row, comments);
		m_comments =  comments;
		m_ctx = ctx;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) m_ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		SimpleDateFormat df = new SimpleDateFormat(m_ctx.getString(R.string.short_time), Locale.getDefault());
		
		View rowView = inflater.inflate(R.layout.comments_row, parent, false);
		TextView lblCommentCreator = (TextView) rowView.findViewById(R.id.lblCommentCreator);
		TextView lblCommentCreated = (TextView) rowView.findViewById(R.id.lblCommentCreated);
		TextView lblCommentBody = (TextView) rowView.findViewById(R.id.lblCommentBody);
		MComment comment = m_comments.get(position);

		lblCommentCreated.setText(df.format(new Date(comment.getCreated())));
		lblCommentBody.setText(comment.getDescription());
		
		if (comment instanceof MRemoteNonSyncedComment) { // Local non synced
			lblCommentCreator.setVisibility(View.GONE);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);			
		} else if  (comment.getCreatorName() == null || "".equals(comment.getCreatorName())) { // Local
			lblCommentCreator.setVisibility(View.GONE);			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);						
		} else { // Remote synced
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)lblCommentCreated.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			lblCommentCreator.setText(comment.getCreatorName());
		}
		
		ImageView imgNonSynced = (ImageView) rowView.findViewById(R.id.imgNonSynced);	
		ImageView imgSyncFailure = (ImageView) rowView.findViewById(R.id.imgSyncFailure); 
		TextView lblSyncError = (TextView) rowView.findViewById(R.id.lblSyncError);
		imgNonSynced.setVisibility(View.GONE);
		imgSyncFailure.setVisibility(View.GONE);
		
		if (comment instanceof MRemoteNonSyncedComment) {
			MRemoteNonSyncedComment rnsc = (MRemoteNonSyncedComment)comment;
			if (rnsc.getFailure()!=null) {
				imgSyncFailure.setVisibility(View.VISIBLE);
				lblSyncError.setVisibility(View.VISIBLE);
				lblSyncError.setText(m_ctx.getString(R.string.sync_error, rnsc.getFailure()));
			} else
				imgNonSynced.setVisibility(View.VISIBLE);
		}
		
		return rowView;
	}

}
