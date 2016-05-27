package com.mindsapp.test.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.ThresholdActivity;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 10/12/2015.
 * Manages all the operation for the calculating where the network is.
 */
public class NetworkManager {
    public static final String NETWORK_PREF = "network prefereces";
    public static final String RSSI_MAP = "RSSI map";
    private static final int NUM_STORED_VALUES = 50;
    private Map<String,WifiNetwork> networks;
    private Map<String,List<Integer>> RSSImap;
    private int negativeDifference;
    private int positiveDifference;
    private int nullDifference;

    public NetworkManager() {
        this.networks = new HashMap<>();
        this.RSSImap = loadMap();
        if(RSSImap==null)
            RSSImap = new HashMap<>();
        this.negativeDifference = 0;
        this.positiveDifference = 0;
        this.nullDifference = 0;
    }

    public static Map<String, List<Integer>> loadMap() {
        Map<String,List<Integer>> outputMap = new HashMap<>();
        SharedPreferences pSharedPref = MainActivity.getContextofApplication().getSharedPreferences(NETWORK_PREF, Activity.MODE_PRIVATE);
        try{
            if (pSharedPref != null){
                String jsonString = pSharedPref.getString(RSSI_MAP, (new JSONObject()).toString());
                JSONObject jsonObject = new JSONObject(jsonString);
                Iterator<String> keysItr = jsonObject.keys();
                while(keysItr.hasNext()) {
                    String key = keysItr.next();
                    JSONArray jlist = jsonObject.getJSONArray(key);
                    List<Integer> list = new ArrayList<>();
                    for(int i=0; i < jlist.length(); i++){
                        list.add(jlist.getInt(i));
                    }
                    outputMap.put(key, list);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return outputMap;
    }

    public static void resetStoredValues() {
        SharedPreferences preferences = MainActivity.getContextofApplication().getSharedPreferences(NetworkManager.NETWORK_PREF, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(NetworkManager.RSSI_MAP);
        editor.commit();
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
        Log.i("z = ", String.valueOf(z));
        SharedPreferences preferences = MainActivity.getContextofApplication().getSharedPreferences(ThresholdActivity.THRES_PREF, Activity.MODE_PRIVATE);
        int approachingThres = preferences.getInt(ThresholdActivity.PREF_APPROACHING, 2);
        int leavingThres = preferences.getInt(ThresholdActivity.PREF_LEAVING,2);

        if((Math.abs(z)<=approachingThres && (Math.abs(this.positiveDifference-this.negativeDifference))<=this.nullDifference))
            return "chaotic motion";
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

    public void saveRSSI() {
        SharedPreferences pref = MainActivity.getContextofApplication().getSharedPreferences(NETWORK_PREF,Activity.MODE_PRIVATE);
        JSONObject jsonObject = new JSONObject(this.RSSImap);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RSSI_MAP, jsonString);
        editor.commit();
    }

    public void updateRSSIMap() {
        for (String SSID:
             RSSImap.keySet()) {
            List<Integer> RSSIvalues = RSSImap.get(SSID);
            if(RSSIvalues.size()>NUM_STORED_VALUES)
                RSSIvalues.subList(RSSIvalues.size()-NUM_STORED_VALUES,RSSIvalues.size());
            RSSImap.put(SSID,RSSIvalues);
        }
    }
}
