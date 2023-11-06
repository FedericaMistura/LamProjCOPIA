package com.example.lamproj.gmap;


import android.location.Location;
import android.location.LocationListener;
import android.os.Handler;

import androidx.annotation.NonNull;

import com.example.lamproj.App;
import com.example.lamproj.PermissionUtils;
import com.example.lamproj.data.Sample;
import com.example.lamproj.data.SampleDbListSampleResultInterface;
import com.example.lamproj.tiles.TileGrid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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

    private void setNewZone(LatLng p0){
        //creiamo il nuovo new TopLeftCorner

        LatLng p1 = SphericalUtil.computeOffset(p0, App.A.zoneSize / 2, 270);
        topLeftCorner = SphericalUtil.computeOffset(p1, App.A.zoneSize / 2, 360);
        setTileGrid();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p0, 12));
    }
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

    public void checkNeedForAutoSampleCollectDistance() {
        if (App.A.auto_recording && current_location != null) {
            double dist = 1000000.0; // numero  molto grande
            double secs = 1000000000; ///

            if (App.A.db.mostRecentSample != null) {
                dist = SphericalUtil.computeDistanceBetween(new LatLng(current_location.getLatitude(), current_location.getLongitude()), App.A.db.mostRecentSample.getLatLng());
                secs = (System.currentTimeMillis() - App.A.db.mostRecentSample.time) / 1000;
            }
            if (dist >= App.A.auto_recording_meters && secs >= App.A.auto_recording_seconds) {
                String msg = String.format("Measurement taken automatically %.1f meters from previous point", App.A.auto_recording_meters);
                App.A.context.recordStateAndInform(msg);
            } else {
                // non registriamo niente
            }
        }
    }


    public void onLocationChanged(@NonNull Location location) {
        current_location=location; //mi serve per creare il nuovo sample
        if(topLeftCorner == null){
            setNewZone(new LatLng(location.getLatitude(),location.getLongitude()));
        }

        checkNeedForAutoSampleCollectDistance();
    }
}