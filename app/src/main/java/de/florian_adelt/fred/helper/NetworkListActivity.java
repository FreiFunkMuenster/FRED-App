package de.florian_adelt.fred.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.service.SynchronizationTask;
import de.florian_adelt.fred.wifi.Wifi;

public class NetworkListActivity extends AppCompatActivity {


    private List<SimpleListable> wifis;
    private RecyclerView list;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager listManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        wifis = new ArrayList<>();

        list = (RecyclerView) findViewById(R.id.network_list_view);

        // use a linear layout manager
        listManager = new LinearLayoutManager(this);
        list.setLayoutManager(listManager);

        // specify an adapter (see also next example)
        adapter = new WifiAdapter(wifis);
        list.setAdapter(adapter);


        update();
    }


    public void update() {

        wifis.clear();
        DatabaseHelper helper = new DatabaseHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query("Scans", null, "synced_at IS NULL", null, null, null, "_id desc");

        while (cursor.moveToNext()) {

            try {

                Type type = new TypeToken<List<Wifi>>() {}.getType();
                Gson gson = new Gson();
                final List<Wifi> currentWifis = gson.fromJson(cursor.getString(cursor.getColumnIndex("result")), type);

                Timestamp stamp = new Timestamp(cursor.getLong(cursor.getColumnIndex("time")));
                Date date = new Date(stamp.getTime());
                SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.GERMAN);
                sdf.setTimeZone(TimeZone.getDefault());
                final String formattedDate = sdf.format(date) + " Uhr";

                double latitude = cursor.getDouble(cursor.getColumnIndex("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndex("longitude"));

                final StringBuilder subtitle = new StringBuilder(formattedDate);
                subtitle.append('\n');
                subtitle.append(getResources().getString(R.string.latitude));
                subtitle.append(": ");
                subtitle.append(latitude);
                subtitle.append('\n');
                subtitle.append(getResources().getString(R.string.longitude));
                subtitle.append(": ");
                subtitle.append(longitude);

                this.wifis.add(new SimpleListable() {
                    @Override
                    public String getTitle() {
                        return getResources().getString(R.string.scan_found_x) + " " + currentWifis.size();
                    }

                    @Override
                    public String getSubtitle() {
                        return subtitle.toString();
                    }

                    @Override
                    public int getTitleColor() {
                        return getResources().getColor(R.color.colorPrimary, getTheme());
                    }
                });
                this.wifis.addAll(currentWifis);
                if (this.wifis.size() >= 500) {
                    break;  // only get a limited amount to save performance
                }
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(getApplicationContext(), "Fred network list", e);
            }

        }


        adapter.notifyDataSetChanged();

        ((TextView)findViewById(R.id.statistic_unsynced_scans)).setText(getString(R.string.statistic_unsynced_scans, cursor.getCount()));


        cursor.close();


        String formattedDate = "Nie";

        //cursor = db.query(true, "Scans", null, "synced_at NOT NULL", null, null, null, "_id desc");
        cursor = db.query(true, "Scans", null, "synced_at NOT NULL", null, null, null, "_id DESC", "1");

        while (cursor.moveToNext()) {

            Timestamp stamp = new Timestamp(cursor.getLong(cursor.getColumnIndex("synced_at")));
            Date date = new Date(stamp.getTime());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM. H:mm", Locale.GERMAN);
            sdf.setTimeZone(TimeZone.getDefault());
            formattedDate = sdf.format(date) + " Uhr";

        }

        cursor.close();
        db.close();


        ((TextView)findViewById(R.id.statistic_last_upload)).setText(getString(R.string.statistic_last_upload, formattedDate));


    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
