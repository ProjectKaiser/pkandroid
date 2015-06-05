package com.projectkaiser.app_android;

import java.io.IOException;

import org.apache.log4j.Logger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.projectkaiser.app_android.async.AsyncCallback;
import com.projectkaiser.app_android.bl.BL;
import com.projectkaiser.app_android.jsonapi.parser.ResponseParser;
import com.projectkaiser.app_android.jsonrpc.auth.AuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.GoogleAuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.PlainAuthScheme;
import com.projectkaiser.app_android.jsonrpc.auth.SessionAuthScheme;
import com.projectkaiser.app_android.jsonrpc.errors.EAppSyncWarning;
import com.projectkaiser.app_android.jsonrpc.errors.EAuthError;
import com.projectkaiser.app_android.jsonrpc.errors.EServerOutDate;
import com.projectkaiser.app_android.settings.SessionManager;
import com.projectkaiser.app_android.settings.SrvConnectionBaseData;
import com.projectkaiser.app_android.settings.SrvConnectionId;
import com.projectkaiser.mobile.sync.BatchRequest;
import com.projectkaiser.mobile.sync.MBasicRequest;
import com.projectkaiser.mobile.sync.MSynchronizeRequestEx;
import com.projectkaiser.mobile.sync.MSynchronizeResponseEx;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class SigninActivity extends Activity implements ConnectionCallbacks,
		OnConnectionFailedListener {

	private final Logger log = Logger.getLogger(SigninActivity.class);
	/**
	 * The default email to populate the email field with.
	 */
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	private static final int REQUEST_CODE_TOKEN_AUTH = 9001;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private AsyncCallback<?> m_currentCallback = null;

	// UI references.
	private EditText mEmailView;
	private CheckBox chbUseGoogleAuth;
	private EditText mPasswordView;
	private EditText mServerUrl;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private RadioButton mSaasServerView;
	private RadioButton mCustomServerView;
	private PlusClient mPlusClient;
	private ProgressDialog mConnectionProgressDialog;
	private ConnectionResult mConnectionResult;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mPlusClient = new PlusClient.Builder(this, this, this).setScopes(
				Scopes.PLUS_LOGIN, Scopes.PROFILE).build();
		
	
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog
				.setMessage(getString(R.string.google_authentication_progress));

		setContentView(R.layout.activity_login);

		// Set up the login form.

		chbUseGoogleAuth = (CheckBox) findViewById(R.id.chbUseGoogleAuth);
		chbUseGoogleAuth
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						LinearLayout pnlPlainAuth = (LinearLayout) findViewById(R.id.pnlPlainAuth);
						pnlPlainAuth.setVisibility(chbUseGoogleAuth.isChecked() ? View.GONE
								: View.VISIBLE);
						if (!chbUseGoogleAuth.isChecked())
							mEmailView.requestFocus();
					}
				});

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.btnSignIn).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});

		findViewById(R.id.btnTaskActionCancel).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ActivityMgr.get(SigninActivity.this).startMain()
								.finishMe();
					}
				});

		mServerUrl = (EditText) findViewById(R.id.edtServerUrl);
		mEmailView = (EditText) findViewById(R.id.email);

		rbnServerChanged(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (m_currentCallback != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String mEmail = mEmailView.getText().toString();
		String mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (!chbUseGoogleAuth.isChecked()) {
			// Check for a valid password.
			if (TextUtils.isEmpty(mPassword)) {
				mPasswordView
						.setError(getString(R.string.error_field_required));
				focusView = mPasswordView;
				cancel = true;
			}

			// Check for a valid email address.
			if (TextUtils.isEmpty(mEmail)) {
				mEmailView.setError(getString(R.string.error_field_required));
				focusView = mEmailView;
				cancel = true;
			} else if (!mEmail.contains("@")) {
				mEmailView.setError(getString(R.string.error_invalid_email));
				focusView = mEmailView;
				cancel = true;
			}

			if (cancel) {
				focusView.requestFocus();
				return;
			}

			PlainAuthScheme scheme = new PlainAuthScheme();
			scheme.setUserName(mEmailView.getText().toString());
			scheme.setPassword(mPasswordView.getText().toString());

			loginRequest(scheme);
		} else {
			startGoogleAuth();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
	}

	private void startGoogleAuth() {
		if (mConnectionResult == null) {
			log.debug("startGoogleAuth - connect PlusClient");
			mConnectionProgressDialog.show();
			mPlusClient.connect();
		} else {
			try {
				log.debug("startGoogleAuth - start resolution");
				mConnectionResult.startResolutionForResult(this,
						REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				mConnectionResult = null;
				mPlusClient.connect();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		if (requestCode == REQUEST_CODE_RESOLVE_ERR) {
			log.debug(String.format("onActivityResult(requestCode=%d, responseCode=%d)", requestCode, responseCode));
			if (responseCode == RESULT_OK) {
				mConnectionResult = null;
				mPlusClient.connect();
			} else {
				mConnectionProgressDialog.dismiss();
				mConnectionResult = null;
				Toast.makeText(
						this,
						getString(R.string.authentication_failed) + ": "
								+ String.valueOf(responseCode),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		log.debug("onConnected");
		mConnectionProgressDialog.dismiss();

		final String account = mPlusClient.getAccountName();

		final GoogleAuthScheme scheme = new GoogleAuthScheme();
		if (mPlusClient.getCurrentPerson()!=null) {
			scheme.setDisplayName(mPlusClient.getCurrentPerson().getDisplayName());
			scheme.setPictureUrl(mPlusClient.getCurrentPerson().getImage().getUrl());
		}
		mPlusClient.clearDefaultAccount();
		mPlusClient.disconnect();

		AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String token = null;

				try {

					log.debug("requesting token");
					token = GoogleAuthUtil.getToken(SigninActivity.this,
							account, "oauth2:" + Scopes.PROFILE);
					log.debug("token="+token);

				} catch (IOException transientEx) {
					log.error(transientEx);
					Toast.makeText(SigninActivity.this,
							getString(R.string.network_error),
							Toast.LENGTH_LONG).show();
				} catch (UserRecoverableAuthException e) {
					log.error(e);
					Intent recover = e.getIntent();
					startActivityForResult(recover, REQUEST_CODE_TOKEN_AUTH);
				} catch (GoogleAuthException authEx) {
					log.error(authEx);
					Toast.makeText(SigninActivity.this,
							getString(R.string.authentication_failed)  ,
							Toast.LENGTH_LONG).show();
				}

				return token;
			}

			@Override
			protected void onPostExecute(String token) {
				log.debug("token received="+token);

				if (token != null) {
					scheme.setEmail(account);
					scheme.setToken(token);
					loginRequest(scheme);
				}
			}

		};
		task.execute();

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		log.debug("onConnectionFailed");
		if (mConnectionProgressDialog.isShowing()) {
			log.debug("onConnectionFailed showing dialog = true");
			if (result.hasResolution()) {
				try {
					log.debug("onConnectionFailed start resolution");
					result.startResolutionForResult(this,
							REQUEST_CODE_RESOLVE_ERR);
				} catch (SendIntentException e) {
					mPlusClient.connect();
				}
			}
		}
		mConnectionResult = result;
	}

	private String getServerUrl() {
		return mServerUrl.getText().toString();
	}

	public void onDisconnected() {
		log.debug("onDisconnected");
		// Do nothing
	}

	void syncRequest(final SrvConnectionId connectionId) {
		log.debug("syncRequest");
		showProgress(true);
		mLoginStatusMessageView.setText(R.string.login_progress_signing_in);

		final AsyncCallback<MSynchronizeResponseEx> syncCallback = new AsyncCallback<MSynchronizeResponseEx>() {

			@Override
			public void onSuccess(MSynchronizeResponseEx response) {
				log.debug("syncRequest success");
				showProgress(false);
				SessionManager sm = SessionManager.get(SigninActivity.this);
				ResponseParser.parseSyncResponse(sm, connectionId.getId(),
						response.getData());

				ActivityMgr.get(SigninActivity.this).startMain().finishMe();
				m_currentCallback = null;
			}

			@Override
			public void onFailure(Throwable e) {
				log.error(e);
				showProgress(false);
				Toast.makeText(getApplicationContext(), e.getMessage(),
						Toast.LENGTH_LONG).show();
				m_currentCallback = null;
			}
		};

		SessionManager sm = SessionManager.get(SigninActivity.this);

		SessionAuthScheme scheme = new SessionAuthScheme();
		SrvConnectionBaseData base = sm.getBaseData(connectionId.getId());

		scheme.setSessionId(base.getSessionId());

		MSynchronizeRequestEx request = new MSynchronizeRequestEx();

		request.setAuthScheme(scheme);
		request.setServerUrl(sm.getServerUrl(connectionId.getId()));
		request.setLocale(getResources().getConfiguration().locale);

		BatchRequest br = new BatchRequest();
		br.setSyncRequest(request);

		m_currentCallback = syncCallback;
		BL.getServer(getApplicationContext()).synchronize(br, syncCallback);
	}

	void loginRequest(final AuthScheme scheme) {
		log.debug("loginRequest");
		showProgress(true);
		mLoginStatusMessageView.setText(R.string.login_progress_signing_in);

		AsyncCallback<String> loginCallback = new AsyncCallback<String>() {

			@Override
			public void onSuccess(String response) {
				
				log.debug("loginRequest success");
				
				SrvConnectionId connectionId = SessionManager.get(
						SigninActivity.this).loggedOn(getServerUrl(), scheme,
						response);

				syncRequest(connectionId);

			}

			@Override
			public void onFailure(Throwable e) {
				
				log.error(e);
								
				showProgress(false);
				if (e instanceof EAuthError) {
					if (scheme instanceof PlainAuthScheme) {
						mPasswordView
								.setError(getString(R.string.error_incorrect_email_or_password));
						mPasswordView.requestFocus();
					} else
						Toast.makeText(getApplicationContext(), e.getMessage(),
								Toast.LENGTH_LONG).show();
				} else if (e instanceof EAppSyncWarning) {
					String newmsg = ((EAppSyncWarning)e).GetErrorText(getApplicationContext()); 
					Toast.makeText(getApplicationContext(),
							newmsg,	Toast.LENGTH_LONG).show();
				} else if (e instanceof EServerOutDate) {
					String newmsg = ((EServerOutDate)e).GetErrorText(getApplicationContext()); 
					Toast.makeText(getApplicationContext(),
							newmsg,
							Toast.LENGTH_LONG).show();
				} else
					Toast.makeText(getApplicationContext(), e.getMessage(),
							Toast.LENGTH_LONG).show();
				m_currentCallback = null;
			}
		};

		MBasicRequest request = new MBasicRequest();

		request.setAuthScheme(scheme);
		request.setServerUrl(getServerUrl());
		request.setLocale(getResources().getConfiguration().locale);

		m_currentCallback = loginCallback;
		BL.getServer(getApplicationContext()).login(request, loginCallback);

	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	public void rbnServerChanged(View view) {

		mSaasServerView = (RadioButton) findViewById(R.id.rbnPKSaas);
		mCustomServerView = (RadioButton) findViewById(R.id.rbnCustomServer);
		mServerUrl = (EditText) findViewById(R.id.edtServerUrl);

		mServerUrl.setEnabled(mCustomServerView.isChecked());
		if (mSaasServerView.isChecked()) {
			mServerUrl.setText(R.string.server_cloud_url);
		} else {
			mServerUrl.setText("http://");
			mServerUrl.setSelection(mServerUrl.getText().length());
		}
	}

}
