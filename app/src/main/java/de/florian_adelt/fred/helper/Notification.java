package de.florian_adelt.fred.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.florian_adelt.fred.MapActivity;
import de.florian_adelt.fred.R;

public class Notification {

    public static int NO_GPS_ID     = 1;
    public static int ACTIVE_ID     = 2;
    public static int NO_WIFI_ID    = 3;
    public static int AUTO_DISABLED = 4;


    public static void cancel(NotificationManager notificationManager, int id) {

        Log.e("Fred notification", "trying removing notification: " + id);
        if (notificationManager != null)
            if (isActive(notificationManager, id))
                notificationManager.cancel(id);
            else
                Log.e("Fred notification", "notification was not active");
    }
    public static void cancel(Context context, int id) {

        cancel((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), id);
    }

    public static void createServiceNotification(Context context, NotificationManager notificationManager, int id, String title, String smallText, @Nullable String longText, int icon) {

        Log.e("Fred notification", "notification change to: " + title);

        /* Only show one of the two notifications, and don't renew it if it already exists */
        if (notificationManager != null) {


            boolean isNoGpsActive = isActive(notificationManager, NO_GPS_ID);
            boolean isGpsActive = isActive(notificationManager, ACTIVE_ID);

            if (id == NO_GPS_ID) {
                notificationManager.cancel(Notification.ACTIVE_ID);
                if (isNoGpsActive) {
                    Log.e("Fred notification", "Already active");
                    return;
                }
            }
            else if (id == ACTIVE_ID) {
                notificationManager.cancel(Notification.NO_GPS_ID);
                if (isGpsActive) {
                    Log.e("Fred notification", "Already active");
                    return;
                }
            }

            Intent broadcastIntent = new Intent("de.florian_adelt.fred.stop");
            broadcastIntent.putExtra("de.florian_adelt.fred.stop.notification_id", id);
            PendingIntent stopServiceIntent =
                    PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                    .setSmallIcon(icon)
                    .setContentTitle(title)
                    .setContentText(smallText)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setOngoing(true)
                    //.addAction(R.drawable.ic_no_gps, context.getString(R.string.stop_fred_service), stopServiceIntent)  // No Button beneath, just use the onclick from next line
                    .setContentIntent(stopServiceIntent)
                    ;


            if (longText != null) {
                builder.setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(longText));
            } /* else {  // This is problematic because the media notification styling is not suitable for out purposes.
                builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0));
            }*/


            notificationManager.notify(id, builder.build());

        }

    }

    public static void enableWifiNotification(Context context) {
        Log.e("Fred notification", "tried to add wifi notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            cancel(notificationManager, Notification.ACTIVE_ID);

            if (!isActive(notificationManager, NO_WIFI_ID)) {
                Log.e("Fred notification", "adding wifi disabled notification");


                Intent broadcastIntent = new Intent("de.florian_adelt.fred.stop");
                broadcastIntent.putExtra("de.florian_adelt.fred.stop.notification_id", Notification.NO_WIFI_ID);
                PendingIntent stopServiceIntent =
                        PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                        .setSmallIcon(R.drawable.ic_no_wifi)
                        .setContentTitle(context.getString(R.string.app_is_inactive_no_wifi))
                        .setContentText(context.getString(R.string.please_enable_wifi))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setOngoing(true)
                        .setContentIntent(stopServiceIntent)
                        ;


                notificationManager.notify(NO_WIFI_ID, builder.build());


            }


        }

    }
    public static void autoDisabledNotification(Context context) {
        Log.e("Fred notification", "try to add auto-diabled notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            if (!isActive(notificationManager, AUTO_DISABLED)) {
                Log.e("Fred notification", "adding auto disabled notification");

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "1")
                        .setSmallIcon(R.drawable.ic_no_gps)
                        .setContentTitle(context.getString(R.string.app_auto_disabled))
                        .setContentText(context.getString(R.string.open_app_to_restart))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setOngoing(true);

                notificationManager.notify(AUTO_DISABLED, builder.build());

            }


        }

    }

    public static boolean isActive(NotificationManager notificationManager, int id) {

        for(StatusBarNotification n : notificationManager.getActiveNotifications()) {
            if (n.getId() == id) {
                return true;
            }
        }
        return false;
    }

    public static void cancelAll(Context context) {
        cancel(context, NO_WIFI_ID);
        cancel(context, NO_GPS_ID);
        cancel(context, ACTIVE_ID);
    }

}
