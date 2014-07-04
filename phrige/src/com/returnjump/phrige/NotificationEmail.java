package com.returnjump.phrige;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;

import java.util.HashMap;
import java.util.List;

/**
 * Created by arturomenacruz on 2014-06-28.
 */
public class NotificationEmail {
    Context context;
    List<FridgeItem> foodExpiring;

    public NotificationEmail(Context context, List<FridgeItem> foodExpiring){
        this.context = context;
        this.foodExpiring = foodExpiring;
    }
    public void cloudEmailSender(){
        /** Notifies through email that that fooditem is going to expire. If it fails to send
         * method warns the android app. Otherwise it alerts the user that it succeeded
         * Examples:
         * cloudEmailSender("milk")
         * Will send an email like so:
         * "Hello Dear User: The following item: milk is about to expire"
         */
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String email = sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, "");
        HashMap<String, Object> params = new HashMap<String, Object>();
        String items_expiring = "";
        for (FridgeItem el : foodExpiring){
            // Temporary fix
            if (!el.isNotifiedEmail()) {
                items_expiring += el.getName() + " ";
            }
        }
        params.put("address", email);
        params.put("Expiring", items_expiring);
        ParseCloud.callFunctionInBackground("emailsender", params, new FunctionCallback<String>() {
            @Override
            public void done(String message, com.parse.ParseException e) {
                if (e == null) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

}
