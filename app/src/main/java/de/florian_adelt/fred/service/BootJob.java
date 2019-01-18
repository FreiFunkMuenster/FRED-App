package de.florian_adelt.fred.service;


import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.util.Log;

public class BootJob extends JobService {
    private static final String TAG = "SyncService";

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("fred boot", "try to start service");
        Intent service = new Intent(getApplicationContext(), LocationService.class);
        getApplicationContext().startService(service);

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}