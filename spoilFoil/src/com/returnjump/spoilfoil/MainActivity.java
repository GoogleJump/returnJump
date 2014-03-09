package com.returnjump.spoilfoil;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		findViewById(R.id.open_arturo_activity).setOnClickListener(openArturoActivity);
		findViewById(R.id.open_jeffrey_activity).setOnClickListener(openJeffreyActivity);
		findViewById(R.id.open_kelsey_activity).setOnClickListener(openKelseyActivity);
		findViewById(R.id.open_tasti_activity).setOnClickListener(openTastiActivity);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private OnClickListener openArturoActivity = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(view.getContext(), ArturoActivity.class);

            startActivity(intent);
		}
		
	};
	
	private OnClickListener openJeffreyActivity = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(view.getContext(), JeffreyActivity.class);

            startActivity(intent);
		}
		
	};
	
	private OnClickListener openKelseyActivity = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(view.getContext(), KelseyActivity.class);

            startActivity(intent);
		}
		
	};
	
	private OnClickListener openTastiActivity = new OnClickListener() {

		@Override
		public void onClick(View view) {
			Intent intent = new Intent(view.getContext(), TastiActivity.class);

            startActivity(intent);
		}
		
	};

}
