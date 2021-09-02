package com.hstelnet.wififorandroidq;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.util.List;


/**
* Library for WiFi Control (Created by Ljp on 2020/09/09)
*
*   API Level 29(Q) : 기존 .scanStart(), .addNetwork()...이 Deprecated 되었으며, WifiNetworkSpecifier.Build() 등 다른 방법이 추가 됨.
*                   : Ref. 1) https://developer.android.com/reference/android/net/wifi/WifiManager
*                   : Ref. 2) https://developer.android.com/reference/android/net/wifi/WifiNetworkSpecifier.Builder#build()
*   API Level < 29  : 기존 방법 채택
*                   : Wi-Fi Scan : WiFiManager을 통해 startScan()을 진행하고, 결과를 Broadcast Receiver를 통해 받는다.
*                   : Wi-Fi Connect : WiFiManager을 통해 WiFiConfiguration을 .addNetwork() 한 이후, 반환 받은 NetworkID로 .enableNetwork()를 통해 연결한다.
*                                     단, 이미 접속했던 이력이 있을 경우 .addNetwork()시 -1을 반환받기 때문에, 아래 라이브러리의 .getExistedNetworkId()을 통해 NetworkID를 가져온다.
*
* **/

public class HSWiFiManager {
    public final static int REQUEST_CODE_LOCATION_PERMISSION    = 100;
    public final static String RECEIVER_ACTION_SCAN_RESULT      = WifiManager.SCAN_RESULTS_AVAILABLE_ACTION;
    public final static String RECEIVER_ACTION_CONNECT_SUCCESS  = "HS_WIFI_MANAGER_CONNECT_SUCCESS";
    public final static String RECEIVER_ACTION_CONNECT_FAIL     = "HS_WIFI_MANAGER_CONNECT_FAIL";

    private static MainActivity activity;

    private WifiManager manager;
    private IntentFilter scanFilter;


    HSWiFiManager(@NonNull MainActivity activity) {
        HSWiFiManager.activity = activity;

        this.manager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        this.scanFilter = new IntentFilter();
        this.scanFilter.addAction(RECEIVER_ACTION_SCAN_RESULT);
        this.scanFilter.addAction(RECEIVER_ACTION_CONNECT_SUCCESS);
        this.scanFilter.addAction(RECEIVER_ACTION_CONNECT_FAIL);
    }


    /********************************************************************************************************************************/
    /******************************************************* Public Functions *******************************************************/
    /********************************************************************************************************************************/
    public void startScan() {
        this.manager.startScan();
    }


    public boolean connect(HSWiFiModel model) {
        if (model == null) return false;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            connectWiFiForLessThen29(model);
        } else {
            return false;
        }

        return true;
    }

    @RequiresApi (api = Build.VERSION_CODES.Q)
    public boolean connect(HSWiFiModel model, ConnectivityManager.NetworkCallback callback) {
        if (model == null) return false;

        connectWiFiForMoreThen29(model, callback);

        return true;
    }

    public void bindToNetwork() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder;
        builder = new NetworkRequest.Builder();
        builder.removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.removeTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
        connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                connectivityManager.bindProcessToNetwork(network);
            }
        });
    }

    public void unbindToNetwork() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder;
        builder = new NetworkRequest.Builder();
        builder.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        connectivityManager.requestNetwork(builder.build(), new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                connectivityManager.bindProcessToNetwork(network);
            }
        });
    }

    public String toStringCurrentWiFiStatus() {
        WifiInfo info = manager.getConnectionInfo();

        return  "SSID : " + info.getSSID()  + "\n" + //<unknown ssid>
                "BSSID : " + info.getBSSID() + "\n" +
                "RSSI : " + info.getRssi() + "\n" +
                "Mac Address : " + info.getMacAddress() + "\n" +
                "Ip Address : " + info.getIpAddress() + "\n" +
                "Network ID : " + info.getNetworkId();
    }

    public String toStringPreviouslyWiFiList() {
        String re = "";
        List<WifiConfiguration> configuredNetworks = manager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {
                re += existingConfig.SSID + "\n";
            }
        }

        return re;
    }


    /*********************************************************************************************************************************/
    /******************************************************* Private Functions *******************************************************/
    /*********************************************************************************************************************************/
    // Common
    private static void sendBroadcastMessage(String action, String key, String value) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);

        broadcastIntent.putExtra(key, value);

        activity.sendBroadcast(broadcastIntent);
    }

    private static void sendBroadcastMessage(String action, String[] keys, String[] values) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(action);

        for (int i=0;i<keys.length;i++) {
            broadcastIntent.putExtra(keys[i], values[i]);
        }

        activity.sendBroadcast(broadcastIntent);
    }


    // For less then SDK Lv.29
    private void connectWiFiForLessThen29(final HSWiFiModel model) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //연결 정보 생성
                WifiConfiguration wifiConfiguration = new WifiConfiguration();

                switch  (model.getPasswordType()) {
                    case "[ESS]":
                        // None Password
                        wifiConfiguration.SSID = "\"".concat(model.getSSID()).concat("\"");
                        wifiConfiguration.priority = 255;
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                        wifiConfiguration.allowedAuthAlgorithms.clear();
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        break;
                    case "[WPA2-PSK-CCMP+TKIP][ESS]":
                    case "[WPA2-PSK-CCMP+TKIP][WPA-PSK-CCMP+TKIP][ESS]":
                        wifiConfiguration.SSID = "\"".concat(model.getSSID()).concat("\"");
                        wifiConfiguration.status = WifiConfiguration.Status.DISABLED;
                        wifiConfiguration.priority = 255;
                        wifiConfiguration.preSharedKey = "\"".concat(model.getPassword()).concat("\"");
                        wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                        wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                        wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                        wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                        break;
                }

                int networkId = manager.addNetwork(wifiConfiguration);

                if (networkId == -1) {
                    // 연결 이력이 있으므로, 연결 이력 조회
                    networkId = getExistedNetworkId(model.getSSID());
                }

                manager.disconnect();
                manager.enableNetwork(networkId, true);

                try {
                    Thread.sleep(3000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (getCurrentConnectionInfo().getSSID().contains(model.getSSID())) {
                    // 연결 성공
                    sendBroadcastMessage(RECEIVER_ACTION_CONNECT_SUCCESS, new String[]{"result", "ssid"}, new String[]{"success", model.getSSID()});
                } else {
                    // 연결 실패
                    sendBroadcastMessage(RECEIVER_ACTION_CONNECT_FAIL, new String[]{"result", "ssid"}, new String[]{"fail", model.getSSID()});
                }
            }
        }).start();
    }

    private int getExistedNetworkId(String SSID) {
        /** 이전에 연결했던 기록이 있다면, manager.addNetwork() 시 -1이 반환 됨. **/
        String value = "\"" + SSID + "\"";
        List<WifiConfiguration> configuredNetworks = manager.getConfiguredNetworks();
        if (configuredNetworks != null) {
            for (WifiConfiguration existingConfig : configuredNetworks) {

                if (value.equals(existingConfig.SSID)) {
                    return existingConfig.networkId;
                }
            }
        }

        return -1;
    }

    // For more then SDK Lv.29
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void connectWiFiForMoreThen29(final HSWiFiModel model, ConnectivityManager.NetworkCallback callback) {
            WifiNetworkSpecifier.Builder builder = new WifiNetworkSpecifier.Builder();
            builder.setSsid(model.getSSID());
            if (model.getPasswordType().contains("WPA")) {
                builder.setWpa2Passphrase(model.getPassword());
            }

            WifiNetworkSpecifier wifiNetworkSpecifier = builder.build();

            NetworkRequest.Builder networkRequestBuilder = new NetworkRequest.Builder();
            networkRequestBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
            networkRequestBuilder.setNetworkSpecifier(wifiNetworkSpecifier);

            NetworkRequest networkRequest = networkRequestBuilder.build();
            ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            cm.requestNetwork(networkRequest, callback);
//            callback = new ConnectivityManager.NetworkCallback() {
//                @Override
//                public void onAvailable(@NonNull Network network) {
//                    //Use this network object to Send request.
//                    //eg - Using OkHttp library to create a service request
//
//                    super.onAvailable(network);
//                }
//            };
    }






    /*********************************************************************************************************************************/
    /******************************************************** Getter & Setter ********************************************************/
    /*********************************************************************************************************************************/
    public WifiInfo getCurrentConnectionInfo() {
        return this.manager.getConnectionInfo();
    }

    public WifiManager getManager() {
        return manager;
    }

    public IntentFilter getScanFilter() {
        return scanFilter;
    }
}
