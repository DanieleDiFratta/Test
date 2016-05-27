package com.mindsapp.test;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.WifiNetwork;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WifiActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;
    BroadcastReceiver scanReciever;
    ProgressDialog progressDialog;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(WifiService.TOTAL_SCAN_NUM);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Scanning...\nYou can leave the app in background");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        lv = (ListView) findViewById(R.id.listView);
        startService(new Intent(this, WifiService.class));
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HashMap<WifiNetwork,String> resultMap = (HashMap<WifiNetwork, String>) intent.getSerializableExtra("map");
                progressDialog.dismiss();
                showResult(resultMap);
            }
        };
        scanReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                progressDialog.setProgress(intent.getIntExtra("scanNum",0));
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter("com.mindsapp.test.action.SCAN_FINISHED_ACTION"));
        registerReceiver(scanReciever,new IntentFilter("com.mindsapp.test.action.NEW_SCAN_ACTION"));
        WifiService.activityVisible = true;
    }

    public void showResult(HashMap<WifiNetwork, String> resultMap){
        ArrayList<String> wifiList = new ArrayList<>();
        for (WifiNetwork network :
                resultMap.keySet()) {
            String info = network.getSSID() + ": " + resultMap.get(network);
            wifiList.add(info);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,wifiList);
        lv.setAdapter(adapter);
    }

/*    private  void displayResults(List<ScanResult> scanResults) {
        ArrayList<String> wifiList = new ArrayList<>();
        for (ScanResult res:
                scanResults) {
            String info = "Nome: " + res.SSID + "\scanNum" +
                    "Potenza segnale: " + res.level + " dBm" + "\scanNum" +
                    "Frequenza: " + res.frequency + "\scanNum" +
                    "Indirizzo Mac: " + res.BSSID + "\scanNum" +
                    "Qualità segnale: " + WifiManager.calculateSignalLevel(res.level,5);
            wifiList.add(info);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,wifiList);
        lv.setAdapter(adapter);
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(scanReciever);
        WifiService.activityVisible = false;
    }

    public static class WifiService extends IntentService {

        public static final int TOTAL_SCAN_NUM = 50;
        private NetworkManager networkManager;
        private int scanNum;
        private BroadcastReceiver receiver;
        public static boolean activityVisible;

        public WifiService() {
            super("WifiService");
            this.networkManager = new NetworkManager();
            scanNum = 0;
            activityVisible = true;
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    scanNum++;
                    Log.i("Scan num", "" + scanNum);
                    networkManager.elaborateResult(wifiManager.getScanResults());
                    Intent myintent = new Intent();
                    myintent.setAction("com.mindsapp.test.action.NEW_SCAN_ACTION");
                    myintent.putExtra("scanNum", scanNum);
                    sendBroadcast(myintent);
                }
            };
            registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            while (scanNum<TOTAL_SCAN_NUM) {
                wifiManager.startScan();
            }
            while (!activityVisible){
                //
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(receiver);
            HashMap<WifiNetwork,String> ResultMap = this.networkManager.getResults();
            networkManager.updateRSSIMap();
            networkManager.saveRSSI();
            Intent intent = new Intent();
            intent.setAction("com.mindsapp.test.action.SCAN_FINISHED_ACTION");
            intent.putExtra("map", ResultMap);
            sendBroadcast(intent);
        }
    }

}
