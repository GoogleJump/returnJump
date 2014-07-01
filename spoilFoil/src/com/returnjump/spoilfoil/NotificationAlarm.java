package com.returnjump.spoilfoil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
        final Calendar rightnow = Calendar.getInstance();
        final DateFormat date_string = new SimpleDateFormat("yyyy-MM-dd");
        String [] column = {DatabaseContract.FridgeTable.COLUMN_NAME_EXPIRY_DATE,DatabaseContract.FridgeTable.COLUMN_NAME_DISMISSED};
        String[] operator =  {DatabaseContract.AND};
        String [] wherevalue = {"<", "="};
        String[] conjunction = {date_string.format(rightnow.getTime()), DatabaseContract.BOOL_FALSE_STR};

        List<FridgeItem> foodexpiring = fridge.getAll(column, operator, wherevalue, conjunction, true);
        NotificationSender ns = new NotificationSender(context, foodexpiring);
        EmailNotifier emailsender = new EmailNotifier(context, foodexpiring);
        emailsender.cloudEmailSender();
        ns.sendNotifications();

    }
}
