package com.mindsapp.test;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.mindsapp.test.model.Threshold;
import com.mindsapp.test.model.ThresholdAdapter;
import com.mindsapp.test.model.ThresholdManager;
import com.mindsapp.test.model.WifiNetwork;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ThresholdActivity extends AppCompatActivity {


    private static final String FILE_PATH = "test.txt";
    public static final String THRES_PREF = "preferenceDefault";
    public static final String PREF_PLACE = "prefPlace";
    public static final String PREF_APPROACHING = "prefApproaching";
    public static final String PREF_LEAVING = "prefLeaving";
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
                SharedPreferences preferences = getSharedPreferences(THRES_PREF, Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREF_PLACE,threshold.getPlace());
                editor.putInt(PREF_APPROACHING, threshold.getApproachingThres());
                editor.putInt(PREF_LEAVING, threshold.getLeavingThres());
                editor.apply();
                Toast.makeText(getApplicationContext(),"Threshold selected.\nPlace: " + threshold.getPlace(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showThresholds(List<Threshold> thresholdList) {
        ThresholdAdapter adapter = new ThresholdAdapter(this,R.layout.rowcustom,thresholdList);
        lv.setAdapter(adapter);
    }
}
