package de.florian_adelt.fred.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import de.florian_adelt.fred.R;

public class LoadSettingsActivity extends SubSettingsActivity {

    @Override
    public int getLayoutId() {
        return R.xml.pref_import;
    }
}
