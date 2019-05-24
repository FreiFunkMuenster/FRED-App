package de.florian_adelt.fred.helper;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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

    private static String CHANNEL_ID = "fred_background";
    private static String CHANNEL_NAME = "Statusupdates";


    public static boolean cancel(NotificationManager notificationManager, int id) {

        if (notificationManager != null)
            if (isActive(notificationManager, id)) {
                notificationManager.cancel(CHANNEL_ID, id);
                notificationManager.cancel(id);
                return true;
            }
        return false;
    }
    public static void cancel(Context context, int id) {
        Logger.log(context, "notification", "trying removing notification: " + id);
        if (!cancel((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE), id)) {
            Logger.log(context, "notification", "notification was not active or notification manager null: " + id);
        }

    }

    public static void createServiceNotification(Context context, NotificationManager notificationManager, int id, String title, String smallText, @Nullable String longText, int icon) {

        Logger.log(context, "notification", "notification change to: " + title);

        /* Only show one of the two notifications, and don't renew it if it already exists */
        if (notificationManager != null) {


            boolean isNoGpsActive = isActive(notificationManager, NO_GPS_ID);
            boolean isGpsActive = isActive(notificationManager, ACTIVE_ID);

            if (id == NO_GPS_ID) {
                cancel(notificationManager, ACTIVE_ID);
                if (isNoGpsActive) {
                    Logger.log(context, "notification", "already active");
                    return;
                }
            }
            else if (id == ACTIVE_ID) {
                cancel(notificationManager, NO_GPS_ID);
                if (isGpsActive) {
                    Logger.log(context, "notification", "already active");
                    return;
                }
            }

            Intent broadcastIntent = new Intent("de.florian_adelt.fred.stop");
            broadcastIntent.putExtra("de.florian_adelt.fred.stop.notification_id", id);
            PendingIntent stopServiceIntent =
                    PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
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


            //notificationManager.notify(id, builder.build());
            show(notificationManager, builder, id);

        }

    }

    public static void enableWifiNotification(Context context) {
        Logger.log(context, "notification", "trying to add wifi notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            cancel(notificationManager, Notification.ACTIVE_ID);

            if (!isActive(notificationManager, NO_WIFI_ID)) {
                Logger.log(context, "notification", "adding wifi disabled notification");


                Intent broadcastIntent = new Intent("de.florian_adelt.fred.stop");
                broadcastIntent.putExtra("de.florian_adelt.fred.stop.notification_id", Notification.NO_WIFI_ID);
                PendingIntent stopServiceIntent =
                        PendingIntent.getBroadcast(context, 0, broadcastIntent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_no_wifi)
                        .setContentTitle(context.getString(R.string.app_is_inactive_no_wifi))
                        .setContentText(context.getString(R.string.please_enable_wifi))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setOngoing(true)
                        .setContentIntent(stopServiceIntent)
                        ;


                //notificationManager.notify(NO_WIFI_ID, builder.build());
                show(notificationManager, builder, NO_WIFI_ID);


            }


        }

    }
    public static void autoDisabledNotification(Context context) {
        Logger.log(context, "notification", "try to add auto-disabled notification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            if (!isActive(notificationManager, AUTO_DISABLED)) {
                Logger.log(context, "notification", "adding auto disabled notification");

                cancelAll(context);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_no_gps)
                        .setContentTitle(context.getString(R.string.app_auto_disabled))
                        .setContentText(context.getString(R.string.open_app_to_restart))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setOngoing(false);

                //notificationManager.notify(AUTO_DISABLED, builder.build());
                show(notificationManager, builder, AUTO_DISABLED);

            }


        }

    }


    private static void show(NotificationManager manager, NotificationCompat.Builder builder, int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // I would suggest that you use IMPORTANCE_DEFAULT instead of IMPORTANCE_HIGH
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_MIN);
            channel.enableVibration(false);
            channel.enableLights(false);
            //channel.canShowBadge();
            channel.setDescription("Statusupdates für FRED");
            // Did you mean to set the property to enable Show Badge?
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
            builder.setChannelId(CHANNEL_ID);

            manager.notify(CHANNEL_ID, id, builder.build());
        } else {
            manager.notify(id, builder.build());
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
