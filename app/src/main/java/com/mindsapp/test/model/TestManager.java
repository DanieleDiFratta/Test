package com.mindsapp.test.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.os.Environment;

import com.mindsapp.test.MainActivity;
import com.mindsapp.test.ThresholdActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by danal on 24/06/2016.
 */
public class TestManager {
    private NetworkManager networkManager;
    private static final double PFA = 0.0001;
    private int negativeDifference;
    private int positiveDifference;
    private int nullDifference;

    public TestManager() {
        this.networkManager = new NetworkManager();
    }

    public void elaborateResult(List<ScanResult> scanResults) {
        networkManager.elaborateResult(scanResults);
    }

    public void updateRSSIMap() {
        networkManager.updateRSSIMap();
    }

    public void saveRSSI() {
        networkManager.saveRSSI();
    }

    public HashMap<WifiNetwork, String> getOldResults() {
        return networkManager.getResults();
    }

    public HashMap<WifiNetwork, String> getNewResults() throws Exception {
        HashMap<WifiNetwork,String> newTestMap = new HashMap<>();
        Map<String,List<Integer>> BSSIDtoRSSI = networkManager.getBSSIDtoRSSI();
        Map<String,WifiNetwork> BSSIDtoWifiNetwork = networkManager.getBSSIDtoWifiNetwork();
        for (String BSSID :
                BSSIDtoWifiNetwork.keySet()) {
            calculateDifference(BSSIDtoRSSI.get(BSSID));
            double mean = calculateMean(BSSIDtoRSSI.get(BSSID));
            double variance = calculateVariance(BSSIDtoRSSI.get(BSSID),mean);
            double z = (variance/Math.pow(mean,2));
            String position = getPosition(z);
            newTestMap.put(BSSIDtoWifiNetwork.get(BSSID),position);
        }
        savePositions(newTestMap);
        saveRssiValues();
        return newTestMap;
    }

    private void saveRssiValues() {
        FileWriter writer;
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = sdcard + File.separator + "RSSI Values.txt";
        try {
            writer = new FileWriter(file, true);
            writer.write(networkManager.getBSSIDtoRSSI().toString());
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePositions(HashMap<WifiNetwork, String> newTestMap) {
        HashMap<String,String> positions = new HashMap<>();
        for (WifiNetwork network :
                newTestMap.keySet()) {
            if (network.getBSSID().contains("04:62:73:48:a6:")) {
                positions.put(network.getSSID(),newTestMap.get(network));
            }
            }
        JSONObject jsonObject = new JSONObject(positions);
        String jsonString = jsonObject.toString();
        FileWriter writer;
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
        String file = sdcard + File.separator + "posizioni.txt";
        try {
            writer = new FileWriter(file, true);
            writer.write(jsonString);
            writer.flush();
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
       }
    }

    private String getPosition(double z) {
        //TODO
        SharedPreferences preferences = MainActivity.getContextofApplication().getSharedPreferences(ThresholdActivity.THRES_PREF, Activity.MODE_PRIVATE);
        double approachingThres = NetworkManager.getDouble(preferences,ThresholdActivity.PREF_APPROACHING, 2);
        double leavingThres = NetworkManager.getDouble(preferences,ThresholdActivity.PREF_LEAVING,2);

        if(z<=0.02)
            return "chaotic motion";
        else
            return "regular motion";
    }

    private double calculateVariance(List<Integer> values, double mean) {
        double variance = 0;
        int numElements = values.size();

        for(double d : values)
            variance += Math.pow(d - mean, 2);

        return variance / (double)numElements;
    }

    private double calculateMean(List<Integer> values) {
        double sum = 0;
        for (Integer rssi :
                values) {
            sum += rssi;
        }
        return sum/values.size();
    }

    public static double invErf(double d) throws Exception {

        if (Math.abs(d)>1) {

            throw new Exception ("Allowed values for argument in [-1,1]");

        }

        if (Math.abs(d) == 1) {

            return (d==-1 ? Double.NEGATIVE_INFINITY :

                    Double.POSITIVE_INFINITY);

        }

        else {

            if (d==0) {

                return 0;

            }

            BigDecimal bd = new BigDecimal(0, MathContext.UNLIMITED);

            BigDecimal x = new

                    BigDecimal(d*Math.sqrt(Math.PI)/2,MathContext.UNLIMITED);

            //System.out.println(x);

            String[] A092676 = {"1", "1", "7", "127", "4369", "34807",

                    "20036983", "2280356863",

                    "49020204823", "65967241200001",

                    "15773461423793767",

                    "655889589032992201",

                    "94020690191035873697", "655782249799531714375489",

                    "44737200694996264619809969",

                    "10129509912509255673830968079", "108026349476762041127839800617281",

                    "10954814567103825758202995557819063",

                    "61154674195324330125295778531172438727",

                    "54441029530574028687402753586278549396607",

                    "452015832786609665624579410056180824562551",

                    "2551405765475004343830620568825540664310892263",



                    "70358041406630998834159902148730577164631303295543",

                    "775752883029173334450858052496704319194646607263417",



                    "132034545522738294934559794712527229683368402215775110881"};



            String[] A092677 = {"1", "3", "30", "630", "22680", "178200",

                    "97297200", "10216206000",

                    "198486288000", "237588086736000",

                    "49893498214560000",

                    "1803293578326240000",

                    "222759794969712000000","1329207696584271504000000",

                    "77094046401887747232000000",

                    "14761242414008506896480000000", "132496911908140357902804480000000",

                    "11262237512191930421738380800000000",

                    "52504551281838779626144331289600000000",

                    "38905872499842535702972949485593600000000",

                    "268090886133368733415443853598208000000000",

                    "1252532276140582782027102181569679872000000000",

                    "28520159927721069946757116674341610685440000000000",



                    "259078091444256105986928093487086396226560000000000",

                    "36256424429074976496234665114956818633529712640000000000"};



            for (int i = 0; i < A092676.length; i++) {

                BigDecimal num = new BigDecimal(new BigInteger(A092676[i]),

                        50);

                BigDecimal den = new BigDecimal(new BigInteger(A092677[i]),

                        50);

                BigDecimal coeff = num.divide(den, RoundingMode.HALF_UP);

                //System.out.println(coeff);

                BigDecimal xBD = x.pow(i*2+1, MathContext.UNLIMITED);



                bd = bd.add(xBD.multiply(coeff, MathContext.UNLIMITED));



            }

            return bd.doubleValue();

        }

    }

    private void calculateDifference(List<Integer> integers) {
        this.negativeDifference = 0;
        this.positiveDifference = 0;
        this.nullDifference = 0;
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

    public HashMap<WifiNetwork, String> getInvertedResults() throws Exception {
        HashMap<WifiNetwork,String> invertedTestMap = new HashMap<>();
        Map<String,List<Integer>> BSSIDtoRSSI = networkManager.getBSSIDtoRSSI();
        Map<String,WifiNetwork> BSSIDtoWifiNetwork = networkManager.getBSSIDtoWifiNetwork();
        for (String BSSID :
                BSSIDtoWifiNetwork.keySet()) {
            calculateDifference(BSSIDtoRSSI.get(BSSID));
            double mean = calculateMean(BSSIDtoRSSI.get(BSSID));
            double variance = calculateVariance(BSSIDtoRSSI.get(BSSID),mean);
            double z = (Math.pow(mean,2)/variance)*invErf(1.0 - 2.0*PFA);
            String position = getPosition(z);
            invertedTestMap.put(BSSIDtoWifiNetwork.get(BSSID),position);
        }
        return invertedTestMap;
    }

    public HashMap<WifiNetwork, Double> getNewTestVariables() throws Exception {
        HashMap<WifiNetwork,Double> newTestVariables = new HashMap<>();
        Map<String,List<Integer>> BSSIDtoRSSI = networkManager.getBSSIDtoRSSI();
        Map<String,WifiNetwork> BSSIDtoWifiNetwork = networkManager.getBSSIDtoWifiNetwork();
        for (String BSSID :
                BSSIDtoWifiNetwork.keySet()) {
            double mean = calculateMean(BSSIDtoRSSI.get(BSSID));
            double variance = calculateVariance(BSSIDtoRSSI.get(BSSID),mean);
            double z = ((variance/Math.pow(mean,2)));
            newTestVariables.put(BSSIDtoWifiNetwork.get(BSSID),z);
        }
        return newTestVariables;
    }

    public HashMap<WifiNetwork, Double> getOldTestVariables() {
        HashMap<WifiNetwork,Double> oldTestVariables = new HashMap<>();
        Map<String,List<Integer>> BSSIDtoRSSI = networkManager.getBSSIDtoRSSI();
        Map<String,WifiNetwork> BSSIDtoWifiNetwork = networkManager.getBSSIDtoWifiNetwork();
        for (String BSSID :
                BSSIDtoWifiNetwork.keySet()) {
            double artMean = calculateMean(BSSIDtoRSSI.get(BSSID));
            double wheMean= calculateWheMean(BSSIDtoRSSI.get(BSSID));
            double z = wheMean - artMean;
            oldTestVariables.put(BSSIDtoWifiNetwork.get(BSSID),z);
        }
        return oldTestVariables;
    }

    private int calculateWheMean(List<Integer> integers) {
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

    public HashMap<WifiNetwork, Integer> getNumRSSI() {
        return this.networkManager.getNumRSSI();
    }
}
