package de.florian_adelt.fred.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Objects;

import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.helper.Notification;

public class ServiceStarter {


    public static void startLocationService(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long wait = preferences.getInt("scan_frequency_time", 20) * 1000;
        startLocationService(context, wait);
    }

    public static void startLocationService(Context context, long wait) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.getBoolean("service_enabled", true)) {  // aggressive
            if (preferences.getLong("service_stop_at_time", Long.MAX_VALUE) > System.currentTimeMillis()) {

                Log.i("fred service", "start service job");
                //context.startService(new Intent(context, LocationService.class));  // seems to crash
                ComponentName serviceComponent = new ComponentName(context, LocationJob.class);
                JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);

                //long wait = Integer.parseInt(preferences.getString("scan_frequency_time", "20")) * 1000;

                Logger.log(context, "fred location service", "starting location service with delay of: " + wait);

                builder.setMinimumLatency(wait); // wait at least
                builder.setOverrideDeadline(wait); // maximum delay
                JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                Objects.requireNonNull(jobScheduler).schedule(builder.build());
            }
            else {
                Logger.log(context, "service", "Service was shut down by timer (overdue by " + (preferences.getLong("service_stop_at_time", Long.MAX_VALUE) - System.currentTimeMillis()) + ")");
                preferences.edit().putBoolean("service_enabled", false).apply();

                Intent broadcastIntent = new Intent("de.florian_adelt.fred.stop");
                context.sendBroadcast(broadcastIntent);
                Notification.cancelAll(context);
                Notification.autoDisabledNotification(context);
            }
        }

    }


    public static void startSynchronizationService(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long wait = Integer.parseInt(preferences.getString("sync_frequency", "180")) * 60 * 1000;
        boolean wifiOnly = preferences.getBoolean("upload_only_wifi", true);
        startSynchronizationService(context, wait, wifiOnly, false);
    }

    public static void startSynchronizationService(Context context, long wait, boolean wifiOnly, boolean notify) {
        Log.i("fred service", "check upload service start");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.getBoolean("auto_upload", true)) {  // aggressive
            Log.i("fred service", "start upload service");
            //context.startService(new Intent(context, LocationService.class));  // seems to crash
            ComponentName serviceComponent = new ComponentName(context, SynchronizationService.class);
            JobInfo.Builder builder = new JobInfo.Builder(1, serviceComponent);
            Logger.log(context, "Fred Sync", "Service start after min " + wait);
            //wait = 10000;  // for testing
            builder.setMinimumLatency(wait); // wait at least
            builder.setOverrideDeadline(wait); // maximum delay
            if (wifiOnly)
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            PersistableBundle bundle = new PersistableBundle();
            bundle.putBoolean("notify", notify);
            builder.setExtras(bundle);

            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            Objects.requireNonNull(jobScheduler).schedule(builder.build());
        }
        else {
            Logger.log(context, "syncService", "auto_upload is set to false, won't start sync service", Logger.LEVEL_INFO);
        }
    }


    public static void startImportService(Context context) {
        Log.i("fred service", "check upload service start");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Log.i("fred service", "start import service");
        ComponentName serviceComponent = new ComponentName(context, ImportService.class);
        JobInfo.Builder builder = new JobInfo.Builder(2, serviceComponent);
        builder.setMinimumLatency(0); // wait at least
        builder.setOverrideDeadline(1000); // maximum delay

        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        Objects.requireNonNull(jobScheduler).schedule(builder.build());
    }

    public static void setTimeToStop(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        setTimeToStop(context, Long.parseLong(preferences.getString("scan_time_to_stop", "0")));
    }
    public static void setTimeToStop(Context context, long value) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        long stopAt;
        if (value == 0) {
            stopAt = Long.MAX_VALUE;
        }
        else {
            stopAt = System.currentTimeMillis() + value * 1000;
        }
        Log.i("fred kill timer", "current: " + System.currentTimeMillis() + ", target: " + stopAt);
        preferences.edit().putLong("service_stop_at_time", stopAt).apply();
    }
}
