package com.mindsapp.test;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.WifiNetwork;
import com.mindsapp.test.model.WifiService;

import java.util.HashMap;
import java.util.Map;

public class WifiActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        startService(new Intent(this, WifiService.class));
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HashMap<WifiNetwork,String> map = (HashMap<WifiNetwork, String>) intent.getSerializableExtra("map");
                showResult(map);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter("com.mindsapp.test.action.SCAN_FINISHED_ACTION"));
    }

    public void showResult(HashMap<WifiNetwork, String> resultMap){
        for (String position :
                resultMap.values()) {
            Log.i("You are: ", position);
        }
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
    }

    public static class WifiService extends IntentService {

        public static final int TOTAL_SCAN_NUM = 10;
        private NetworkManager networkManager;
        private int scanNum;
        private BroadcastReceiver receiver;

        public WifiService() {
            super("WifiService");
            this.networkManager = new NetworkManager();
            scanNum = 0;
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
                }
            };
            registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            while (scanNum<TOTAL_SCAN_NUM) {
                wifiManager.startScan();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(receiver);
            HashMap<WifiNetwork,String> ResultMap = this.networkManager.getResults();
            Intent intent = new Intent();
            intent.setAction("com.mindsapp.test.action.SCAN_FINISHED_ACTION");
            intent.putExtra("map",ResultMap);
            sendBroadcast(intent);
        }
    }

}
