package com.returnjump.spoilfoil;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;

import java.util.List;

/**
 * Created by arturomenacruz on 2014-06-17.
 */
public class NotificationSender{
        public Context context;
        public List<FridgeItem> items_expiring;

        public NotificationSender(Context context, List<FridgeItem> items_expiring){
            this.items_expiring = items_expiring;
            this.context = context;
        };

        public void sendNotifications(){
            /**
             * Sends notification in case you weren't able to tell.
             * Example:
             * //Create instance of the class
             * NotificationSender ns = new NotificationSender(context, items);
             * //Call the method
             * ns.sendNotifications;
             */

        //Setting up the intent for the notification
        Intent intent = new Intent(this.context, KelseyActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Creates the Notification mBuilder
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Expiring Items")
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVibrate(new long[]{ 0, 500, 250, 500 })
                        .setLights(Color.GREEN, 500, 1000);
        String current_text = "The following items are about to expire: ";
        int number_items_expiring = items_expiring.size();
        for (int i=0; i < items_expiring.size(); i ++){
            current_text += items_expiring.get(i).getName() + " ";
           number_items_expiring ++;
        }
        mBuilder.setContentText(current_text)
                .setNumber(number_items_expiring)
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        //sets the type of alert
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) this.context.getSystemService(this.context.NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    };
}
