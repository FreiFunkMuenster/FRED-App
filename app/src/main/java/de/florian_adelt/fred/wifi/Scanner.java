package de.florian_adelt.fred.wifi;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.helper.Notification;
import de.florian_adelt.fred.helper.Status;
import de.florian_adelt.fred.service.LocationService;
import de.florian_adelt.fred.service.SynchronizationTask;

public class Scanner {


    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver;
    private Context context;
    private List<Wifi> scanResults;
    private boolean isScanning;
    private Location location;
    private LocationService service;

    public Scanner(WifiManager wifiManager, LocationService service) {
        this.wifiManager = wifiManager;
        this.context = service.getApplicationContext();
        this.service = service;
        this.scanResults = new ArrayList<>();
        this.isScanning = false;


        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //context.unregisterReceiver(this);
                handleScanResult(context, intent);
            }
        };

        context.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void scan(Location location) {


        if (isScanning) {
            Logger.log(context, "fred scanner", "scanning already in progress");
            return;
        }
        if (location == null) {
            Logger.log(context, "fred scanner", "Tried to scan with null location, restarting");
            service.killAndRestart();
            Status.broadcastStatus(context, R.string.initializing_scan);
            return;
        }


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        float lastLatitude = preferences.getFloat("last_latitude", 0);
        float lastLongitude = preferences.getFloat("last_longitude", 0);


        this.location = location;

        Logger.log(context, "fred scanner", "set location to: " + location.getLatitude() + ", " + location.getLongitude());


        float distance = distFrom(lastLatitude, lastLongitude, (float) location.getLatitude(), (float) location.getLongitude());


        Log.e("Fred Scanner", "Travelled Distance: " + distance);

        if (distance < preferences.getInt("scan_frequency_distance", 10)) {
            Logger.log(context, "Fred Scanner", "Travelled Distance was not enough: " + distance + "/" + preferences.getInt("scan_frequency_distance", 10));
            service.killAndRestart();
            Intent i = new Intent("de.florian_adelt.fred.snackbar");
            Bundle extras = new Bundle();
            extras.putString("de.florian_adelt.fred.snackbar.message", context.getString(R.string.travelled_distance_not_far_enough, (int) distance, preferences.getInt("scan_frequency_distance", 10)));
            i.putExtras(extras);
            context.sendBroadcast(i);

            Status.broadcastStatus(context, context.getString(R.string.travelled_distance_not_far_enough, (int) distance, preferences.getInt("scan_frequency_distance", 10)));
            return;
        }

        if (location.getAccuracy() > preferences.getInt("scan_frequency_accuracy", 15)) {
            Logger.log(context, "Fred Scanner", "Accuracy was insufficient: " + location.getAccuracy());
            service.killAndRestart();
            Intent i = new Intent("de.florian_adelt.fred.snackbar");
            Bundle extras = new Bundle();
            extras.putString("de.florian_adelt.fred.snackbar.message", context.getString(R.string.insufficent_accuracy, (int) location.getAccuracy()));
            i.putExtras(extras);
            context.sendBroadcast(i);

            Status.broadcastStatus(context,  context.getString(R.string.insufficent_accuracy, (int) location.getAccuracy()));
            return;
        }


        if (!wifiManager.isWifiEnabled()) {
            //wifiManager.setWifiEnabled(true);
            Notification.enableWifiNotification(context);
            Status.broadcastStatus(context, R.string.app_is_inactive_no_wifi);
        }
        else {
            Notification.cancel(context, Notification.NO_WIFI_ID);
        }

        isScanning = true;


        if (wifiManager.startScan()) {
            service.removeUpdates();
            return ;
        }

        isScanning = false;

        Logger.log(context, "fred scanner", "scanning didn't start");
        Toast.makeText(context, "Wifi Scan nicht möglich", Toast.LENGTH_SHORT).show();
    }

    public void handleScanResult(Context context, Intent intent) {
        if (this.location == null) {
            Logger.log(context, "fred scanner", "Tried to handle scan results with null location");
            return;
        }
        Log.e("fred scanner", "scan results");
        List<ScanResult> results = wifiManager.getScanResults();
        Log.e("fred scanner", "found " + results.size() + " networks in total (raw value)");
        scanResults.clear();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit()
                .putFloat("last_latitude", (float) location.getLatitude())
                .putFloat("last_longitude", (float) location.getLongitude())
                .apply();

        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        Gson gson = new Gson();
        List<String> networksToScan = gson.fromJson(preferences.getString("target_ssids", "[]"), type);

        for (ScanResult scanResult : results) {
            if (networksToScan != null && networksToScan.contains(scanResult.SSID))
                scanResults.add(new Wifi(scanResult.level, scanResult.SSID, scanResult.BSSID, scanResult.capabilities, scanResult.centerFreq0, scanResult.centerFreq1, scanResult.channelWidth, scanResult.frequency, scanResult.isPasspointNetwork(), scanResult.is80211mcResponder()));
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context.getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {

            long time = System.currentTimeMillis();
            String scanResultJson = getScanResultJson();
            ContentValues values = new ContentValues();
            values.put("time", time);
            values.put("result", scanResultJson);

            values.put("latitude", location.getLatitude());
            values.put("longitude", location.getLongitude());
            values.put("altitude", location.getAltitude());
            values.put("accuracy", location.getAccuracy());


            long id = -1;

            if (!scanResults.isEmpty()) {
                id = db.insert("Scans", null, values);
            }

            de.florian_adelt.fred.wifi.ScanResult scanResult = new de.florian_adelt.fred.wifi.ScanResult(
                    id,
                    time,
                    location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getAccuracy(),
                    "success",
                    scanResults);

            //String json = gson.toJson(scanResult);
            //Intent i = new Intent("de.florian_adelt.fred.update");
            //Bundle extras = new Bundle();
            //extras.putString("de.florian_adelt.fred.update.scan", json);
            //i.putExtras(extras);
            //context.sendBroadcast(i);
            //Log.e("fred broadcast", "broadcasting");
            Status.broadcastScanResultStatus(context, scanResult);

        }
        catch (Exception e) {
            e.printStackTrace();
            Logger.e(context, "fred scanner", e);
        }
        finally {
            db.close();
            isScanning = false;
        }

        service.killAndRestart();
    }

    public String getScanResultJson() {

        Gson gson = new Gson();
        return gson.toJson(scanResults);
    }

    public void dispose() {
        context.unregisterReceiver(wifiReceiver);
    }


    /* from: https://stackoverflow.com/questions/837872/calculate-distance-in-meters-when-you-know-longitude-and-latitude-in-java */
    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

}
