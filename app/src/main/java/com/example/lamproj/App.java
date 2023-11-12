package com.example.lamproj;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

import com.example.lamproj.data.SampleDB;
import com.example.lamproj.data.SampleDao;
import com.example.lamproj.gmap.MapManager;
import com.example.lamproj.sensors.SensorHub;
import com.example.lamproj.tiles.TileGrid;

import java.util.Map;

/*
Classe utilizzata per inizializzare e mantenere oggetti e risorse
globali (DB, MapManager e SensorHub) a livello di applicazione.
 */
public class App extends Application{
    public SampleDB db;
    public MapManager mapManager;
    public SensorHub sensorHub;
    public MainActivity context;
    public LocationService locationService;

    public static App A; //Accesso globale all'istanza

    //Valori di taglio
    public double lteLow=2; //inferiore di 2 è basso
    public double lteMid=4; //2 e 3 medio, 4 e 5 buono.
    public double wifiLow=-71;
    public double wifiMid=-50;
    public double noiseLow=-10;
    public double noiseMid=-50;

    //Setting colori
    public int colorNone=0x00000000;
    public int colorLow=0x30FF0000;
    public int colorMid=0x30FFFF00;
    public int colorHigh=0x3000FF00;
    public boolean auto_recording=false;
    public double auto_recording_meters=100;
    public double auto_recording_seconds = 60;
    public  double radiusInMeters = 500;
    public double zoneSize = 30000;
    public int nMeasurementsForAverage = 10;
    public double last_measurement_seconds = 10;
    private static final String CHANNEL_ID = "LamProjApp";
    NotificationCompat.Builder notificationBuilder = null;
    NotificationManager manager = null;

    /*
    Singleton A permette accesso centralizzato e globale a tutte le risorse
    e gli occetti dell'applicazione
     */
    public App() {
        A=this;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /*
    Chiamato quando l'app viene inizializzata
     */
    @Override
    public void onCreate() {
        super.onCreate();
        /*
        Inizializza il database utilizzando Room
         */
        db = Room.databaseBuilder(getApplicationContext(),
                SampleDB.class, "samples").allowMainThreadQueries().build();
        mapManager=new MapManager(); //Creazione istanza mapManager
        loadSettings();
        sensorHub=new SensorHub();  //Inizializzazione sensori

        NotificationChannel chan = new NotificationChannel(
                CHANNEL_ID,
                "LamProj Application",
                NotificationManager.IMPORTANCE_LOW);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_SECRET);

        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(chan);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);


    }

    public void sendNotification(int id, String text) {
        if (notificationBuilder != null) {
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(text)
                    .setPriority(NotificationManager.IMPORTANCE_LOW)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setChannelId(CHANNEL_ID)
                    .build();
            manager.notify(id,notification);
        }
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /*
    Caricamento impostazioni dell'applicazione che possono essere modificate dall'utente

     */
    public void loadSettings(){
        Map<String,?> pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getAll();

        this.auto_recording = (boolean) getSetting(pref, "auto_sample_recording", false);
        this.auto_recording_meters = getDoubleSetting(pref, "auto_sample_recording_distance",100.0);
        this.auto_recording_seconds = getDoubleSetting(pref, "auto_sample_recording_time", 100.0);

        this.radiusInMeters = getDoubleSetting(pref, "cell_size_meters", 100.0);
        this.zoneSize = getDoubleSetting(pref, "zone_size_meters", 30000.0);
        this.nMeasurementsForAverage = getIntSetting(pref, "n_measurement_average", 10);

        this.colorLow = (int) getSetting(pref, "color_low", 0x30FF0000);
        this.colorMid = (int) getSetting(pref, "color_mid", 0x30FFFF00);
        this.colorHigh = (int) getSetting(pref, "color_high", 0x3000FF00);
        this.last_measurement_seconds = getDoubleSetting(pref, "last_measurement_seconds", 10.0);

    }

    /*
    Per ottenere i valori specifici dal Map delle preferenze
     */
    public static Object getSetting(Map<String,?> map, String settingName, Object defaultValue) {
        if (map.containsKey(settingName)) {
            return  map.get(settingName);
        } else return defaultValue;
    }

    public static double getDoubleSetting(Map<String,?> map, String settingName, Double defaultValue) {
        if (map.containsKey(settingName)) {
            return  Double.parseDouble((String) map.get(settingName));
        } else return defaultValue;
    }

    public static int getIntSetting(Map<String,?> map, String settingName, int defaultValue) {
        if (map.containsKey(settingName)) {
            return  Integer.parseInt((String) map.get(settingName));
        } else return defaultValue;
    }

}