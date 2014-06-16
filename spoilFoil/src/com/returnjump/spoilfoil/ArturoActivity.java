package com.returnjump.spoilfoil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArturoActivity extends Activity {
    List<String> fridge = new ArrayList<String>();
    List<FoodItem> ItemsToNotify = new ArrayList<FoodItem>();
    private FridgeDbHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arturo);
        setupActionBar();
        findViewById(R.id.pushing_my_buttons).setOnClickListener(notifier);
        findViewById(R.id.send_emails).setOnClickListener(sendemail);
        // Show the Up button in the action bar.
        Parse.initialize(this, getResources().getString(R.string.parse_app_id), "S4JauKQ70Rrtz8MYp6Sw6sCLt75RD8eAzY26rici");
    }


    public OnClickListener sendemail = new OnClickListener(){
        public void onClick(View View){
                cloudEmailSender("random fooditem");
        }
    };
    public OnClickListener notifier = new OnClickListener() {
        @Override
        public void onClick(View v) {
            NotificationSender();
        }
    };

        public void cloudEmailSender(String fooditem){
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
            NotificationSender(expiring_Items);
        }
    }; */

    public void NotificationSender(/*List<FridgeItem>items_expiring  */ ) {

               //Setting up the intent for the notification
        Intent intent = new Intent(getApplicationContext(), KelseyActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

               // Creates the Notification mBuilder
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext())
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Expiring Items")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{ 0, 1000 })
                        .setLights(Color.GREEN, 500, 1000);
        String current_text = "The following items are about to expire: ";
        int number_items_expiring = 0;
        //for (int i=0; i < items_expiring.size(); i ++){
        //    current_text += items_expiring.get(i).getFoodItem() + " ";
         //   number_items_expiring ++;
        //}
        mBuilder.setContentText(current_text)
                .setNumber(number_items_expiring)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        //sets the type of alert
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
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
