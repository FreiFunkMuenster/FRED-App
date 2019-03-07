package de.florian_adelt.fred.service;


import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import de.florian_adelt.fred.MapActivity;
import de.florian_adelt.fred.R;
import de.florian_adelt.fred.helper.Notification;
import de.florian_adelt.fred.wifi.Scanner;

public class LocationService extends Service {


    public static final String TAG = "fred location service";

    private Location location;
    private Scanner wifiScanner;

    private int serviceId;

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
        this.serviceId = startId;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.e(TAG, "GPS is disabled");


            Intent notificationIntent = new Intent(getApplicationContext(), MapActivity.class);
            PendingIntent activeNotification = PendingIntent.getActivity(getApplicationContext(), Notification.NO_GPS_ID, notificationIntent, PendingIntent.FLAG_NO_CREATE);

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.createServiceNotification(
                    getApplicationContext(),
                    notificationManager,
                    Notification.NO_GPS_ID,
                    getString(R.string.app_is_inactive),
                    getString(R.string.please_activate_gps),
                    getString(R.string.please_activate_gps_long),
                    R.drawable.ic_no_gps
            );

            if (notificationManager != null) {
                notificationManager.cancel(Notification.ACTIVE_ID);
            }

            if (activeNotification == null) {

                /*Intent myIntent = new Intent(getApplicationContext(), MapActivity.class);
                myIntent.setAction("de.florian_adelt.fred.stop");
                myIntent.putExtra("de.florian_adelt.fred.stop.notification_id", Notification.NO_GPS_ID);
                PendingIntent stopServiceIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, myIntent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1")
                        .setSmallIcon(R.drawable.ic_no_gps)
                        .setContentTitle(getString(R.string.app_is_inactive))
                        .setContentText(getString(R.string.please_activate_gps))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(getString(R.string.please_activate_gps_long)))
                        .setPriority(NotificationCompat.PRIORITY_MIN)
                        .setOngoing(true)
                        .addAction(R.drawable.ic_no_gps, getString(R.string.stop_fred_service), stopServiceIntent);

                if (notificationManager != null)
                    notificationManager.notify(Notification.NO_GPS_ID, builder.build());
                */

            }


            Intent i = new Intent("de.florian_adelt.fred.update");
            Bundle extras = new Bundle();
            extras.putBoolean("de.florian_adelt.fred.update.gps", false);
            i.putExtras(extras);
            getApplicationContext().sendBroadcast(i);

            killAndRestart();
        }
        else {

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager == null || !wifiManager.isWifiEnabled()) {
                Notification.enableWifiNotification(getApplicationContext());
            } else {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                Notification.createServiceNotification(
                        getApplicationContext(),
                        notificationManager,
                        Notification.ACTIVE_ID,
                        getString(R.string.app_is_active),
                        getString(R.string.touch_to_stop),
                        null,
                        R.drawable.ic_gps
                );

                Intent i = new Intent("de.florian_adelt.fred.update");
                Bundle extras = new Bundle();
                extras.putBoolean("de.florian_adelt.fred.update.gps", true);
                i.putExtras(extras);
                getApplicationContext().sendBroadcast(i);
            }


        }
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        wifiScanner = new Scanner((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE), this);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    // we should not use android to determine if a scan is needed by frequency and distance because it will keep gps on until a scan is made.
                    // instead we use a default min time value and set the min distance to 0. Like this, we force an almost immediate result.
                    // also, we will then check by ourselves what the travelled distances since the last scan was and scan accordingly
                    2000,  // todo: determine best value
                    0,
                    locationListener);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }

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
                Log.e(TAG, "fail to remove location listeners. This should be ignorable as everything else should keep working afterwards", ex);
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

    public void killAndRestart() {
        stopSelf(serviceId);
        ServiceStarter.startLocationService(getApplicationContext());
    }


    public void removeUpdates() {
        locationManager.removeUpdates(locationListener);
    }

}
