package com.example.lamproj.sensors;

import android.content.Context;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;

import com.example.lamproj.App;

public class WifiSubSystem {
    private WifiManager wifiManager;
    private Handler timerHandler;
    private Runnable timerRunnable;
    boolean isMetering=false;

    public void stopMetering(){
        isMetering=false;
        timerHandler.removeCallbacks(timerRunnable);
    }
    public void startMetering() {
        wifiManager = (WifiManager) App.A.context.getSystemService(Context.WIFI_SERVICE);
        isMetering = true;
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                App.A.sensorHub.level_wifi= wifiInfo.getRssi();
                if (isMetering) {
                    timerHandler.postDelayed(this, 500);
                }
            }
        };
        timerHandler.post(timerRunnable);

    }

}
