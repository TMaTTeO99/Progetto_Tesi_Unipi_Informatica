package com.example.GreenApp.Alert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.content.ContextCompat;

//classe che mi permette all'avvio del dispositivo di avviare il servizio in background
public class StartActivityOnBootReceiver extends BroadcastReceiver {

    @Override
    /**
     * funzione eseguita quando riavvio il dispositivo o lo accendo e si completa la fase di boot iniziale
     * mi garantisce che in caso di notifiche attive esse ripartiranno in automatico
     */
    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //ForegroundService.stoptimer();
            Intent serviceIntent = new Intent(context, ForegroundService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(serviceIntent);
            } else {
                // Pre-O behavior.
                context.startService(serviceIntent);
            }
            //ContextCompat.startForegroundService(context, serviceIntent);
            Log.d("STARTACTIVITYONBOOT","BOOT COMPLETATO");
        }
    }
}
