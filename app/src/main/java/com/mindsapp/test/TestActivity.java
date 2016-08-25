package com.mindsapp.test;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.TestManager;
import com.mindsapp.test.model.WifiNetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TestActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ListView lv;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver scanReciever;
    private Context activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        activityContext = this;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(TestService.TOTAL_SCAN_NUM);
        progressDialog.setProgress(0);
        progressDialog.setMessage("Scanning...\nYou can leave the app in background");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.show();
        lv = (ListView) findViewById(R.id.test_list_view);
        startService(new Intent(this, TestService.class));
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                HashMap<WifiNetwork,String> oldTestMap = (HashMap<WifiNetwork, String>) intent.getSerializableExtra("oldTestMap");
                HashMap<WifiNetwork,String> newTestMap = (HashMap<WifiNetwork, String>) intent.getSerializableExtra("newTestMap");
                HashMap<WifiNetwork,Integer> oldTestVariables = (HashMap<WifiNetwork, Integer>) intent.getSerializableExtra("oldTestVariables");
                HashMap<WifiNetwork,Integer> newTestVariables = (HashMap<WifiNetwork, Integer>) intent.getSerializableExtra("newTestVariables");
                HashMap<WifiNetwork,Integer> numRSSI = (HashMap<WifiNetwork, Integer>) intent.getSerializableExtra("numRSSI");
                progressDialog.dismiss();
                showResult(oldTestMap,newTestMap,oldTestVariables,newTestVariables,numRSSI);
                startService(new Intent(activityContext,TestService.class));
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
        registerReceiver(broadcastReceiver, new IntentFilter("com.mindsapp.test.action.SCAN_FINISHED_TEST_ACTION"));
        registerReceiver(scanReciever,new IntentFilter("com.mindsapp.test.action.NEW_SCAN_TEST_ACTION"));
        TestService.activityVisible = true;
    }

    public void showResult(HashMap<WifiNetwork, String> oldTestMap, HashMap<WifiNetwork, String> newTestMap, HashMap<WifiNetwork, Integer> oldTestVariables, HashMap<WifiNetwork, Integer> newTestVariables, HashMap<WifiNetwork, Integer> numRSSI){
        ArrayList<String> wifiList = new ArrayList<>();
        for (WifiNetwork network :
                oldTestMap.keySet()) {
            if(network.getBSSID().contains("04:62:73:48:a6:")) {
                String info = network.getSSID() + ": " + network.getBSSID() + " (old: " + oldTestMap.get(network) + ", new: " + newTestMap.get(network)
                        + ")" + "\nOld Test Variable: " + oldTestVariables.get(network)
                        + ", New Test Variable: " + newTestVariables.get(network)
                        + ", Num RSSI: " + numRSSI.get(network);
                wifiList.add(info);
            }
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
        TestService.activityVisible = false;
    }


    public static class TestService extends IntentService{

        public static final int TOTAL_SCAN_NUM = 50;
        private TestManager testManager;
        private int scanNum;
        private BroadcastReceiver receiver;
        public static boolean activityVisible;

        public TestService() {
            super("TestService");
            this.testManager = new TestManager();
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
                    testManager.elaborateResult(wifiManager.getScanResults());
                    Intent myintent = new Intent();
                    myintent.setAction("com.mindsapp.test.action.NEW_SCAN_TEST_ACTION");
                    myintent.putExtra("scanNum", scanNum);
                    sendBroadcast(myintent);
                    wifiManager.startScan();
                }
            };
            registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
            while (scanNum<TOTAL_SCAN_NUM) {
                //
            }
            while (!activityVisible){
                //
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            unregisterReceiver(receiver);
            HashMap<WifiNetwork,String> oldTestMap = this.testManager.getOldResults();
            HashMap<WifiNetwork,String> newTestMap = null;
            HashMap<WifiNetwork,String> invertedTestMap = null;
            HashMap<WifiNetwork,Double> oldTestVariables = null;
            HashMap<WifiNetwork, Double> newTestVariables = null;
            HashMap<WifiNetwork,Integer> numRSSI = this.testManager.getNumRSSI();
            try {
                newTestMap = this.testManager.getNewResults();
                invertedTestMap = this.testManager.getInvertedResults();
                oldTestVariables = this.testManager.getOldTestVariables();
                newTestVariables = this.testManager.getNewTestVariables();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //testManager.updateRSSIMap();
            //testManager.saveRSSI();
            Intent intent = new Intent();
            intent.setAction("com.mindsapp.test.action.SCAN_FINISHED_TEST_ACTION");
            intent.putExtra("oldTestMap", oldTestMap);
            intent.putExtra("newTestMap",newTestMap);
            intent.putExtra("invertedTestMap",invertedTestMap);
            intent.putExtra("oldTestVariables",oldTestVariables);
            intent.putExtra("newTestVariables",newTestVariables);
            intent.putExtra("numRSSI",numRSSI);
            sendBroadcast(intent);
        }
    }
}
