package com.mindsapp.test.model;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 10/12/2015.
 */
public class ChannelManager {
    private Map<Integer,Channel> channels;

    public ChannelManager(){
        this.channels = new HashMap<>();
        initializeChannel();
    }

    private void initializeChannel() {
        channels.put(2412, new Channel(1, 2412));
        channels.put(2417, new Channel(2, 2417));
        channels.put(2422, new Channel(3, 2422));
        channels.put(2427, new Channel(4, 2427));
        channels.put(2432, new Channel(5, 2432));
        channels.put(2437, new Channel(6, 2437));
        channels.put(2442, new Channel(7, 2442));
        channels.put(2447, new Channel(8, 2447));
        channels.put(2452, new Channel(9, 2452));
        channels.put(2457, new Channel(10, 2457));
        channels.put(2462, new Channel(11, 2462));
        channels.put(2467, new Channel(12, 2467));
        channels.put(2472, new Channel(13, 2472));
    }

    private void addNetworksToChannel(List<ScanResult> results){
        for (ScanResult result:
             results) {
            Channel ch = channels.get(result.frequency);
            WifiNetwork network = new WifiNetwork(result.frequency,result.level,result.SSID);
            if(ch!=null)
                ch.addNetwork(network);
            network.setChannel(ch);
        }
    }

//    public Channel selectChannel(List<ScanResult> results) {
//        addNetworksToChannel(results);
//        OverlappingCalculator calculator = new OverlappingCalculator();
//        calculator.calculateOverlapping(this.channels.values());
//        return calculator.minorOverlapping(this.channels.values());
//    }

    public Collection<Channel> selectChannel(List<ScanResult> results) {
        List<Channel> channels = new ArrayList<>(this.channels.values());
        resetChannels(channels);
        addNetworksToChannel(results);
        OverlappingCalculator calculator = new OverlappingCalculator();
        calculator.calculateOverlapping(channels);
        Collections.sort(channels,new OverlappingComparator());
        return channels;
    }

    private void resetChannels(List<Channel> channels) {
        for (Channel ch:
             channels) {
            ch.deleteNetworks();
        }
    }
}
