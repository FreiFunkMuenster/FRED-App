package de.florian_adelt.fred.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FredHelper {

    protected Context context;
    protected SharedPreferences preferences;
    protected String[] ssids;

    public FredHelper(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        ssids = preferences.getString("target_ssids", "").split(";");
    }



    public boolean isTargetSsid(String ssid) {
        for (String s : ssids) {
            if (s.equals(ssid))
                return true;
        }

        return false;
    }
}
