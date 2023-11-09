package com.example.lamproj;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.lamproj.databinding.FragmentFirstBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;
    private boolean updatesEnabled=false;
    private TextView tvNoise,  tvLte, tvWifi, tvCount, tvMode, tvViewMode, tvNoData;
    private Handler timerHandler;
    private Runnable timerRunnable;
    private FloatingActionButton fab;

    private Animation anim;

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
        fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Invoca il metodo per registrare le misurazioni
                App.A.context.recordStateAndInform("The measurement taken manually");
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
        tvMode = (TextView) view.findViewById(R.id.txt_mode);
        tvViewMode = (TextView) view.findViewById(R.id.txt_view_mode);
        tvNoData = (TextView) view.findViewById(R.id.txt_no_data);


        anim = new AlphaAnimation(0.0f, 1.0f); //Cambia l'alfa dell'oggetto
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE); //continua subito a lampeggiare
    }
/*
    private  void stopGetUpdates(){
        updatesEnabled=false;
        timerHandler.removeCallbacks(timerRunnable);
    }

 */

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
            tvLte.setText( String.format("  LTE level %.0f", App.A.sensorHub.level_lte));
            tvWifi.setText( String.format(" WIFI %.1f dBm", App.A.sensorHub.level_wifi));
            tvNoise.setText( String.format("NOISE %.1f dB", App.A.sensorHub.level_noise));
            tvCount.setText( String.format("SAMPLES %d", App.A.mapManager.getSamplesCount()));
        }

        if (App.A.mapManager.getCurrentLocation() != null) {
            if(App.A.auto_recording){
                tvMode.setText("Recording Mode: AUTO");
            } else {
                tvMode.setText("Recording Mode: MANUAL");
            }
            hideManualRecordButton(App.A.auto_recording);
            showDataWarningIfAppropriate();
        } else {
            tvMode.setText("WAITING FOR CURRENT LOCATION");
            hideManualRecordButton(true);
            tvNoData.setAnimation(null);
            tvNoData.setVisibility(GONE);
        }


        tvViewMode.setText(App.A.mapManager.txt_view_mode);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.A.sensorHub.stopMeters();
        updatesEnabled=false;
        binding = null;
    }


    /*
     il bottone "ADD" appare solo quando siamo in manual
     il no-data warning appare quando siamo in manual e il mapManager ci dice
     che il tile in cui cade la posizione attuale non contiene samples  o non esiste
     */

    public void showDataWarningIfAppropriate() {
        boolean showNoDataWarning=false;

        boolean noRecentDataZone = App.A.mapManager.isNoRecentDataZone();
        boolean noDataZone = App.A.mapManager.isNoDataZone();
        showNoDataWarning= noDataZone || noRecentDataZone;

        if(noDataZone){
            tvNoData.setText("NO DATA ZONE");
        } else {
            tvNoData.setText("NO RECENT DATA");
        }

        if (showNoDataWarning) {
            tvNoData.setVisibility(VISIBLE);
            tvNoData.setAnimation(anim);
        } else {
            tvNoData.setAnimation(null);
            tvNoData.setVisibility(GONE);
        }
    }

    public void hideManualRecordButton(boolean stateHidden){
        if(fab != null) { //non sappiamo quando viene chiamato
            if(stateHidden){
                fab.setVisibility(GONE);
            } else{
                fab.setVisibility(VISIBLE);
            }
        }
    }
}