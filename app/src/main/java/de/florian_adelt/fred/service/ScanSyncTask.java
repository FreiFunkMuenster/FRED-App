package de.florian_adelt.fred.service;

import android.content.Context;
import android.content.SharedPreferences;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;

public class ScanSyncTask extends SynchronizationTask {
    public ScanSyncTask(Context context, SharedPreferences preferences, DatabaseHelper dbHelper, boolean notify) {
        super(context, preferences, dbHelper, notify);

        this.successMessage = R.string.scans_synced_successfully;
        this.errorMessage = R.string.error_001;
    }

    @Override
    protected void onSuccess() {
        dbHelper.setSynced();
    }
}
