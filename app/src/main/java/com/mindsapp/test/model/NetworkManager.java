package com.mindsapp.test.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.util.Log;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.ThresholdActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
    public static final String NETWORKS_MAP = "networks map";
    private Map<String,WifiNetwork> BSSIDtoWifiNetwork;
    private Map<String,List<Integer>> BSSIDtoRSSI;

    public NetworkManager() {
        try {
            this.BSSIDtoWifiNetwork = loadNetworks();
        } catch (IOException e) {
            BSSIDtoWifiNetwork = new HashMap<>();
        } catch (ClassNotFoundException e) {
            BSSIDtoWifiNetwork = new HashMap<>();
        }
        this.BSSIDtoRSSI = loadMap();
        if(BSSIDtoRSSI ==null)
            BSSIDtoRSSI = new HashMap<>();
    }

    private Map<String, WifiNetwork> loadNetworks() throws IOException, ClassNotFoundException {
        File file = new File(MainActivity.getContextofApplication().getDir("data", Activity.MODE_PRIVATE), "map");
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        HashMap<String,WifiNetwork> BSSIDtoWifiNetwork = (HashMap<String, WifiNetwork>) inputStream.readObject();
        return BSSIDtoWifiNetwork;
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
        editor.remove(RSSI_MAP);
        editor.apply();
        File file = new File(MainActivity.getContextofApplication().getDir("data", Activity.MODE_PRIVATE), "map");
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(file));
            outputStream.reset();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void elaborateResult(List<ScanResult> scanResults) {
        for (ScanResult result :
                scanResults) {
            if(BSSIDtoWifiNetwork.get(result.BSSID)==null) {
                BSSIDtoWifiNetwork.put(result.BSSID, new WifiNetwork(result));
                List<Integer> RSSIlist = new ArrayList<>();
                RSSIlist.add(result.level);
                BSSIDtoRSSI.put(result.BSSID, RSSIlist);
            }
            else {
                List<Integer> RSSIlist = BSSIDtoRSSI.get(result.BSSID);
                RSSIlist.add(result.level);
                BSSIDtoRSSI.put(result.BSSID, RSSIlist);
            }
        }
    }

    private HashMap<WifiNetwork, Double> calculateAritmeticMean() {
        HashMap<WifiNetwork,Double> result = new HashMap<>();
        for (String BSSID :
                BSSIDtoRSSI.keySet()) {
            Double dbl = auxArtMean(BSSIDtoRSSI.get(BSSID));
            WifiNetwork wifiNetwork = BSSIDtoWifiNetwork.get(BSSID);
            result.put(wifiNetwork,dbl);
        }
        return result;
    }

    private double auxArtMean(List<Integer> integers) {
        double sum=0;
        int i=0;
        for (int value :
                integers) {
            sum += value;
            i++;
        }
        return sum/i;
    }

    public HashMap<WifiNetwork, String> getResults() {
        HashMap<WifiNetwork,String> result = new HashMap<>();
        HashMap<WifiNetwork,Double> artMap = calculateAritmeticMean();
        HashMap<WifiNetwork,Double> wheMap = calculateWheightedMean();
        for (WifiNetwork network :
                artMap.keySet()) {
            result.put(network,getPosition(Math.abs(wheMap.get(network)) - Math.abs(artMap.get(network))));
        }
        return result;
    }

    private String getPosition(double z) {
        Context applicationContext = MainActivity.getContextofApplication();
        SharedPreferences sharedPreferences = applicationContext.getSharedPreferences(ThresholdActivity.THRES_PREF,Activity.MODE_PRIVATE);
        double threshold = getDouble(sharedPreferences,ThresholdActivity.PREF_CHAOTIC,2);
        if(Math.abs(z)>threshold)
            return "regular motion";
        else
            return "chaotic motion";
    }

    private HashMap<WifiNetwork, Double> calculateWheightedMean() {
        HashMap<WifiNetwork,Double> result = new HashMap<>();
        for (String BSSID :
                BSSIDtoRSSI.keySet()) {
            Double aDouble = auxWeightedMean(BSSIDtoRSSI.get(BSSID));
            WifiNetwork wifiNetwork = BSSIDtoWifiNetwork.get(BSSID);
            result.put(wifiNetwork,aDouble);
        }
        return result;
    }

    private double auxWeightedMean(List<Integer> integers) {
        int i = 1;
        double sumRssi=0 , sumI=0;
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
        try {
            saveNetworks();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObject = new JSONObject(this.BSSIDtoRSSI);
        String jsonString = jsonObject.toString();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(RSSI_MAP, jsonString);
        editor.commit();
    }

    private void saveNetworks() throws IOException {
        File file = new File(MainActivity.getContextofApplication().getDir("data", Activity.MODE_PRIVATE), "map");
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(this.BSSIDtoWifiNetwork);
        outputStream.flush();
        outputStream.close();
    }

    public void updateRSSIMap() {
        for (String BSSID:
             BSSIDtoRSSI.keySet()) {
            List<Integer> RSSIvalues = BSSIDtoRSSI.get(BSSID);
            if(RSSIvalues.size()>NUM_STORED_VALUES)
                RSSIvalues.subList(RSSIvalues.size()-NUM_STORED_VALUES,RSSIvalues.size());
            BSSIDtoRSSI.put(BSSID,RSSIvalues);
        }
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        if ( !prefs.contains(key))
            return defaultValue;

        return Double.longBitsToDouble(prefs.getLong(key, 0));
    }

    public Map<String, WifiNetwork> getBSSIDtoWifiNetwork() {
        return BSSIDtoWifiNetwork;
    }

    public Map<String, List<Integer>> getBSSIDtoRSSI() {
        return BSSIDtoRSSI;
    }

    public HashMap<WifiNetwork, Integer> getNumRSSI() {
        HashMap<WifiNetwork,Integer> result = new HashMap<>();
        for (String BSSID :
                BSSIDtoRSSI.keySet()) {
            result.put(BSSIDtoWifiNetwork.get(BSSID),BSSIDtoRSSI.get(BSSID).size());
        }
        return result;
    }
}
