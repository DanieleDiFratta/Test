package com.mindsapp.test.model;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniele on 17/01/2016.
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
            Threshold temp = new Threshold(values[0],Integer.parseInt(values[1]),Integer.parseInt(values[2]));
            thresholdList.add(temp);
        }
        reader.close();
    }

    public List<Threshold> getThresholdList() {
        return thresholdList;
    }
}
