package de.florian_adelt.fred.service;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class BootJob extends JobService {  // todo: rename class
    private static final String TAG = "Fred LocationService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "try to start service");
        //Intent service = new Intent(getApplicationContext(), LocationService.class);
        //getApplicationContext().startService(service);

        ServiceStarter.startLocationService(getApplicationContext());
        ServiceStarter.startSynchronizationService(getApplicationContext());

        //ServiceStarter.startLocationService(getApplicationContext());

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}