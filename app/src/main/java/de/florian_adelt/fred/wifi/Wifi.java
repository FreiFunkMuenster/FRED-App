package de.florian_adelt.fred.wifi;

public class Wifi {

    protected String ssid;
    protected int level;


    public Wifi(String ssid, int level) {
        this.ssid = ssid;
        this.level = level;
    }

    public String getSsid() {
        return ssid;
    }

    public int getLevel() {
        return level;
    }
}
