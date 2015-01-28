package com.projectkaiser.app_android;

import java.util.ArrayList;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import com.projectkaiser.app_android.fragments.issue.CommentsFragment;
import com.projectkaiser.app_android.fragments.issue.IssueDetailsFragment;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsListener;
import com.projectkaiser.app_android.fragments.issue.intf.ITaskDetailsProvider;
import com.projectkaiser.app_android.services.PkAlarmManager;
import com.projectkaiser.mobile.sync.MIssue;

public class ViewIssueActivity extends ActionBarActivity implements ITaskDetailsProvider {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	MIssue m_details;
	
	private void createUi() {
		setContentView(R.layout.activity_issue);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.vpTask);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						actionBar.setSelectedNavigationItem(position);
					}
				});

	}
	
	private void createTabs() {
		
		final ActionBar actionBar = getSupportActionBar();
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
				// Ignore this event
			}

			@Override
			public void onTabSelected(Tab tab, FragmentTransaction arg1) {
				mViewPager.setCurrentItem(tab.getPosition());
			}

			@Override
			public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
				// Ignore this event
			}
		};

		Tab tab = actionBar.newTab().setText(R.string.tab_details)
				.setTabListener(tabListener);
		actionBar.addTab(tab);

		tab = actionBar.newTab().setText(R.string.tab_comments)
				.setTabListener(tabListener);
		actionBar.addTab(tab);
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_details = (MIssue)getIntent().getExtras().getSerializable(MIssue.class.getName());
		setTitle(m_details.getName());

		createUi();
		
		createTabs();
		for (ITaskDetailsListener listener:m_listeners)
			listener.taskLoaded(m_details);		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.issue_remote, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			if (position == 0) {
				return IssueDetailsFragment.newInstance();
			} else 
				return CommentsFragment.newInstance();
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.tab_details).toUpperCase(l);
			case 1:
				return getString(R.string.tab_comments).toUpperCase(l);
			}
			return null;
		}
	}

	
	private ArrayList<ITaskDetailsListener> m_listeners = new ArrayList<ITaskDetailsListener>();
	@Override
	public void registerListener(ITaskDetailsListener listener) {
		m_listeners.add(listener);
		if (m_details != null)
			listener.taskLoaded(m_details);
	}	


}
