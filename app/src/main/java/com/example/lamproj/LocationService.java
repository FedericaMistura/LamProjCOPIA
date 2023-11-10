package com.example.lamproj;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

public class LocationService extends Service  {
    private static final String CHANNEL_ID = "LocService";
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static int CMD_START_FOREGROUND = 1;
    private static int CMD_STOP_FOREGROUND = 2;

    /*
    Avvio del servizio di foreground
    Creazione dell'intent con l'azione CMD_START
     */
    public static void start(Context ctx) {
        Intent serviceIntent = new Intent(ctx, LocationService.class);
        serviceIntent.putExtra("action", CMD_START_FOREGROUND);
        ContextCompat.startForegroundService(ctx, serviceIntent);
    }
    /*
    Fermare il foreground service
    Creazione dell'intent con l'azione CMD_STOP
     */
    public static void stop(Context ctx) {
        Intent serviceIntent = new Intent(ctx, LocationService.class);
        serviceIntent.putExtra("action", CMD_STOP_FOREGROUND);
        ContextCompat.startForegroundService(ctx, serviceIntent);
    }

    /*
    viene chiamato quando viene avviato il servizio
    Gestione delle azioni specificate nell'intent(CMD_STOP, CMD_START)
    All'avvio del servizio, si ha una notifica e avvia la ricezione degli aggiornamenti sulla posizione
     */
    @SuppressLint("MissingPermission")
    public int onStartCommand(Intent intent, int flags, int startId) {
        int action = intent.getIntExtra("action",1);

        if (action == CMD_START_FOREGROUND) {
            App.A.locationService=this;

            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "LamProj Background Location Service",
                    NotificationManager.IMPORTANCE_LOW);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "MyChannelId");
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("running on foreground")
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setChannelId(CHANNEL_ID)
                    .build();

            startForeground(100, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST);

            // Check if Google Play Services is available on the device
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

            if (resultCode == ConnectionResult.SUCCESS) {
                // Google Play Services is available
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(App.A.context);
                createLocationCallback();
                requestLocationUpdates();
            } else {
                // Google Play Services is not available or is outdated, to do Notification
            }
            return START_NOT_STICKY;
        }
        else if (action == CMD_STOP_FOREGROUND) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            App.A.locationService=null;
            stopForeground(true);
            stopSelfResult(startId);
            return START_STICKY;
        }

        return START_NOT_STICKY;
    }
    /*
    Quando viene distrutto il servizio, rimuove gli aggiornamenti sulla posizione
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates( locationCallback );
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /*
    Creazione della locationCallback.
     */
    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
//                App.A.mapManager.onLocationChanged(location);
                //Trasmissione dell'aggiornamento sulla posizione
                broadcastMessage(location,"ok");
            }
        };
    }

    /*
    Richiesta aggiornamenti sulla posizione utilizzando FusedLocationProviderClient

     */
    public void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setSmallestDisplacement((float) App.A.auto_recording_meters)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval((long) (App.A.auto_recording_seconds*1000))
                .setFastestInterval(500);  // Fastest update interval
        /*
        Se il permesso è concesso: aggiornamenti sulla posizione
        altrimenti viene chiamato enableMyLocation()
         */
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            App.A.context. enableMyLocation();
        }

    }
    /*
    Inviare un messaggio di broadcast tramite LocalBroadCastManager
    con lo stato e la posizione nel messaggio di broadcast
     */
    private void broadcastMessage(Location l, String msg) {
        Intent intent = new Intent("LocationUpdate");
        intent.putExtra("Status", msg);
        Bundle b = new Bundle();
        b.putParcelable("Location", l);
        intent.putExtra("Location", b);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}