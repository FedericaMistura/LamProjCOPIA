package com.example.lamproj;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import com.example.lamproj.gmap.MapManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.lamproj.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RECORD_AUDIO_PERMISSION_REQUEST_CODE = 2;
    private static final int BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 3;
    private boolean locationPermissionDenied = false;
    private boolean backgroundLocationPermissionDenied = false;
    private boolean recordinAudioPermissionDenied = false;
    private  NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.A.context=this;

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_settings) {
            navController.navigate(R.id.action_FirstFragment_to_MySettingsFragment);
        }
        else if (id==R.id.action_lte) {
            App.A.mapManager.setCurrentView(MapManager.VIEW_LTE);
        }
        else if (id==R.id.action_wifi) {
            App.A.mapManager.setCurrentView(MapManager.VIEW_WIFI);
        }
        else if (id==R.id.action_noise) {
            App.A.mapManager.setCurrentView(MapManager.VIEW_NOISE);
        }
        else if (id==R.id.action_quit) {
            LocationService.stop(this);
            finishAndRemoveTask();
        }
        else if (id==R.id.action_delete){
            showDeleteSamplesConfirmationDialog();
        } else if(id == R.id.action_get_help) {
            navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteSamplesConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm deletion of samples.");
        builder.setMessage("Do you really want to delete all the samples?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                App.A.db.deleteAllSamples();
                App.A.mapManager.clearAllSamples();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("MissingPermission")
    public void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableBackgroundLocation();
            return;
        }
        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,  Manifest.permission.ACCESS_FINE_LOCATION, true);
    }
    /*
    Si registra come ricevitore di messaggi
    subito dopo aver startato il locationservice
     */
    @SuppressLint("MissingPermission")
    public void enableBackgroundLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            App.A.mapManager.mMap.setMyLocationEnabled(true);
            LocationService.start(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(  mMessageReceiver, new IntentFilter("LocationUpdate"));
            return;
        }
        PermissionUtils.requestPermission(this, BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE,  Manifest.permission.ACCESS_BACKGROUND_LOCATION, true);
    }

    @SuppressLint("MissingPermission")
    public boolean isRecordAudioEnabled() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ) {
            return true;
        }
        PermissionUtils.requestPermission(this, RECORD_AUDIO_PERMISSION_REQUEST_CODE,  Manifest.permission.RECORD_AUDIO, true);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, android.Manifest.permission.ACCESS_FINE_LOCATION) ||
                    PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_COARSE_LOCATION)){
                enableMyLocation();
                enableBackgroundLocation();
            } else {
                locationPermissionDenied = true;
            }
        }
        else if (requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                enableBackgroundLocation();
            } else {
                backgroundLocationPermissionDenied = true;
            }
        }
        else if (requestCode == RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
            if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.RECORD_AUDIO)) {
                App.A.sensorHub.startAudioSubSystemMetering();
            } else {
                recordinAudioPermissionDenied = true;
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    /*

    mostrare messaggio all'utente
     */
    public void snap(String msg){
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void recordStateAndInform(String msg){

        App.A.sensorHub.recordNewSample();
        snap(msg);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            Bundle b = intent.getBundleExtra("Location");
            Location lastKnownLoc = (Location) b.getParcelable("Location");
            if (lastKnownLoc != null) {
                App.A.mapManager.onLocationChanged(lastKnownLoc);
            }
        }
    };

}