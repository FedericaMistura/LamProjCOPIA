package com.example.lamproj.tiles;

import static com.example.lamproj.gmap.MapManager.VIEW_LTE;

import android.graphics.Color;

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

    public double horizontalSizeInMeters; //larghezza
    public double verticalSizeInMeters;

    public ArrayList<Tile> tiles;
    public PolygonOptions bounds;

    public TileGrid(LatLng _topLeftCorner, double hsize, double vsize, double _tileRadiusInMeters) {
        topLeftCorner = _topLeftCorner;
        tileRadiusInMeters = _tileRadiusInMeters;
        horizontalSizeInMeters = hsize;
        verticalSizeInMeters = vsize;
        createTiles();
        createBounds();
    }

    public void setRadius(double tileRadiusInMeters) {
        this.tileRadiusInMeters = tileRadiusInMeters;
        createTiles();
    }

    /*
    Generazione griglia di esagoni che copre una determinata area.
     */
    private void createTiles() {
        tiles = new ArrayList<Tile>();
        double offset = tileRadiusInMeters * Math.sqrt(3); //Distanza tra centro esagono e vertice
        int nCols = (int) (horizontalSizeInMeters / offset);
        int nRows = (int) (verticalSizeInMeters / offset);
        Tile t;
        LatLng c;
        LatLng start = topLeftCorner;
        LatLng tl2 = SphericalUtil.computeOffset(topLeftCorner, offset, 150);

        for (int row = 0; row < nRows; row++) {
            if ((row % 2) == 1) {
                start = new LatLng(SphericalUtil.computeOffset(tl2, (row - 1) * tileRadiusInMeters * 1.5, 180).latitude, tl2.longitude);


            } else {
                start = new LatLng(SphericalUtil.computeOffset(topLeftCorner, row * tileRadiusInMeters * 1.5, 180).latitude, topLeftCorner.longitude);
            }

            for (int col = 0; col < nCols; col++) {
                c = SphericalUtil.computeOffset(start, col * offset, 90);
                t = new Tile(c, tileRadiusInMeters);


                tiles.add(t);
            }

        }
    }

    private void createBounds(){
        List<LatLng> vert = new ArrayList<>();
        vert.add(topLeftCorner);
        LatLng p1 = SphericalUtil.computeOffset(topLeftCorner, horizontalSizeInMeters, 90);//punto che rappresenta il vertice
        vert.add(p1);
        LatLng p2 = SphericalUtil.computeOffset(p1, verticalSizeInMeters, 180);
        vert.add(p2);
        LatLng p3 = SphericalUtil.computeOffset(p2, horizontalSizeInMeters, 270);//punto che rappresenta il vertice
        vert.add(p3);
        bounds =  new PolygonOptions().addAll(vert).strokeColor(Color.GREEN);
    }
    /*
    rimuovere tutti gli oggetti presenti precedentemente sulla mappa e quindi aggiungere le tiles
     */
    public void addToGoogleMap(GoogleMap mMap, int view) {
        addToGoogleMap(mMap, view, true);
    }

    /*
    Controlla se rimuovere o meno gli oggetti precedentemente presenti sulla mappa
    poi aggiunge nuove tiles.
     */
    public void addToGoogleMap(GoogleMap mMap, int view, boolean clear) {
        if (clear) {
            mMap.clear();
            mMap.addPolygon(bounds);
        }
        for (Tile t : tiles) {
            if (t.hasSamples()) {
                PolygonOptions polygon = t.getPolygonOptions(view);
                mMap.addPolygon(polygon);
            }
        }
    }

    /*
    Aggiunge il sample al relativo tile calcolando la distanza
    */
    public void addSample(Sample s) {
        Tile t=getTileContainingLatLng(s.latitude,s.longitude);
        if (t != null) {
            t.addSample(s);
        }
    }

    public Tile getTileContainingLatLng(double latitude, double longitude){
        double dMin = tileRadiusInMeters * 10;
        Tile tMin = null;
        for (Tile t : tiles) {
            double distance = t.distanceFrom(latitude, longitude);
            if (distance <= tileRadiusInMeters) {
                if (distance < dMin) {
                    dMin = distance;
                    tMin = t;
                }
            }
        }
        return tMin;
    }

    /*
    Aggiunge una lista di samples al tile.
     */
    public void populate(List<Sample> samples) {
        // popoliamo  i samples delle tiles, e siccome è un processo che dura molto
        // rendiamo asincrona  l'operazione,
        // inoltre lavoriamo su una copia della lista per evitare problemi di concorrenza
        // nel caso il processo principale aggiunge un nuovo sample

        ArrayList<Sample> l = new ArrayList<Sample>(samples);

        new Thread(new Runnable() {
            @Override
            public void run() {
                clearTilesSamples();
                for (Sample s : l) {
                    addSample(s);
                }
            }
        }).start();

    }

    public void clearTilesSamples() {
        for (Tile t : tiles) {
            t.clearSamples();
        }
    }
}