package com.example.lamproj;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

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
        if(preference.getKey().equals("cell_size_meters")){
            int selectedRadius = Integer.parseInt(newValue.toString());
            App.A.mapManager.setTileGrid(selectedRadius);
        }
        return true;
    }
}