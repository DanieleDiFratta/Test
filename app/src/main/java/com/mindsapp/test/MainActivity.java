package com.mindsapp.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mindsapp.test.model.Threshold;

public class MainActivity extends AppCompatActivity {

    private static Context applicationContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applicationContext = getApplicationContext();
        Button thresholdButton = (Button) findViewById(R.id.thresholdButton);
        thresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,ThresholdActivity.class);
                startActivity(intent);
            }
        });
        Button wifiButton = (Button) findViewById(R.id.wifiButton);
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (!wifi.isWifiEnabled()) {
                    Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
                    wifi.setWifiEnabled(true);
                } else {
                    Intent intent = new Intent(MainActivity.this, WifiActivity.class);
                    startActivity(intent);
                }
            }
        });
        Button channelButton = (Button) findViewById(R.id.channelButton);
        channelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChannelSelectionActivity.class);
                startActivity(intent);
            }
        });
        Button chartButton = (Button) findViewById(R.id.chartbutton);
        chartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return  true;
    }

    public static Context getContextofApplication() {
        return applicationContext;
    }
}
