package com.example.lamproj.tiles;

import static com.example.lamproj.gmap.MapManager.VIEW_LTE;
import static com.example.lamproj.gmap.MapManager.VIEW_NOISE;
import static com.example.lamproj.gmap.MapManager.VIEW_WIFI;

import android.graphics.Color;

import com.example.lamproj.App;
import com.example.lamproj.data.Sample;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;

import java.util.Comparator;

import java.util.List;

import java.util.stream.Stream;
/*
Classe per rappresentare l'esagono
 */
public class Tile {
    public LatLng center;
    public double radiusInMeters;
    private PolygonOptions polygon;
    private double avgLte=0;
    private double avgWifi=0;
    private double avgNoise=0;
    private ArrayList<Sample> samples = new ArrayList<Sample>();
    public boolean notificationTimeSent = false;
    public int id = 0;

    public Tile (LatLng c, double r) {
        center=c;
        radiusInMeters=r;
    }

    public Tile (Double lat, Double lon, double r) {
        center=new LatLng(lat,lon);
        radiusInMeters=r;
    }
    /*
    Aggiunta sample, ricalcolo medie
     */
    public void addSample(Sample s) {
        samples.add(s);
        recalcAvg();
        notificationTimeSent = false;
    }
    //Eliminazione di tutti i samples, ricalcolo medie
    public void clearSamples() {
        samples.clear();
        recalcAvg();
    }
    /*
    Calcolo delle medie prendendo gli ultimi n valori
    registrati. N può essere deciso dall'utente nei settings
     */
    protected  void recalcAvg() {
        if (samples.size() == 0) {
            avgLte = 0;
            avgWifi = 0;
            avgNoise = 0;
        } else {
            //calcoliamo la media
            // nota: generiamo dall'ArrayList samples uno stream per ogni utilizzo

            Stream<Sample> st1,st2,st3; // stream che contengono gli elementi sui quali calcolare la media
            if (samples.size()<=App.A.nMeasurementsForAverage) {
                // se la lista contiene meno elementi del massimo previsto, li prendiamo tutti
                st1=samples.stream();
                st2=samples.stream();
                st3=samples.stream();
            } else {
                // se la lista contiene più elementi del massimo previsto
                // li ordiniamo in ordine inverso rispetto al campo "time", preso con il segno negativo
                // fornito dalla funzione getNegativeTime, in questo modo avremo gli elementi più recenti in cima alla lista
                // di questi ci prendiamo il numero di elementi previsto da App.A.nMeasurementsForAverage
                st1=samples.stream().sorted(Comparator.comparingLong(Sample::getNegativeTime)).limit(App.A.nMeasurementsForAverage);
                st2=samples.stream().sorted(Comparator.comparingLong(Sample::getNegativeTime)).limit(App.A.nMeasurementsForAverage);
                st3=samples.stream().sorted(Comparator.comparingLong(Sample::getNegativeTime)).limit(App.A.nMeasurementsForAverage);
            }

            // facciamo la media degli elementi dello stream appropriato

            avgLte =st1.mapToDouble(e -> e.lte).average().getAsDouble();
            avgWifi = st2.mapToDouble(e -> e.wifi).average().getAsDouble();
            avgNoise = st3.mapToDouble(e -> e.noise).average().getAsDouble();
        }
    }

    /*
    Colora correttamente l'esagono in base al tipo di vista (WiFi, LTE, noise)
    secondo la media calcolata per ogni tipo di rilevazione.
     */
    public int getProperFillColor(int viewType){
        int color= App.A.colorNone;
        if (samples.size()>0){
            switch (viewType) {
                case VIEW_WIFI:
                    if (avgWifi<App.A.wifiLow)
                        color=App.A.colorLow;
                    else if (avgWifi<App.A.wifiMid)
                        color=App.A.colorMid;
                    else
                        color=App.A.colorHigh;
                    break;
                case VIEW_LTE:
                    if (avgLte<App.A.lteLow)
                        color=App.A.colorLow;
                    else if (avgLte<App.A.lteMid)
                        color=App.A.colorMid;
                    else
                        color=App.A.colorHigh;
                    break;
                case VIEW_NOISE:
                    if (avgNoise<App.A.noiseLow)
                        color=App.A.colorLow;
                    else if (avgNoise<App.A.noiseMid)
                        color=App.A.colorMid;
                    else
                        color=App.A.colorHigh;
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

    private PolygonOptions toPolygon(){
        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(createHexagon(center,radiusInMeters))
                .strokeColor(Color.GREEN);

        return polygonOptions;
    }
    /*
    Creazione dell'area identificata come un esagono
     */
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
    public boolean hasRecentSamples(double seconds){
        if(samples.size() > 0){
            Sample recent = samples.stream().sorted(Comparator.comparingLong(Sample::getNegativeTime)).findFirst().get();

            long currentTime = System.currentTimeMillis();

            return ((currentTime - recent.time) <= (seconds * 1000));
        } else {
            return false; //Non ci sono sample recenti
        }

    }

    /*
    Calcola la distanza in metri tra il centro dell'esagono e il punto di rilevazione.
    In questo modo si può capire dentro quale area ricade il punto.
     */
    public double distanceFrom(double latitude, double longitude){
        //distanze in metri
        return SphericalUtil.computeDistanceBetween(center, new LatLng(latitude, longitude));
    }

}
