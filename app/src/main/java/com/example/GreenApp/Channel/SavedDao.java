package com.example.GreenApp.Channel;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 *
 */

@Dao
public interface SavedDao {
    /**
     *
     * @return lista di tutti i channel presenti nel database
     */
    @Query("SELECT * FROM savedValues")
    List<savedValues> getAll();

    /**
     * funzione che inserisce il channel nel database
     * @param savedValues: channel da inserire
     */
    @Insert
    void insert(savedValues savedValues);

    /**
     * cancella tutti gli elementi inseriti nel database
     */
    @Query("DELETE FROM savedValues")
    void deleteAll();

    /**
     * cancella il singolo channel
     * @param savedValues: channel da cancellare
     */
    @Delete
    void delete(savedValues savedValues);
}