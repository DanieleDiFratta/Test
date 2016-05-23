package com.mindsapp.test;

import android.app.Activity;
import android.app.IntentService;
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
import android.widget.TextView;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.RSSIManager;
import com.mindsapp.test.model.WifiNetwork;

import java.util.HashMap;

public class RSSICollectorActivity extends AppCompatActivity {

    public static final String RSSI_COLLECTOR_PREFERENCES = "RSSICollectorPreferences";
    public static final String NUMBER_OF_TESTS = "NumberOfTests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssicollector);
        TextView numberOfTests = (TextView) findViewById(R.id.NumberOfTestText);
        numberOfTests.setText("Number Of Completed Tests: " + getSharedPreferences(RSSI_COLLECTOR_PREFERENCES,Activity.MODE_PRIVATE).getInt(NUMBER_OF_TESTS,0));
    }

    public void calculateThres(View view) {
        RSSIManager rssiManager = new RSSIManager();
        rssiManager.calculateThres();
    }

    public static class RSSIService extends IntentService {

        public static final int TOTAL_SCAN_NUM = 100;
        private RSSIManager rssiManager;
        private int scanNum;
        private BroadcastReceiver receiver;
        public static boolean activityVisible;

        public RSSIService() {
            super("RSSIService");
            this.rssiManager = new RSSIManager();
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
                    rssiManager.elaborateResult(wifiManager.getScanResults());
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
            SharedPreferences sharedPreferences = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES, Activity.MODE_PRIVATE);
            int NoT = sharedPreferences.getInt(NUMBER_OF_TESTS,0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(NUMBER_OF_TESTS,NoT++);
            editor.commit();
            rssiManager.save();
        }
    }
}
