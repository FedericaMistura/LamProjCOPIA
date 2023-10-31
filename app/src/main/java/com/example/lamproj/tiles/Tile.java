package com.example.lamproj.tiles;

import static com.example.lamproj.gmap.MapManager.VIEW_LTE;
import static com.example.lamproj.gmap.MapManager.VIEW_NOISE;
import static com.example.lamproj.gmap.MapManager.VIEW_WIFI;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lamproj.App;
import com.example.lamproj.data.Sample;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class Tile {
    public LatLng center;
    public double radiusInMeters;
    private PolygonOptions polygon;
    private double avgLte=0;
    private double avgWifi=0;
    private double avgNoise=0;
    private ArrayList<Sample> samples = new ArrayList<Sample>();


    public Tile (LatLng c, double r) {
        center=c;
        radiusInMeters=r;
    }

    public Tile (Double lat, Double lon, double r) {
        center=new LatLng(lat,lon);
        radiusInMeters=r;
    }
    /*
    Oltre ad aggiungere, fa il ricalcolo della media
     */
    public void addSample(Sample s) {
        samples.add(s);
        recalcAvg();
    }
    //Elimina i samples
    public void clearSamples() {
        samples.clear();
        recalcAvg();
    }

    protected  void recalcAvg() {
        if (samples.size() == 0) {
            avgLte = 0;
            avgWifi = 0;
            avgNoise = 0;
        } else { //calcoliamo media
            avgLte = samples.stream().mapToDouble(e -> e.lte).sum() / samples.size();
            avgWifi = samples.stream().mapToDouble(e -> e.wifi).sum() / samples.size();
            avgNoise = samples.stream().mapToDouble(e -> e.noise).sum() / samples.size();
        }
    }

    public int getProperFillColor(int viewType){
        int color= App.A.mapManager.colorNone;
        if (samples.size()>0){
            switch (viewType) {
                case VIEW_WIFI:
                    if (avgWifi<App.A.mapManager.wifiLow)
                        color=App.A.mapManager.colorLow;
                    else if (avgWifi<App.A.mapManager.wifiMid)
                        color=App.A.mapManager.colorMid;
                    else
                        color=App.A.mapManager.colorHigh;
                    break;
                case VIEW_LTE:
                    if (avgLte<App.A.mapManager.lteLow)
                        color=App.A.mapManager.colorLow;
                    else if (avgLte<App.A.mapManager.lteMid)
                        color=App.A.mapManager.colorMid;
                    else
                        color=App.A.mapManager.colorHigh;
                    break;
                case VIEW_NOISE:
                    if (avgNoise<App.A.mapManager.noiseLow)
                        color=App.A.mapManager.colorLow;
                    else if (avgNoise<App.A.mapManager.noiseMid)
                        color=App.A.mapManager.colorMid;
                    else
                        color=App.A.mapManager.colorHigh;
                    break;
            }        }

        return color;
    }

    public PolygonOptions getPolygonOptions(int view) {
        if(polygon == null){
            polygon = this.toPolygon();
        }
        polygon.strokeColor(0x10ff0000);
        //proprietà di colore
        polygon.fillColor(getProperFillColor(view));
        return polygon;
    }

    /*
    public PolygonOptions setColorHexagone(){
        if(polygon == null){
            polygon = this.toPolygon();
        }

        polygon.strokeColor(0x30808080);
        if(samples.size()<= 10){
            polygon.fillColor(0x90ff0000);
        } else if(samples.size() <= 25){
            polygon.fillColor(0x90ffff00);
        } else {
            polygon.fillColor(0x9000ff00);
        }
        return polygon;
    }
   */
    private PolygonOptions toPolygon(){
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(createHexagon(center,radiusInMeters))
                .strokeColor(Color.GREEN);

        return polygonOptions;
    }

    private List<LatLng> createHexagon(LatLng center, double radiusInMeters){
        List<LatLng> hexagon = new ArrayList<>();
        double angle = 60.0;
        for(int i = 0; i < 6; i++){
            LatLng vertex = SphericalUtil.computeOffset(center, radiusInMeters, i * angle);
            hexagon.add(vertex);
        }
        return hexagon;
    }
    public boolean hasSamples(){
        return  samples.size()>0;
    }

    public double distanceFrom(double latitude, double longitude){
        //distanze in metri
        return SphericalUtil.computeDistanceBetween(center, new LatLng(latitude, longitude));
    }

}
