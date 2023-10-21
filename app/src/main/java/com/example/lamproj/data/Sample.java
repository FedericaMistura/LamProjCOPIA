package com.example.lamproj.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Sample {

    public Sample(){
        //in questo modo riesco a comprendere quale è il più recente
        uid= (int) (System.currentTimeMillis() & 0xfffffff);
        time= System.currentTimeMillis();
    }

    @PrimaryKey
    public int uid;

    @ColumnInfo(name = "time")
    public long time;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "lte")
    public double lte;

    @ColumnInfo(name = "wifi")
    public double wifi;

    @ColumnInfo(name = "noise")
    public double noise;

}