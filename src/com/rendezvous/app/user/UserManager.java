package com.rendezvous.app.user;

import android.content.Context;
import android.telephony.TelephonyManager;

public class UserManager {

	private static UserManager sInstance = null;
	
	private Context mContext;

	private User mUser = null;

	private UserManager(Context context) {
		mContext = context;
	}

	public static UserManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new UserManager(context);
		}
		return sInstance;
	}

	public User getUser() {
		return mUser;
	}

	public void setCurrentUser(User user) {
		mUser = user;
	}

	public void unsetCurrentUser() {
		setCurrentUser(null);
	}

	public boolean isUserLoggedIn() {
		return (mUser != null);
	}
	
	public String getDeviceNumber() {
		final TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE); 
		final String number = tm.getLine1Number();
		return number;
	}
	
	// private User findAuthorizedUser() {}
}
