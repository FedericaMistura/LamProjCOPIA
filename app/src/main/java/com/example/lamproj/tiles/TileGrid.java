package com.example.lamproj.tiles;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class TileGrid {
    public double tileRadiusInMeters;
    public LatLng topLeftCorner;

    public double horizontalSizeInMeters;
    public double verticalSizeInMeters;

    public TileGrid(LatLng _topLeftCorner,  double hsize, double vsize, double _tileRadiusInMeters) {
        topLeftCorner=_topLeftCorner;
        tileRadiusInMeters=_tileRadiusInMeters;
        horizontalSizeInMeters=hsize;
        verticalSizeInMeters=vsize;
    }

    public List<Tile> getTiles(){
        List<Tile> l= new ArrayList<Tile>();
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



                l.add(t);
            }

        }


        return l;
    }

    public  void addToGoogleMap(GoogleMap mMap) {
        addToGoogleMap(mMap,true);
    }
    public void addToGoogleMap(GoogleMap mMap,  boolean clear) {
        if (clear)
            mMap.clear();

        for (Tile t: getTiles() ) {
            mMap.addPolygon(t.toPolygon());
        }
    }
}