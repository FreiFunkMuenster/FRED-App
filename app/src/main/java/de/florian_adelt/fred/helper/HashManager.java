package de.florian_adelt.fred.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashManager {


    @Nullable
    public static String getHash(Context context, String url) {
        Map<String, String> hashes = getMap(context);
        if (hashes.containsKey(url)) {
            return hashes.get(url);
        }

        return null;

    }

    public static void setHash(Context context, String url, String hash) {
        Map<String, String> hashes = getMap(context);
        hashes.put(url, hash);
        setMap(context, hashes);
    }

    private static Map<String, String> getMap(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        @Nullable
        String jsonString = preferences.getString("user_hash_map", null);

        if (jsonString == null) {
            Logger.log(context, "hashmanager", "User had no hash map stored, creating new one");
            jsonString = "[]";
            preferences.edit().putString("user_hash_map", jsonString).apply();
        }

        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        return gson.fromJson(jsonString, mapType);
    }

    private static void setMap(Context context, Map<String, String> map) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putString("user_hash_map", json).apply();
    }

    public static void unset(Context context, String url) {
        Map<String, String> hashes = getMap(context);
        hashes.remove(url);
        setMap(context, hashes);
    }

    // for debug purposes only
    public static void info(Context context) {
        Map<String, String> map = getMap(context);
        for (String key : map.keySet()) {
            Logger.log(context, "hashmanager", "Entry for " + key + ": " + map.get(key));
        }
    }

}
