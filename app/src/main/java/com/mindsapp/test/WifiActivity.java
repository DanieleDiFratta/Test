package com.mindsapp.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiActivity extends AppCompatActivity {

    WifiManager wifi;
    ListView lv;
    BroadcastReceiver br;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        lv = (ListView) findViewById(R.id.listView);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifi.startScan();
                displayResults(wifi.getScanResults());
            }
        };
        registerReceiver(br, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onResume() {
        super.onResume();
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                wifi.startScan();
                displayResults(wifi.getScanResults());
            }
        };
        registerReceiver(br, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private  void displayResults(List<ScanResult> scanResults) {
        ArrayList<String> wifiList = new ArrayList<String>();
        for (ScanResult res:
                scanResults) {
            String info = "Nome: " + res.SSID + "\n" +
                    "Potenza segnale: " + res.level + " dBm" + "\n" +
                    "Frequenza: " + res.frequency + "\n" +
                    "Indirizzo Mac: " + res.BSSID + "\n" +
                    "Qualità segnale: " + WifiManager.calculateSignalLevel(res.level,5);
            wifiList.add(info);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,wifiList);
        lv.setAdapter(adapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }
}
