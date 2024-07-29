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

/**
 * Modifiche aggiunte da Matteo Torchia 599899
 * Aggiungo un altro metodo per poter effettuare la ricerca del canale Tramite ID2 e Key2
 */

@Dao
public interface ChannelDao {
    /**
     *
     * @return lista di tutti i channel presenti nel database
     */
    @Query("SELECT * FROM channel")
    List<Channel> getAll();

    /**
     * trova il channel presente nel database che rispettata tutti i parametri
     * @param first: id del channel
     * @param last: chiave di lettura
     * @return il channel associato
     */
    @Query("SELECT * FROM channel WHERE id_key LIKE :first AND "
            + "read_key LIKE :last LIMIT 1")
    Channel findByName(String first, String last);


    @Query("SELECT * FROM channel WHERE lett_id_2 LIKE :first AND "
            + "lett_read_key_2 LIKE :last LIMIT 1")
    Channel findBySecondName(String first, String last);

    /**
     * inserisce un insieme di channel
     * @param channels: lista di channel da inserire
     */
    @Insert
    void insertAll(Channel... channels);

    /**
     * funzione che inserisce il channel nel database
     * @param channel: channel da inserire
     */
    @Insert
    void insert(Channel channel);

    /**
     * cancella tutti gli elementi inseriti nel database
     */
    @Query("DELETE FROM Channel")
    void deleteAll();

    /**
     * cancella il singolo channel
     * @param channel: channel da cancellare
     */
    @Delete
    void delete(Channel channel);
}