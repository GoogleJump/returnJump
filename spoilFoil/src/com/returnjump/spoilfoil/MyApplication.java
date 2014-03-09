package com.returnjump.spoilfoil;

import com.parse.Parse;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		
		// Place Parse initialization below
		//Parse.initialize(this, getString(R.string.parse_app_id), getString(R.string.parse_client_key));
	}

}
