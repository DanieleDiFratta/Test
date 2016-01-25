package com.mindsapp.test.model;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by Daniele on 16/01/2016.
 */
public class Threshold {

    String place;
    int approachingThres;
    int leavingThres;

    public Threshold(String place, int approachingThres, int leavingThres) {
        this.place = place;
        this.approachingThres = approachingThres;
        this.leavingThres = leavingThres;
    }

    public String getPlace() {
        return place;
    }

    public int getApproachingThres() {
        return approachingThres;
    }

    public int getLeavingThres() {
        return leavingThres;
    }
}
