package de.florian_adelt.fred.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import de.florian_adelt.fred.R;

public class SynchronizationSettingsActivity extends SubSettingsActivity {

    @Override
    public int getLayoutId() {
        return R.xml.pref_data_sync;
    }
}
