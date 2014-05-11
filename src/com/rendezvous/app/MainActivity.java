package com.rendezvous.app;

import java.util.UUID;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.eclipsesource.json.JsonObject;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.model.GraphUser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.rendezvous.app.http.BaseAsyncHttpResponseHandler;
import com.rendezvous.app.user.User;
import com.rendezvous.app.user.UserManager;

public class MainActivity extends Activity {

	private Session.StatusCallback mStatusCallback = new SessionStatusCallback();
	
	private UserManager mUserManager;
	
	private final String TAG = "MainActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mUserManager = UserManager.getInstance(this);

		prepareSession(savedInstanceState);

		setViewListeners();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_main, container, false);

			return rootView;
		}

	}

	public void prepareSession(Bundle savedInstanceState) {
		Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

		Session session = Session.getActiveSession();
		if (session == null) {
			if (savedInstanceState != null) {
				session = Session.restoreSession(this, null, mStatusCallback, savedInstanceState);
			}
			if (session == null) {
				session = new Session(this);
			}
			Session.setActiveSession(session);
			if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
				session.openForRead(new Session.OpenRequest(this).setCallback(mStatusCallback));
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final Session session = Session.getActiveSession();
		Session.saveSession(session, outState);
	}

	private void onLoggedIn() {
		
		final Session session = Session.getActiveSession();
		if (session.isOpened()) {
			// make request to the /me API
			final Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
				// callback after Graph API response with user object
				@Override
				public void onCompleted(GraphUser graphUser, Response response) {
					if (graphUser != null) {
						final Session session = Session.getActiveSession();
				        final User user = new User(session, graphUser, null);
						mUserManager.setCurrentUser(user);
						sendUserInfoToServer();
						
					}
				}
			});

			final Bundle params = request.getParameters();
			
		    params.putString("fields", "id,email,first_name,last_name");
			
			request.setParameters(params);
			
			request.executeAsync();
		}
	}
	
	private void onLoggedInCompleted() {
		
	}
	
	private void sendUserInfoToServer() {
		final AsyncHttpClient httpClient = new AsyncHttpClient();

		// the url to send the sign out http request to
		final String serverUrl = Constants.SERVER_BASE_URL + "/user/exists/fb/";
		
		final RequestParams params = new RequestParams();
		// params.add("facebook_id", mUserManager.getUser().getGraphUser().getId());
		params.add("facebook_id", UUID.randomUUID().toString());
		
		// submit http request
		httpClient.get(this, serverUrl, params, new BaseAsyncHttpResponseHandler((Context) this) {
			// the user has successfully logged in
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				super.onSuccess(statusCode, headers, responseBody);
				onLoggedInCompleted();
				//Toast.makeText(getApplicationContext(), R.string.message_invites_have_been_sent, Toast.LENGTH_SHORT).show();
				
				final JsonObject json = JsonObject.readFrom(new String(responseBody));

				if (json.get("availability").asBoolean()) {
					startSignUpActivity();
				} else {
				}
				
				finish();
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				super.onFailure(statusCode, headers, responseBody, error);
				Log.e(TAG, "Failed to connect to server to authenticate.");

				//dismissProgressDialog();
			}
		});
	}
	
	private void startSignUpActivity() {
		final Intent intent = new Intent(this, SignUpActivity.class);
		startActivity(intent);
	}

	private void setViewListeners() {
		final Button loginWithFacebook = (Button) findViewById(R.id.login_with_facebook);
		loginWithFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onClickLogin();
			}
		});
	}

	private void onClickLogin() {
		Session session = Session.getActiveSession();
		if (!session.isOpened() && !session.isClosed()) {
			session.openForRead(new Session.OpenRequest(this).setCallback(mStatusCallback));
		} else {
			Session.openActiveSession(this, true, mStatusCallback);
		}
	}
	
	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state, Exception exception) {
			if (state.isOpened()) {
				onLoggedIn();
			}
		}
	}
	
}
