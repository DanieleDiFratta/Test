package com.mindsapp.test.model;

import android.net.wifi.ScanResult;

import com.mindsapp.test.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 18/05/2016.
 *
 * Manages all the oparation for collecting RSSI data
 */
public class RSSIManager {

    public static final String FILE_PATH = "RSSI.txt";
    private Map<String,List<Integer>> RSSIvalues;

    public RSSIManager(){
        this.RSSIvalues = this.load();
    }

    private Map<String, List<Integer>> load() {
        Map<String,List<Integer>> outputMap = new HashMap<>();
        try {
            FileInputStream inputStream = new FileInputStream(MainActivity.getContextofApplication().getFilesDir()+File.separator+FILE_PATH);
            StringBuilder builder = new StringBuilder();
            int ch;
            while((ch = inputStream.read()) != -1){
                builder.append((char)ch);
            }
            String jsonString = builder.toString();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    public void elaborateResult(List<ScanResult> scanResults) {
        for (ScanResult result :
                scanResults) {
            if(RSSIvalues.get(result.SSID)!=null){
                List<Integer> RSSI = RSSIvalues.get(result.SSID);
                RSSI.add(result.level);
                RSSIvalues.put(result.SSID,RSSI);
            }
            else{
                List<Integer> RSSI = new ArrayList<>();
                RSSI.add(result.level);
                RSSIvalues.put(result.SSID,RSSI);
            }
        }
    }

    public void save() {
        JSONObject jsonObject = new JSONObject(this.RSSIvalues);
        String jsonString = jsonObject.toString();
        File file = new File(MainActivity.getContextofApplication().getFilesDir(), FILE_PATH);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public double calculateThres() {
        ThresholdCalculator thresholdCalculator = new ThresholdCalculator();
        double threshold = 0;
        try {
            threshold = thresholdCalculator.calculateThres(RSSIvalues);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return threshold;
    }

    public static boolean resetStoredValues() {
        File file = new File(MainActivity.getContextofApplication().getFilesDir(), FILE_PATH);
        return file.delete();
    }
}
