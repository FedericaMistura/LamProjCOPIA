package com.example.lamproj.sensors;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.example.lamproj.App;

public class LteSubSystem {
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;
    boolean isMetering=false;

    public void stopMetering(){
        isMetering=false;
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
    public void startMetering() {
        telephonyManager = (TelephonyManager) App.A.context.getSystemService(Context.TELEPHONY_SERVICE);

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);

                // Get the LTE signal strength in dBm
                App.A.sensorHub.level_lte = signalStrength.getLevel();
            }
        };

        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

}