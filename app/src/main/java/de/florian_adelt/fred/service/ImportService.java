package de.florian_adelt.fred.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.List;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.LogEntry;
import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.wifi.ScanResult;

public class ImportService extends JobService {

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        Logger.log(getApplicationContext(), "import", "import started");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        GetConfigTask task = new GetConfigTask(getApplicationContext());

        task.execute(
            preferences.getString("import_url", "https://fredbackend.ffmsl.de")
        );

        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        Log.i("import", "service ended");
        return false;
    }
}
