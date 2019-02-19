package de.florian_adelt.fred.service;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.util.List;

import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.wifi.ScanResult;

public class SynchronizationService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Log.e("fred sync", "sync started");
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        List<ScanResult> scanResults = db.getUnsynchedScans();

        if(scanResults.size() > 0) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SynchronizationTask task = new SynchronizationTask(preferences, new DatabaseHelper(getApplicationContext()));

            Gson gson = new Gson();

            StringBuilder payload = new StringBuilder();
            payload.append("{\"scans\":");
            payload.append(gson.toJson(scanResults));
            payload.append("}");


            task.execute(
                    "https://fredbackend.ffmsl.de/api/v1/",
                    "scans/create",
                    "test",
                    payload.toString()
            );
        }


        ServiceStarter.startSynchronizationService(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.e("fred sync", "sync ended");
        return true;
    }
}
