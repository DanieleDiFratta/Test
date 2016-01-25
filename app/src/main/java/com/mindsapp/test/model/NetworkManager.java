package com.mindsapp.test.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.ThresholdActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 10/12/2015.
 */
public class NetworkManager {
    private Map<String,WifiNetwork> networks;
    private Map<String,List<Integer>> RSSImap;
    private int negativeDifference;
    private int positiveDifference;
    private int nullDifference;

    public NetworkManager() {
        this.networks = new HashMap<>();
        this.RSSImap = new HashMap<>();
        this.negativeDifference = 0;
        this.positiveDifference = 0;
        this.nullDifference = 0;
    }


    public void elaborateResult(List<ScanResult> scanResults) {
        for (ScanResult result :
                scanResults) {
            if(networks.get(result.SSID)==null) {
                networks.put(result.SSID, new WifiNetwork(result));
                List<Integer> RSSIlist = new ArrayList<>();
                RSSIlist.add(result.level);
                RSSImap.put(result.SSID, RSSIlist);
            }
            else {
                networks.put(result.SSID, new WifiNetwork(result));
                List<Integer> RSSIlist = RSSImap.get(result.SSID);
                RSSIlist.add(result.level);
                RSSImap.put(result.SSID, RSSIlist);
            }
        }
    }

    private HashMap<WifiNetwork, Integer> calculateAritmeticMean() {
        HashMap<WifiNetwork,Integer> result = new HashMap<>();
        for (String SSID :
                RSSImap.keySet()) {
            Integer integer = auxArtMean(RSSImap.get(SSID));
            WifiNetwork wifiNetwork = networks.get(SSID);
            result.put(wifiNetwork,integer);
        }
        return result;
    }

    private int auxArtMean(List<Integer> integers) {
        int sum=0,i=0;
        for (int value :
                integers) {
            sum += value;
            i++;
        }
        return sum/i;
    }

    public HashMap<WifiNetwork, String> getResults() {
        HashMap<WifiNetwork,String> result = new HashMap<>();
        HashMap<WifiNetwork,Integer> artMap = calculateAritmeticMean();
        HashMap<WifiNetwork,Integer> wheMap = calculateWheightedMean();
        for (WifiNetwork network :
                artMap.keySet()) {
            calculateDifference(RSSImap.get(network.getSSID()));
            result.put(network,getPosition(wheMap.get(network) - artMap.get(network)));
        }
        return result;
    }

    private void calculateDifference(List<Integer> integers) {
        this.negativeDifference = 0;
        this.positiveDifference = 0;
        int previous = 0;
        for (int current:
             integers) {
            if(previous-current<0)
                this.positiveDifference++;
            else if(previous-current>0)
                this.negativeDifference++;
            else
                this.nullDifference++;
            previous = current;
        }
    }

    private String getPosition(int z) {
        SharedPreferences preferences = MainActivity.getContextofApplication().getSharedPreferences(ThresholdActivity.THRES_PREF, Activity.MODE_PRIVATE);
        int approachingThres = preferences.getInt(ThresholdActivity.PREF_APPROACHING, 2);
        int leavingThres = preferences.getInt(ThresholdActivity.PREF_LEAVING,2);
        if (z>=approachingThres && this.positiveDifference>this.negativeDifference)
            return "approaching";
        else
        if(z<=leavingThres && this.negativeDifference>this.positiveDifference)
            return "leaving";

        if((Math.abs(z)<=approachingThres && (Math.abs(this.positiveDifference-this.negativeDifference))<=this.nullDifference))
            return "chaotic motion";
        return "indeterminable";
    }

    private HashMap<WifiNetwork, Integer> calculateWheightedMean() {
        HashMap<WifiNetwork,Integer> result = new HashMap<>();
        for (String SSID :
                RSSImap.keySet()) {
            Integer integer = auxWeightedMean(RSSImap.get(SSID));
            WifiNetwork wifiNetwork = networks.get(SSID);
            result.put(wifiNetwork,integer);
        }
        return result;
    }

    private int auxWeightedMean(List<Integer> integers) {
        int i = 1;
        int sumRssi=0 , sumI=0;
        for (int value :
                integers) {
            sumRssi += value*i;
            sumI += i;
            i++;
        }
        return sumRssi/sumI;
    }
}
