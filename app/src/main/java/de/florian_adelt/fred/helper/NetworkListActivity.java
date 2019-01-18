package de.florian_adelt.fred.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.wifi.Wifi;

public class NetworkListActivity extends AppCompatActivity {


    private List<Wifi> wifis;
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

        Cursor cursor = db.query("Scans", null, null, null, null, null, "_id desc");

        while (cursor.moveToNext()) {

            try {
                JSONArray array = new JSONArray(cursor.getString(cursor.getColumnIndex("result")));

                for (int i=0; i<array.length(); i++) {

                    JSONObject json = array.getJSONObject(i);

                    Wifi wifi = new Wifi(json.getString("ssid"), json.getInt("level"));
                    wifis.add(wifi);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
    }

}
