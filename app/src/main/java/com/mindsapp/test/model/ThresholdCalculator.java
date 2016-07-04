package com.mindsapp.test.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.RSSICollectorActivity;
import com.mindsapp.test.utility.MathCalcultor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 22/05/2016.
 * Calculates Threshold giving RSSI values
 */
public class ThresholdCalculator {

    private static final double PFA = 0.0001;
    private List<Integer> testVariables;

    public ThresholdCalculator() {
        testVariables = new ArrayList<>();
    }

    public double calculateThres(Map<String, List<Integer>> rssiValues) throws Exception {
        double variance,mean,threshold;
        createTestValues(rssiValues);
        mean = MathCalcultor.calculateArtMean(this.testVariables);
        variance = MathCalcultor.calculateVariance(this.testVariables,mean);
        threshold = mean + (2.0*Math.sqrt(variance)) * MathCalcultor.invErf(1.0 - 2.0 * PFA);
        return threshold;
    }

    public void createTestValues(Map<String, List<Integer>> rssiValues) {
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
        int artMean = MathCalcultor.calculateArtMean(partialValues);
        int wheMean = MathCalcultor.calculateWheMean(partialValues);
        this.testVariables.add(wheMean-artMean);
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
