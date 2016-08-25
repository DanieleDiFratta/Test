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
    double chaoticThres;
    double approachingThres;
    double leavingThres;

    public Threshold(String place, double chaoticThres, double approachingThres, double leavingThres) {
        this.place = place;
        this.chaoticThres = chaoticThres;
        this.approachingThres = approachingThres;
        this.leavingThres = leavingThres;
    }

    public String getPlace() {
        return place;
    }

    public double getApproachingThres() {
        return approachingThres;
    }

    public double getLeavingThres() {
        return leavingThres;
    }

    public double getChaoticThres() {
        return chaoticThres;
    }
}
