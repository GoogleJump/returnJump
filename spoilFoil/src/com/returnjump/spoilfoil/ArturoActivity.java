package com.returnjump.spoilfoil;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arturo);
        setupActionBar();
        findViewById(R.id.pushing_my_buttons).setOnClickListener(set_number);
        findViewById(R.id.popping_my_buttons).setOnClickListener(notify_me);
        findViewById(R.id.send_emails).setOnClickListener(sendemail);
        // Show the Up button in the action bar.
        Parse.initialize(this, "Gi733s6fybmROYVSXCrSoOCejvcYVM2zLr9n6LRO", "S4JauKQ70Rrtz8MYp6Sw6sCLt75RD8eAzY26rici");

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
                cloudEmailSender(ultimate_email);
        }
    };

    /* public static boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    } */

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


        public void cloudEmailSender(String fooditem){
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
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
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle("Testing notifications")
                                .setContentText(item_popped)
                                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                .setVibrate(new long[]{ 0, 1000 })
                                .setLights(Color.GREEN, 500, 1000);
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
