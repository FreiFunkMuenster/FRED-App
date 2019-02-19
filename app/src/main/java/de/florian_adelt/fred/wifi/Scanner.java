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
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.service.SynchronizationTask;

public class Scanner {


    private WifiManager wifiManager;
    private BroadcastReceiver wifiReceiver;
    private Context context;
    private List<Wifi> scanResults;
    private boolean isScanning;
    private LocationManager locationManager;
    private Location location;

    public Scanner(WifiManager wifiManager, Context context, LocationManager locationManager) {
        this.wifiManager = wifiManager;
        this.context = context;
        this.locationManager = locationManager;
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
            Log.e("fred scanner", "scanning already in progress");
            return;
        }

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        this.location = location;
        isScanning = true;


        if (wifiManager.startScan()) {
            return ;
        }

        isScanning = false;

        Log.e("fred scanner", "scanning didn't start");
        Toast.makeText(context, "Wifi Scan nicht möglich", Toast.LENGTH_SHORT).show();
    }

    public void handleScanResult(Context context, Intent intent) {
        Log.e("fred handle", "scan results");
        List<ScanResult> results = wifiManager.getScanResults();
        scanResults.clear();


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
            if (location != null) {
                values.put("latitude", location.getLatitude());
                values.put("longitude", location.getLongitude());
                values.put("altitude", location.getAltitude());
                values.put("accuracy", location.getAccuracy());


                long id = db.insert("Scans", null, values);

                de.florian_adelt.fred.wifi.ScanResult scanResult = new de.florian_adelt.fred.wifi.ScanResult(
                        id,
                        time,
                        location.getLatitude(),
                        location.getLongitude(),
                        location.getAltitude(),
                        location.getAccuracy(),
                        "success",
                        scanResults);

                String json = gson.toJson(scanResult);
                Intent i = new Intent("de.florian_adelt.fred.update");
                Bundle extras = new Bundle();
                extras.putString("de.florian_adelt.fred.update.scan", json);
                i.putExtras(extras);
                context.sendBroadcast(i);
                Log.e("fred broadcast", "broadcasting");
            }
            else {
                Log.e("fred scanner", "location was null, skipped update broadcast");
            }


        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            db.close();
            isScanning = false;
        }


    }


    public String getScanResultJson() {

        Gson gson = new Gson();
        return gson.toJson(scanResults);



        /*StringBuilder builder = new StringBuilder();
        builder.append('[');

        for (int i=0; i < scanResults.size(); i++) {
            builder.append('{');
            builder.append("ssid: ");
            builder.append('"');
            builder.append(scanResults.get(i).getSsid());
            builder.append('"');
            builder.append(',');
            builder.append("level: ");
            builder.append(scanResults.get(i).getLevel());
            builder.append('}');
            if (i != scanResults.size() - 1)
                builder.append(',');
        }

        builder.append(']');

        return builder.toString();*/
    }

    public void dispose() {
        context.unregisterReceiver(wifiReceiver);
    }

}
