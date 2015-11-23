package com.mindsapp.test.model;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Daniele on 16/11/2015.
 */
public class WifiPullService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WifiPullService() {
        super("WifiPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
