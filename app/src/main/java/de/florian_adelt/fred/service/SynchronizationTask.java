package de.florian_adelt.fred.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.Logger;
import de.florian_adelt.fred.helper.Status;

public abstract class SynchronizationTask extends NetworkTask {


    protected SharedPreferences preferences;
    protected DatabaseHelper dbHelper;
    protected boolean notify;

    public SynchronizationTask(Context context, SharedPreferences preferences, DatabaseHelper dbHelper, boolean notify) {
        super(context);
        this.preferences = preferences;
        this.dbHelper = dbHelper;
        this.notify = notify;
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
        de.florian_adelt.fred.helper.Status.broadcastStatus(context, R.string.starting_synchronization);

        if ("".equals(userHash) || "".equals(userId)) {
            Response response = request(
                    baseUrl + "app-user/create?api_key=" + apiKey,
                    "POST",
                    "{\"device-make\":\"" + Build.MANUFACTURER + "\", \"device-model\": \"" + Build.MODEL + "\"}");

            if (response == null) {
                Logger.log(context, "sync", "create user returned null");
                showToast(R.string.error_000);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, R.string.error_000);
                return "";
            }

            Type type = new TypeToken<CreateUserResponse>() {}.getType();
            Gson gson = new Gson();
            CreateUserResponse createUserResponse = gson.fromJson(response.getData(), type);

            if ("".equals(createUserResponse.getHash())) {
                Logger.log(context, "sync", "create user returned empty hash value");
                showToast(R.string.error_002);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, R.string.error_002);
                return "";
            }

            preferences.edit()
                    .putString("user_hash", createUserResponse.getHash())
                    .putString("user_id", createUserResponse.getId())
                    .apply();

            userHash = preferences.getString("user_hash", "");
            userId = preferences.getString("user_id", "");

        }

        if (preferences.getBoolean("nickname_changed", true)) {

            try {
                Response response = request(
                        baseUrl + "app-user/update?api_key=" + apiKey + "&hash=" + userHash,
                        "POST",
                        "{\"nickname\":\"" + preferences.getString("nickname", "Anonym") + "\"}");

                if (response == null) {
                    Logger.log(context, "sync", "tried to update user nickname, but no response was given");
                } else if (response.getCode() == 200) {
                    Logger.log(context, "sync", "updated nickname successfully");
                    preferences.edit()
                            .putBoolean("nickname_changed", false)
                            .apply();
                }
            } catch (Exception e) {
                Logger.e(context, "sync_name", e);
            }

        }

        Response response = request(baseUrl + actionUrl + "?api_key=" + apiKey + "&hash=" + userHash, "POST", data);

        //System.out.println(response.toString());
        if (data.length() > 20 && response != null && response.getCode() >= 200 && response.getCode() < 300) {
            if (!"".equals(context.getString(successMessage))) {
                showToast(successMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, successMessage);
            }
            onSuccess();
        }
        else {
            if (!"".equals(context.getString(errorMessage))) {
                showToast(errorMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, errorMessage);
            }
        }

        return "";
    }

    @Override
    protected void showToast(int textResource) {
        Logger.log(context, "sync", "show toast: " + notify);
        if (notify)
            super.showToast(textResource);
    }
}

