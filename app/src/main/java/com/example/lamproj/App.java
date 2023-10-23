package com.example.lamproj;
import android.app.Application;
import android.content.res.Configuration;

import androidx.room.Room;

import com.example.lamproj.data.SampleDB;
import com.example.lamproj.data.SampleDao;
import com.example.lamproj.gmap.MapManager;
import com.example.lamproj.sensors.SensorHub;

/*
Classe utilizzata per inizializzare e mantenere oggetti e risorse
globali (DB, MapManager e SensorHub) a livello di applicazione.
 */
public class App extends Application{
    public SampleDB db;
    public MapManager mapManager;
    public SensorHub sensorHub;
    public MainActivity context;
    public static App A;

    public App() {
        A=this;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        /*
        Inizializza il database utilizzando Room
         */
        db = Room.databaseBuilder(getApplicationContext(),
                SampleDB.class, "samples").allowMainThreadQueries().build();
        mapManager=new MapManager();
        sensorHub=new SensorHub();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}