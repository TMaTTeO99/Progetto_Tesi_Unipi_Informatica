package com.example.GreenApp.Alert;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.MainActivity;
import com.example.firstapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 *
 * aumentato Alexander Kocian 3/4/2021, 6/8/2022 (remove notification)
 *
 */

/**
 * Modifiche fatte da Matteo Torchia 599899
 *
 */
public class ForegroundService extends Service {
    public String NOTIFICATION_CHANNEL_ID = "com.example.firstApp";
    public String channelName = "GreenApp background service";
    private static AppDatabase database;
    private static MyTimerTask myTimerTask;
    private static Timer timer;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Log.d("ForegroundService","servizio background creato");
        //recupero il database dei channel
        database = AppDatabase.getDataBase(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        scheduleTask();
        return START_STICKY;
    }

    /**
     * Metodo aggiunto da MAtteo Torchia 599899
     * Invece di utilizzare un thread normale che potrebbe
     * non essere svegliato dal sistema operativo per motici di
     * ottimizzazione delle risorse utilizzo un alarmmanager
     */
    private void scheduleTask() {

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ForegroundService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_MUTABLE);

        /*
        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000,60000, pi);
        * */

        manager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 50000,50000, pi);
    }

    /*@Override
    public void onStart(Intent intent, int startId) {
        Log.d("ForegroundServices", "SERVICE avvia");
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        //elimito tutte le strutture precedentemente create
        //stoptimer();

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, ForegroundService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_MUTABLE);
        manager.cancel(pi);

        stopForeground(true);
        Log.d("ForegroundServices","distruggo");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {


        String input = ("Notifications active");
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                0, notificationIntent, PendingIntent.FLAG_MUTABLE);
        Notification notification = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("GreenApp")
                .setContentText(input)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        //setto una variabile per capire se devo lanciare almeno un servizio oppure no
        // int k=0;
        //recupero dalla lista tutti i channel e se ne esiste almeno uno con le notifiche attive lo eseguo in background
        List<Channel> allchannel = database.getAllChannelStd();//ChannelDao().getAll();
        List<Channel> channelNotification = new ArrayList<>();

        //faccio una scansione di tutti i canali per vedere se c'è qualcuno con le notifiche attive
        for(int i = 0; i<allchannel.size(); i++) {
            Channel actualchannel = allchannel.get(i);
            //se ho le notifiche abilitata lo avvio
            if (actualchannel.getNotification()) {
                //inserisco i channel in un array
                channelNotification.add(actualchannel);
            }
        }

        //onCreate();
        myTimerTask = new MyTimerTask(channelNotification, context, database);
        timer = new Timer();
        timer.schedule(myTimerTask,10);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }
        else{
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
        }


        int flag = 0;  // notify only once 
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        if(activeNotifications!=null){     // check if notification does already exist in the statusbar 
            for(StatusBarNotification mynotification : activeNotifications ){
                if (mynotification.getId() == 1) {
                    flag = 1;
               }
            }
        }

        if (flag == 0) {
            startForeground(1, notification);
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}