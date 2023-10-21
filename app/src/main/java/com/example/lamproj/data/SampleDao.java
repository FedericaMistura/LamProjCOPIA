package com.example.lamproj.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SampleDao {
    @Query("SELECT * FROM sample")
    List<Sample> getAll();

    /*
    @Query("SELECT * FROM sample WHERE uid IN (:userIds)")
    List<Sample> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM sample WHERE latitude LIKE :first AND " +
            "longitude LIKE :last LIMIT 1")
    Sample findByName(String first, String last);
*/
    @Insert
    void insertAll(Sample... samples);

    @Delete
    void delete(Sample sample);
}
