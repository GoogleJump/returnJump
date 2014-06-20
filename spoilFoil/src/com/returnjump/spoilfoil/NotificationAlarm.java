package com.returnjump.spoilfoil;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by arturomenacruz on 2014-06-17.
 */
public class NotificationAlarm extends BroadcastReceiver {
    //Method gets called when the alarm goes off
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationSender ns = new NotificationSender(context);
        Intent scheduledIntent = new Intent(context, NotificationAlarm.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, scheduledIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        ns.sendNotifications();
    }
}
