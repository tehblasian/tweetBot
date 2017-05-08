package com.tehblasian.twitterbot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	// Shared Preferences
	private static SharedPreferences sharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Shared Preferences
		sharedPreferences = getApplicationContext().getSharedPreferences("userPreferences", 0);
		Boolean loggedIn = sharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
		Log.e("Logged in?", ""+loggedIn);
		if(!loggedIn) {
			//create intent to open the main activity
			Intent loginIntent = new Intent(this, LoginActivity.class);
			//start the new activity
			startActivity(loginIntent);
			finish();
		}
		else {
			//create intent to open the main activity
			Intent mainIntent = new Intent(this, MainActivity.class);
			//start the new activity
			startActivity(mainIntent);
			finish();
		}
	}
}
