package com.mindsapp.test.model;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Daniele on 10/12/2015.
 */
public class Scanner {
    private ChannelManager channelManager;
    private NetworkManager networkManager;

    public Scanner() {
        this.channelManager = new ChannelManager();
        this.networkManager = new NetworkManager();
    }

    public Collection<Channel> selectChannel(List<ScanResult> results){
       return channelManager.selectChannel(results);
    }

}
