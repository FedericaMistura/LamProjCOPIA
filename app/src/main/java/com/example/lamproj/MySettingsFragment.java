package com.example.lamproj;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.rarepebble.colorpicker.ColorPreference;

import com.example.lamproj.tiles.TileGrid;

public class MySettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);


        findPreference("auto_sample_recording").setOnPreferenceChangeListener(this);
        findPreference("cell_size_meters").setOnPreferenceChangeListener(this);


    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case "cell_size_meters":
                App.A.mapManager.radiusInMeters = Double.parseDouble(newValue.toString());
                App.A.mapManager.setTileGrid();
                break;
            case "zone_size_meters":
                App.A.mapManager.zoneSize = Double.parseDouble(newValue.toString());
                App.A.mapManager.setTileGrid();
                break;
            case "auto_sample_recording_distance":
                App.A.auto_recording_meters = Double.parseDouble(newValue.toString());
                App.A.mapManager.checkNeedForAutoSampleCollectDistance();
                break;
            case "color_low":
                App.A.mapManager.colorLow = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
            case "color_mid":
                App.A.mapManager.colorMid = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
            case "color_high":
                App.A.mapManager.colorHigh = (int) newValue;
                App.A.mapManager.invalidateView(); //ridisegnare con il nuovo valore
        }

        return true;
    }
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ColorPreference) {
            ((ColorPreference) preference).showDialog(this, 0);
        } else super.onDisplayPreferenceDialog(preference);
    }
}