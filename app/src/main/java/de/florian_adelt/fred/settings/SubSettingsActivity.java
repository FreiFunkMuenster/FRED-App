package de.florian_adelt.fred.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.helper.NetworkListActivity;
import de.florian_adelt.fred.service.ServiceStarter;

public abstract class SubSettingsActivity extends AppCompatActivity {


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                preference.setSummary(stringValue);
            }

            if ("scan_time_to_stop".equals(preference.getKey()) && !stringValue.equals(((ListPreference) preference).getValue())) {
                ServiceStarter.setTimeToStop(preference.getContext(), Long.parseLong(stringValue));
            }

            return true;
        }
    };



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager
                .beginTransaction();
        MyFragment myFragment = new MyFragment();
        myFragment.setLayoutId(getLayoutId());
        mFragmentTransaction.replace(android.R.id.content, myFragment);
        mFragmentTransaction.commit();


    }

    public abstract int getLayoutId();


    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference == null)
            return;

        if (PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getAll().get(preference.getKey()) instanceof Boolean)
            return;
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        try {
            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
        catch (Exception e) {
            e.printStackTrace();
            try {
                sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                        PreferenceManager
                                .getDefaultSharedPreferences(preference.getContext())
                                .getInt(preference.getKey(), 0));
            }
            catch (Exception e2) {
                e2.printStackTrace();
            }
        }
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

    public static class MyFragment extends PreferenceFragment {
        protected int layoutId;

        public void setLayoutId(int layoutId) {
            this.layoutId = layoutId;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(layoutId);

            for (String key : getPreferenceManager().getSharedPreferences().getAll().keySet()) {
                bindPreferenceSummaryToValue(findPreference(key));
            }

            Preference importButton = findPreference(getString(R.string.data_import_button));
            if (importButton != null) {
                importButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // todo: add import
                        Log.i("Fred Import", "Button Press");
                        ServiceStarter.startImportService(getContext());
                        return true;
                    }
                });
            }
            Preference uploadButton = findPreference(getString(R.string.data_upload_button));
            if (uploadButton != null) {
                uploadButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.i("Fred Upload", "Button Press");
                        ServiceStarter.startSynchronizationService(getContext(), 500, false, true);
                        return true;
                    }
                });
            }
            Preference networksButton = findPreference("target_ssids_button");
            if (networksButton != null) {
                networksButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.i("Fred target ssid", "Button Press");
                       startActivity(new Intent(getActivity(), TargetNetworksActivity.class));
                        return true;
                    }
                });
            }



        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                getActivity().onBackPressed();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
