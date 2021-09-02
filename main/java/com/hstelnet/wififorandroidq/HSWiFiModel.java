package com.hstelnet.wififorandroidq;

public class HSWiFiModel {
    private String SSID;
    private String RSSI;
    private String password;
    private String passwordType;

    HSWiFiModel(String SSID, String RSSI) {
        this.SSID = SSID;
        this.RSSI = RSSI;
        this.password = null;
        this.passwordType = null;
    }

    HSWiFiModel(String SSID, String RSSI, String password, String passwordType) {
        this.SSID = SSID;
        this.RSSI = RSSI;
        this.password = password;
        this.passwordType = passwordType;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public String getRSSI() {
        return RSSI;
    }

    public void setRSSI(String RSSI) {
        this.RSSI = RSSI;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordType() {
        return passwordType;
    }

    public void setPasswordType(String passwordType) {
        this.passwordType = passwordType;
    }
}
