package com.projectkaiser.app_android.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.projectkaiser.app_android.R;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.consts.Priority;
import com.projectkaiser.app_android.consts.State;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MRemoteIssue;
import com.projectkaiser.app_android.MainActivity;
import com.projectkaiser.app_android.fragments.main.LocalRemovedItem;

public class IssuesArrayAdapter extends ArrayAdapter<MIssue> {

	Context m_ctx;
	List<MIssue> m_tasks;
	LocalRemovedItem adapterRemovedItem = null;

	public IssuesArrayAdapter(Context ctx, List<MIssue> tasks) {
		super(ctx, R.layout.inbox_row, tasks);
		m_ctx = ctx;
		m_tasks = tasks;
	}

	private final static int COLOR_OVERDUE = 0xffcc0000;
	private final static int COLOR_BLOCKER = 0xff990000;
	private final static int COLOR_HIGH = 0xffff0000;
	private final static int COLOR_LOW = 0xffaaaaaa;
	private final static int COLOR_NOTSTARTED = 0xffbbbbbb;

	public void setremovedItem(LocalRemovedItem removedItem) {
		adapterRemovedItem = removedItem;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) m_ctx
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = null;
		int disPos = -1;
		if (m_tasks == null) {
			return rowView;
		}
		if (adapterRemovedItem != null) {
			disPos = adapterRemovedItem.dismissPosition;
		}
		if (disPos == position) {
			rowView = inflater.inflate(R.layout.inbox_dismiss_row, parent,
					false);
			rowView.setTag(m_ctx.getString(R.string.NOT_SWIPING_ITEM));
			TextView lblTaskName = (TextView) rowView
					.findViewById(R.id.lblDismissIssue);
			TextView txtRestoreIssue = (TextView) rowView
					.findViewById(R.id.txtRestoreIssue);
			MIssue task = m_tasks.get(position);
			if (task == null) {
				return rowView;
			}

			txtRestoreIssue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (adapterRemovedItem == null)
						return;
					MIssue m_details = m_tasks
							.get(adapterRemovedItem.dismissPosition);
					if (m_details instanceof MLocalIssue) {
						long time = adapterRemovedItem.Modified;
						BL.getLocal(m_ctx).updateTaskEx(
								(MLocalIssue) m_details, time);
						MainActivity myAct = (MainActivity) m_ctx;
						myAct.RefreshItemRevert(adapterRemovedItem.dismissPosition);
					}
					return;
				}
			});

			if (task != null && lblTaskName != null) {
				if (task.getState() == 0) {
					lblTaskName.setText(task.getName() + " - "
							+ m_ctx.getString(R.string.completed));
					txtRestoreIssue.setText(m_ctx.getString(R.string.cancel));
				} else {
					lblTaskName.setText(task.getName() + " - "
							+ m_ctx.getString(R.string.resumed));
					txtRestoreIssue.setText(m_ctx.getString(R.string.cancel));
				}
			}
		} else {

			rowView = inflater.inflate(R.layout.inbox_row, parent, false);
			TextView lblTaskName = (TextView) rowView
					.findViewById(R.id.lblInboxRowTaskName);
			TextView lblTaskStatus = (TextView) rowView
					.findViewById(R.id.lblInboxRowTaskStatus);
			TextView lblTaskPriority = (TextView) rowView
					.findViewById(R.id.lblInboxRowPriority);
			TextView lblTaskDue = (TextView) rowView
					.findViewById(R.id.lblInboxRowTaskDueDate);
			MIssue task = m_tasks.get(position);

			lblTaskName.setText(task.getName());

			if (task instanceof MRemoteIssue) {
				MRemoteIssue ri = (MRemoteIssue) task;
				lblTaskStatus.setText(ri.getStatusName());
			} else if (task.getDescription() != null) {
				String descr = task.getDescription();
				if (descr.length() > 40)
					descr = descr.substring(0, 37) + "...";
				lblTaskStatus.setText(descr.replaceAll("\n", " "));
			} else {
				lblTaskStatus.setText("");
			}

			if (task.getDueDate() != null && task.getDueDate() > 0) {
				boolean nullDueTime;
				Calendar cdt = Calendar.getInstance();
				cdt.setTime(new Date(task.getDueDate()));
				nullDueTime = (cdt.get(Calendar.HOUR_OF_DAY)) == 0;
				SimpleDateFormat df;
				if (!nullDueTime)
					df = new SimpleDateFormat(
							m_ctx.getString(R.string.short_date_time),
							Locale.getDefault());
				else
					df = new SimpleDateFormat(
							m_ctx.getString(R.string.short_date),
							Locale.getDefault());
				lblTaskDue.setText(m_ctx.getString(R.string.issue_due_date,
						df.format(new Date(task.getDueDate()))));
				Calendar now = new GregorianCalendar(Locale.getDefault());
				Calendar due = new GregorianCalendar(Locale.getDefault());
				due.setTimeInMillis(task.getDueDate());
				lblTaskDue.setVisibility(View.VISIBLE);
				if (due.before(now))
					lblTaskDue.setTextColor(COLOR_OVERDUE);
			} else
				lblTaskDue.setVisibility(View.GONE);

			if (task.getState() == State.CLEAR) {
				lblTaskName.setTextColor(COLOR_NOTSTARTED);
			}

			int priority = Priority.NORMAL;
			if (task.getPriority() != null)
				priority = task.getPriority().intValue();
			switch (priority) {
			case Priority.BLOCKER:
				lblTaskPriority.setBackgroundColor(COLOR_BLOCKER);
				break;
			case Priority.HIGH:
				lblTaskPriority.setBackgroundColor(COLOR_HIGH);
				break;
			case Priority.LOW:
				lblTaskPriority.setBackgroundColor(COLOR_LOW);
				break;
			}

			ImageView imgNonSynced = (ImageView) rowView
					.findViewById(R.id.imgNonSynced);
			ImageView imgSyncFailure = (ImageView) rowView
					.findViewById(R.id.imgSyncFailure);

			imgNonSynced.setVisibility(View.GONE);
			imgSyncFailure.setVisibility(View.GONE);

			if (task instanceof MRemoteNotSyncedIssue) {
				MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) task;
				if (rnsi.getFailure() != null) {
					imgSyncFailure.setVisibility(View.VISIBLE);
				} else {
					imgNonSynced.setVisibility(View.VISIBLE);
				}
			}
		}
		return rowView;
	}
}
