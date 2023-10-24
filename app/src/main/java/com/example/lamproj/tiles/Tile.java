package com.example.lamproj.tiles;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    public ArrayList<Sample> samples = new ArrayList<Sample>();

    public Tile (LatLng c, double r) {
        center=c;
        radiusInMeters=r;
    }

    public Tile (Double lat, Double lon, double r) {
        center=new LatLng(lat,lon);
        radiusInMeters=r;
    }

    /*
    public PolygonOptions getPolygonOptions() {
        if(polygon == null){
            polygon = this.toPolygon();
        }
        //proprietà di colore
        if(samples.size() > 0) {
            polygon.strokeColor(0x50ff0000);
            polygon.fillColor(0x30ff0000);
        } else {
            polygon.strokeColor(0x20ff0000);
            polygon.fillColor(0x20ff0000);
        }
        return polygon;
    }
    */

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

    public double distanceFrom(double latitude, double longitude){
        //distanze in metri
        return SphericalUtil.computeDistanceBetween(center, new LatLng(latitude, longitude));
    }

}
