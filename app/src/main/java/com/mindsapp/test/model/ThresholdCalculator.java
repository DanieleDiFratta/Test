package com.mindsapp.test.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.RSSICollectorActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by danal on 22/05/2016.
 */
public class ThresholdCalculator {

    private List<Integer> testVariables;

    public ThresholdCalculator() {
        testVariables = new ArrayList<>();
    }

    public void calculateThres(Map<String, List<Integer>> rssiValues) {
        List<Integer> maxValues = getMaxValues(rssiValues);
        List<Integer> partialValues = new ArrayList<>();
        int count = 0;
        int testVariablesCount = maxValues.size() / MainActivity.getContextofApplication()
                .getSharedPreferences(RSSICollectorActivity.RSSI_COLLECTOR_PREFERENCES, Activity.MODE_PRIVATE)
                .getInt(RSSICollectorActivity.NUMBER_OF_TESTS, 0);
        for (Integer rssi :
                maxValues) {
            if(count<testVariablesCount){
                partialValues.add(rssi);
                count++;
            }
            else{
                calculateTestVariable(partialValues);
                partialValues.clear();
                partialValues.add(rssi);
                count = 1;
            }
        }
        //calculateTestVariable(partialValues);
    }

    private void calculateTestVariable(List<Integer> partialValues) {

    }

    private List<Integer> getMaxValues(Map<String, List<Integer>> rssiValues) {
        int max = 0;
        List<Integer> result = null;
        for (List<Integer> list :
                rssiValues.values()) {
            max = list.size() > max ? list.size() : max;
            result = list;
        }
        return result;
    }
}
