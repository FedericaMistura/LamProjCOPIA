package com.example.lamproj.gmap;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.lamproj.App;
import com.example.lamproj.data.Sample;
import com.example.lamproj.data.SampleDbListSampleResultInterface;
import com.example.lamproj.tiles.Tile;
import com.example.lamproj.tiles.TileGrid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import java.util.List;


public class MapManager implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMapClickListener, LocationListener {
    public GoogleMap mMap=null;

    public static final int VIEW_NONE = 0;
    public static final int VIEW_WIFI = 1;
    public static final int VIEW_LTE = 2;
    public static final int VIEW_NOISE = 3;
    public int mapType=GoogleMap.MAP_TYPE_SATELLITE;

    private Location current_location;

    private List<Sample> allSamples;
    private int current_view=VIEW_NONE;
    private boolean current_view_is_valid=false;

    private Handler timerHandler;
    private Runnable timerRunnable;

    public TileGrid tiles;
    LatLng  topLeftCorner = null;
    public String txt_view_mode = "";
    public Tile currentTile = null;


    public int getSamplesCount(){
        if (allSamples != null) {
            return allSamples.size();
        } else return 0;
    }

    public Location getCurrentLocation() {
        return current_location;
    }

    public void setTileGrid(){
        tiles = new TileGrid(topLeftCorner, App.A.zoneSize, App.A.zoneSize, App.A.radiusInMeters);
        if(allSamples != null){
            tiles.populate(allSamples);
        }
        invalidateView(); //forzare la riscrittura
    }

    /*
    quando la mappa è pronta er essere utilizzata.
    Registra i listener per i click sulla posizione e sulla mappa.
     */
    public void onMapReady(GoogleMap googleMap) {
        this.mMap=googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setMapType(mapType);

        App.A.context.enableMyLocation();

        // all'inizio ci carichiamo in memoria tutti i samples presenti nel db
        App.A.db.getAllSamples(new SampleDbListSampleResultInterface() {
            @Override
            public void onGetListSampleComplete(List<Sample> ss) {
                allSamples =ss;
                if(tiles != null) {
                    tiles.populate(ss); //si fa dopo
                }
            }
        });

        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!current_view_is_valid) {
                    redrawCurrentView();
                }
                timerHandler.postDelayed(this, 500);
            }
        };
        timerHandler.post(timerRunnable);


    }

    /*
    è chiamato quando l'utente clicca sulla mappa per impostare una nuova griglia
     */
    private void setNewZone(LatLng p0){
        //creiamo il nuovo new TopLeftCorner

        LatLng p1 = SphericalUtil.computeOffset(p0, App.A.zoneSize / 2, 270);
        topLeftCorner = SphericalUtil.computeOffset(p1, App.A.zoneSize / 2, 360);
        setTileGrid();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p0, 12));
    }

    /*
    Aggiunta di un nuovo sample nella lista e nella griglia
    invalida la visualizzazione per aggiornarsi
     */
    public void newSampleAdded(Sample s) {
        // aggiorniamo la lista di sample in memoria senza dovere rileggere tutto dal db
        if (allSamples != null) {
            allSamples.add(s);
            tiles.addSample(s);
        }
        invalidateView();
    }

    public void redrawCurrentView(){
        switch(current_view) {
            case VIEW_LTE:
                showLteData();
                txt_view_mode = "LTE";
                break;
            case VIEW_WIFI:
                showWiFiData();
                txt_view_mode = "Wifi";
                break;
            case VIEW_NOISE:
                showNoiseData();
                txt_view_mode = "Noise";
                break;
            default:
                mMap.clear();
                if(tiles != null){
                    mMap.addPolygon(tiles.bounds);
                }
                txt_view_mode=" ";

        }
        current_view_is_valid=true;
    }

    public void invalidateView() {
        current_view_is_valid=false;
    }
    public void setCurrentView(int view_id) {
        current_view=view_id;
        invalidateView();
    }

    private void showLteData(){
        current_view=VIEW_LTE;
        if (tiles != null) {
            tiles.addToGoogleMap(mMap, current_view);
        }

    }

    private void showWiFiData(){
        current_view=VIEW_WIFI;
        if (tiles != null) {
            tiles.addToGoogleMap(mMap, current_view);
        }
    }

    private void showNoiseData(){
        current_view=VIEW_NOISE;
        if (tiles != null) {
            tiles.addToGoogleMap(mMap, current_view);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        setNewZone(latLng);


    }

    public void clearAllSamples(){
        allSamples.clear();
        tiles.clearTilesSamples();
        invalidateView();
    }

    /*
    Quando l'applicazione è in automatic mode, questo metodo consente
    di calcolare quando, in base a tempo e distanza, è necessario che si registri automaticamente un nuovo
    campione.
     */
    public void checkNeedForAutoSampleCollectDistance() {
        if (App.A.auto_recording && current_location != null) {
            double dist = 1000000.0; // numero  molto grande
            double secs = 1000000000; ///

            if (App.A.db.mostRecentSample != null) {
                dist = SphericalUtil.computeDistanceBetween(new LatLng(current_location.getLatitude(), current_location.getLongitude()), App.A.db.mostRecentSample.getLatLng());
                secs = (System.currentTimeMillis() - App.A.db.mostRecentSample.time) / 1000;
            }
            //Informa user per nuova registrazione automatica
            if (dist >= App.A.auto_recording_meters && secs >= App.A.auto_recording_seconds) {
                String msg = String.format("Measurement taken automatically %.1f meters from previous point", App.A.auto_recording_meters);
                App.A.mapManager.recordStateAndInform(msg);
            } else {
                // non registriamo niente
            }
        }
    }

    protected void onNewTileEntered(Tile t) {
        if (!t.hasSamples()) {
            int id=(int) (System.currentTimeMillis() & 0xfffffff);
            String msg = String.format("Entering Tile id %d with no  samples ",t.id);
            App.A.sendNotification(id,msg);
        }
    }

    public void onLocationChanged(@NonNull Location location) {
        current_location=location; //mi serve per creare il nuovo sample
        if(tiles != null){
            Tile t = tiles.getTileContainingLatLng(location.getLatitude(),location.getLongitude());
            boolean tileChanged=false;
            if (currentTile == null )
                tileChanged=true;
            else
                //Cambia quando cambia la latitudine o la longitudine
                tileChanged= currentTile.center.latitude != t.center.latitude ||  currentTile.center.longitude != t.center.longitude;
            currentTile=t; //Per essere sicuri che tile sia aggiornato
            if(tileChanged){
                onNewTileEntered(t);
            }
        }
        if(topLeftCorner == null){
            setNewZone(new LatLng(location.getLatitude(),location.getLongitude()));
        }

        checkNeedForAutoSampleCollectDistance();
    }
    /*
    Verifica che il tile corrente non abbia dati
     */
    public boolean isNoDataZone(){

        if (currentTile==null)

            return false;
        else
            return !currentTile.hasSamples();
    }
    public Tile getCurrentTile(){return currentTile;}



    /*
    Verifica che non ci sono dati recenti in quel tile
    entro l'intervallo di tempo definito dall'utente
     */
    public boolean isNoRecentDataZone(){
        if (currentTile==null)
            return false;
        else
            return !currentTile.hasRecentSamples(App.A.last_measurement_seconds);
    }


    /*
  Per ricevere messaggi di aggiornamento sulla posizione quando
  l'app è in background.
  Lo stato e la posizione vengono estratti dall'Intent e aggiorna
  la posizione sulla mappa
   */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra("Status");
            Bundle b = intent.getBundleExtra("Location");
            Location lastKnownLoc = (Location) b.getParcelable("Location");
            if (lastKnownLoc != null) {
                onLocationChanged(lastKnownLoc);
            }
        }
    };
    /*
    Viene chiamato dalla MainActivity quando abilita la registrazione
    Registra il mapManager come client del service.
     */

    @SuppressLint("MissingPermission")
    public void onMyLocationEnabled(){
        mMap.setMyLocationEnabled(true);
        LocalBroadcastManager.getInstance(App.A.context).registerReceiver(  mMessageReceiver, new IntentFilter("LocationUpdate"));
    }

    /*
Registrazione di un nuovo campione
Mostrare il messaggio all'utente
 */
    public void recordStateAndInform(String msg){
        Log.i("MapManager","sto per registrare un nuovo sample");
        App.A.sensorHub.recordNewSample();
        if (App.A.context != null) {
            App.A.context.snap(msg);
        }
    }


}