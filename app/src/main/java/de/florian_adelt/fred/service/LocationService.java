package de.florian_adelt.fred.service;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import de.florian_adelt.fred.wifi.Scanner;

public class LocationService extends Service {


    public static final String TAG = "fred location service";

    private Location location;
    private Scanner wifiScanner;

    private LocationManager locationManager;
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location newLocation) {
            location = newLocation;
            Log.e("fred location change", "fred latitude: " + newLocation.getLatitude());
            Log.e("fred location change", "fred longitude: " + newLocation.getLongitude());
            Log.e("fred location change", "fred altitude: " + newLocation.getAltitude());
            Log.e("fred location change", "fred accuracy: " + newLocation.getAccuracy());
            wifiScanner.scan(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    Long.parseLong(preferences.getString("scan_frequency_time", "20")) * 1000,  // todo: evaluate best value
                    Float.parseFloat(preferences.getString("scan_frequency_distance", "10")),
                    locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        wifiScanner = new Scanner((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE), this, locationManager);
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listeners. This should be ignorable as everything else should keep working afterwards", ex);
            }
        }
        wifiScanner.dispose();
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }




}
