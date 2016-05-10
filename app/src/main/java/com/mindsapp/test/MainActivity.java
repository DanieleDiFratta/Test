package com.mindsapp.test;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.Threshold;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static Context applicationContext;
    private WifiManager wifi;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        applicationContext = getApplicationContext();

        //pemissions
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},20);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},10);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        }
        //end permission+
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if(!wifi.isWifiEnabled()) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
            adBuilder.setTitle("Wifi Disabled");
            adBuilder
                    .setMessage("Do you want to enable WIFI ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //enable wifi
                            wifi.setWifiEnabled(true);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            alertDialog = adBuilder.create();
            alertDialog.show();
        }
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder adBuilder = new AlertDialog.Builder(MainActivity.this);
            adBuilder.setTitle("GPS is Disabled");
            adBuilder
                    .setMessage("Do you want to enable GPS ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //enable GPS
                            Intent gpsSettintIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(gpsSettintIntent);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            alertDialog = adBuilder.create();
            alertDialog.show();
        }
        Button thresholdButton = (Button) findViewById(R.id.thresholdButton);
        thresholdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ThresholdActivity.class);
                startActivity(intent);
            }
        });
        Button wifiButton = (Button) findViewById(R.id.wifiButton);
        wifiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WifiActivity.class);
                startActivity(intent);
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
                Intent intent = new Intent(MainActivity.this, ChartsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public static Context getContextofApplication() {
        return applicationContext;
    }
}
