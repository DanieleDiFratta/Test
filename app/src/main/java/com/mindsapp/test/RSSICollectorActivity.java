package com.mindsapp.test;

import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.RSSIManager;
import com.mindsapp.test.model.Threshold;
import com.mindsapp.test.model.ThresholdManager;
import com.mindsapp.test.model.WifiNetwork;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class RSSICollectorActivity extends AppCompatActivity {

    public static final String RSSI_COLLECTOR_PREFERENCES = "RSSICollectorPreferences";
    public static final String NUMBER_OF_TESTS = "NumberOfTests";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssicollector);
        TextView numberOfTests = (TextView) findViewById(R.id.NumberOfTestText);
        numberOfTests.setText("Number Of Completed Tests: " + getSharedPreferences(RSSI_COLLECTOR_PREFERENCES, Activity.MODE_PRIVATE).getInt(NUMBER_OF_TESTS, 0));
        startService(new Intent(this,RSSIService.class));
    }

    public void calculateThres(View view) {
        RSSIManager rssiManager = new RSSIManager();
        double threshold = rssiManager.calculateThres();
        showAlertDialog(threshold);
    }

    private void showAlertDialog(final double threshold) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Enter the name of the place");
        alert.setTitle("PLACE");

        alert.setView(edittext);

        alert.setPositiveButton("Yes Option", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //OR
                String place = edittext.getText().toString();
                Threshold thres = new Threshold(place,(int)threshold,(int)threshold);
                ThresholdManager manager = new ThresholdManager();
                manager.setThreshold(thres);
                try {
                    saveThreshold(thres);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("No Option", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    private void saveThreshold(Threshold thres) throws IOException {
        FileWriter writer;
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = sdcard + File.separator + ThresholdActivity.FILE_PATH;
        if(new File(file).exists())
        {
            writer = new FileWriter(file, true);
            writer.write("\n" + thres.getPlace() + "\t" + thres.getApproachingThres() + "\t" + thres.getLeavingThres());
        }
        else
        {
            writer = new FileWriter(file, true);
            writer.write(thres.getPlace() + "\t" + thres.getApproachingThres() + "\t" + thres.getLeavingThres());
        }
        writer.flush();
        writer.close();
    }

    public static class RSSIService extends IntentService {

        public static final int TOTAL_SCAN_NUM = 50;
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
            editor.putInt(NUMBER_OF_TESTS,NoT+1);
            editor.apply();
            rssiManager.save();
        }
    }
}
