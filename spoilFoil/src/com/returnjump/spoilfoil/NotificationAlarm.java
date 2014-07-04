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

        FridgeDbHelper fridgeDbHelper = new FridgeDbHelper(context);
        List<FridgeItem> expiredFridgeItems = getExpiredItems(fridgeDbHelper, context);

        // Trigger notifications if something is about to expire
        if (expiredFridgeItems.size() > 0) {

            // Only send notifications the the preferences set in their settings
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean pushPref = sharedPreferences.getBoolean(SettingsActivity.PREF_CHECKBOX_PUSH, SettingsActivity.PREF_CHECKBOX_PUSH_DEFAULT);
            boolean emailPref = sharedPreferences.getBoolean(SettingsActivity.PREF_CHECKBOX_EMAIL, SettingsActivity.PREF_CHECKBOX_EMAIL_DEFAULT);

            setExpiredItems(expiredFridgeItems, fridgeDbHelper, pushPref, emailPref);

            if (pushPref) {
                NotificationSender ns = new NotificationSender(context, expiredFridgeItems);
                ns.sendNotifications();
            }

            if (emailPref) {
                EmailNotifier emailSender = new EmailNotifier(context, expiredFridgeItems);
                emailSender.cloudEmailSender();
            }
        }

    }

    private static List<FridgeItem> getExpiredItems(FridgeDbHelper fridgeDbHelper, Context context) {
        String[] column = {DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE, DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED};
        String[] operator =  {"<=", "="};
        String[] whereValue = {FridgeDbHelper.calendarToString(Calendar.getInstance(), DatabaseContract.FORMAT_DATE), DatabaseContract.BOOL_FALSE_STR};
        String[] conjunction = {DatabaseContract.AND};

        return fridgeDbHelper.getAll(column, operator, whereValue, conjunction, true);
    }

    // Daily background job to set the expired flag in the database
    private static List<FridgeItem> setExpiredItems(List<FridgeItem> expiredFridgeItems, FridgeDbHelper fridgeDbHelper, boolean push, boolean email) {
        for(FridgeItem item: expiredFridgeItems) {
            fridgeDbHelper.update(item.getRowId(), null, null, null, null, DatabaseContract.BOOL_TRUE, null, null, null, null, push ? DatabaseContract.BOOL_TRUE : null, email ? DatabaseContract.BOOL_TRUE : null);
        }

        return expiredFridgeItems;
    }
}
