package de.florian_adelt.fred.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Objects;

public class ServiceStarter {


    public static void startLocationService(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.getBoolean("service_enabled", true)) {  // aggressive
            Log.e("fred service", "start service job");
            //context.startService(new Intent(context, LocationService.class));  // seems to crash
            ComponentName serviceComponent = new ComponentName(context, BootJob.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            builder.setMinimumLatency(15000); // wait at least
            builder.setOverrideDeadline(30000); // maximum delay
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            Objects.requireNonNull(jobScheduler).schedule(builder.build());
        }

    }

    public static void startSynchronizationService(Context context) {
        Log.e("fred service", "check upload service start");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (preferences.getBoolean("auto_upload", true)) {  // aggressive
            Log.e("fred service", "start upload service");
            //context.startService(new Intent(context, LocationService.class));  // seems to crash
            ComponentName serviceComponent = new ComponentName(context, SynchronisationService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
            long wait = preferences.getInt("sync_frequency", 180) * 60 * 1000;
            builder.setMinimumLatency(wait); // wait at least
            builder.setOverrideDeadline(wait * 2); // maximum delay
            if (preferences.getBoolean("upload_only_wifi", true))  // aggressive
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
            Objects.requireNonNull(jobScheduler).schedule(builder.build());
        }
    }

}
