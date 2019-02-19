package de.florian_adelt.fred.wifi;

public class Wifi {

    protected int level;
    protected String ssid;
    protected String bssid;
    protected String capabilities;
    protected int center_frequency_0;
    protected int center_frequency_1;
    protected int channel_bandwidth;
    protected int frequency;
    protected boolean passpoint;
    protected boolean mc_resposnder;
    protected String distance;
    protected String distance_sd;
    protected String channel_mode;
    protected String bss_load_element;


    public Wifi(int level, String ssid, String bssid, String capabilities, int center_frequency_0, int center_frequency_1, int channel_bandwidth, int frequency, boolean passpoint, boolean mc_resposnder) {
        this.level = level;
        this.ssid = ssid;
        this.bssid = bssid;
        this.capabilities = capabilities;
        this.center_frequency_0 = center_frequency_0;
        this.center_frequency_1 = center_frequency_1;
        this.channel_bandwidth = channel_bandwidth;
        this.frequency = frequency;
        this.passpoint = passpoint;
        this.mc_resposnder = mc_resposnder;
    }

    public int getLevel() {
        return level;
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public int getCenter_frequency_0() {
        return center_frequency_0;
    }

    public int getCenter_frequency_1() {
        return center_frequency_1;
    }

    public int getChannel_bandwidth() {
        return channel_bandwidth;
    }

    public int getFrequency() {
        return frequency;
    }

    public boolean isPasspoint() {
        return passpoint;
    }

    public boolean isMc_resposnder() {
        return mc_resposnder;
    }

    public String getDistance() {
        return distance;
    }

    public String getDistance_sd() {
        return distance_sd;
    }

    public String getChannel_mode() {
        return channel_mode;
    }

    public String getBss_load_element() {
        return bss_load_element;
    }
}
