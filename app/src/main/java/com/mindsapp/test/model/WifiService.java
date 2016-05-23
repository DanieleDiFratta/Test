package com.mindsapp.test.model;

import android.app.IntentService;
import android.content.Intent;

import com.mindsapp.test.WifiActivity;

/**
 * Created by Daniele on 18/12/2015.
 */
public class WifiService extends IntentService {

    public static final String INTENT_ACTION = "com.mindsapp.test.android.receiver.intent.action";

    private NetworkManager networkManager;

    public WifiService() {
        super("RSSIService");
        this.networkManager = new NetworkManager();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent intent = new Intent();
        intent.setAction(INTENT_ACTION);
        sendBroadcast(intent);
    }
}
