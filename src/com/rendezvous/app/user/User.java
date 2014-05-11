package com.rendezvous.app.user;

import com.facebook.Session;
import com.facebook.model.GraphUser;

public class User {

	private Session mSession;
	private GraphUser mGraphUser;
	private String mUsername;
	private String mApiToken;

	public User(Session session, GraphUser graphUser, String apiToken) {
		mSession = session;
		mGraphUser = graphUser;
		mApiToken = apiToken;
	}

	public Session getSession() {
		return mSession;
	}
	
	public String getUsername() {
		return mUsername;
	}

	public GraphUser getGraphUser() {
		return mGraphUser;
	}

	public void setGraphUser(GraphUser graphUser) {
		mGraphUser = graphUser;
	}
	
	public void setUsername(String username) {
		mUsername = username;
	}

	public void setSession(Session session) {
		mSession = session;
	}

	public String getApiToken() {
		return mApiToken;
	}

}
