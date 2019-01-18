package de.florian_adelt.fred.service;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

public class SynchronisationService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.e("fred sync", "sync started");


        ServiceStarter.startSynchronizationService(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e("fred sync", "sync ended");
        return true;
    }
}
