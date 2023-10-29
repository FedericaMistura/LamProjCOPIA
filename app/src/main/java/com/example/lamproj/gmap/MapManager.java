package com.example.lamproj.gmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.example.lamproj.App;
import com.example.lamproj.PermissionUtils;
import com.example.lamproj.data.Sample;
import com.example.lamproj.data.SampleDbListSampleResultInterface;
import com.example.lamproj.tiles.TileGrid;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class MapManager implements GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener, GoogleMap.OnMapClickListener {
    public GoogleMap mMap=null;

    public static final int VIEW_NONE = 0;
    public static final int VIEW_WIFI = 1;
    public static final int VIEW_LTE = 2;
    public static final int VIEW_NOISE = 3;

    public int colorNone=0x00000000;
    public int colorLow=0x30FF0000;
    public int colorMid=0x30FFFF00;
    public int colorHigh=0x3000FF00;

    //punti di taglio
    public double lteLow=-86;
    public double lteMid=-66;
    public double wifiLow=-71;
    public double wifiMid=-50;
    public double noiseLow=-10;
    public double noiseMid=-50;
    public int mapType=GoogleMap.MAP_TYPE_SATELLITE;

    private Location current_location;

    private List<Sample> allSamples;
    private int current_view=VIEW_NONE;
    private boolean current_view_is_valid=false;

    private Handler timerHandler;
    private Runnable timerRunnable;

    private TileGrid tiles;
    private double radiusInMeters = 500;
    LatLng  finaleEmilia = new LatLng(44.830321, 11.290487);

    public int getSamplesCount(){
        return allSamples.size();
    }
    public Location getCurrentLocation() {
        return current_location;
    }

    public void setTileGrid(double radiusInMeters){
        //LatLng BOLOGNA_NW = new LatLng(44.52, 11.286387);
        tiles = new TileGrid(finaleEmilia,40000.0,40000.0,radiusInMeters);

    }
    public void onMapReady(GoogleMap googleMap) {
        this.mMap=googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setMapType(mapType);

        App.A.context.enableMyLocation();
        LatLng BOLOGNA = new LatLng(44.496781, 11.356387);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(finaleEmilia, 12));

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                current_location=arg0;
            }
        });

        // all'inizio ci carichiamo in memoria tutti i samples presenti nel db
        App.A.db.getAllSamples(new SampleDbListSampleResultInterface() {
            @Override
            public void onGetListSampleComplete(List<Sample> ss) {
                allSamples =ss;
                tiles.populate(ss);
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

        setTileGrid(this.radiusInMeters);
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
                break;
            case VIEW_WIFI:
                showWiFiData();
                break;
            case VIEW_NOISE:
                showNoiseData();
                break;
            default:
                mMap.clear();
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
    /*
    private void showTestData(){
        mMap.clear();

        // Add a marker in Bologna and move the camera
        LatLng BOLOGNA = new LatLng(44.496781, 11.356387);
        LatLng BOLOGNA_NW = new LatLng(44.52, 11.286387);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BOLOGNA, 12));



    }
    */

    private void showLteData(){
        current_view=VIEW_LTE;
        mMap.clear();
        tiles.addToGoogleMap(mMap, current_view);
    }

    private void showWiFiData(){
        current_view=VIEW_WIFI;
        mMap.clear();
        tiles.addToGoogleMap(mMap, current_view);
    }

    private void showNoiseData(){
        current_view=VIEW_NOISE;
        mMap.clear();
        tiles.addToGoogleMap(mMap, current_view);
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
        // When clicked on map
        // Initialize marker options
        MarkerOptions markerOptions=new MarkerOptions();
        // Set position of marker
        markerOptions.position(latLng);
        // Set title of marker
        markerOptions.title(latLng.latitude+" : "+latLng.longitude);
        // Remove all marker
        mMap.clear();
        // Animating to zoom the marker
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
        // Add marker on map
        mMap.addMarker(markerOptions);
    }

}