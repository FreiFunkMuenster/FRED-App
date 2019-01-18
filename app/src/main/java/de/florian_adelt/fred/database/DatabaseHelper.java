package de.florian_adelt.fred.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

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
                    "result TEXT NOT NULL DEFAULT '[]'" +
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
}
