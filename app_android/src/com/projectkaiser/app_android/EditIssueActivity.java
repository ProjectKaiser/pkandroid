package com.projectkaiser.app_android;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;

import com.projectkaiser.app_android.adapters.IssueFoldersAdapter;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.bl.local.ILocalBL;
import com.projectkaiser.app_android.bl.obj.MRemoteNotSyncedIssue;
import com.projectkaiser.app_android.bl.obj.SelectedIssuesFolder;
import com.projectkaiser.app_android.consts.Priority;
import com.projectkaiser.app_android.consts.State;
import com.projectkaiser.app_android.fragments.DatePickerFragment;
import com.projectkaiser.app_android.fragments.IChangeDateListener;
import com.projectkaiser.app_android.fragments.dialogs.FolderDialogFragment;
import com.projectkaiser.app_android.fragments.dialogs.FolderDialogFragment.FolderListener;
import com.projectkaiser.app_android.fragments.dialogs.SelectUserDialogFragment;
import com.projectkaiser.app_android.fragments.dialogs.SelectUserDialogFragment.UsersListener;
import com.projectkaiser.app_android.misc.Time;
import com.projectkaiser.app_android.services.PkAlarmManager;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.mobile.sync.MDataHelper;
import com.projectkaiser.mobile.sync.MFolder;
import com.projectkaiser.mobile.sync.MIssue;
import com.projectkaiser.mobile.sync.MLocalIssue;
import com.projectkaiser.mobile.sync.MMyProject;
import com.projectkaiser.mobile.sync.MTeamMember;

public class EditIssueActivity extends ActionBarActivity implements
		IChangeDateListener {

	ArrayList<SelectedIssuesFolder> m_currentFolder = new ArrayList<SelectedIssuesFolder>();

	Calendar m_dueDate;

	MTeamMember m_assignee = null;
	MTeamMember m_responsible = null;
	MIssue m_details;
	String curServerName = "";
	boolean missue_modified = false;
	TextWatcher anyEditTextWatcher = null;
	boolean mIgnoreModified = false;

	ArrayList<CharSequence> m_priorities = new ArrayList<CharSequence>();

	private void setissue_modified() {
		if (mIgnoreModified)
			return;
		missue_modified = true;
		refreshButtons();
	}

	@Override
	public void onDateSet(int year, int month, int day) {
		m_dueDate = Calendar.getInstance();
		m_dueDate.set(Calendar.YEAR, year);
		m_dueDate.set(Calendar.MONTH, month);
		m_dueDate.set(Calendar.DAY_OF_MONTH, day);
		setDueDate(m_dueDate);
	}

	SelectedIssuesFolder getSelectedFolder() {
		if (m_currentFolder.size() > 0
				&& m_currentFolder.get(0).getProject() != null) {
			return m_currentFolder.get(0);
		}
		return null;
	}

	void setIssuesFolder(String connectionId, Long id) {
		final Spinner cmbFolder = (Spinner) findViewById(R.id.cmbFolder);
		m_currentFolder.clear();
		SelectedIssuesFolder folder = new SelectedIssuesFolder(connectionId);

		if (id == null || id.equals(0L)) {
			folder.setId(0L);
			folder.setName(getString(R.string.set_local));
			folder.setProject(null);
		} else {
			MDataHelper hlp = new MDataHelper(getApplicationContext(),
					connectionId);
			MMyProject p = hlp.findProjectByFolder(id);
			MFolder f = null;
			folder.setId(id);
			folder.setProject(p);
			if (p != null)
				f = hlp.findFolder(id, p);
			if (f != null)
				folder.setName(f.getName());
			else
				folder.setName("?");
		}
		m_currentFolder.add(folder);

		IssueFoldersAdapter adapterFolders = new IssueFoldersAdapter(
				getBaseContext(), m_currentFolder);
		cmbFolder.setAdapter(adapterFolders);
		cmbFolder.setSelection(0);

		updateTeamCombos();
		updateMoreSettingsCombo();
		setissue_modified();
	}

	private void updateTeamCombos() {
		SelectedIssuesFolder folder = getSelectedFolder();
		if (folder == null || folder.getProject() == null)
			return;

		setAssignee(null);
		setResponsible(null);

	}

	private void updateMoreSettingsCombo() {
		final Spinner cmbMoreSettings = (Spinner) findViewById(R.id.cmbMoreSettings);
		final ArrayList<CharSequence> arrMoreSettings = new ArrayList<CharSequence>();
		arrMoreSettings.add(getString(R.string.more_settings_label));

		if (!isPanelVisible(R.id.pnlDueDate))
			arrMoreSettings.add(getString(R.string.issue_due_date_label));

		if (!isPanelVisible(R.id.pnlBudget))
			arrMoreSettings.add(getString(R.string.issue_budget_label));

		if (!isPanelVisible(R.id.cmbAssignee) && getSelectedFolder() != null)
			arrMoreSettings.add(getString(R.string.issue_assignee_label));

		if (!isPanelVisible(R.id.cmbResponsible) && getSelectedFolder() != null)
			arrMoreSettings.add(getString(R.string.issue_responsible_label));

		if (!isPanelVisible(R.id.edtDescription))
			arrMoreSettings.add(getString(R.string.issue_description_label));

		final ArrayAdapter<CharSequence> adapterMoreSettings = new ArrayAdapter<CharSequence>(
				getBaseContext(), android.R.layout.simple_spinner_item,
				arrMoreSettings);
		adapterMoreSettings
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		cmbMoreSettings.setAdapter(adapterMoreSettings);

		cmbMoreSettings.setVisibility(arrMoreSettings.size() > 1 ? View.VISIBLE
				: View.GONE);

		cmbMoreSettings.setSelection(0);
		cmbMoreSettings.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				CharSequence selected = (CharSequence) cmbMoreSettings
						.getSelectedItem();
				if (selected.toString().equals(
						getString(R.string.issue_due_date_label))) {
					switchPanel(cmbMoreSettings, adapterMoreSettings,
							selected.toString(), R.id.pnlDueDate);
					showSelectDateDlg();
				} else if (selected.toString().equals(
						getString(R.string.issue_budget_label))) {
					switchPanel(cmbMoreSettings, adapterMoreSettings,
							selected.toString(), R.id.pnlBudget);
					EditText hours = (EditText) findViewById(R.id.edtHours);
					hours.setFocusableInTouchMode(true);
					hours.requestFocus();
				} else if (selected.toString().equals(
						getString(R.string.issue_assignee_label))) {
					switchPanel(cmbMoreSettings, adapterMoreSettings,
							selected.toString(), R.id.cmbAssignee);
					selectAssignee();
				} else if (selected.toString().equals(
						getString(R.string.issue_responsible_label))) {
					switchPanel(cmbMoreSettings, adapterMoreSettings,
							selected.toString(), R.id.cmbResponsible);
					selectResponsible();
				} else if (selected.toString().equals(
						getString(R.string.issue_description_label))) {
					switchPanel(cmbMoreSettings, adapterMoreSettings,
							selected.toString(), R.id.edtDescription);
					EditText descr = (EditText) findViewById(R.id.edtDescription);
					descr.setFocusableInTouchMode(true);
					descr.requestFocus();
				}
				cmbMoreSettings.setVisibility(cmbMoreSettings.getAdapter()
						.getCount() > 1 ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}

	private void showSelectDateDlg() {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	private void setDueDate(Calendar dueDate) {

		Button btnClearDueDate = (Button) findViewById(R.id.btnClearDueDate);
		TextView lblDue = (TextView) findViewById(R.id.lblDue);

		m_dueDate = dueDate;
		ArrayList<CharSequence> arr = new ArrayList<CharSequence>();
		if (dueDate == null) {
			arr.add(getString(R.string.issue_click_to_assign_due_date));
			btnClearDueDate.setVisibility(View.GONE);
			lblDue.setVisibility(View.GONE);
		} else {
			arr.add(Time.formatDate(getApplicationContext(),
					m_dueDate.getTimeInMillis()));
			btnClearDueDate.setVisibility(View.VISIBLE);
			lblDue.setVisibility(View.VISIBLE);
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				getBaseContext(), android.R.layout.simple_spinner_item, arr);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Spinner cmbDueDate = (Spinner) findViewById(R.id.cmbDueDate);
		cmbDueDate.setAdapter(adapter);
		cmbDueDate.setSelection(0);
		setissue_modified();
	}

	private void setAssignee(Long userId) {

		m_assignee = null;
		ArrayList<CharSequence> arr = new ArrayList<CharSequence>();
		if (userId == null) {
			arr.add(getString(R.string.assignee_not_specified));
		} else {
			SelectedIssuesFolder folder = getSelectedFolder();
			if (folder == null)
				arr.add(getString(R.string.project_not_found));
			else {
				m_assignee = folder.findTeamMember(userId);
				if (m_assignee == null)
					arr.add(getString(R.string.user_not_found));
				else
					arr.add(m_assignee.getName());
			}
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				getBaseContext(), android.R.layout.simple_spinner_item, arr);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Spinner combo = (Spinner) findViewById(R.id.cmbAssignee);
		combo.setAdapter(adapter);
		combo.setSelection(0);
		setissue_modified();
	}

	private void setResponsible(Long userId) {

		m_responsible = null;

		ArrayList<CharSequence> arr = new ArrayList<CharSequence>();
		if (userId == null) {
			arr.add(getString(R.string.responsible_not_specified));
		} else {
			SelectedIssuesFolder folder = getSelectedFolder();
			if (folder == null)
				arr.add(getString(R.string.project_not_found));
			else {
				m_responsible = folder.findTeamMember(userId);
				if (m_responsible == null)
					arr.add(getString(R.string.user_not_found));
				else
					arr.add(m_responsible.getName());
			}
		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				getBaseContext(), android.R.layout.simple_spinner_item, arr);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		final Spinner combo = (Spinner) findViewById(R.id.cmbResponsible);
		combo.setAdapter(adapter);
		combo.setSelection(0);
		setissue_modified();
	}

	private void selectAssignee() {
		SelectedIssuesFolder folder = getSelectedFolder();

		if (folder == null)
			return;

		SelectUserDialogFragment dialog = new SelectUserDialogFragment(folder);
		dialog.setListener(new UsersListener() {
			@Override
			public void userSelected(Long userId) {
				setAssignee(userId);
			}

			@Override
			public void nobodySelected() {
				setAssignee(null);
			}
		});
		dialog.show(getSupportFragmentManager(), "folder");
	}

	private void selectResponsible() {
		SelectedIssuesFolder folder = getSelectedFolder();
		if (folder == null)
			return;
		SelectUserDialogFragment dialog = new SelectUserDialogFragment(folder);
		dialog.setListener(new UsersListener() {
			@Override
			public void userSelected(Long userId) {
				setResponsible(userId);
			}

			@Override
			public void nobodySelected() {
				setResponsible(null);
			}
		});
		dialog.show(getSupportFragmentManager(), "folder");
	}

	private void refreshButtons() {
		Button btn1 = (Button) findViewById(R.id.btnButton1);
		if (m_details == null) {
			btn1.setText(getString(R.string.save));
			btn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					saveChanges();
				}
			});
		} else {
			/*
			 * 1. local: save, complete 2. remote non synced: save, delete
			 */
			if (m_details instanceof MLocalIssue) {
				if (!missue_modified) {
					btn1.setText(m_details.getState() == State.TERMINATED ? getString(R.string.resume)
							: getString(R.string.completed));
				} else {
					btn1.setText(getString(R.string.save));
				}

				btn1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!missue_modified) {
							taskCompleted();
						} else {
							saveChanges();
						}
					}
				});
			} else if (m_details instanceof MRemoteNotSyncedIssue) {
				if (!missue_modified) {
					btn1.setText(getString(R.string.delete));
				} else {
					btn1.setText(getString(R.string.save));
				}
				btn1.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (!missue_modified) {
							deleteTask();
						} else {
							saveChanges();
						}
					}
				});
			}

		}
	}

	private void showFolderDialog() {
		FolderDialogFragment dialog = new FolderDialogFragment();
		dialog.setListener(new FolderListener() {
			@Override
			public void onLocalSelected() {
				setIssuesFolder(null, null);
			}

			@Override
			public void onFolderSelected(String connectionId, Long projectId,
					Long folderId) {
				setIssuesFolder(connectionId, folderId);
			}
		});

		dialog.show(getSupportFragmentManager(), "folder");
		return;
	}

	private void createUi() {
		mIgnoreModified = true;
		try {
			setContentView(R.layout.activity_new_issue);

			final ActionBar actionBar = getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowTitleEnabled(true);

			// ////////////////////////////////////////////////////////
			// Buttons
			//
			refreshButtons();
			// ////////////////////////////////////////////////////////
			// Name
			//
			anyEditTextWatcher = new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
					setissue_modified();
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				@Override
				public void afterTextChanged(Editable s) {
				}
			};

			EditText name = (EditText) findViewById(R.id.edtIssueName);
			name.addTextChangedListener(anyEditTextWatcher);
			EditText hours = (EditText) findViewById(R.id.edtHours);
			hours.addTextChangedListener(anyEditTextWatcher);
			EditText description = (EditText) findViewById(R.id.edtDescription);
			description.addTextChangedListener(anyEditTextWatcher);

			if (m_details == null)
				name.requestFocus();

			final InputMethodManager inputMethodManager = (InputMethodManager) getBaseContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputMethodManager.showSoftInput(name,
					InputMethodManager.SHOW_IMPLICIT);
			inputMethodManager.showSoftInput(hours,
					InputMethodManager.SHOW_IMPLICIT);
			inputMethodManager.showSoftInput(description,
					InputMethodManager.SHOW_IMPLICIT);
			// ////////////////////////////////////////////////////////
			// Priority
			//

			m_priorities.add(getString(R.string.issue_priority,
					getString(R.string.priority_low)));
			m_priorities.add(getString(R.string.issue_priority,
					getString(R.string.priority_normal)));
			m_priorities.add(getString(R.string.issue_priority,
					getString(R.string.priority_high)));
			m_priorities.add(getString(R.string.issue_priority,
					getString(R.string.priority_blocker)));

			Spinner cmbPriority = (Spinner) findViewById(R.id.cmbPriority);
			ArrayAdapter<CharSequence> priorityAdapter = new ArrayAdapter<CharSequence>(
					getBaseContext(), android.R.layout.simple_spinner_item,
					m_priorities);

			cmbPriority.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.performClick();
					setissue_modified();
					return true;
				}
			});

			priorityAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			cmbPriority.setAdapter(priorityAdapter);
			cmbPriority.setSelection(1);

			// ////////////////////////////////////////////////////////
			// Folder
			//
			final Spinner cmbFolder = (Spinner) findViewById(R.id.cmbFolder);

			setIssuesFolder(null, null);

			if (this.getServerName().isEmpty()) {
				cmbFolder.setVisibility(View.GONE);
				// cmbFolder.setEnabled(false);
			}
			cmbFolder.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {

						SessionManager sm = SessionManager
								.get(EditIssueActivity.this);
						if (sm.getConnections().size() == 0) {
							return true; // no connections configured
						}
						showFolderDialog();
						return true;
					}
					return true;
				}
			});

			// ////////////////////////////////////////////////////////
			// Due Date
			//
			final Spinner cmbDueDate = (Spinner) findViewById(R.id.cmbDueDate);
			final Button btnClearDueDate = (Button) findViewById(R.id.btnClearDueDate);
			btnClearDueDate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					setDueDate(null);
				}
			});
			cmbDueDate.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						showSelectDateDlg();
						return true;
					}
					return false;
				}
			});

			// ////////////////////////////////////////////////////////
			// Assignee
			//
			final Spinner cmbAssignee = (Spinner) findViewById(R.id.cmbAssignee);
			cmbAssignee.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						selectAssignee();
						return true;
					}
					return false;
				}
			});

			// ////////////////////////////////////////////////////////
			// Responsible
			//
			final Spinner cmbResponsible = (Spinner) findViewById(R.id.cmbResponsible);
			cmbResponsible.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						selectResponsible();
						return true;
					}
					return false;
				}
			});

			// ////////////////////////////////////////////////////////
			// Fill details for existing task
			//
			if (m_details != null) {
				// failure
				if (m_details instanceof MRemoteNotSyncedIssue) {
					MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) m_details;
					if (rnsi.getFailure() != null) {
						TextView lblFailure = (TextView) findViewById(R.id.lblSyncError);
						lblFailure.setVisibility(View.VISIBLE);
						lblFailure.setText(getString(R.string.sync_error,
								rnsi.getFailure()));
					}
				}

				// name
				name.setText(m_details.getName());

				// priority
				int priority = m_details.getPriority() == null ? Priority.NORMAL
						: m_details.getPriority();
				switch (priority) {
				case Priority.LOW:
					cmbPriority.setSelection(0);
					break;
				case Priority.HIGH:
					cmbPriority.setSelection(2);
					break;
				case Priority.BLOCKER:
					cmbPriority.setSelection(3);
					break;
				default:
					cmbPriority.setSelection(1);
				}

				// folder
				if (m_details instanceof MLocalIssue)
					setIssuesFolder(null, null);
				else if (m_details instanceof MRemoteNotSyncedIssue) {
					MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) m_details;
					setIssuesFolder(rnsi.getSrvConnId(), rnsi.getFolderId());
				} else
					throw new RuntimeException(
							"View doesn't support this kind of Issue");

				// due date
				if (m_details.getDueDate() != null
						&& m_details.getDueDate() > 0) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(m_details.getDueDate());
					onDateSet(c.get(Calendar.YEAR), c.get(Calendar.MONTH),
							c.get(Calendar.DAY_OF_MONTH));
					findViewById(R.id.pnlDueDate).setVisibility(View.VISIBLE);
				}

				// budget
				if (m_details.getBudget() != null && m_details.getBudget() > 0) {
					hours = (EditText) findViewById(R.id.edtHours);
					EditText minutes = (EditText) findViewById(R.id.edtMinutes);

					int hh = m_details.getBudget() / 60;
					if (hh > 0)
						hours.setText(String.valueOf(hh));
					else
						hours.setText("0");

					int mm = m_details.getBudget() % 60;
					if (mm > 0)
						minutes.setText(String.valueOf(mm));
					else
						minutes.setText("0");
					findViewById(R.id.pnlBudget).setVisibility(View.VISIBLE);
				}

				// assignee
				if (m_details instanceof MRemoteNotSyncedIssue) {
					MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) m_details;
					if (rnsi.getAssigneeId() != null
							&& rnsi.getAssigneeId() > 0) {
						setAssignee(rnsi.getAssigneeId());
						findViewById(R.id.cmbAssignee).setVisibility(
								View.VISIBLE);
					}
				}

				// responsible
				if (m_details instanceof MRemoteNotSyncedIssue) {
					MRemoteNotSyncedIssue rnsi = (MRemoteNotSyncedIssue) m_details;
					if (rnsi.getResponsibleId() != null
							&& rnsi.getResponsibleId() > 0) {
						setResponsible(rnsi.getResponsibleId());
						findViewById(R.id.cmbResponsible).setVisibility(
								View.VISIBLE);
					}
				}

				// description
				if (m_details.getDescription() != null) {
					description.setText(m_details.getDescription());
					findViewById(R.id.edtDescription).setVisibility(
							View.VISIBLE);
				}
			}

			updateMoreSettingsCombo();
		} finally {
			mIgnoreModified = false;
		}
	}

	private boolean isPanelVisible(int resource) {
		final View pnl = (View) findViewById(resource);
		return (pnl.getVisibility() == View.VISIBLE);
	}

	public String getServerName() {
		return curServerName;
	}

	private void switchPanel(Spinner cmb, ArrayAdapter<CharSequence> adapter,
			String label, int resource) {
		final View pnl = (View) findViewById(resource);
		pnl.setVisibility(View.VISIBLE);
		adapter.remove(label);
		cmb.setAdapter(adapter);
		if (adapter.getCount() <= 1)
			cmb.setVisibility(View.GONE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		curServerName = "";
		if (getIntent().getExtras() != null) {
			m_details = (MIssue) getIntent().getExtras().get(
					MIssue.class.getName());
			curServerName = getIntent().getExtras().get("SRVNAME").toString();
		} else
			m_details = null;

		if (m_details == null)
			if (curServerName.isEmpty()) {
				setTitle(R.string.action_newlocaltask);
			} else {
				setTitle(R.string.action_newtask);
			}
		else
			setTitle(m_details.getName());

		createUi();

		if (!(getServerName().isEmpty()) && savedInstanceState==null) {
			showFolderDialog();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (m_details == null) {
			getMenuInflater().inflate(R.menu.new_issue, menu);
		} else if (m_details instanceof MLocalIssue) {
			if (m_details.getState() == State.TERMINATED)
				getMenuInflater().inflate(R.menu.edit_local_closed, menu);
			else
				getMenuInflater().inflate(R.menu.edit_local_issue, menu);
		} else if (m_details instanceof MRemoteNotSyncedIssue) {
			getMenuInflater().inflate(R.menu.edit_ns_issue, menu);
		}
		return true;
	}

	private MIssue validateInput() {

		final Spinner cmbFolder = (Spinner) findViewById(R.id.cmbFolder);
		Long folderId = cmbFolder.getSelectedItemId();

		MIssue t = (folderId != null && folderId != 0L) ? new MRemoteNotSyncedIssue()
				: new MLocalIssue();

		t.setId(m_details == null ? null : m_details.getId());

		// ////////////////////////////////////////////////////////
		// / Validate name
		// /
		EditText edtName = (EditText) findViewById(R.id.edtIssueName);
		if (edtName.getText().toString().trim().length() == 0) {
			edtName.setError(getString(R.string.error_field_required));
			edtName.requestFocus();
			return null;
		} else
			t.setName(edtName.getText().toString().trim());

		// ////////////////////////////////////////////////////////
		// / Validate folder
		// /
		if (t instanceof MRemoteNotSyncedIssue)
			((MRemoteNotSyncedIssue) t).setFolderId(cmbFolder
					.getSelectedItemId());

		// ////////////////////////////////////////////////////////
		// / Validate priority
		// /
		Spinner cmbPriority = (Spinner) findViewById(R.id.cmbPriority);
		switch (cmbPriority.getSelectedItemPosition()) {
		case 0:
			changePriority(t, Priority.LOW);
			break;
		case 2:
			changePriority(t, Priority.HIGH);
			break;
		case 3:
			changePriority(t, Priority.BLOCKER);
			break;
		default:
			changePriority(t, Priority.NORMAL);
		}

		// ////////////////////////////////////////////////////////
		// / Validate due date
		// /
		if (m_dueDate != null)
			t.setDueDate(m_dueDate.getTimeInMillis());
		else
			t.setDueDate(0L);

		// ////////////////////////////////////////////////////////
		// / Validate budget
		// /
		LinearLayout pnlBudget = (LinearLayout) findViewById(R.id.pnlBudget);
		if (pnlBudget.getVisibility() == View.VISIBLE) {
			EditText hours = (EditText) findViewById(R.id.edtHours);
			EditText minutes = (EditText) findViewById(R.id.edtMinutes);
			t.setBudget(Integer.valueOf(Time.getMinutes(hours.getText()
					.toString(), minutes.getText().toString())));
		} else
			t.setBudget(null);

		// ////////////////////////////////////////////////////////
		// / Validate assignee
		// /
		if (t instanceof MRemoteNotSyncedIssue) {
			Spinner cmbAssignee = (Spinner) findViewById(R.id.cmbAssignee);
			if (cmbAssignee.getVisibility() == View.VISIBLE) {
				if (m_assignee != null)
					((MRemoteNotSyncedIssue) t).setAssigneeId(m_assignee
							.getId());
				else
					((MRemoteNotSyncedIssue) t).setAssigneeId(null);
			} else
				((MRemoteNotSyncedIssue) t).setAssigneeId(null);

		}

		// ////////////////////////////////////////////////////////
		// / Validate responsible
		// /
		if (t instanceof MRemoteNotSyncedIssue) {
			Spinner cmbResponsible = (Spinner) findViewById(R.id.cmbResponsible);
			if (cmbResponsible.getVisibility() == View.VISIBLE) {
				if (m_responsible != null)
					((MRemoteNotSyncedIssue) t).setResponsibleId(m_responsible
							.getId());
				else
					((MRemoteNotSyncedIssue) t).setResponsibleId(null);
			} else
				((MRemoteNotSyncedIssue) t).setResponsibleId(null);

		}

		// ////////////////////////////////////////////////////////
		// / Validate description
		// /
		EditText description = (EditText) findViewById(R.id.edtDescription);
		if (description.getVisibility() == View.VISIBLE) {
			t.setDescription(description.getText().toString());
		} else {
			t.setDescription(null);
		}

		if (m_details == null) {
			if (t instanceof MLocalIssue) {
				t.setState(State.RUNNING);
			} else { // MRemoteNotSyncedIssue
				MRemoteNotSyncedIssue rnsi = ((MRemoteNotSyncedIssue) t);
				rnsi.setSrvConnId(getSelectedFolder().getConnectionId());
			}
		} else {
			if (t instanceof MRemoteNotSyncedIssue) {
				((MRemoteNotSyncedIssue) t).setSrvConnId(getSelectedFolder()
						.getConnectionId());
			}
		}

		return t;
	}

	void changePriority(MIssue t, int _priority) {
		t.setPriority(_priority);
	}

	void deleteTask() {
		new AlertDialog.Builder(this)
				.setMessage(R.string.delete_task_confirm)
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (m_details instanceof MLocalIssue)
									BL.getLocal(getApplicationContext())
											.deleteTask((MLocalIssue) m_details);
								else if (m_details instanceof MRemoteNotSyncedIssue)
									BL.getLocal(getApplicationContext())
											.deleteTask(
													(MRemoteNotSyncedIssue) m_details);
								setResult(RESULT_OK);
								finish();
							}
						}).setNegativeButton(R.string.no, null).show();
	}

	void taskCompleted() {
		if (m_details instanceof MLocalIssue)
			BL.getLocal(getApplicationContext()).completeTask(
					(MLocalIssue) m_details);
		finish();
	}

	void saveChanges() {
		final MIssue task = validateInput();
		if (task != null) { // validated
			ILocalBL bl = BL.getLocal(getApplicationContext());
			if (m_details == null) {
				if (task instanceof MLocalIssue)
					bl.addTask((MLocalIssue) task);
				else if (task instanceof MRemoteNotSyncedIssue)
					bl.addTask((MRemoteNotSyncedIssue) task);
			} else {
				if (task instanceof MLocalIssue) {
					if (m_details instanceof MRemoteNotSyncedIssue) {
						bl.deleteTask((MRemoteNotSyncedIssue) m_details);
						task.setId(null);
						bl.addTask((MLocalIssue) task);
					} else
						bl.updateTask((MLocalIssue) task);
				} else if (task instanceof MRemoteNotSyncedIssue) {
					if (m_details instanceof MLocalIssue) {
						bl.deleteTask((MLocalIssue) m_details);
						task.setId(null);
						bl.addTask((MRemoteNotSyncedIssue) task);
					} else
						bl.updateTask((MRemoteNotSyncedIssue) task);
				}
			}
			Intent intent = new Intent();
			intent.putExtra("task", task);
			setResult(RESULT_OK, intent);
			finish();
		}
	}

	void cancelEditing() {
		finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_ok:
			saveChanges();
			break;
		case R.id.action_cancel:
			cancelEditing();
			break;
		case R.id.action_task_close:
		case R.id.action_task_resume:
			taskCompleted();
			break;
		case R.id.action_task_delete:
			deleteTask();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStart() {
		PkAlarmManager.activityStarted(getApplicationContext());
		super.onStart();
	}

	@Override
	protected void onStop() {
		PkAlarmManager.activityStopped(getApplicationContext());
		super.onStop();
	}
}
