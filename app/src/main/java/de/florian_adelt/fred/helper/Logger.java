package de.florian_adelt.fred.helper;

import android.content.Context;
import android.util.Log;

import de.florian_adelt.fred.database.DatabaseHelper;

public class Logger {


    public static int LEVEL_INFO = 0;
    public static int LEVEL_WARNING = 1;
    public static int LEVEL_ERROR = 2;

    public static void log(Context context, String tag, String message, int level) {

        DatabaseHelper helper = new DatabaseHelper(context);
        //helper.log(message, level, tag);  // todo: crashes on Android 9 device with: table Logs has no column named tag
        Log.e("fred_" + tag, message);

    }

    public static void log(Context context, String tag, String message) {
        log(context, tag, message, LEVEL_INFO);
    }

    public static void e(Context context, String tag, Exception e) {
        log(context, tag, e.getMessage(), LEVEL_ERROR);
    }


}
