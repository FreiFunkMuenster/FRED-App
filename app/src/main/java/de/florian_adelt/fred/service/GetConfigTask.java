package de.florian_adelt.fred.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import de.florian_adelt.fred.helper.Logger;

public class GetConfigTask extends NetworkTask {
    public GetConfigTask(Context context) {
        super(context);
    }


    @Override
    protected String doInBackground(String... params) {
        String baseUrl = params[0]; // URL to call (https://fredbackend.ffmsl.de/)
        String fileName = "/config.json";
        boolean error = false;

        Response response = request(baseUrl + fileName, "GET", "");
        if (response == null) {
            error = true;
            Logger.log(context, "import", "Tried to import, but config file was not optained; " + baseUrl + fileName);
        } else {
            try {

                Type type = new TypeToken<GetConfigResponse>() {}.getType();
                Gson gson = new Gson();
                GetConfigResponse config = gson.fromJson(response.getData(), type);

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                StringBuilder builder = new StringBuilder("[");
                for (int i=0; i<config.getTarget_ssids().length; i++) {
                    String ssid = config.getTarget_ssids()[i];
                    builder.append('"');
                    builder.append(ssid);
                    builder.append('"');
                    if (i < config.getTarget_ssids().length - 1) {
                        builder.append(',');
                    }
                }
                builder.append(']');

                preferences.edit()
                        .putString("backend_url", config.getBackend_url())
                        .putString("tile_server_url", config.getTile_server_url())
                        .putString("target_ssids", builder.toString())
                        .putInt("scan_frequency_time", config.getScan_frequency_time())
                        .putInt("scan_frequency_distance", config.getScan_frequency_distance())
                        .putInt("scan_frequency_accuracy", config.getScan_frequency_accuracy())
                        .putString("sync_frequency", Integer.toString(config.getSync_frequency()))
                        .putString("scan_time_to_stop", Long.toString(config.getScan_time_to_stop()))
                        .putBoolean("auto_upload", config.isAuto_upload())
                        .putBoolean("upload_only_wifi", config.isUpload_only_wifi())
                        .apply();

            } catch (Exception e) {
                Logger.e(context, "import", e);
                error = true;
            }
        }

        if (!error && response.getCode() >= 200 && response.getCode() < 300) {
            if (!"".equals(context.getString(successMessage))) {
                showToast(successMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, successMessage);

            }
        }
        else {
            if (!"".equals(context.getString(errorMessage))) {
                showToast(errorMessage);
                de.florian_adelt.fred.helper.Status.broadcastStatus(context, errorMessage);
            }
        }

        return null;
    }


    protected class GetConfigResponse {
        private int scan_frequency_time;
        private int scan_frequency_distance;
        private int scan_frequency_accuracy;
        private String[] target_ssids;
        private long scan_time_to_stop;
        private boolean auto_upload;
        private boolean upload_only_wifi;
        private String backend_url;
        private int sync_frequency;
        private String tile_server_url;

        public GetConfigResponse(int scan_frequency_time, int scan_frequency_distance, int scan_frequency_accuracy, String[] target_ssids, long scan_time_to_stop, boolean auto_upload, boolean upload_only_wifi, String backend_url, int sync_frequency, String tile_server_url) {
            this.scan_frequency_time = scan_frequency_time;
            this.scan_frequency_distance = scan_frequency_distance;
            this.scan_frequency_accuracy = scan_frequency_accuracy;
            this.target_ssids = target_ssids;
            this.scan_time_to_stop = scan_time_to_stop;
            this.auto_upload = auto_upload;
            this.upload_only_wifi = upload_only_wifi;
            this.backend_url = backend_url;
            this.sync_frequency = sync_frequency;
            this.tile_server_url = tile_server_url;
        }

        public int getScan_frequency_time() {
            return scan_frequency_time;
        }

        public void setScan_frequency_time(int scan_frequency_time) {
            this.scan_frequency_time = scan_frequency_time;
        }

        public int getScan_frequency_distance() {
            return scan_frequency_distance;
        }

        public void setScan_frequency_distance(int scan_frequency_distance) {
            this.scan_frequency_distance = scan_frequency_distance;
        }

        public int getScan_frequency_accuracy() {
            return scan_frequency_accuracy;
        }

        public void setScan_frequency_accuracy(int scan_frequency_accuracy) {
            this.scan_frequency_accuracy = scan_frequency_accuracy;
        }

        public String[] getTarget_ssids() {
            return target_ssids;
        }

        public void setTarget_ssids(String[] target_ssids) {
            this.target_ssids = target_ssids;
        }

        public long getScan_time_to_stop() {
            return scan_time_to_stop;
        }

        public void setScan_time_to_stop(long scan_time_to_stop) {
            this.scan_time_to_stop = scan_time_to_stop;
        }

        public boolean isAuto_upload() {
            return auto_upload;
        }

        public void setAuto_upload(boolean auto_upload) {
            this.auto_upload = auto_upload;
        }

        public boolean isUpload_only_wifi() {
            return upload_only_wifi;
        }

        public void setUpload_only_wifi(boolean upload_only_wifi) {
            this.upload_only_wifi = upload_only_wifi;
        }

        public String getBackend_url() {
            return backend_url;
        }

        public void setBackend_url(String backend_url) {
            this.backend_url = backend_url;
        }

        public int getSync_frequency() {
            return sync_frequency;
        }

        public void setSync_frequency(int sync_frequency) {
            this.sync_frequency = sync_frequency;
        }

        public String getTile_server_url() {
            return tile_server_url;
        }

        public void setTile_server_url(String tile_server_url) {
            this.tile_server_url = tile_server_url;
        }
    }

    @Override
    protected void onSuccess() {

    }

}
