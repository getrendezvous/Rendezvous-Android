package com.rendezvous.app.http;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.rendezvous.app.user.UserManager;

public class JSONParamsWithToken extends JSONObject {

    private final UserManager mUserManager;
    
    private final String TAG = "JSONParamWithToken";

    public JSONParamsWithToken(Context context) {
        super();
        
        mUserManager = UserManager.getInstance(context);

        try {
        	// api token
			put("key", mUserManager.getUser().getApiToken());
		} catch (JSONException e) {
			Log.d(TAG, "Error when encoding user api token.");
		}
    }
}
