package com.returnjump.frij;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by arturomenacruz on 2014-06-28.
 */
public class NotificationEmail {
    Context context;
    List<FridgeItem> expiredFridgeItems;

    public NotificationEmail(Context context, List<FridgeItem> expiredFridgeItems){
        this.context = context;
        this.expiredFridgeItems = expiredFridgeItems;
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
        String email = SettingsActivity.getEmailAddress(sharedPref.getString(SettingsActivity.PREF_EMAIL_ADDRESS, SettingsActivity.PREF_EMAIL_ADDRESS_DEFAULT));
        HashMap<String, Object> params = new HashMap<String, Object>();

        params.put("email", email);
        params.put("expiredFridgeItems", new JSONArray(getNameArray(expiredFridgeItems)));
        ParseCloud.callFunctionInBackground("sendEmail", params, new FunctionCallback<String>() {
            @Override
            public void done(String message, com.parse.ParseException e) {
                if (e != null) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private List<String> getNameArray(List<FridgeItem> fridgeItems) {
        List<String> names = new ArrayList<String>();

        for (FridgeItem item : fridgeItems) {
            names.add(item.getName());
        }

        return names;
    }

}
