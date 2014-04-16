package com.returnjump.spoilfoil;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View.OnClickListener;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.TextView;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class ArturoActivity extends Activity {
	List<String> fridge = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_arturo);
		// Show the Up button in the action bar.
		setupActionBar();
		findViewById(R.id.pushing_my_buttons).setOnClickListener(set_number);
		findViewById(R.id.popping_my_buttons).setOnClickListener(notify_me);
		findViewById(R.id.send_emails).setOnClickListener(sendemail);
	}
	public void refresh_view(){
		TextView updated_list_view = (TextView) findViewById(R.id.fridge_list);
		String fridge_string = "";
		int x = fridge.size();
		for (int i = 0; i < x; i++){
			fridge_string = fridge_string + fridge.get(i)+ "\n";
		}
		
		updated_list_view.setText(fridge_string);
	}
		
	
	public OnClickListener sendemail = new OnClickListener(){
		public void onClick(View View){
			{
                // TODO Auto-generated method stub

                try {   
                    /*GmailSender sender = new GmailSender("returnjump@gmail.com", getString(R.string.email_password));
                    sender.sendMail("This is Subject",   
                            "This is Body",   
                            "arturo.1035@gmail.com",   
                            "arturo.1035@gmail.com");   */
                } catch (Exception e) {   
                    Log.e("SendMail", e.getMessage(), e);   
                } 

            }
		}
	};
			
	public OnClickListener set_number = new OnClickListener(){
		public void onClick(View view){
			fridge.add("Banananananannanana");
			refresh_view();
			
		}
	};
	public OnClickListener notify_me = new OnClickListener(){
		public void onClick(View view){
			if (!fridge.isEmpty()){
				String item_popped = fridge.get(fridge.size()-1);
				fridge.remove(fridge.size()-1);
			// Creates the Notification mBuilder
				NotificationCompat.Builder mBuilder =
					    new NotificationCompat.Builder(view.getContext())
					    .setSmallIcon(R.drawable.ic_launcher)
					    .setContentTitle("Testing notifications")
					    .setContentText(item_popped);
				//sets the type of alert
				int mNotificationId = 001;
				// Gets an instance of the NotificationManager service
				NotificationManager mNotifyMgr = 
				        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
				// Builds the notification and issues it.
				mNotifyMgr.notify(mNotificationId, mBuilder.build());
				refresh_view();
			}
		}
	};
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.arturo, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	} 
}

