package com.returnjump.spoilfoil;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

/**
 * Created by arturomenacruz on 2014-06-17.
 */
public class NotificationAlarm extends BroadcastReceiver {
    //Method gets called when the alarm goes off
    private FridgeDbHelper fridge;
    @Override
    public void onReceive(Context context, Intent intent) {
        /** Once alarm is fired checks for all items that are expiring within the database and notifies
         * user locally.
         */
        Toast.makeText(context, "Don't panik but your time is up!!!!.",
                Toast.LENGTH_LONG).show();
        fridge = new FridgeDbHelper(context);
        Calendar rightnow = Calendar.getInstance();
        String expiry_date_params = DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE;
        String already_expired_param = DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED;

        List<FridgeItem> foodexpiring = fridge.getAll([expiry_date_params, already_expired_param ], [DatabaseContract.AND], ["<", "="], ["2014-01-01", DatabaseContract.BOOL_FALSE], true);
        NotificationSender ns = new NotificationSender(context);
        Intent scheduledIntent = new Intent(context, NotificationAlarm.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, scheduledIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ns.sendNotifications();
    }
}
