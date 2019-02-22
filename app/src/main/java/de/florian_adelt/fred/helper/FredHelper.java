package de.florian_adelt.fred.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class FredHelper {

    protected Context context;
    protected SharedPreferences preferences;
    protected List<String> ssids;

    public FredHelper(Context context) {
        this.context = context;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = preferences.getString("target_ssids", "");
        Gson gson = new Gson();
        Type type = new TypeToken<List<String>>() {}.getType();
        ssids = gson.fromJson(json, type);
    }



    public boolean isTargetSsid(String ssid) {
        return ssids.contains(ssid);
    }
}
