package com.example.lamproj.tiles;

import android.graphics.Color;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    public LatLng center;
    public double radiusInMeters;

    public Tile (LatLng c, double r) {
        center=c;
        radiusInMeters=r;
    }

    public Tile (Double lat, Double lon, double r) {
        center=new LatLng(lat,lon);
        radiusInMeters=r;
    }

    public PolygonOptions toPolygon(){
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

}
