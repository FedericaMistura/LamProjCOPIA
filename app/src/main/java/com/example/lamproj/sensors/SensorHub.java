package com.example.lamproj.sensors;

import android.view.View;

import com.example.lamproj.App;
import com.example.lamproj.R;
import com.example.lamproj.data.Sample;
import com.google.android.material.snackbar.Snackbar;

public class SensorHub {
    public double level_lte = 0;
    public double level_noise = 0;
    public double level_wifi = 0;

    private AudioSubSystem audioSubSystem;
    private WifiSubSystem wifiSubSystem;
    private LteSubSystem lteSubSystem;


    public SensorHub() {
        audioSubSystem=new AudioSubSystem();
        wifiSubSystem=new WifiSubSystem();
        lteSubSystem=new LteSubSystem();
    }
    /*
    Questo salva sul database
     */
    public void recordNewSample(){
        Sample s = new Sample();

        s.latitude = App.A.mapManager.getCurrentLocation().getLatitude();
        s.longitude = App.A.mapManager.getCurrentLocation().getLongitude();
        s.time= System.currentTimeMillis();
        s.lte=level_lte;
        s.wifi=level_wifi;
        s.noise = level_noise;

        // salviamo il sample nel db per renderlo persistente
        App.A.db.putSample(s);
        // informiamo il mapmanager che abbiamo aggiunto un nuovo sample
        App.A.mapManager.newSampleAdded(s);

    }


    public void startMeters(){
        audioSubSystem.startMetering();
        wifiSubSystem.startMetering();
        lteSubSystem.startMetering();
    }
    public void stopMeters(){
        audioSubSystem.stopMetering();
        wifiSubSystem.stopMetering();
        lteSubSystem.stopMetering();

    }



}
