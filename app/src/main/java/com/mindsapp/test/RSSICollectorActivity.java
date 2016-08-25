package com.mindsapp.test;

import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mindsapp.test.model.RSSIManager;
import com.mindsapp.test.model.Threshold;
import com.mindsapp.test.model.ThresholdManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RSSICollectorActivity extends AppCompatActivity {

    public static final String RSSI_COLLECTOR_PREFERENCES = "RSSICollectorPreferences";
    public static final String NUMBER_OF_TESTS = "NumberOfTests";
    public static final String NEW_RSSI_SCAN_ACTION = "com.mindsapp.test.action.NEW_RSSI_SCAN_ACTION";
    private static Context activityContext;

    ProgressDialog progressDialog;
    Button newScanButton;
    private BroadcastReceiver scanReciever;
    private BroadcastReceiver finishedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssicollector);
        activityContext = this;
        newScanButton = (Button) findViewById(R.id.scanButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(RSSIService.TOTAL_SCAN_NUM);
        progressDialog.setMessage("Scanning...\nYou can leave the app in background");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.show();
        scanReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                progressDialog.setProgress(intent.getIntExtra("scanNum",0));
                if(progressDialog.getProgress()==50) {
                    progressDialog.dismiss();
                }
            }
        };
        finishedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startService(new Intent(activityContext,RSSIService.class));
            }
        };
        TextView numberOfTests = (TextView) findViewById(R.id.NumberOfTestText);
        numberOfTests.setText("Number Of Completed Tests: " + getSharedPreferences(RSSI_COLLECTOR_PREFERENCES, Activity.MODE_PRIVATE).getInt(NUMBER_OF_TESTS, 0));
        startService(new Intent(this,RSSIService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(scanReciever,new IntentFilter(NEW_RSSI_SCAN_ACTION));
        registerReceiver(finishedReceiver,new IntentFilter("com.mindsapp.test.action.SCAN_FINISHED_RSSI_ACTION"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(scanReciever);
        unregisterReceiver(finishedReceiver);
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

        alert.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //OR
                String place = edittext.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES,Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("Place",place);
                putDouble(editor, "ChaoticThres", threshold);
                double leavingThres = getDouble(sharedPreferences,"LeavingThres",50.0);
                double approachingThres = getDouble(sharedPreferences,"ApproachingThres",50.0);
                editor.apply();
                try {
                    saveThreshold(place,threshold,approachingThres,leavingThres);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    private void saveThreshold(String place, double chaoticThres, double approachingThres, double leavingThreshold) throws IOException {
        FileWriter writer;
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = sdcard + File.separator + ThresholdActivity.FILE_PATH;
        if(new File(file).exists())
        {
            writer = new FileWriter(file, true);
            writer.write("\n" + place + "\t" + chaoticThres + "\t" + approachingThres + "\t" + leavingThreshold );
        }
        else
        {
            writer = new FileWriter(file, true);
            writer.write(place + "\t" + chaoticThres + "\t" + approachingThres + "\t" + leavingThreshold);
        }
        writer.flush();
        writer.close();
    }

    public void startNewScan(View view) {
        startService(new Intent(this,RSSIService.class));
        progressDialog.setProgress(0);
        progressDialog.show();
    }

    public void saveApproachingThres(View view) {
        RSSIManager rssiManager = new RSSIManager();
        double threshold = rssiManager.calculateThres();
        showAlertDialogApproaching(threshold);
    }

    private void showAlertDialogApproaching(final double threshold) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Enter the name of the place");
        alert.setTitle("PLACE");

        alert.setView(edittext);

        alert.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String place = edittext.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES,Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                putDouble(editor,"ApproachingThres", threshold);
                editor.apply();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    public void saveLeavingThres(View view) {
        RSSIManager rssiManager = new RSSIManager();
        double threshold = rssiManager.calculateThres();
        showAlertDialogLeaving(threshold);
    }

    private void showAlertDialogLeaving(final double threshold) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("Enter the name of the place");
        alert.setTitle("PLACE");

        alert.setView(edittext);

        alert.setPositiveButton("Insert", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String place = edittext.getText().toString();
                SharedPreferences sharedPreferences = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES,Activity.MODE_PRIVATE);
                double chaoticThres = getDouble(sharedPreferences,"ChaoticThres",50.0);
                double approachingThres = getDouble(sharedPreferences,"ApproachingThres",50.0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                putDouble(editor,"LeavingThres",threshold);
                editor.apply();
                try {
                    saveThreshold(place,chaoticThres,approachingThres,threshold);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // what ever you want to do with No option.
            }
        });

        alert.show();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;

        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    public void resetStoredValues(View view) {
        if(RSSIManager.resetStoredValues())
            Log.i("FILE","DELETED");
        else
            Log.i("FILE","PROBLEM");
        SharedPreferences sp = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES,Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(NUMBER_OF_TESTS,0);
        editor.apply();
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
                    myintent.setAction("com.mindsapp.test.action.NEW_RSSI_SCAN_ACTION");
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
            SharedPreferences sharedPreferences = getSharedPreferences(RSSI_COLLECTOR_PREFERENCES, Activity.MODE_PRIVATE);
            int NoT = sharedPreferences.getInt(NUMBER_OF_TESTS,0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(NUMBER_OF_TESTS,NoT+1);
            editor.apply();
            rssiManager.save();
            Intent intent = new Intent();
            intent.setAction("com.mindsapp.test.action.SCAN_FINISHED_RSSI_ACTION");;
            sendBroadcast(intent);
            Log.i("OK","OK");
        }
    }
}
