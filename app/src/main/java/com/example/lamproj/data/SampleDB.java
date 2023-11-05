package com.example.lamproj.data;

import android.media.AudioRecord;
import android.util.Log;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.lamproj.App;

import java.util.List;

@Database(entities = {Sample.class}, version = 1, exportSchema = false)
public abstract class SampleDB extends RoomDatabase {
    public abstract SampleDao sampleDao();

    protected  SampleDao dao;
    public SampleDB() {
        dao = sampleDao();
    }

    public Sample mostRecentSample;

    public void putSample(Sample s){
        new Thread(new Runnable() {
            @Override
            public void run() {
                dao.insertAll(s);
                mostRecentSample=s;
            }
        }).start();
    }

    public void getAllSamples(SampleDbListSampleResultInterface receiver){
        List<Sample> samples;
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Sample>  samples = dao.getAll();
                if (samples.size()>0) {
                    mostRecentSample= samples.get(samples.size() - 1);
                }
                receiver.onGetListSampleComplete(samples);
            }
        }).start();
    }
    public void deleteAllSamples(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Sample>  samples = dao.getAll();
                for (Sample x: samples) {
                    dao.delete(x);
                }
                mostRecentSample=null;
            }
        }).start();
    }


    public void deleteSamples(List<Sample> samples){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Sample x: samples) {
                    dao.delete(x);
                }
            }
        }).start();
    }


}