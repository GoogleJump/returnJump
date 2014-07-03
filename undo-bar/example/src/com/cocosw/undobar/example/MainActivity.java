package com.cocosw.undobar.example;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.button1).setOnClickListener(this);
		findViewById(R.id.button2).setOnClickListener(this);
		findViewById(R.id.button3).setOnClickListener(this);
		findViewById(R.id.button4).setOnClickListener(this);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.button1:
			startActivity(new Intent(this, UndoStyle.class));
			break;
		case R.id.button2:
			startActivity(new Intent(this, RetryStyle.class));
			break;
		case R.id.button3:
			startActivity(new Intent(this, MessageStyle.class));
			break;
		case R.id.button4:
			startActivity(new Intent(this, Customize.class));
			break;
		default:
			break;
		}

	}
}
