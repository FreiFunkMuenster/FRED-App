package de.florian_adelt.fred.service;

import android.app.IntentService;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.LogEntry;
import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.helper.Status;
import de.florian_adelt.fred.wifi.ScanResult;

public class SynchronizationService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Logger.log(getApplicationContext(), "sync", "sync started");
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        List<ScanResult> scanResults = db.getUnsynchedScans();
        List<LogEntry> logEntries = db.getUnsyncedLogs();
        boolean notify = jobParameters.getExtras().getBoolean("notify", false);

        Log.i("fred sync", "notify value: " + notify);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SynchronizationTask task = new ScanSyncTask(getApplicationContext(), preferences, new DatabaseHelper(getApplicationContext()), notify);

        Gson gson = new Gson();

        if(scanResults.size() > 0) {


            StringBuilder payload = new StringBuilder();
            payload.append("{\"scans\":");
            payload.append(gson.toJson(scanResults));
            payload.append("}");


            //Log.e("Fred sync", "payload: " + payload.toString());

            task.execute(
                    preferences.getString("backend_url", "https://fredbackend.ffmsl.de") + "/api/v1/",
                    "scans/create",
                    "test",
                    payload.toString()
            );
        }
        else if (notify){
            Toast.makeText(getApplicationContext(), R.string.no_new_scans_to_upload, Toast.LENGTH_SHORT).show();
        }
        else {
            Log.w("fred sync", "No scans to upload");
        }

        if(logEntries.size() > 0) {


            StringBuilder payload = new StringBuilder();
            payload.append("{\"logs\":");
            payload.append(gson.toJson(logEntries));
            payload.append("}");

            task = new LogSyncTask(getApplicationContext(), preferences, new DatabaseHelper(getApplicationContext()), notify);

            Log.e("Fred sync", "payload: " + payload.toString());

            task.execute(
                    preferences.getString("backend_url", "https://fredbackend.ffmsl.de") + "/api/v1/",
                    "logs/create",
                    "test",
                    payload.toString()
            );
        }
        else {
            Log.w("fred sync", "No logs to upload");
        }





        ServiceStarter.startSynchronizationService(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("fred sync", "sync ended");
        return true;
    }
}
