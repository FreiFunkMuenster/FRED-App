package de.florian_adelt.fred.wifi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ScanResult {

    protected long id;
    protected long time;
    protected double latitude;
    protected double longitude;
    protected double altitude;
    protected double accuracy;
    protected String status;
    protected List<Wifi> networks;

    public ScanResult(long id, long time, double latitude, double longitude, double altitude, double accuracy, String status, List<Wifi> networks) {
        this.id = id;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.status = status;
        this.networks = networks;
    }


    public List<Wifi> getSortedWifiList() {
        Collections.sort(networks, new Comparator<Wifi>() {
            @Override
            public int compare(Wifi aWifi, Wifi bWifi)
            {
                return  bWifi.getLevel() - aWifi.getLevel();
            }
        });

        return networks;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Wifi> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Wifi> networks) {
        this.networks = networks;
    }
}
