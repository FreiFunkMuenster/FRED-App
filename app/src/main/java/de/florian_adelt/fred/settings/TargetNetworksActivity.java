package de.florian_adelt.fred.settings;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
import de.florian_adelt.fred.helper.SimpleListable;
import de.florian_adelt.fred.helper.SsidAdapter;
import de.florian_adelt.fred.helper.WifiAdapter;
import de.florian_adelt.fred.wifi.Wifi;

public class TargetNetworksActivity extends AppCompatActivity {


    private List<SimpleListable> ssids;
    private RecyclerView list;
    private WifiAdapter adapter;
    private RecyclerView.LayoutManager listManager;
    private View.OnClickListener itemClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_network_list);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_target_list);
        //setSupportActionBar(toolbar);
        //Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);


        ssids = new ArrayList<>();

        list = (RecyclerView) findViewById(R.id.network_list_view);

        // use a linear layout manager
        listManager = new LinearLayoutManager(this);
        list.setLayoutManager(listManager);

        // specify an adapter (see also next example)
        adapter = new SsidAdapter(ssids);

        final AppCompatActivity self = this;

        itemClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(self);
                builder.setTitle(getResources().getString(R.string.remove_ssid_dialog_title));
                int itemPosition = list.getChildLayoutPosition(view);
                final String ssid = ssids.get(itemPosition).getTitle();


                // Set up the buttons
                builder.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeSsid(ssid);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        };

        this.adapter.setItemClickListener(itemClickListener);


        FloatingActionButton addButton = findViewById(R.id.add_ssid_fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(self);
                builder.setTitle(getResources().getString(R.string.add_ssid_dialog_title));

                // Set up the input
                final EditText input = new EditText(getApplicationContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setTextColor(getResources().getColor(R.color.text, getTheme()));
                input.setPadding(input.getPaddingLeft() * 2, input.getPaddingTop(), input.getPaddingRight() * 2, input.getPaddingBottom());
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton(getResources().getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addSsid(input.getText().toString());
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

        list.setAdapter(adapter);
        update();
    }

    public void addSsid(String ssid) {
        Log.i("fred ssid", "adding " + ssid);
        if ("".equals(ssid)) {
            return;
        }
        List<String> loadedSsids = loadSsids();
        if (!loadedSsids.contains(ssid)) {
            loadedSsids.add(ssid);
        }

        save(loadedSsids);
    }

    public void removeSsid(String ssid) {
        Log.i("fred ssid", "removing " + ssid);
        List<String> loadedSsids = loadSsids();
        List<String> newSsids = new ArrayList<>();

        for (String currentSsid : loadedSsids) {
            if (!currentSsid.equals(ssid)) {
                newSsids.add(currentSsid);
            }
        }

        save(newSsids);

    }

    public void save(List<String> newSsids) {

        Gson gson = new Gson();
        String toSave = gson.toJson(newSsids);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferences.edit()
                .putString("target_ssids", toSave)
                .apply();

        update();
    }

    public void update() {

        ssids.clear();

        List<String> loadedSsids = loadSsids();

        for (final String ssid : loadedSsids) {
            SimpleListable entry = new SimpleListable() {
                @Override
                public String getTitle() {
                    return ssid;
                }

                @Override
                public String getSubtitle() {
                    return "";
                }

                @Override
                public int getTitleColor() {
                    return 0;
                }
            };
            ssids.add(entry);
        }

        adapter.notifyDataSetChanged();
    }


    public List<String> loadSsids() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String rawNetworks = preferences.getString("target_ssids", "[]");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(rawNetworks, type);
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
