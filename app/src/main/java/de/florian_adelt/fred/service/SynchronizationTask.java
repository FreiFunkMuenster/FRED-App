package de.florian_adelt.fred.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import de.florian_adelt.fred.database.DatabaseHelper;

public class SynchronizationTask extends NetworkTask {


    protected SharedPreferences preferences;
    protected DatabaseHelper dbHelper;

    public SynchronizationTask(SharedPreferences preferences, DatabaseHelper dbHelper) {
        this.preferences = preferences;
        this.dbHelper = dbHelper;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        String baseUrl = params[0]; // URL to call (https://fredbackend.ffmsl.de/)
        String actionUrl = params[1]; // URL to call (api/v1/app-user/create)
        String apiKey = params[2]; // Api Key
        String data = params[3]; //data to post
        String userHash = preferences.getString("user_hash", "");
        String userId = preferences.getString("user_id", "");

        if ("".equals(userHash) || "".equals(userId)) {
            Response response = request(
                    baseUrl + "app-user/create?api_key=" + apiKey,
                    "POST",
                    "{\"device-make\":\"" + Build.MANUFACTURER + "\", \"device-model\": \"" + Build.MODEL + "\"}");

            Type type = new TypeToken<CreateUserResponse>() {}.getType();
            Gson gson = new Gson();
            CreateUserResponse createUserResponse = gson.fromJson(response.getData(), type);

            preferences.edit()
                    .putString("user_hash", createUserResponse.getHash())
                    .putString("user_id", createUserResponse.getId())
                    .apply();

            userHash = preferences.getString("user_hash", "");
            userId = preferences.getString("user_id", "");

        }

        Response response = request(baseUrl + actionUrl + "?api_key=" + apiKey + "&hash=" + userHash, "POST", data);

        //System.out.println(response.toString());
        if (response != null && response.getCode() >= 200 && response.getCode() < 300) {
            dbHelper.setSynced();
        }

        return "";
    }

}
