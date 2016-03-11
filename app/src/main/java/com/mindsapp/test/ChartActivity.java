package com.mindsapp.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.List;
import java.util.Random;

public class ChartActivity extends AppCompatActivity {

    private BroadcastReceiver br;
    private LineChart chart;
    private WifiManager manager;
    private int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        chart = (LineChart) findViewById(R.id.lineChart);
        LineData data = new LineData();
        chart.setData(data);
        manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                addEntry();
                manager.startScan();
            }
        };
        manager.startScan();
    }

    private void addEntry() {
        LineData data = chart.getData();

        for (ScanResult result :
                manager.getScanResults()) {
            ILineDataSet set = data.getDataSetByLabel(result.SSID,true);
            if(set==null){
                set = createSet(result.SSID);
                data.addDataSet(set);
            }
            data.addEntry(new Entry(result.level, set.getEntryCount()), data.getIndexOfDataSet(set));
        }

        data.addXValue("");

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        // limit the number of visible entries
        chart.setVisibleXRangeMaximum(10);

        // move to the latest entry
        chart.moveViewToX(data.getXValCount() - 11);

    }

    private ILineDataSet createSet(String SSID) {
        Random rand = new Random();
        LineDataSet set = new LineDataSet(null, SSID);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.rgb(rand.nextInt(),rand.nextInt(),rand.nextInt()));
        set.setCircleColor(Color.BLACK);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.BLACK);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(br,new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(br);
    }
}
