package com.returnjump.phrige;

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
public class NotificationPush {
        public Context context;
        public List<FridgeItem> expiredFridgeItems;

        public NotificationPush(Context context, List<FridgeItem> expiringFridgeItems){
            this.expiredFridgeItems = expiringFridgeItems;
            this.context = context;
        };

        private String generateMessage() {
            if (expiredFridgeItems.size() == 1) {
                return String.format("Your %s has expired", expiredFridgeItems.get(0).getName());
            } else if (expiredFridgeItems.size() == 2) {
                return String.format("Your %s and %s have expired", expiredFridgeItems.get(0).getName(), expiredFridgeItems.get(1).getName());
            } else {
                String plural = "";
                if (expiredFridgeItems.size() > 3) {
                    plural = "s";
                }

                int random = (int) (Math.random() * expiredFridgeItems.size());
                return String.format("Your %s and %d other%s have expired", expiredFridgeItems.get(random).getName(), expiredFridgeItems.size(), plural);
            }
        }

        public void sendNotifications(){
            /**
             * Sends notification in case you weren't able to tell.
             * Example:
             * //Create instance of the class
             * NotificationPush ns = new NotificationPush(context, items);
             * //Call the method
             * ns.sendNotifications;
             */

            //Setting up the intent for the notification
            Intent intent = new Intent(this.context, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent, 0);

            // Creates the Notification mBuilder
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Phrige")
                            .setContentText(generateMessage())
                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                            .setVibrate(new long[]{ 0, 250, 100, 250 })
                            .setLights(Color.GREEN, 500, 1000)
                            .setNumber(expiredFridgeItems.size())
                            .setContentIntent(pIntent)
                            .setAutoCancel(true);

            int mNotificationId = 001;
            NotificationManager mNotifyMgr =
                    (NotificationManager) context.getSystemService(this.context.NOTIFICATION_SERVICE);
            mNotifyMgr.notify(mNotificationId, mBuilder.build());
        }
}
