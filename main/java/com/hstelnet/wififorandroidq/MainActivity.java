package com.hstelnet.wififorandroidq;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.hstelnet.wififorandroidq.HSWiFiManager.REQUEST_CODE_LOCATION_PERMISSION;

public class MainActivity extends AppCompatActivity {
    //Manager
    private HSWiFiManager wifiManager;

    //Layout
    private RecyclerView recyclerView;
    private HSWiFiRecyclerViewAdapter adapter;

    //Member variables
    private List<HSWiFiModel> models;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkLocationPermission()) {
                    showSnackBar("Start Scanning...");
                    Log.e("WiFi Status", "Info : " + wifiManager.toStringCurrentWiFiStatus());
                    registerReceiver(receiver, wifiManager.getScanFilter());
                    wifiManager.startScan();
                }
            }
        });

        initializationWiFiManager();
        initializationLayout();
    }

    private void showSnackBar(String content) {
        Snackbar.make(findViewById(R.id.PARENT), content, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @AfterPermissionGranted(REQUEST_CODE_LOCATION_PERMISSION)
    public boolean checkLocationPermission() {
        /**
         * Add to AndroidManifest.xml
         *     <uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" /> : For network binding.
         *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> : For Wi-Fi scanning.
         *     <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />    : For getting Wi-Fi information.
         *     <uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />    : For Wi-Fi connect/disconnect.
         * **/

        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.ACCESS_WIFI_STATE};
        if(EasyPermissions.hasPermissions(this, perms)) {
            return true;
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_CODE_LOCATION_PERMISSION, perms);
        }

        return false;
    }

    // 내기기 위치정보 권한 응답
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initializationLayout() {
        recyclerView = findViewById(R.id.RECYCLER_VIEW);
        adapter = new HSWiFiRecyclerViewAdapter(this, models);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initializationWiFiManager() {
        wifiManager = new HSWiFiManager(this);
        models = new ArrayList<>();
    }

    public void connectWiFi(int position) {
        final HSWiFiModel model = models.get(position);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            wifiManager.connect(model);
        } else {
            wifiManager.connect(model,
                new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        //Use this network object to Send request.
                        //eg - Using OkHttp library to create a service request
                        Log.e("connectWiFi", "Successfully Connected WiFi : " + model.getSSID());

                        super.onAvailable(network);
                    }
                });
        }
    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null)
                return;

            Log.e("Receiver", "[Action] : " + intent.getAction());

            switch (intent.getAction()) {
                /** Wi-Fi Scan **/
                case HSWiFiManager.RECEIVER_ACTION_SCAN_RESULT:
                    WifiInfo currentWiFiInfo = wifiManager.getCurrentConnectionInfo();
                    List<ScanResult> results = wifiManager.getManager().getScanResults();

                    models.clear();
                    for (ScanResult scanResult : results) {
                        // 현재 연결한 Wi-Fi
                        if (scanResult.SSID.equals(currentWiFiInfo.getSSID()))
                            continue;

                        models.add(new HSWiFiModel(scanResult.SSID, String.valueOf(scanResult.level), "", scanResult.capabilities));
                        adapter.notifyDataSetChanged();
                    }

                    showSnackBar("Wi-Fi Scan Complete!");
                    wifiManager.toStringPreviouslyWiFiList();

                    unregisterReceiver(receiver);
                    break;

                /** Wi-Fi Connect **/
                case HSWiFiManager.RECEIVER_ACTION_CONNECT_SUCCESS:
                    showSnackBar("Successfully connected to [" + intent.getStringExtra("ssid") + "].");
                    break;
                case HSWiFiManager.RECEIVER_ACTION_CONNECT_FAIL:
                    showSnackBar("Failed connect to [" + intent.getStringExtra("ssid") + "].");
                    break;
            }
        }
    };
}