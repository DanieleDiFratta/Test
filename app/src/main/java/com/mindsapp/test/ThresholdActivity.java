package com.mindsapp.test;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mindsapp.test.model.NetworkManager;
import com.mindsapp.test.model.Threshold;
import com.mindsapp.test.model.ThresholdAdapter;
import com.mindsapp.test.model.ThresholdManager;

import java.io.IOException;
import java.util.List;

public class ThresholdActivity extends AppCompatActivity {


    public static final String FILE_PATH = "test.txt";
    public static final String THRES_PREF = "preferenceDefault";
    public static final String PREF_PLACE = "prefPlace";
    public static final String PREF_APPROACHING = "prefApproaching";
    public static final String PREF_LEAVING = "prefLeaving";
    public static String CurrentPlace;
    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_threshold);
        lv = (ListView) findViewById(R.id.listView3);
        ThresholdManager thresholdManager = new ThresholdManager();
        try {
            thresholdManager.loadThresholds(FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        showThresholds(thresholdManager.getThresholdList());
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Threshold threshold = (Threshold) parent.getItemAtPosition(position);
                setThreshold(threshold);
                Toast.makeText(getApplicationContext(),"Threshold selected.\nPlace: " + threshold.getPlace(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setThreshold(Threshold threshold) {
        ThresholdManager manager = new ThresholdManager();
        manager.setThreshold(threshold);
    }

    private void showThresholds(List<Threshold> thresholdList) {
        ThresholdAdapter adapter = new ThresholdAdapter(this,R.layout.rowcustom,thresholdList);
        lv.setAdapter(adapter);
    }
}
