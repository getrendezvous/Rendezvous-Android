package com.rendezvous.app.http;

import org.apache.http.Header;

import android.content.Context;

import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * A base class for {@link AsyncHttpResponseHandler} to do whatever's necessary on success and on
 * failure for all http responses. Inherit from this class every time you make an async http request
 */
public class BaseAsyncHttpResponseHandler extends AsyncHttpResponseHandler {

	/** The context to use for interacting with user */
	private final Context mContext;

	/** Constructor */
	public BaseAsyncHttpResponseHandler(Context context) {
		mContext = context.getApplicationContext();
	}

	@Override
	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

	}

	@Override
	public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
		int messageResourceId;

		switch (statusCode) {
		case 401: // unauthorized
			//messageResourceId = R.string.message_unauthorized_error;
			break;

		case 0: // server is down
			//messageResourceId = R.string.message_server_unavailable_error;
			break;

		default:
			//messageResourceId = R.string.message_unexpected_error;
			break;
		}

		//Toast.makeText(mContext, messageResourceId, Toast.LENGTH_SHORT).show();

		error.printStackTrace();
	}
}
