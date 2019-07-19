package de.florian_adelt.fred.service;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class LocationJob extends JobService {
    private static final String TAG = "Fred locationjob";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "try to start location service");
        Intent service = new Intent(getApplicationContext(), LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // this is a hack for Android O+ and will be replaced later on with using this JobService instead of the current LocationService
            getApplicationContext().startForegroundService(service);
        } else {
            getApplicationContext().startService(service);
        }


        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}