package com.returnjump.spoilfoil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;

import java.util.HashMap;

public class ArturoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arturo);
        setupActionBar();
        findViewById(R.id.pushing_my_buttons).setOnClickListener(notifier);
        findViewById(R.id.send_emails).setOnClickListener(sendemail);
        // Alarm Set up
        //Calendar cal = Calendar.getInstance();

        //Creates intent that will be called when alarm goes off
        //Intent intent = new Intent(getBaseContext(), NotificationAlarm.class);
        //PendingIntent pIntent = PendingIntent.getBroadcast(getBaseContext(), 0, intent, 0);

        //AlarmManager alarm = (AlarmManager) getSystemService(getBaseContext().ALARM_SERVICE);
        //alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), (60 * 1000* 60 * 24), pIntent);

    }

    public OnClickListener sendemail = new OnClickListener(){
        public void onClick(View View){
                cloudEmailSender("random fooditem");
        }
    };
    public OnClickListener notifier = new OnClickListener() {
        @Override
        public void onClick(View v) {
            NotificationSender ns = new NotificationSender(getApplicationContext());
            ns.sendNotifications();
        }
    };

        public void cloudEmailSender(String fooditem){
            /** Notifies through email that that fooditem is going to expire. If it fails to send
             * method warns the android app. Otherwise it alerts the user that it succeeded
             * Examples:
             * cloudEmailSender("milk")
             * Will send an email like so:
             * "Hello Dear User: The following item: milk is about to expire"
             */
             SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());
            String email = sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, "");
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("address", email);
            params.put("Expiring", fooditem);
            ParseCloud.callFunctionInBackground("emailsender", params, new FunctionCallback<String>() {
                @Override
                public void done (String message, com.parse.ParseException e) {
                    if (e == null){
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


    /* public void ExpiredItemsNotifier() {
        List<FridgeItem> expiring_Items = dbhelper.getAll(DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, "<", FridgeDbHelper.calendarToString(GregorianCalendar.getInstance(), DatabaseContract.FORMAT_DATETIME), true);
        if (expiring_Items.size() != 0) {
            notificationSender.sendNotification(expiring_Items);
        }
    }; */

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
