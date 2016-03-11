package com.mindsapp.test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.mindsapp.test.model.Channel;
import com.mindsapp.test.model.Scanner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ChannelSelectionActivity extends AppCompatActivity {

    private ListView lv;
    private BroadcastReceiver broadcastReceiver;
    private WifiManager wifiManager;
    private Scanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_selection);
        lv = (ListView) findViewById(R.id.listView2);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanner = new Scanner();
        wifiManager.startScan();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showResult(scanner.selectChannel(wifiManager.getScanResults()));
                wifiManager.startScan();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    //    private void showResult(Channel bestChannel) {
//        ArrayList<String> channelList = new ArrayList<>();
//        String info = "Id: " + bestChannel.getId() + "\n" +
//                "Frequency: " + bestChannel.getFrequency() + "\n" +
//                "Overlapping: " + bestChannel.getOverlapping() + "\n" +
//                "Networks List: " + bestChannel.getNetworks();
//        channelList.add(info);
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,channelList);
//        lv.setAdapter(adapter);
//    }

    private void showResult(Collection<Channel> bestChannel) {
        ArrayList<String> channelList = new ArrayList<>();
        for (Channel channel:
             bestChannel) {
            String info = "Id: " + channel.getId() + "\n" +
                    "Frequency: " + channel.getFrequency() + "\n" +
                    "Overlapping: " + channel.getOverlapping() + "\n";
            if(channel.getNetworks().equals(Collections.EMPTY_LIST))
                info += "No networks on this channel.";
            else
                info += "Networks List: " + channel.getNetworks();
            channelList.add(info);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,channelList);
        lv.setAdapter(adapter);
    }
}
