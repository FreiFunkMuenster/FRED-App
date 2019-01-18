package de.florian_adelt.fred;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.FredHelper;
import de.florian_adelt.fred.helper.NetworkListActivity;
import de.florian_adelt.fred.service.LocationService;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);
        map.setUseDataConnection(true);  // can be interesting later on
        map.setClickable(true);


        final IMapController mapController = map.getController();
        mapController.setZoom(20);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        final MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(map);
        locationOverlay.enableMyLocation(); // not on by default
        //locationOverlay.disableFollowLocation();
        locationOverlay.setDrawAccuracyEnabled(true);
        locationOverlay.enableFollowLocation();
        locationOverlay.setOptionsMenuEnabled(true);
        //locationOverlay.setPersonIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_location_icon));

        map.getOverlays().add(locationOverlay);


        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // TODO: handle missing permission which is needed to run the app
            Log.e("fred missing Permission", "fred permission");
            return;
        }

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
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);



        currentWifis = findViewById(R.id.current_wifis);

        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        if (preferences.contains("service_enabled")) {
            toggleButton.setChecked(preferences.getBoolean("service_enabled", true));
        }

        serviceToggled();

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serviceToggled();
            }
        });

        currentWifis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), NetworkListActivity.class));
            }
        });



        /*lm.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                Long.parseLong(preferences.getString("scan_frequency_time", "20")) * 1000,  // todo: evaluate best value
                .1f,
                locationListener
        );*/




        /*BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                unregisterReceiver(this);
                wifiScanner.handleScanResult(context, intent);
            }
        };
        wifiScanner = new Scanner((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE), wifiReceiver, this, lm);*/
        //wifiScanner.scan();



    }

    public void serviceToggled() {
        ToggleButton toggleButton = findViewById(R.id.toggleButton);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("service_enabled", toggleButton.isChecked());
        editor.apply();

        stopService(new Intent(this, LocationService.class));

        if (toggleButton.isChecked()) {
            Log.e("fred service", "start service");
            startService(new Intent(this, LocationService.class));
        }

    }


    public void registerUpdateReceiver() {

        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.e("fred receiver", "received: ");

                Gson gson = new Gson();
                ScanResult scanResult = gson.fromJson(intent.getStringExtra("de.florian_adelt.fred.update.scan"), ScanResult.class);

                List<Wifi> wifis = scanResult.getSortedWifiList();

                if (wifis.size() == 0) {
                    currentWifis.setText(getResources().getString(R.string.last_scan_resulted_empty));
                    return;
                }

                int max = 3;
                int last = wifis.size() > max ? max : wifis.size();

                List<Wifi> targetWifis = new ArrayList<>();
                List<Wifi> otherWifis = new ArrayList<>();

                FredHelper helper = new FredHelper(context);

                StringBuilder builder = new StringBuilder();

                for (int i=0; i<last; i++) {
                    if (helper.isTargetSsid(wifis.get(i).getSsid()))
                        targetWifis.add(wifis.get(i));
                    else
                        otherWifis.add(wifis.get(i));
                }

                for (int i=0; i<targetWifis.size() && i<max; i++) {
                    builder.append(wifiText(targetWifis.get(i), helper));
                    if (i != max - 1)
                        builder.append(", ");
                }
                max = max - targetWifis.size();
                for (int i=0; i<otherWifis.size() && i<max; i++) {
                    builder.append(wifiText(otherWifis.get(i), helper));
                    if (i != max - 1)
                        builder.append(", ");
                }

                if (wifis.size() > 3) {
                    builder.append(getResources().getString(R.string.and_x_other, wifis.size() - 3));
                }
                currentWifis.setText(Html.fromHtml(builder.toString()));

            }
        };
        registerReceiver(updateReceiver, new IntentFilter("de.florian_adelt.fred.update"));

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

                Toast.makeText(MapActivity.this, "Netzwerke gelöscht.",
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
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

        registerUpdateReceiver();
    }

    @Override
    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up

        unregisterReceiver(updateReceiver);
    }

}
