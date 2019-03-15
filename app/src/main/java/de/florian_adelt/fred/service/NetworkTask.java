package de.florian_adelt.fred.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.helper.Logger;

public abstract class NetworkTask extends AsyncTask<String, String, String> {


    protected Context context;

    public NetworkTask(Context context) {
        this.context = context;
    }

    @Nullable
    protected Response request(String endpointUrl, String method, String payload) {

        OutputStream out = null;

        try {
            URL url = new URL(endpointUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(payload);
            writer.flush();
            writer.close();
            out.close();

            urlConnection.connect();

            InputStream is = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();
            try (InputStreamReader isr = new InputStreamReader(is)) {
                char[] buffer = new char[1024];
                int len;
                while ((len = isr.read(buffer)) != -1) {
                    builder.append(buffer, 0, len);
                }
            }
            is.close();
            Logger.log(context, "Fred Sync Response", builder.toString());

            return new Response(urlConnection.getResponseCode(), builder.toString());

        } catch (Exception e) {
            Log.e("Fed Sync", e.getMessage());
            Logger.e(context, "fred sync", e);
        }

        return null;
    }



    protected class Response {
        private int code;
        private String data;

        public Response(int code, String data) {
            this.code = code;
            this.data = data;
        }

        public int getCode() {
            return code;
        }

        public String getData() {
            return data;
        }
    }

    protected class CreateUserResponse {

        private String hash;
        private String id;

        public CreateUserResponse(String hash, String id) {
            this.hash = hash;
            this.id = id;
        }

        public String getHash() {
            return hash;
        }

        public void setHash(String hash) {
            this.hash = hash;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    protected void showToast(final int textResource) {
        Log.e("fred sync", "showing toast");
        try {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(context, textResource, Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
