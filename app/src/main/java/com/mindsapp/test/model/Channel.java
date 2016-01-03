package com.mindsapp.test.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Daniele on 08/12/2015.
 */
public class Channel {
    private int id;
    private int frequency;
    private int overlapping;
    private List<WifiNetwork> networks;

    public Channel(int id, int frequency) {
        this.frequency = frequency;
        this.id = id;
        this.networks = new ArrayList<>();
    }

    public Channel() {

    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public int getOverlapping() {
        return overlapping;
    }

    public void setOverlapping(int overlapping) {
        this.overlapping = overlapping;
    }

    public List<WifiNetwork> getNetworks() {
        return networks;
    }

    public void addNetwork(WifiNetwork network) {
        this.networks.add(network);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void deleteNetworks() {
        networks = new ArrayList<>();
    }
}
