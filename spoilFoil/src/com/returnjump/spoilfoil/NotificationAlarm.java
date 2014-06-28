package com.returnjump.spoilfoil;

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
        String [] column = {DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED};
        String[] operator =  {DatabaseContract.AND};
        String [] wherevalue = {"<", "="};
        String[] conjunction = {"2014-01-01", DatabaseContract.BOOL_FALSE_STR};



        List<FridgeItem> foodexpiring = fridge.getAll(column, operator, wherevalue, conjunction, true);
        NotificationSender ns = new NotificationSender(context, foodexpiring);
        //Intent scheduledIntent = new Intent(context, NotificationAlarm.class);
        // PendingIntent pIntent = PendingIntent.getActivity(context, 0, scheduledIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ns.sendNotifications();
    }
}
