package com.returnjump.spoilfoil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

/**
 * Created by arturomenacruz on 2014-06-17.
 */
public class NotificationAlarm extends BroadcastReceiver {

    //Method gets called when the alarm goes off
    @Override
    public void onReceive(Context context, Intent intent) {
        /** Once alarm is fired checks for all items that are expiring within the database and notifies
         * user locally.
         */

        Log.wtf("RECEIVER", "CALLED!");

        FridgeDbHelper fridgeDbHelper = new FridgeDbHelper(context);
        final Calendar rightnow = Calendar.getInstance();
        String[] column = {DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED};
        String[] operator =  {"<", "="};
        String[] wherevalue = {FridgeDbHelper.calendarToString(rightnow, DatabaseContract.FORMAT_DATE), DatabaseContract.BOOL_FALSE_STR};
        String[] conjunction = {DatabaseContract.AND};

        List<FridgeItem> foodexpiring = fridgeDbHelper.getAll(column, operator, wherevalue, conjunction, true);

        // Only send notifications the the preferences set in their settings
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean pushPref = sharedPreferences.getBoolean(SettingsActivity.PREF_CHECKBOX_PUSH, SettingsActivity.PREF_CHECKBOX_PUSH_DEFAULT);
        boolean emailPref = sharedPreferences.getBoolean(SettingsActivity.PREF_CHECKBOX_EMAIL, SettingsActivity.PREF_CHECKBOX_EMAIL_DEFAULT);

        if (pushPref) {
            NotificationSender ns = new NotificationSender(context, foodexpiring);
            ns.sendNotifications();
        }

        if (emailPref) {
            EmailNotifier emailsender = new EmailNotifier(context, foodexpiring);
            emailsender.cloudEmailSender();
        }

    }
}
