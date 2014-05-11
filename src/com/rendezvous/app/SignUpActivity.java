package com.rendezvous.app;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.facebook.model.GraphUser;
import com.loopj.android.http.AsyncHttpClient;
import com.rendezvous.app.http.BaseAsyncHttpResponseHandler;
import com.rendezvous.app.user.UserManager;

public class SignUpActivity extends ActionBarActivity {

	public final static String TAG = "SignUpActivity";

	private UserManager mUserManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		mUserManager = UserManager.getInstance(this);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);

			final String devicePhoneNumber = UserManager.getInstance(getActivity()).getDeviceNumber();
			if (devicePhoneNumber != null) {
				final EditText devicePhoneNumberEditText = (EditText) rootView.findViewById(R.id.device_phone_number);
				devicePhoneNumberEditText.setText(devicePhoneNumber);
			}

			setViewListeners(rootView);

			return rootView;
		}

		private void setViewListeners(View rootView) {

			final Button signUpButton = (Button) rootView.findViewById(R.id.sign_up);
			signUpButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final EditText usernameEditText = (EditText) getView().findViewById(R.id.username);
					final String username = usernameEditText.getText().toString();
					
					UserManager.getInstance(getActivity()).getUser().setUsername(username);

					registerUser();
				}
			});
			
			final Button submitVerificationCodeButton = (Button) rootView.findViewById(R.id.submit_verification_code);
			submitVerificationCodeButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final String username = UserManager.getInstance(getActivity()).getUser().getUsername();
					completeUserRegistration(username);
				}
			});
		}

		private void registerUser() {

			final AsyncHttpClient httpClient = new AsyncHttpClient();

			final JSONObject jsonParams = new JSONObject();
			try {
				final EditText usernameEditText = (EditText) getView().findViewById(R.id.username);
				final String username = usernameEditText.getText().toString();

				final EditText devicePhoneNumberEditText = (EditText) getView().findViewById(R.id.device_phone_number);
				final String devicePhoneNumber = devicePhoneNumberEditText.getText().toString();

				final GraphUser graphUser = UserManager.getInstance(getActivity()).getUser().getGraphUser();

				// set parameters for the http request
				jsonParams.put("username", username);
				jsonParams.put("password", "BLANK");
				jsonParams.put("firstname", graphUser.getFirstName());
				jsonParams.put("lastname", graphUser.getLastName());
				//jsonParams.put("facebook_id", graphUser.getId());
				jsonParams.put("facebook_id", UUID.randomUUID().toString());
				jsonParams.put("picture", getFacebookImageUrl(graphUser.getId()));
				jsonParams.put("phone", devicePhoneNumber);

			} catch (JSONException e) {
			}

			// the url to send the sign out http request to
			final String serverUrl = Constants.SERVER_BASE_URL + "/user/new/";

			StringEntity entity = null;
			try {
				entity = new StringEntity(jsonParams.toString());
			} catch (UnsupportedEncodingException e) {
			}

			// submit http request
			httpClient.post(getActivity(), serverUrl, entity, "application/json", new BaseAsyncHttpResponseHandler(
					getActivity()) {
				// the user has successfully logged in
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					super.onSuccess(statusCode, headers, responseBody);
					// Toast.makeText(getApplicationContext(), R.string.message_invites_have_been_sent,
					// Toast.LENGTH_SHORT).show();

					Log.d(TAG, String.valueOf(statusCode));

					hideSignUpForm();
					showVerificationForm();
					
					

					// startHomeActivity();

				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					super.onFailure(statusCode, headers, responseBody, error);
					Log.e(StatusActivity.TAG, "Failed to send invites.");

					showSignUpForm();
					hideVerificationForm();

					// dismissProgressDialog();
				}
			});
		}
		
		private void completeUserRegistration(String username) {

			final AsyncHttpClient httpClient = new AsyncHttpClient();

			final JSONObject jsonParams = new JSONObject();
			try {

				final EditText verificationCodeEditText = (EditText) getView().findViewById(R.id.verification_code);
				final String verificationCode = verificationCodeEditText.getText().toString();

				// set parameters for the http request
				jsonParams.put("username", username);
				jsonParams.put("code", verificationCode);

			} catch (JSONException e) {
			}

			// the url to send the sign out http request to
			final String serverUrl = Constants.SERVER_BASE_URL + "/user/new/";

			StringEntity entity = null;
			try {
				entity = new StringEntity(jsonParams.toString());
			} catch (UnsupportedEncodingException e) {
			}

			// submit http request
			httpClient.put(getActivity(), serverUrl, entity, "application/json", new BaseAsyncHttpResponseHandler(
					getActivity()) {
				// the user has successfully logged in
				@Override
				public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
					super.onSuccess(statusCode, headers, responseBody);
					// Toast.makeText(getApplicationContext(), R.string.message_invites_have_been_sent,
					// Toast.LENGTH_SHORT).show();

					Log.d(TAG, String.valueOf(statusCode));
					Log.d(TAG, "user registration successfully completed.");
				}

				@Override
				public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
					super.onFailure(statusCode, headers, responseBody, error);
					Log.e(StatusActivity.TAG, "Failed to send invites.");

					// dismissProgressDialog();
				}
			});
		}

		private void startStatusActivity() {
			final Intent intent = new Intent(getActivity(), StatusActivity.class);
			startActivity(intent);
		}

		private void showSignUpForm() {
			final View view = getView().findViewById(R.id.sign_up_form);
			view.setVisibility(View.VISIBLE);
		}

		private void hideSignUpForm() {
			final View view = getView().findViewById(R.id.sign_up_form);
			view.setVisibility(View.GONE);
		}

		private void showVerificationForm() {
			final View view = getView().findViewById(R.id.verification_form);
			view.setVisibility(View.VISIBLE);
		}

		private void hideVerificationForm() {
			final View view = getView().findViewById(R.id.verification_form);
			view.setVisibility(View.GONE);
		}

		final String getFacebookImageUrl(String graphUserId) {
			return "https://graph.facebook.com/" + graphUserId + "/picture";
		}
	}

}
