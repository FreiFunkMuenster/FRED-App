package de.florian_adelt.fred.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.florian_adelt.fred.wifi.ScanResult;
import de.florian_adelt.fred.wifi.Wifi;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fred.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String sql = "CREATE TABLE Scans (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "time INTEGER NOT NULL DEFAULT 0, " +
                    "latitude REAL NOT NULL DEFAULT 0, " +
                    "longitude REAL NOT NULL DEFAULT 0, " +
                    "altitude REAL NOT NULL DEFAULT 0, " +
                    "accuracy REAL NOT NULL DEFAULT 0, " +
                    "status TEXT NOT NULL DEFAULT 'success', " +
                    "result TEXT NOT NULL DEFAULT '[]'," +
                    "synced_at INTEGER DEFAULT NULL" +
                ")";

        sqLiteDatabase.execSQL(sql);


    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    @Nullable
    public Location getLastLocation() {
        Location result = null;
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = { "latitude", "longitude" };
            Cursor cursor = db.query(
                    true,
                    "Scans",
                    columns,
                    null,
                    null,
                    null,
                    null,
                    BaseColumns._ID + " DESC",
                    "1"
            );

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = new Location("");
            result.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
            result.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
        }

        cursor.close();
        db.close();
        return result;
    }

    public List<ScanResult> getUnsynchedScans() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"*"};
        Cursor cursor = db.query(
                false,
                "Scans",
                columns,
                "synced_at IS NULL",
                null,
                null,
                null,
                null,
                null
        );
        List<ScanResult> result = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            List<Wifi> wifis = new ArrayList<>();

            try {

                Type type = new TypeToken<List<Wifi>>() {}.getType();
                Gson gson = new Gson();
                wifis = gson.fromJson(cursor.getString(cursor.getColumnIndex("result")), type);


                /*JSONArray array = new JSONArray(cursor.getString(cursor.getColumnIndex("result")));

                for (int i=0; i<array.length(); i++) {

                    JSONObject json = array.getJSONObject(i);

                    Wifi wifi = new Wifi(json.getString("ssid"), json.getInt("level"));
                    networks.add(wifi);
                }*/

            } catch (Exception e) {
                e.printStackTrace();
            }

            ScanResult scanResult = new ScanResult(
                    cursor.getInt(cursor.getColumnIndex("_id")),
                    cursor.getLong(cursor.getColumnIndex("time")),
                    cursor.getDouble(cursor.getColumnIndex("latitude")),
                    cursor.getDouble(cursor.getColumnIndex("longitude")),
                    cursor.getDouble(cursor.getColumnIndex("altitude")),
                    cursor.getDouble(cursor.getColumnIndex("accuracy")),
                    cursor.getString(cursor.getColumnIndex("status")),
                    wifis
            );

            result.add(scanResult);

        }




        cursor.close();
        db.close();
        return result;

    }


    public void setSynced() {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("synced_at", System.currentTimeMillis());

        db.update("Scans", cv, "synced_at IS NULL", null);

        db.close();
    }

}
