package com.mindsapp.test.model;

import android.net.wifi.ScanResult;

import java.io.Serializable;

/**
 * Created by Daniele on 08/12/2015.
 */
public class WifiNetwork implements Serializable{
    private int frequency;
    private int RSSI;
    private Channel channel;
    private String SSID;
    private String BSSID;

    public WifiNetwork(int frequency, int RSSI, String SSID, String BSSID) {
        this.frequency = frequency;
        this.RSSI = RSSI;
        this.SSID = SSID;
        this.BSSID = BSSID;
    }

    public WifiNetwork(ScanResult result) {
        this(result.frequency, result.level, result.SSID, result.BSSID);
    }

    public WifiNetwork(String SSID) {
        this.SSID = SSID;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    @Override
    public String toString() {
        return "WifiNetwork{" +
                "frequency=" + frequency +
                ", RSSI=" + RSSI +
                ", SSID='" + SSID + '\'' +
                ", BSSID='" + BSSID + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WifiNetwork that = (WifiNetwork) o;

        if (!SSID.equals(that.SSID)) return false;
        return BSSID.equals(that.BSSID);

    }

    @Override
    public int hashCode() {
        int result = SSID.hashCode();
        result = 31 * result + BSSID.hashCode();
        return result;
    }

    public String getBSSID() {
        return BSSID;
    }
}
