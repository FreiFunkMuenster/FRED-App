package de.florian_adelt.fred.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.helper.Logger;

public class SettingsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.network_settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NetworkSettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.synchronization_settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SynchronizationSettingsActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.load_settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoadSettingsActivity.class);
                startActivity(intent);
            }
        });



        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new
                SharedPreferences.OnSharedPreferenceChangeListener() {
                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                                          String key) {
                        switch (key) {
                            case "nickname":
                                Logger.log(getBaseContext(), "nickname", "nickname changed");
                                sharedPreferences.edit()
                                        .putBoolean("nickname_changed", true)
                                        .apply();
                                break;
                        }
                    }
                };


        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
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
