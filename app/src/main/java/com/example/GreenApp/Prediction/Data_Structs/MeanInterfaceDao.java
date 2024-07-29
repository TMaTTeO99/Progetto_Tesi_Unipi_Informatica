package com.example.GreenApp.Prediction.Data_Structs;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MeanInterfaceDao {

    @Query("SELECT * FROM Mean WHERE data = :data")
    Mean getByData(String data);

    /*@Query("SELECT * FROM Mean WHERE idChannel = :id1 OR idChannel = :id2")
    List<Mean> getDataFromId(String id1, String id2);*/

    @Query("SELECT * " +
           "FROM Mean " +
           "WHERE (idChannel = :id1 OR idChannel = :id2)" +
           "AND fieldName = :temp " +
           "AND data BETWEEN :startDate AND :endDate " +
           "ORDER BY DATE(data) ASC")
    List<Mean> getDataFromIdTemperature(String id1, String id2, String temp, String startDate, String endDate);

    @Query("SELECT * " +
            "FROM Mean " +
            "WHERE (idChannel = :id1 OR idChannel = :id2)" +
            "AND fieldName = :irrad " +
            "AND data BETWEEN :startDate AND :endDate " +
            "ORDER BY DATE(data) ASC")
    List<Mean> getDataFromIdIrradiance(String id1, String id2, String irrad, String startDate, String endDate);

    @Query("SELECT * " +
            "FROM Mean " +
            "WHERE (idChannel = :id1 OR idChannel = :id2) " +
            "AND fieldName = :choice " +
            "AND data BETWEEN :startDate AND :endDate " +
            "ORDER BY DATE(data) ASC")
    List<Mean> getDataFromIdChoice(String id1, String id2, String choice, String startDate, String endDate);



    @Query("DELETE FROM Mean WHERE idChannel = :id1 OR idChannel = :id2")
    void deleteFromID(String id1, String id2);

    @Query("DELETE FROM Mean")
    void deleteAll();

    @Query("SELECT * FROM Mean")
    List<Mean> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Mean... mean);

    @Delete
    void delete(Mean mean);
}
