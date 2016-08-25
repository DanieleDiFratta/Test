package com.mindsapp.test.model;

import android.net.wifi.ScanResult;
import android.os.Environment;
import android.util.Log;

import com.mindsapp.test.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 18/05/2016.
 *
 * Manages all the operation for collecting RSSI data
 */
public class RSSIManager {

    public static final String FILE_PATH = "RSSI.txt";
    public static final String BSSID_PATH = "BSSID.txt";
    private Map<String,String> BSSIDtoSSID;
    private Map<String,List<Integer>> RSSIvalues;
    private static String filePath = "Valori Rssi.txt";

    public RSSIManager(){
        this.RSSIvalues = this.loadRSSIvalues();
        Log.i("Num valori", String.valueOf(RSSIvalues.get("04:62:73:48:a6:f0").size()));
        this.BSSIDtoSSID = this.loadBSSIDvalues();
    }

    private Map<String, String> loadBSSIDvalues() {
        Map<String,String> outputMap = new HashMap<>();
        try {
            FileInputStream inputStream = new FileInputStream(MainActivity.getContextofApplication().getFilesDir()+File.separator+BSSID_PATH);
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
                String value = jsonObject.getString(key);
                outputMap.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputMap;
    }

    private Map<String, List<Integer>> loadRSSIvalues(){
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        Map<String,List<Integer>> outputMap = new HashMap<>();
        try {
            FileInputStream inputStream = new FileInputStream(sdcard+File.separator+filePath);
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
                String string = jsonObject.getString(key);
                JSONArray jlist = new JSONArray(string);
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

    /*private Map<String, List<Integer>> loadRSSIvalues() {
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
    }*/

    public void elaborateResult(List<ScanResult> scanResults) {
        for (ScanResult result :
                scanResults) {
            if(RSSIvalues.get(result.BSSID)!=null){
                List<Integer> RSSI = RSSIvalues.get(result.BSSID);
                RSSI.add(result.level);
                RSSIvalues.put(result.BSSID,RSSI);
            }
            else{
                List<Integer> RSSI = new ArrayList<>();
                RSSI.add(result.level);
                RSSIvalues.put(result.BSSID,RSSI);
                BSSIDtoSSID.put(result.BSSID,result.SSID);
            }
        }
    }

    public void save() {
        JSONObject jsonObject = new JSONObject(this.RSSIvalues);
        String jsonString = jsonObject.toString();
        File file = new File(MainActivity.getContextofApplication().getFilesDir(), FILE_PATH);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        try {
            saveInExternalStorage(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveBSSIDmap();
    }

    private void saveBSSIDmap() {
        JSONObject jsonObject = new JSONObject(this.BSSIDtoSSID);
        String jsonString = jsonObject.toString();
        File file = new File(MainActivity.getContextofApplication().getFilesDir(), BSSID_PATH);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,false);
            fileOutputStream.write(jsonString.getBytes());
            fileOutputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void saveInExternalStorage(String jsonString) throws IOException {
        FileWriter writer;
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = sdcard + File.separator + "Valori Rssi.txt";
        writer = new FileWriter(file,false);
        writer.write(jsonString);
        writer.flush();
        writer.close();
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
        File file2 = new File(MainActivity.getContextofApplication().getFilesDir(),BSSID_PATH);
        File file3 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),filePath);
        file3.delete();
        file2.delete();
        return file.delete();
    }
}
