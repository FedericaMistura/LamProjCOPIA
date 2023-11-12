package com.example.lamproj.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

/*
    rappresenta un oggetto di dati per immagazzinare campioni.
    Associata a un database Room per la persistenza di dati
 */
@Entity
public class Sample {

    public Sample(){
        //Generazione del valore univoco uid utilizzando l'orologio di sistema
        //memorizzazione del timestamp
        //in questo modo riesco a comprendere quale è il più recente
        uid= (int) (System.currentTimeMillis() & 0xfffffff);
        time= System.currentTimeMillis();
    }

    @PrimaryKey
    public int uid; //identificatore unico per il campione

    @ColumnInfo(name = "time")
    public long time; //Timestamp che indica il momento in cui il campione è stato acquisito

    @ColumnInfo(name = "latitude")
    public double latitude; //latitudine del luogo in cui è stata rilevata la misurazione

    @ColumnInfo(name = "longitude")
    public double longitude; //longitudine del luogo in cui è stata rilevata la misurazione


    @ColumnInfo(name = "lte")
    public double lte; //valore lte acquisito durante la rilevazione

    @ColumnInfo(name = "wifi")
    public double wifi; //valore wifi acquisito durante la rilevazione

    @ColumnInfo(name = "noise")
    public double noise; //valore rumore acquisito durante la rilevazione

    public LatLng getLatLng(){
        return new LatLng( latitude,longitude);
    }
    public long getNegativeTime(){
        return -time;
    }
}