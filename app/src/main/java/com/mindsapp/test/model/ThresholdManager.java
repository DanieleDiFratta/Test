package com.mindsapp.test.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Environment;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.ThresholdActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniele on 17/01/2016.
 * Manages all operations to calculate Threshold.
 */
public class ThresholdManager {

    private List<Threshold> thresholdList;

    public ThresholdManager(){
        this.thresholdList = new ArrayList<>();
    }

    public void loadThresholds(String filePath) throws IOException {
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        BufferedReader reader = new BufferedReader(new FileReader(sdcard + File.separator + filePath));
        String line;
        while ((line = reader.readLine())!=null){
            String values [] = line.split("\t");
            Threshold temp = new Threshold(values[0],Double.parseDouble(values[1]),Double.parseDouble(values[2]),Double.parseDouble(values[3]));
            thresholdList.add(temp);
        }
        reader.close();
    }

    public void setThreshold(Threshold threshold){
        if(ThresholdActivity.CurrentPlace!=null)
            if(!ThresholdActivity.CurrentPlace.equals(threshold.getPlace()))
                NetworkManager.resetStoredValues();
        ThresholdActivity.CurrentPlace = threshold.getPlace();
        SharedPreferences preferences = MainActivity.getContextofApplication().getSharedPreferences(ThresholdActivity.THRES_PREF, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(ThresholdActivity.PREF_PLACE,threshold.getPlace());
        putDouble(editor,ThresholdActivity.PREF_CHAOTIC, threshold.getChaoticThres());
        putDouble(editor,ThresholdActivity.PREF_APPROACHING, threshold.getApproachingThres());
        putDouble(editor,ThresholdActivity.PREF_LEAVING, threshold.getLeavingThres());
        editor.apply();
    }

    SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }


    public List<Threshold> getThresholdList() {
        return thresholdList;
    }
}
