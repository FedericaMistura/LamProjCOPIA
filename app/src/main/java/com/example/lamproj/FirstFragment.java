package com.example.lamproj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.lamproj.databinding.FragmentFirstBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private boolean updatesEnabled=false;
    private TextView tvNoise,  tvLte, tvWifi, tvCount;
    private Handler timerHandler;
    private Runnable timerRunnable;


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);

        SupportMapFragment supportMapFragment=(SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.gmap);

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                App.A.mapManager.onMapReady(googleMap);
                startGetUpdates();
            }
        });

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLte= (TextView) view.findViewById(R.id.txt_lte);
        tvNoise= (TextView) view.findViewById(R.id.txt_noise);
        tvWifi= (TextView) view.findViewById(R.id.txt_wifi);
        tvCount = (TextView) view.findViewById(R.id.txt_count);
    }

    private  void stopGetUpdates(){
        updatesEnabled=false;
        timerHandler.removeCallbacks(timerRunnable);
    }

    private  void startGetUpdates(){
        App.A.sensorHub.startMeters();
        updatesEnabled=true;
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateMeters();
                if (updatesEnabled) {
                    timerHandler.postDelayed(this, 200);
                }
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void updateMeters(){
        if (tvWifi != null) {
            tvLte.setText( String.format("  LTE %.1f dBm", App.A.sensorHub.level_lte));
            tvWifi.setText( String.format(" WIFI %.1f dBm", App.A.sensorHub.level_wifi));
            tvNoise.setText( String.format("NOISE %.1f dB", App.A.sensorHub.level_noise));
            tvCount.setText( String.format("SAMPLES %d", App.A.mapManager.getSamplesCount()));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.A.sensorHub.stopMeters();
        updatesEnabled=false;
        binding = null;
    }

}