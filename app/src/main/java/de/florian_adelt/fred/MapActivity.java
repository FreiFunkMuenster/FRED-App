package de.florian_adelt.fred;

import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.FredHelper;
import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.helper.NetworkListActivity;
import de.florian_adelt.fred.helper.Notification;
import de.florian_adelt.fred.service.LocationService;
import de.florian_adelt.fred.service.ServiceStarter;
import de.florian_adelt.fred.service.SynchronizationService;
import de.florian_adelt.fred.settings.SettingsActivity;
import de.florian_adelt.fred.wifi.ScanResult;
import de.florian_adelt.fred.wifi.Scanner;
import de.florian_adelt.fred.wifi.Wifi;

public class MapActivity extends AppCompatActivity {


    protected MapView map = null;
    protected LocationManager locationManager;
    protected Location location;
    protected Scanner wifiScanner;

    protected TextView currentWifis;

    protected BroadcastReceiver updateReceiver;
    protected BroadcastReceiver snackbarReceiver;
    protected BroadcastReceiver stopReceiver;

    protected MyLocationNewOverlay locationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeApp();

    }

    private void initializeApp() {


        if(!requestPermissions()) {
            setContentView(R.layout.inactive_layout);
            Button button = findViewById(R.id.start_app_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initializeApp();
                }
            });
            return;
        }


        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_map);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);  // todo: can be interesting later on
        map.setClickable(true);


        final IMapController mapController = map.getController();
        mapController.setZoom(20);
        GeoPoint startPoint = new GeoPoint(preferences.getFloat("last_latitude", 50.5f), preferences.getFloat("last_longitude", 10.05f));
        mapController.setCenter(startPoint);




        /*LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
        {
            this.location = location;
            Log.w("fred lat", location.getLatitude() + " fred");
            Log.w("lng", location.getLongitude() + "");
        }
        else
        {
            Log.e("Location fred", "Location null");
        }*/

        String targetSSIDs = preferences.getString("target_ssids", null);
        if (targetSSIDs == null) {
            preferences.edit().putString("target_ssids", getResources().getString(R.string.pref_default_ssids)).apply();
        }


        currentWifis = findViewById(R.id.current_wifis);

        boolean isEnabled = preferences.getBoolean("service_enabled", true);
        final ToggleButton toggleButton = findViewById(R.id.toggleButton);

        toggleButton.setChecked(isEnabled);
        serviceToggled();

        if (isEnabled) {
            Log.e("fred start", "service enabled");
            enableLocationOverlay();
        }
        else {
            Log.e("fred start", "service disabled");
            disableLocationOverlay();
        }

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceToggled();
            }
        });

        findViewById(R.id.follow_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationOverlay.enableFollowLocation();
            }
        });

        currentWifis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), NetworkListActivity.class));
            }
        });

        TextView osmButton = findViewById(R.id.osm_copyright);
        osmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(getString(R.string.osm_copyright_href));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        //currentWifis.setText(Html.fromHtml(getResources().getString(R.string.last_scan_result) + " " + preferences.getString("cached_last_scan_result", getResources().getString(R.string.waiting_for_scan))));


        if (isMyServiceRunning(SynchronizationService.class)) {
            stopService(new Intent(this, SynchronizationService.class));
        }
        ServiceStarter.startSynchronizationService(getApplicationContext());
        ServiceStarter.startLocationService(getApplicationContext(), 1000);  // Start first location Service immediately


        updateStatus(new Intent(), new DatabaseHelper(getApplicationContext()).getLastScanResult());

    }

    public void serviceToggled() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("service_enabled", toggleButton.isChecked());
        editor.apply();

        stopService(new Intent(this, LocationService.class));


        if (preferences.getBoolean("service_enabled", true)) {
            enableLocationOverlay();
            Log.e("fred service", "start service");
            //startService(new Intent(this, LocationService.class));
            ServiceStarter.setTimeToStop(getApplicationContext());
            ServiceStarter.startLocationService(this.getApplicationContext());
        }
        else {

            //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            //notificationManager.cancel(Notification.NO_GPS_ID);

            Notification.cancel((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE), Notification.NO_GPS_ID);

            disableLocationOverlay();
        }

    }


    public void registerUpdateReceiver() {

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Gson gson = new Gson();
                updateStatus(intent, gson.fromJson(intent.getStringExtra("de.florian_adelt.fred.update.scan"), ScanResult.class));

            }
        };
        registerReceiver(updateReceiver, new IntentFilter("de.florian_adelt.fred.update"));


        snackbarReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.e("fred snackbar", "showing snackbar");

                //int messageId = intent.getIntExtra("de.florian_adelt.fred.snackbar.message_id", 0);
                //if (messageId == 0) {
                //    Log.e("fred snackbar", "no message id given");
                //    return;
                //}
                Snackbar.make(findViewById(R.id.main_map_layout), intent.getStringExtra("de.florian_adelt.fred.snackbar.message"), Snackbar.LENGTH_LONG).show();
            }
        };
        registerReceiver(snackbarReceiver, new IntentFilter("de.florian_adelt.fred.snackbar"));

        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.e("fred stop receiver", "received");

                int notificationId = intent.getIntExtra("de.florian_adelt.fred.stop.notification_id", -1);
                int[] toCancel = { Notification.NO_GPS_ID, Notification.ACTIVE_ID, Notification.NO_WIFI_ID };
                for (int id : toCancel) {
                    Notification.cancel((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE), id);
                }

                ToggleButton toggleButton = findViewById(R.id.toggleButton);
                toggleButton.setChecked(false);
                serviceToggled();



            }
        };
        registerReceiver(stopReceiver, new IntentFilter("de.florian_adelt.fred.stop"));

    }

    public void updateStatus(Intent intent, ScanResult scanResult) {

        Log.e("fred receiver", "update status");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isGpsUpdate = intent.hasExtra("de.florian_adelt.fred.update.gps");

        if (isGpsUpdate) {
            if (intent.getBooleanExtra("de.florian_adelt.fred.update.gps", false)) {

                if (currentWifis.getText().equals(getResources().getString(R.string.gps_disabled))) {
                    // GPS was switched to enabled since last scan
                    currentWifis.setText(getResources().getString(R.string.gps_enabled));
                }
                else {
                    currentWifis.setText(Html.fromHtml(preferences.getString("cached_last_scan_result", getResources().getString(R.string.waiting_for_scan))));
                }
            }
            else {
                currentWifis.setText(getResources().getString(R.string.gps_disabled));
            }

            return;
        }

        if (scanResult == null) {
            Logger.log(getApplicationContext(), "fred receiver", "scan result was null");
            currentWifis.setText(getResources().getString(R.string.waiting_for_scan));
            return;
        }

        List<Wifi> wifis = scanResult.getSortedWifiList();

        if (wifis.size() == 0) {
            currentWifis.setText(getResources().getString(R.string.last_scan_resulted_empty));
            return;
        }

        int max = 3;
        int last = wifis.size() > max ? max : wifis.size();

        List<Wifi> targetWifis = new ArrayList<>();
        List<Wifi> otherWifis = new ArrayList<>();

        FredHelper helper = new FredHelper(getApplicationContext());

        StringBuilder builder = new StringBuilder();

        for (int i=0; i<last; i++) {
            boolean isShownSsid = false;
            for (Wifi w : targetWifis) {
                if (wifis.get(i).getSsid().equals(w.getSsid())) {
                    isShownSsid = true;
                    break;
                }
            }
            if (isShownSsid) {
                continue;
            }
            if (helper.isTargetSsid(wifis.get(i).getSsid()))
                targetWifis.add(wifis.get(i));
            else
                otherWifis.add(wifis.get(i));
        }

        for (int i=0; i<targetWifis.size() && i<max; i++) {
            builder.append(wifiText(targetWifis.get(i), helper));
            if (i != targetWifis.size() - 1)
                builder.append(", ");
        }
        max = max - targetWifis.size();
        for (int i=0; i<otherWifis.size() && i<max; i++) {
            builder.append(wifiText(otherWifis.get(i), helper));
            if (i != max - 1)
                builder.append(", ");
        }

        if (wifis.size() > 3) {
            builder.append(" ");
            builder.append(getResources().getString(R.string.and_x_other, wifis.size() - 3));
        }
        String result = builder.toString();
        currentWifis.setText(Html.fromHtml(result));
        preferences.edit()
                .putString("cached_last_scan_result", result)
                .apply();
    }

    protected String wifiText(Wifi wifi, FredHelper helper) {
        StringBuilder builder = new StringBuilder();
        String ssid = wifi.getSsid();
        if ("".equals(ssid))
            ssid = getResources().getString(R.string.ssid_unknown);
        if (helper.isTargetSsid(ssid)) {
            builder.append("<strong>");
            builder.append(ssid);
            builder.append("</strong>");
        }
        else {
            builder.append(ssid);
        }
        builder.append(" (");
        builder.append(wifi.getLevel());
        builder.append("db)");
        return builder.toString();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    public void startSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);

        startActivity(intent);
    }
    public void startNetworkList() {
        Intent intent = new Intent(this, NetworkListActivity.class);

        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.navigation_settings:
                startSettings();
                return true;
            case R.id.network_list:
                startNetworkList();
                return true;
            case R.id.navigation_delete_networks:
                DatabaseHelper helper = new DatabaseHelper(this);
                SQLiteDatabase db = helper.getWritableDatabase();
                db.execSQL("delete from Scans where 1");
                db.close();

                Toast.makeText(MapActivity.this, R.string.networks_deleted,
                        Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if (map != null)
            map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

        if (hasPermissions())
            registerUpdateReceiver();
    }

    @Override
    public void onPause(){
        super.onPause();


        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        if (map != null)
            map.onPause();  //needed for compass, my location overlays, v6.0.0 and up

        if (hasPermissions()) {
            unregisterReceiver(updateReceiver);
            unregisterReceiver(snackbarReceiver);
            unregisterReceiver(stopReceiver);
        }
    }

    private void enableLocationOverlay() {
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
            locationOverlay.enableFollowLocation();
            return;
        }

        locationOverlay = new MyLocationNewOverlay(map);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        locationOverlay.setDrawAccuracyEnabled(true);
        locationOverlay.setOptionsMenuEnabled(true);
        //locationOverlay.setPersonIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_location_icon));

        map.getOverlays().add(locationOverlay);
    }
    private void disableLocationOverlay() {
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }

    // from https://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean requestPermissions() {

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        return hasPermissions();
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
