package com.example.lamproj.tiles;

import com.example.lamproj.data.Sample;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class TileGrid {
    public double tileRadiusInMeters;
    public LatLng topLeftCorner;

    public double horizontalSizeInMeters;
    public double verticalSizeInMeters;

    public ArrayList<Tile> tiles;

    public TileGrid(LatLng _topLeftCorner,  double hsize, double vsize, double _tileRadiusInMeters) {
        topLeftCorner=_topLeftCorner;
        tileRadiusInMeters=_tileRadiusInMeters;
        horizontalSizeInMeters=hsize;
        verticalSizeInMeters=vsize;
        createTiles();
    }

    public void setRadius(double tileRadiusInMeters){
        this.tileRadiusInMeters = tileRadiusInMeters;
        createTiles();
    }

    private void createTiles(){
        tiles = new ArrayList<Tile>();
        double offset=tileRadiusInMeters*Math.sqrt(3);
        int nCols= (int) (horizontalSizeInMeters/offset);
        int nRows= (int) (verticalSizeInMeters/offset);
        Tile t;
        LatLng c;
        LatLng start=topLeftCorner;
        LatLng tl2=SphericalUtil.computeOffset(topLeftCorner , offset, 150);
        for (int row=0; row<nRows; row++ ) {
            if ((row % 2)==1) {
                start= new LatLng( SphericalUtil.computeOffset(tl2 , (row-1)*tileRadiusInMeters*1.5, 180).latitude ,tl2.longitude);


            } else {
                start= new LatLng( SphericalUtil.computeOffset(topLeftCorner , row*tileRadiusInMeters*1.5, 180).latitude ,topLeftCorner.longitude);
            }

            for ( int col=0; col<nCols; col++) {
                c= SphericalUtil.computeOffset(start , col*offset, 90);
                t= new Tile(c,tileRadiusInMeters);



                tiles.add(t);
            }

        }
    }

    public  void addToGoogleMap(GoogleMap mMap) {
        addToGoogleMap(mMap,true);
    }
    public void addToGoogleMap(GoogleMap mMap,  boolean clear) {
        if (clear)
            mMap.clear();

        for (Tile t: tiles) {
            //PolygonOptions polygon = t.getPolygonOptions();
            PolygonOptions polygon = t.setColorHexagone();
            mMap.addPolygon(polygon);
        }
    }

    public void addSample(Sample s){
        double dMin = tileRadiusInMeters * 10;
        Tile tMin = null;
        for(Tile t : tiles){
            double distance = t.distanceFrom(s.latitude, s.longitude);
            if(distance <= tileRadiusInMeters){
                if(distance < dMin){
                    dMin = distance;
                    tMin = t;
                }
            }
        }
        if(tMin != null){
            tMin.samples.add(s);
        }
    }
    public void populate(List<Sample> samples){
        for(Tile t : tiles){
            t.samples.clear();
        }
        for(Sample s : samples){
            addSample(s);
        }

    }
}