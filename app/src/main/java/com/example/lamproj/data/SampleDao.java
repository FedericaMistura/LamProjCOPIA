package com.example.lamproj.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SampleDao {

    /*
    Restituisce tutti i campioni presenti nel database ordinati in base al tempo
    In base al tempo perchè serve per calcolare la media delle ultime n misurazioni.
     */
    @Query("SELECT * FROM sample ORDER BY sample.time")
    List<Sample> getAll();

    @Insert
    void insertAll(Sample... samples);

    @Delete
    void delete(Sample sample);
}
