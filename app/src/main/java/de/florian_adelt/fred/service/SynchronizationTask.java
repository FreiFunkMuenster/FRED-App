package de.florian_adelt.fred.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
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
import java.util.Map;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.database.DatabaseHelper;
import de.florian_adelt.fred.helper.HashManager;
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
        boolean retry = true;
        if (params.length > 4) {
            if ("0".equals(params[4])) {  // "0" if the request should not be retried. Important for error handling
                retry = false;
            }
        }
        // String userHash = preferences.getString("user_hash", "");
        @Nullable
        String userHash = HashManager.getHash(context, baseUrl);
        String userId = preferences.getString("user_id", "");  // todo: remove as the id is not really used, we only use the hash right now
        de.florian_adelt.fred.helper.Status.broadcastStatus(context, R.string.starting_synchronization);

        if (userHash == null || "".equals(userHash)) {
            userHash = createUserHash(baseUrl, apiKey);
        }

        if (preferences.getBoolean("nickname_changed", true)) {
            updateNickname(baseUrl, apiKey, userHash);
        }

        Response response = request(baseUrl + actionUrl + "?api_key=" + apiKey + "&hash=" + userHash, "POST", data);

        //System.out.println(response.toString());
        if (response != null && response.getCode() >= 200 && response.getCode() < 300) {
            if (!"".equals(context.getString(successMessage))) {
                showToast(successMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, successMessage);
            }
            onSuccess();
        }
        else {
            try {
                if (retry && response != null && response.getData() != null) {
                    Gson gson = new Gson();
                    Type mapType = new TypeToken<Map<String, String>>(){}.getType();
                    Map<String, String> responseMap = gson.fromJson(response.getData(), mapType);
                    for (String key : responseMap.keySet()) {
                        Logger.log(context, "hashmanager", "Entry for " + key + ": " + responseMap.get(key));
                    }
                    if (responseMap.containsKey("error_code")) {
                        if ("WRONG_USER_HASH".equals(responseMap.get("error_code"))) {
                            // in this case, we should renew the user hash. For some reason it could not be located on the desired backend server
                            Logger.log(context, "sync", "First request failed due to wrong user hash. Retrying with new hash once...", Logger.LEVEL_ERROR);
                            HashManager.unset(context, baseUrl);
                            doInBackground(baseUrl, actionUrl, apiKey, data, "0");
                        }
                    }
                }

            } catch (Exception e) {
                Logger.e(context, "sync", e);
            }

            if (!"".equals(context.getString(errorMessage))) {
                showToast(errorMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, errorMessage);
            }
        }

        return "";
    }

    protected void updateNickname(String baseUrl, String apiKey, String userHash) {
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

    protected String createUserHash(String baseUrl, String apiKey) {
        // String deviceHash = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);  // todo: this could be enabled later on to re-recognize an user
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

        // todo: maybe remove parts of the following code as the HashManager is used, instead of putting a single hash directly in the storage
        preferences.edit()
                .putString("user_hash", createUserResponse.getHash())
                .putString("user_id", createUserResponse.getId())
                .putBoolean("nickname_changed", true)
                .apply();

        HashManager.setHash(context, baseUrl, createUserResponse.getHash());
        HashManager.info(context);

        return createUserResponse.getHash();
    }

    @Override
    protected void showToast(int textResource) {
        Logger.log(context, "sync", "show toast: " + notify);
        if (notify)
            super.showToast(textResource);
    }
}

