package com.example.lamproj;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.rarepebble.colorpicker.ColorPreference;


import java.util.Map;

public class MySettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        Map<String,?> pref =  PreferenceManager.getDefaultSharedPreferences(App.A.getApplicationContext()).getAll();

        for (String prKey: pref.keySet())  {
            Preference pr = findPreference(prKey);
                    if(pr != null){
                        pr.setOnPreferenceChangeListener(this);
                    }
        }
    }
    /*
    Cattura tutti i cambi di preferenza
     */
    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "auto_sample_recording":
                App.A.auto_recording = (boolean) newValue;
                break;
            case "auto_sample_recording_time":
                App.A.auto_recording_seconds = Double.parseDouble(newValue.toString());
                App.A.mapManager.checkNeedForAutoSampleCollectDistance();
                break;
            case "auto_sample_recording_distance":
                App.A.auto_recording_meters = Double.parseDouble(newValue.toString());
                App.A.mapManager.checkNeedForAutoSampleCollectDistance();
                break;

            case "cell_size_meters":
                App.A.radiusInMeters = Double.parseDouble(newValue.toString());
                App.A.mapManager.setTileGrid();
                break;
            case "zone_size_meters":
                App.A.zoneSize = Double.parseDouble(newValue.toString());
                App.A.mapManager.setTileGrid();
                break;
            case "n_measurement_average":
                App.A.nMeasurementsForAverage= Integer.parseInt((String)newValue);
                break;

            case "color_low":
                App.A.colorLow = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
                break;
            case "color_mid":
                App.A.colorMid = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
                break;
            case "color_high":
                App.A.colorHigh = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
                break;
        }

        return true;
    }
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        if (preference instanceof ColorPreference) {
            ((ColorPreference) preference).showDialog(this, 0);
        } else super.onDisplayPreferenceDialog(preference);
    }
}