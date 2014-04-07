package com.returnjump.spoilfoil;

import com.parse.Parse;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Parse App ID and Client Key can be found in /res/values/secret.xml
		Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
	}

}
