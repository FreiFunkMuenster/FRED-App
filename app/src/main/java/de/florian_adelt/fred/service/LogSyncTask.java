package de.florian_adelt.fred.service;

import android.content.Context;
import android.content.SharedPreferences;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;

public class LogSyncTask extends SynchronizationTask {
    public LogSyncTask(Context context, SharedPreferences preferences, DatabaseHelper dbHelper, boolean notify) {
        super(context, preferences, dbHelper, notify);

        this.successMessage = R.string.empty;
        this.errorMessage = R.string.empty;
    }

    @Override
    protected void onSuccess() {
        dbHelper.setLogsSynced();
    }
}
