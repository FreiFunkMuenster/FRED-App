package de.florian_adelt.fred.service;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Objects;

import de.florian_adelt.fred.helper.Logger;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Logger.log(context, "boot", "fred boot event received");
        //Log.e("fred boot", intent.getAction());

        ServiceStarter.startLocationService(context);
        ServiceStarter.startSynchronizationService(context);
    }


}


