package de.florian_adelt.fred.helper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import de.florian_adelt.fred.R;
import de.florian_adelt.fred.wifi.ScanResult;
import de.florian_adelt.fred.wifi.Wifi;

public class Status {


    public static void broadcastStatus(Context context, int res) {
        broadcastStatus(context, context.getString(res));
    }

    public static void broadcastStatus(Context context, String value) {

        Intent i = new Intent("de.florian_adelt.fred.update");
        Bundle extras = new Bundle();
        extras.putString("de.florian_adelt.fred.update.status", value);
        i.putExtras(extras);
        context.sendBroadcast(i);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString("last_status_update", value).apply();
    }

    public static void broadcastScanResultStatus(Context context, ScanResult scanResult) {

        if (scanResult == null) {
            Logger.log(context, "fred status", "scan result was null");
            broadcastStatus(context, R.string.waiting_for_scan);
            return;
        }

        List<Wifi> wifis = scanResult.getSortedWifiList();

        if (wifis.size() == 0) {
            broadcastStatus(context, R.string.last_scan_resulted_empty);
            return;
        }

        int max = 3;
        int last = wifis.size() > max ? max : wifis.size();

        List<Wifi> targetWifis = new ArrayList<>();
        List<Wifi> otherWifis = new ArrayList<>();

        FredHelper helper = new FredHelper(context);

        StringBuilder builder = new StringBuilder();

        for (int i=0; i<last; i++) {
            boolean isShownSsid = false;
            for (Wifi w : targetWifis) {
                if (wifis.get(i).getSsid().equals(w.getSsid())) {
                    isShownSsid = true;
                    break;
                }
            }
            if (isShownSsid) {
                continue;
            }
            if (helper.isTargetSsid(wifis.get(i).getSsid()))
                targetWifis.add(wifis.get(i));
            else
                otherWifis.add(wifis.get(i));
        }

        for (int i=0; i<targetWifis.size() && i<max; i++) {
            builder.append(wifiText(context, targetWifis.get(i), helper));
            if (i != targetWifis.size() - 1)
                builder.append(", ");
        }
        max = max - targetWifis.size();
        for (int i=0; i<otherWifis.size() && i<max; i++) {
            builder.append(wifiText(context, otherWifis.get(i), helper));
            if (i != max - 1)
                builder.append(", ");
        }

        if (wifis.size() > 3) {
            builder.append(" ");
            builder.append(context.getResources().getString(R.string.and_x_other, wifis.size() - 3));
        }
        String result = builder.toString();

        broadcastStatus(context, result);
    }

    public static String wifiText(Context context, Wifi wifi, FredHelper helper) {
        StringBuilder builder = new StringBuilder();
        String ssid = wifi.getSsid();
        if ("".equals(ssid))
            ssid = context.getString(R.string.ssid_unknown);
        if (helper.isTargetSsid(ssid)) {
            builder.append("<strong>");
            builder.append(ssid);
            builder.append("</strong>");
        }
        else {
            builder.append(ssid);
        }
        builder.append(" (");
        builder.append(wifi.getLevel());
        builder.append("db)");
        return builder.toString();
    }


    public static void broadcastGpsDisablesStatus(Context context) {
        broadcastStatus(context, R.string.gps_disabled);

    }
    public static void broadcastGpsEnablesStatus(Context context) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(preferences.getString("last_status_update", "").equals(context.getString(R.string.gps_disabled))) {
            broadcastStatus(context, R.string.gps_enabled);
        }


    }

}
