package com.example.GreenApp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Channel.ChannelDao;
import com.example.GreenApp.Channel.SavedDao;
import com.example.GreenApp.Channel.savedValues;
import com.example.GreenApp.Prediction.Data_Structs.Mean;
import com.example.GreenApp.Prediction.Data_Structs.MeanInterfaceDao;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 */

/**
 * dichiarazione del database con le due classi associate (channel,savedvalues)
 */

/**
 * Modifiche fatte da Matteo Torchia 599899
 * Aggiornato database con i dati neccessari a salvare le medie
 * dei valori da prevedere nell'activity di previsione.
 * Aggiunto meccanismo di sincronizzazione per la lettura e scrittura
 * del database
 * Rendo il database singleton in modo da recuperare in ogni parte del codice
 * la setssa istanza
 */
@Database(entities = {Channel.class, savedValues.class, Mean.class}, version = 10,exportSchema = false)


public abstract class AppDatabase extends RoomDatabase {
    public abstract ChannelDao ChannelDao();
    public abstract SavedDao SavedDao();
    public abstract MeanInterfaceDao Mean();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static AppDatabase INSTANCE = null;

    public static AppDatabase getDataBase(Context context){

        if(INSTANCE == null) {
            synchronized (AppDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "prodiction")
                            //consente l'aggiunta di richieste nel thred principale
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            //build mi serve per costruire il tutto
                            .build();
                }
            }
        }
        return INSTANCE;
    }


    /**
     * Metodi per le chiamate al DB fatte per SavedDao
     */

    //utilizzo un oggetto future per ritornare al chiamante il valore recuperato dal DB
    public List<savedValues> getChannelsSaved() {

        Future<List<savedValues>> future = executor.submit(new Callable<List<savedValues>>() {
            @Override
            public List<savedValues> call() {
                return SavedDao().getAll();
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
    public void insertChannelSaved(final savedValues saved) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                SavedDao().insert(saved);
            }
        });
    }
    public void delateAllSaved(){

        executor.execute(new Runnable() {
            @Override
            public void run() {
                SavedDao().deleteAll();
            }
        });
    }

    public void delateSaved(final savedValues saved) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                SavedDao().delete(saved);
            }
        });
    }

    /**
     * Metodi per le chiamate al DB fatte per ChannelDao
     */

    public void insertChannelStd(final Channel channel) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChannelDao().insert(channel);
            }
        });
    }
    public void delateAllStd(){

        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChannelDao().deleteAll();
            }
        });
    }
    public void delateChannelStd(final Channel channel) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                ChannelDao().delete(channel);
            }
        });
    }

    //utilizzo un oggetto future per ritornare al chiamante il valore recuperato dal DB
    public Channel getChannelStd(final String id, String k, int option) {

        Future<Channel> future = executor.submit(new Callable<Channel>() {
            @Override
            public Channel call() {
                if(option == 0) return ChannelDao().findByName(id, k);
                else return ChannelDao().findBySecondName(id, k);
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public List<Channel> getAllChannelStd() {

        Future<List<Channel>> future = executor.submit(new Callable<List<Channel>>() {
            @Override
            public List<Channel> call() {
                return ChannelDao().getAll();
            }
        });
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }


    /**
     * Metodi utilizati per inserimento e eliminazione dei dati sulle medie
     */
    public List<Mean> getAllMean(){

        Future<List<Mean>> future = executor.submit(new Callable<List<Mean>>() {
            @Override
            public List<Mean> call() {return Mean().getAll();}
        });
        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e){
            return null;
        }
    }


    public List<Mean> getDataFromBDTemperature(String id1, String id2, String temperature, String start, String end) {

        Future<List<Mean>> future = executor.submit(new Callable<List<Mean>>() {
            @Override
            public List<Mean> call() {return Mean().getDataFromIdTemperature(id1, id2, temperature, start, end);}
        });
        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e){
            return null;
        }

    }
    public List<Mean> getDataFromBDIrradiance(String id1, String id2, String irradiance, String start, String end) {

        Future<List<Mean>> future = executor.submit(new Callable<List<Mean>>() {
            @Override
            public List<Mean> call() {return Mean().getDataFromIdIrradiance(id1, id2, irradiance, start, end);}
        });
        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e){
            return null;
        }

    }
    public List<Mean> getDataFromBDChoice(String id1, String id2, String choice,String start, String end) {

        Future<List<Mean>> future = executor.submit(new Callable<List<Mean>>() {
            @Override
            public List<Mean> call() {return Mean().getDataFromIdChoice(id1, id2, choice, start, end);}
        });
        try {
            return future.get();
        }
        catch (InterruptedException | ExecutionException e){
            return null;
        }

    }
    public void insertAllMean(Mean means){

        executor.execute(new Runnable() {
            @Override
            public void run() {Mean().insertAll(means);}
        });
    }

    public void deleteMeanFromID(String id1, String id2){

        executor.execute(new Runnable() {
            @Override
            public void run() {Mean().deleteFromID(id1, id2);}

        });
    }

    public void deleteAllMean(){

        executor.execute(new Runnable() {
            @Override
            public void run() {Mean().deleteAll();}

        });
    }


}
