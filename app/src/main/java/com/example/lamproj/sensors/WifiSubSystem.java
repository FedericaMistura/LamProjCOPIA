package com.example.lamproj.sensors;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;


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
        //Gestione del monitoraggio continuo del segnale
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                App.A.sensorHub.level_wifi= wifiInfo.getRssi(); //Ottenimento del livello di segnale
                if (isMetering) {
                    timerHandler.postDelayed(this, 500); //Esecuzione del metodo Run ogni 500 ms
                }
            }
        };
        timerHandler.post(timerRunnable);

    }

}
