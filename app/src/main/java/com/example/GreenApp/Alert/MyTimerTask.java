package com.example.GreenApp.Alert;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.ChannelFieldsSelected;
import com.example.GreenApp.MainActivity;
import com.example.firstapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

import static com.example.GreenApp.Alert.App.CHANNEL_1_ID;

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
 * Modifiche fatte da MAtteo Torchia 599899
 * Modifico il codice per controllare anche il secondo canale
 */

public class MyTimerTask extends TimerTask {
    private static List<Channel> channel;
    private static Context cont;
    public NotificationManagerCompat notificationManager;
    private static AppDatabase db;
    private int minuti=0;
    private String ID_irrigation_drainage = null;
    private ChannelFieldsSelected checkFlags = new ChannelFieldsSelected();
    /**
     * metodo costruttore
     * @param chan: lista contenente tutti i channel
     * @param context: context di riferimento
     * @param database:database utilizzato
     */
    public MyTimerTask(List<Channel> chan, Context context, AppDatabase database) {
        channel=chan;
        db=database;
        cont=context;
        notificationManager = NotificationManagerCompat.from(cont);
    }

    /**
     * funzione eseguita all'avvio del Task
     */
    @Override
    public void run() {
        //recupero la lista e controllo lo stato dei channel con il database
        for(int i=0;i<channel.size();i++) {

            Channel actualchannel = null;
            String id1 = channel.get(i).getLett_id();
            String id2 = channel.get(i).getLett_id_2();

            if(id1 != null)actualchannel = db.getChannelStd(id1, channel.get(i).getLett_read_key(), 0);//db.ChannelDao().findByName(id1, channel.get(i).getLett_read_key());
            else if(id2 != null)actualchannel = db.getChannelStd(id2, channel.get(i).getLett_read_key_2(), 1);//db.ChannelDao().findBySecondName(id2, channel.get(i).getLett_read_key_2());

            //uso le due url per recuperare i dati dai due canali eventuali
            String url_1 = null;
            String url_2 = null;


            //con questa chiamata posso recuperare dalla risposta sia last_data_age che id del canale e poter effettuare la chiamata corretta successivamente
            if(id1 != null) url_1 = "https://api.thingspeak.com/channels/" + id1 + "/feeds.json/?api_key=" + actualchannel.getLett_read_key() + "&results=1";
            if(id2 != null) url_2 = "https://api.thingspeak.com/channels/" + id2 + "/feeds.json/?api_key=" + actualchannel.getLett_read_key_2() + "&results=1";
            
            //String u = "https://api.thingspeak.com/channels/" + actualchannel.getLett_id() + "/feeds/last_data_age.json?api_key=" + actualchannel.getLett_read_key();
            getlasttime(url_1, url_2, actualchannel, id1, id2);
        }
    }

    /**
     * metodo per reperire la data dell'ultimo elemento inserito
     * 
     * @param url_1
     * @param url_2
     * @param channel
     * @param id1
     * @param id2
     */
    private void getlasttime(String url_1, String url_2, final Channel channel, String id1, String id2){

        //variabile per sapere dentro il codice di risposta a quale chiamata mi trovo
        //necessario usare un array final per avere visibilità dentro il codice del JsonObjectRequest

        final int[] idxToCall = {0};
        HashMap<Integer, Boolean> flag = new HashMap<>();
        HashMap<Integer, Boolean> flag_insert = new HashMap<>();
        HashMap<Integer, Boolean> flag_delate = new HashMap<>();
        String urlT0Call = null;

        /**
         * Controllo se verranno effettuate due chiamate o una sola per decidere quando eliminare il channel dal DB
         */
        if(url_1 != null) {
            flag.put(0, true);
            flag_insert.put(0, true);
            flag_delate.put(0, true);
        }
        else{
            flag.put(0, false);
            flag_insert.put(0, false);
            flag_delate.put(0, false);
        }

        if(url_2 != null){
            flag.put(1, true);
            flag_insert.put(1, true);
            flag_delate.put(1, true);
        }
        else {
            flag.put(1, false);
            flag_insert.put(1, false);
            flag_delate.put(1, false);
        }

        for(int i = 0; i<2; i++) {

            if (i == 0) urlT0Call = url_1;
            else urlT0Call = url_2;

            if (urlT0Call != null) {

                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlT0Call, null,

                        new Response.Listener<JSONObject>() {
                            int minuti=0;
                            @Override
                            public void onResponse(JSONObject response) {
                                try {

                                    idxToCall[0]++;//incremento la variabile per capire a quale risposta mi trovo

                                    //recupero l'id del canale che mi ha dato la risposta
                                    String idFromServer = response.getJSONObject("channel").get("id").toString();

                                    //recupero il valore ottenuto
                                    JSONArray jsonArray = response.getJSONArray("feeds");

                                    //effettuo il calcolo del last_data_age
                                    String cretime = retreiveLastDataAge((JSONObject) jsonArray.get(0));


                                    minuti= Integer.parseInt(cretime);
                                    minuti =(minuti /60)+1;

                                    //controllo se devo eliminare dal DB il canale
                                    if(idxToCall[0] == 1){
                                        if(!flag.get(1)) db.delateChannelStd(channel);//db.ChannelDao().delete(channel);
                                    }
                                    else db.delateChannelStd(channel);

                                    //se sono alla prima risposta allora setto il minutaggio della prima chiamata
                                    if(idFromServer.equals(id1))channel.setMinutes((double)minuti);
                                    else channel.setMinutes_cannel_2((double)minuti);//altrimenti il minutaggio della seconda (secondo canale)


                                    //Controllo se va inserito in questa chiamata o nell'eventuale chiamata successiva nel DB
                                    if(idxToCall[0] == 1){
                                        if(!flag.get(1)) db.insertChannelStd(channel);//db.ChannelDao().insert(channel);
                                    }
                                    else db.insertChannelStd(channel);

                                    int dist = 0;

                                    //se l'utente non ha settato il range di tempo per la media conto come distanza il tempo dall'ultimo valore
                                    if(idFromServer.equals(id1))dist = channel.getLastimevalues() + minuti;
                                    else dist = channel.getLastimevalues_channel_2() + minuti;


                                    String url = null;

                                    if(idFromServer.equals(id1)) url = "https://api.thingspeak.com/channels/" + channel.getLett_id() + "/feeds.json?api_key=" + channel.getLett_read_key()
                                            + "&minutes=" + dist + "&offset=" + com.example.GreenApp.Graphic.MainActivity.getCurrentTimezoneOffset();
                                    else url = "https://api.thingspeak.com/channels/" + channel.getLett_id_2() + "/feeds.json?api_key=" + channel.getLett_read_key_2()
                                            + "&minutes=" + dist + "&offset=" + com.example.GreenApp.Graphic.MainActivity.getCurrentTimezoneOffset();

                                    if(channel.getNotification()){
                                        Log.d("MYTIMERTASK","AVVIO CHANNEL: " + channel.getLett_id() + " " + channel.getLett_id_2());
                                        getJsonResponse(url, channel, flag, flag_insert, id1);
                                    }
                                    Log.d("URL", url);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Thread background", "errore download getlasttime");
                    }
                });
                Volley.newRequestQueue(cont).add(jsonObjectRequest);


            }
        }
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Calcolo il last_data_age dell'ultimo valore recuperato dal canale
     */
    private String retreiveLastDataAge(JSONObject value){

        try {
            String data = value.get("created_at").toString();

            Instant dataIniziale = Instant.parse(data);
            Instant ora = Instant.now();
            Duration durata = Duration.between(dataIniziale, ora);

            return String.valueOf(durata.getSeconds());
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * metodo per reperire le risposte json
     * @param urlString:indirizzo per reperire i valori (precedentemente settato in getlasttime)
     * @param channel:channel di riferimento
     */
    private void getJsonResponse(String urlString, final Channel channel, HashMap<Integer, Boolean> flag_insert, HashMap<Integer, Boolean> flag_delate,
                                 String id1) {


        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public synchronized void onResponse(JSONObject response) {

                        try {
                            //recupero l'array feeds
                            JSONArray jsonArray = response.getJSONArray("feeds");

                            //recupero i fields associati al channel
                            ArrayList<String> fields = new ArrayList<String>();
                            ArrayList<String> fieldsKey = new ArrayList<String>();

                            int dim = response.getJSONObject("channel").length();
                            Log.d("Thread background", "download eseguito getJsonResponse");

                            //salvo tutti i field nell'array
                            for (int i = 0; i < dim; i++) {

                                try {
                                    fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i + 1))));
                                    fieldsKey.add("field" + (i + 1));
                                }
                                catch (Exception e) { Log.d("Thread background", "error JSONObject");}
                            }


                            //inizializzo le variabili
                            Double t = 0.0;
                            Double somt=0.0;
                            Double u = 0.0;
                            Double somu=0.0;
                            Double p = 0.0;
                            Double somp=0.0;
                            Double c = 0.0;
                            Double somc=0.0;
                            Double ir = 0.0;
                            Double somir=0.0;
                            Double ev = 0.0;
                            Double somev=0.0;
                            Double so = 0.0;
                            Double somso=0.0;

                            //variabili aggiunte da Matteo Torchia 599899
                            Double pesoPianta = 0.0;
                            Double sPesoPianta = 0.0;

                            Double vento = 0.0;
                            Double sVento = 0.0;

                            //stringa che mi salva l'ultimo data di aggiornamento dei valori
                            String cretime=null;
                            Channel v = channel;
                            String idFromServer = response.getJSONObject("channel").get("id").toString();

                            //scorro tutto l'array
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //recupero il primo oggetto dell'array
                                final JSONObject value = jsonArray.getJSONObject(i);

                                String field = null;

                                try{
                                    field = setDataFieldCorrect(fields, value, fieldsKey, "temperature", 1, v);
                                    //field = retreiveValue(fields.get(0), "temperature", v, value, 1 ,1, fields, 0);
                                    t = t + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    somt++;
                                }
                                catch (Exception e){}
                                try {
                                    field = setDataFieldCorrect(fields, value, fieldsKey, "P0", 2, v);
                                    //field = retreiveValue(fields.get(0), "P0", v, value, 2, 1, fields, 8);
                                    pesoPianta = pesoPianta + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    sPesoPianta++;
                                }
                                catch (Exception e){}

                                try {

                                    field = setDataFieldCorrect(fields, value, fieldsKey, "humidity", 3, v);
                                    //field = retreiveValue(fields.get(1), "humidity", v, value, 3, 2, fields, 1);
                                    u=u+(Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    somu++;
                                }
                                catch (Exception e){}
                                try {

                                    field = setDataFieldCorrect(fields, value, fieldsKey, "windspeed", 8, v);
                                    //field = retreiveValue(fields.get(1), "windspeed", v, value, 8, 2, fields, 9);
                                    vento = vento + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    sVento++;
                                }
                                catch (Exception e){}

                                try {
                                    field = setDataFieldCorrect(fields, value, fieldsKey, "pH_value", 4, v);
                                    //field = retreiveValue(fields.get(2), "pH_value", v, value, 4, 3, fields, 2);
                                    p = p + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    somp++;
                                }
                                catch (Exception e){}

                                try {
                                    field = setDataFieldCorrect(fields, value, fieldsKey, "electric_conductivity", 5, v);
                                    //field = retreiveValue(fields.get(3), "electric_conductivity", v, value, 5, 4, fields, 3);
                                    c = c + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    somc++;
                                }
                                catch (Exception e){}
                                try {

                                    field = setDataFieldCorrect(fields, value, fieldsKey, "irradiance", 6, v);
                                    //field = retreiveValue(fields.get(4), "irradiance", v, value, 6, 5, fields, 4);
                                    ir = ir + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                    somir++;

                                }
                                catch (Exception e){}
                                try {

                                    field = setDataFieldCorrect(fields, value, fieldsKey, "moisture", 7, v);
                                    //field = retreiveValue(fields.get(5), "moisture", v, value, 7, 6, fields, 5);
                                    so=so+Math.round(Double.parseDouble(String.format(field)));
                                    somso++;

                                }
                                catch (Exception e){}
                                try {

                                    //se ho impostato un valore, inserisci quello,altrimenti non scrivo nulla
                                    String name = null;
                                    if((name = v.getImagepeso()) != null){

                                        String val = value.getString(fieldsKey.get(fields.indexOf(name)));

                                        if(val != null && !val.isEmpty() && !val.equals("null")){

                                            field = val;
                                            ev=ev+(Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                            somev++;

                                            checkFlags.getMap().put(field, true);
                                        }
                                    }
                                    else {

                                        if(fields.contains("irrigation") && fields.contains("drainage")){

                                            String idCur = response.getJSONObject("channel").get("id").toString();
                                            String url = null, id = null;

                                            if(idCur.equals(channel.getLett_id())){
                                                id = channel.getLett_id();
                                                url = "https://api.thingspeak.com/channels/"+channel.getLett_id()+"/fields/7-8.json?api_key=" + channel.getLett_read_key();
                                            }
                                            else {
                                                id = channel.getLett_id();
                                                url = "https://api.thingspeak.com/channels/"+channel.getLett_id_2()+"/fields/7-8.json?api_key=" + channel.getLett_read_key_2();
                                            }
                                            downloadEvapotraspirazione(url,channel, id);
                                        }
                                    }
                                }
                                catch (Exception e){}

                                try {
                                    cretime = value.getString("created_at");
                                    minuti=(distanza(cretime)/60)+2;
                                }catch (Exception e){ }
                            }

                            //setto la distanza in minuti approssimata ad un minuto in più nel database del channel utilizzato


                            checkDelate(flag_delate, db, v);

                            if(idFromServer.equals(id1))v.setMinutes((double)minuti);
                            else v.setMinutes_cannel_2((double)minuti);//altrimenti il minutaggio della seconda (secondo canale)

                            checkInsert(flag_insert, db, v);

                            Log.d("ALERTACTIVITY/MINUTES:",String.valueOf((double) minuti));

                            //calcolo la media di tutti i valori e la confronto con i miei valori,se la supera invio la notifica
                            t=Math.round(t/somt * 100.0) / 100.0;
                            u=Math.round(u/somu * 100.0) / 100.0;
                            p=Math.round(p/somp * 100.0) / 100.0;
                            c=Math.round(c/somc * 100.0) / 100.0;
                            ir=Math.round(ir/somir * 100.0) / 100.0;
                            ev=Math.round(ev/somev * 100.0) / 100.0;
                            so=Math.round(so/somso * 100.0) / 100.0;

                            vento = Math.round(vento/sVento * 100.0) / 100.0;
                            pesoPianta = Math.round(pesoPianta/sPesoPianta * 100.0) / 100.0;

                            Log.d("SOMMA VALORI: ","t:"+somt+" u:"+ somu +" ph:"+ somp +" c:"+ somc +" ir:"+ somir +" ev:"+ somev + " so:"+ somso + " svento:" + sVento + " spesoPianta:" + sPesoPianta);
                            Log.d("MEDIA VALORI: ","t:"+t+" u:"+ u +" ph:"+ p +" c:"+ c +" ir:"+ ir  +" ev:"+ ev +" so:"+ so + " vento:" + vento + " pesoPianta:" + pesoPianta);

                            //invio le notifiche se i valori non rispettano le soglie imposte
                            if(channel.getNotification()) {

                                //controllo se ho letto effettivamente dei valori
                                if(somt!=0) notification(t,channel.getImagetemp(),channel.getTempMin(),channel.getTempMax(),channel,1,"temperature", 1);
                                if(somu!=0) notification(u,channel.getImageumid(),channel.getUmidMin(),channel.getUmidMax(),channel,2,"humidity", 3);
                                if(somp!=0) notification(p,channel.getImageph(),channel.getPhMin(),channel.getPhMax(),channel,3,"pH_value", 4);
                                if(somc!=0) notification(c,channel.getImagecond(),channel.getCondMin(),channel.getCondMax(),channel,4,"electric_conductivity", 5);
                                if(somir!=0) notification(ir,channel.getImageirra(),channel.getIrraMin(),channel.getIrraMax(),channel,5,"irradiance", 6);
                                if(somev!=0) notification(ev,channel.getImagepeso(),channel.getPesMin(),channel.getPesMax(),channel,6,"evapotranspiration", 9);
                                if(somso!=0) notification(so,channel.getImagesoil(),channel.getSoilMin(),channel.getSoilMax(),channel,7,"moisture", 7);
                                if(sPesoPianta!=0) notification(pesoPianta,channel.getImagePesoPianta(),channel.getPesPiantaMin(),channel.getPesPiantaMax(),channel,7,"P0", 2);
                                if(sVento!=0)notification(vento,channel.getImageVento(),channel.getVentoMin(),channel.getVentoMax(),channel,7,"windspeed", 8);
                                try{
                                    Log.d("TEMPO:","distanza settata: "+channel.getTempomax()*60+" distanza attuale: "+ distanza(cretime));
                                    if (channel.getTempomax() != 0 && distanza(cretime) > channel.getTempomax()*60){

                                        String id_1 = channel.getLett_id();
                                        String id_2 = channel.getLett_id_2();
                                        id_1 = id_1 == null ? "" : id_1;
                                        id_2 = id_2 == null ? "" : id_2;
                                        if(id_1 != null && id_2 != null) printnotify("Channel(" + id_1 + "," + id_2 + ") tempo alto!", 13*Integer.valueOf(channel.getLett_id()));
                                        else if(id_1 == null && id_2 != null)printnotify("Channel(" + id_2 + ") tempo alto!", 13*Integer.valueOf(id_2));
                                        else if(id_1 != null && id_2 == null)printnotify("Channel(" + id_1 + ") tempo alto!", 13*Integer.valueOf(id_1));

                                    }

                                }catch (Exception e) {
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Thread background", "errore download getJsonResponse");
            }
        });
        Volley.newRequestQueue(cont).add(jsonObjectRequest);
    }


    /**
     * metodo per stampare a schermo le notifiche
     * @param text:stringa contenente il messaggio da visualizzare
     * @param i:indice associato alla notifica del channel
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void printnotify(String text, int i){

        NotificationManager notificationManager = (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);

        //se la versione di android è superiore dell'8.0 allora imposto i vari parametri (garantisce la compatibilità)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String CHANNEL_ID = "channel"+ i;
            CharSequence name = "channel"+ i;
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 100, 200, 100, 200, 100});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }




        NotificationCompat.Builder builder = new NotificationCompat.Builder(cont, "channel"+ i)
                .setSmallIcon(R.drawable.ic_greenapp)
                .setContentTitle("GreenApp")
                .setContentText(text);
                //.setOngoing(true)  //sticky message

        Intent resultIntent = new Intent(cont, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(cont);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(resultPendingIntent);
        builder.setOnlyAlertOnce(true);
        StatusBarNotification[] activeNotifications = notificationManager.getActiveNotifications();
        int flag = 0;
        if(activeNotifications!=null){     /* check if notification does already exist in the statusbar */
            for(StatusBarNotification notification : activeNotifications ){
                if (notification.getId() == i) {
                    flag = 1;
                }
            }
        }

        if (flag == 0) {
            notificationManager.notify(i, builder.build());
        }
    }

    /**
     *
     * @return il valore contenente l'offset di riferimento associato (in stringa)
     */
    public static String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        int offsetInMillis = tz.getRawOffset();
        /*int DSTSavInMillis = tz.getDSTSavings();
        //if(tz.inDaylightTime(new Date())){
        //    offsetInMillis = offsetInMillis + DSTSavInMillis;
        }*/
        return String.valueOf( offsetInMillis/(1000*3600));
    }


    /**
     * restituisce la distanza in secondi dall'ultimo aggiornamento
     * @param data: contiene il valore in stringa di una data (reperita dal server)
     * @return intero contenete la durata in secondi dall'ultimo aggiornamento
     */
    private int distanza(String data) {
        Calendar date_now= Calendar.getInstance ();
        date_now.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar date_value = Calendar.getInstance ();

        //parsing della data
        int giorno=Integer.valueOf(data.substring(8, 10));
        int mese=Integer.valueOf(data.substring(5, 7));
        int anno=Integer.valueOf(data.substring(0, 4));
        int ore=Integer.valueOf(data.substring(11, 13));
        int minuti=Integer.valueOf(data.substring(14, 16));
        int secondi=Integer.valueOf(data.substring(17, 19));

        //setto le impostazioni relative alla data
        date_value.set (Calendar.YEAR,anno);
        date_value.set (Calendar.MONTH,mese-1);
        date_value.set (Calendar.DAY_OF_MONTH,giorno);
        date_value.set (Calendar.HOUR_OF_DAY,ore);
        date_value.set (Calendar.MINUTE,minuti);
        date_value.set (Calendar.SECOND, secondi);

        // Log.d("DATE","DATA ORA: "+ date_now.getTime().toString() +"DATA CLOUD: "+ date_value.getTime().toString());
        //durata in secondi dall'ultimo aggiornamento
        long durata= (date_now.getTimeInMillis()/1000 - date_value.getTimeInMillis()/1000);

        return (int) durata;
    }

    /**
     * funzione che mi confronta i vari valori scaricati con quelli memorizzati nel database e provvede all'invio delle notifiche
     * @param t: valore da controllare
     * @param getimage: field associato (in caso non si utilizza quello di default)
     * @param getmin:il valore minimo preimpostato
     * @param getmacx: valore massimo preimpostato
     * @param channel:channel in uso
     * @param i:codice identificativo della notifica
     * @param defaultvalue: nome del field da visualizzare nelle notifiche
     */
    private void notification(Double t, String getimage, Double getmin,Double getmacx,Channel channel,int i, String defaultvalue, int imageIDX) {
        try {
            //controllo che ho inserito un valore nella temperatura minima
            if (getmin != null){

                if(t < getmin) {
                    String id = null;

                    if (getimage == null) {
                        if(!defaultvalue.equals("evapotranspiration")){
                            id = retreiveCorrectID(channel, defaultvalue);
                        }
                        else {
                            id = ID_irrigation_drainage;
                        }
                        printnotify("Channel(" + id + ") " + defaultvalue + " low!", i + Integer.valueOf(id));
                    }
                    else {
                        id = retreiveCorrectID(channel, getimage);
                        printnotify("Channel(" + id + ") " + getimage + " low!", i + Integer.valueOf(id));
                    }

                }

            }
            if (getmacx != null){

                if(t > getmacx) {

                    String id = null;

                    if (getimage == null){
                        if(!defaultvalue.equals("evapotranspiration")){
                            id = retreiveCorrectID(channel, defaultvalue);
                        }
                        else {
                            id = ID_irrigation_drainage;
                        }
                        printnotify("Channel(" + id + ") " + defaultvalue + " high!", (i + 10) + Integer.valueOf(id));
                    }
                    else {
                        id = retreiveCorrectID(channel, getimage);
                        printnotify("Channel (" + id + ") " + getimage + " high!", (i + 10) + Integer.valueOf(id));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("La mia exception: " + e.getMessage() + e.getCause());
            e.printStackTrace();
        }
    }

    /**
     * Modifiche effettuate da Matteo Torchia 599899
     * Controllo anche il secondo canale per la rimozione
     */
    /**
     * cancella un channel dalla lista (in caso di disattivazione delle notifiche)
     * @param x:channel da rimuovere
     */


    public static void remove(Channel x){

        if(channel!=null){

            for(int i=0;i<channel.size();i++){

                /**
                 * Data l'aggiunta del secondo canale devo i possibili casi
                 */

                String id1_c = channel.get(i).getLett_id();
                String id1_x = x.getLett_id();
                String id2_c = channel.get(i).getLett_id_2();
                String id2_x = x.getLett_id_2();

                if(id1_c != null){
                    if(id1_x != null && id1_c.equals(id1_x))channel.remove(i);
                    else if(id2_x != null && id1_c.equals(id2_x))channel.remove(i);
                }
                else if(id2_c != null){
                    if(id1_x != null && id2_c.equals(id1_x))channel.remove(i);
                    else if(id2_x != null && id2_c.equals(id2_x))channel.remove(i);
                }
            }
        }
    }

    /**
     * notifiche dedicata all'evapotraspirazione
     * @param urlString:indirizzo per reperire i parametri
     * @param channel:channel in uso
     */
    private void downloadEvapotraspirazione(String urlString,final Channel channel, String id) {
        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlString, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            //recupero l'array feeds
                            JSONArray jsonArray = response.getJSONArray("feeds");

                            //recupero i fields associati al channel
                            ArrayList<String> fields = new ArrayList<String>();
                            int dim = response.getJSONObject("channel").length();
                            Log.d("Thread background", "download eseguito JsonObjectRequest");
                            //salvo tutti i nomi dei field nell'array
                            try {
                                for(int i=0;i<dim;i++) {
                                    fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i+1))));
                                }
                            }catch(Exception e) {
                            }

                            Boolean ok=false;
                            Boolean ok1=false;
                            Double irrigazione =0.0;
                            Double drainaggio = 0.0;

                            //scandisco tutti i 100 valori per trovare i valori di irrigazione e drenaggio
                            for (int k = 0; k < jsonArray.length(); k++) {
                                JSONObject valori = jsonArray.getJSONObject(k);
                                try {
                                    if (!valori.getString("field7").equals("") && !valori.getString("field7").equals("null") && fields.get(6).equals("irrigation")) {
                                        ok=true;
                                        irrigazione=Double.parseDouble(valori.getString("field7"));
                                    }
                                }catch (Exception e){ }

                                try {
                                    if (!valori.getString("field8").equals("") && !valori.getString("field8").equals("null") && fields.get(7).equals("drainage")) {
                                        ok1=true;
                                        drainaggio=Double.parseDouble(valori.getString("field8"));
                                    }
                                }catch (Exception e){ }
                            }

                            if(channel.getNotification()) {
                                Double ev=null;
                                if(ok && ok1){
                                    ev=Math.round((irrigazione - drainaggio) * 100.0) / 100.0;
                                    ID_irrigation_drainage = id;
                                    notification(ev,channel.getImagepeso(),channel.getPesMin(),channel.getPesMax(),channel,6,"evapotranspiration", 9);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Thread background", "errore download downloadEvapotraspirazione");
            }
        });
        Volley.newRequestQueue(cont).add(jsonObjectRequest);
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     */

    private void checkDelate(HashMap<Integer, Boolean>  flag, AppDatabase db, Channel x){

        if(flag.get(0) != null){

            flag.put(0, null);
            if(!flag.get(1)){
                flag.put(1, null);
                db.delateChannelStd(x);
            }
        }
        else if(flag.get(1) != null){
            flag.put(1, null);
            db.delateChannelStd(x);
        }
    }
    private void checkInsert(HashMap<Integer, Boolean>  flag, AppDatabase db, Channel x){

        if(flag.get(0) != null){

            flag.put(0, null);
            if(!flag.get(1)){
                flag.put(1, null);
                db.insertChannelStd(x);
            }
        }
        else if(flag.get(1) != null){
            flag.put(1, null);
            db.insertChannelStd(x);
        }
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Recupero il valore corretto restituito dal server
     */
    private String retreiveValue(String fieldInResponse, String fieldToSearch, Channel v, JSONObject value,
                                 int imageIDX, int numField, ArrayList<String> fields, int flagPosition) throws Exception{

        String field = null;

        if(retreiveImage(imageIDX, v) != null) {
            field = supportForPosition(flagPosition, imageIDX, v, value, fields);
        }
        else {
            if(fieldInResponse.equals(fieldToSearch))field = value.getString("field" + numField);

        }
        return field;
    }
    /**
     * Metodo aggiunto da Matteo Torchia 599899
     */
    private String retreiveImage(int field, Channel v) {

        String arrayImg = null;
        switch (field) {
            case 1:
                arrayImg = v.getImagetemp();
                break;
            case 2:
                arrayImg = v.getImagePesoPianta();
                break;
            case 3:
                arrayImg = v.getImageumid();
                break;
            case 4:
                arrayImg = v.getImageph();
                break;
            case 5:
                arrayImg = v.getImagecond();
                break;
            case 6:
                arrayImg = v.getImageirra();
                break;
            case 7:
                arrayImg = v.getImagesoil();
                break;
            case 8:
                arrayImg = v.getImageVento();
                break;
            case 9:
                arrayImg = v.getImagepeso();
                break;
            case 10:

                break;
            case 11:
                break;
        }
        return arrayImg;

    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * @param flagposition
     * @param imageIDX
     * @param v
     * @param value
     * @param fields
     * @return
     * @throws Exception
     */
    private String supportForPosition(int flagposition, int imageIDX, Channel v, JSONObject value, ArrayList<String> fields) throws Exception{

        String field = null;

        int tmpPos = retreivePos(imageIDX, v);
        int idxInFields = -1;
        int realPos = -1;

        if(tmpPos > 8) {

            realPos = ((tmpPos - 1) % 8) + 1;
            String nameFIeld = select_check_Field(realPos - 1, -2, "", v);
            idxInFields = fields.indexOf(nameFIeld);

            if(idxInFields != -1)field = value.getString("field" + (idxInFields + 1));


        }
        else {

            String nameFIeld = select_check_Field(tmpPos - 1, -1, "", v);
            idxInFields = fields.indexOf(nameFIeld);

            if(idxInFields != -1)field = value.getString("field" + (idxInFields + 1));

        }
        return field;
    }
    private String select_check_Field(int field, int choice, String fieldName, Channel v) {

        switch (field) {

            case 0:
                if(choice == 1)v.setFiled1(fieldName);
                else if(choice == 2) v.setFiled1_2(fieldName);
                else if(choice == -1) return v.getFiled1();
                else if(choice == -2) return v.getFiled1_2();

                break;
            case 1:
                if(choice == 1)v.setFiled2(fieldName);
                else if(choice == 2)v.setFiled2_2(fieldName);
                else if(choice == -1) return v.getFiled2();
                else if(choice == -2) return v.getFiled2_2();

                break;
            case 2:
                if(choice == 1)v.setFiled3(fieldName);
                else if(choice == 2)v.setFiled3_2(fieldName);
                else if(choice == -1) return v.getFiled3();
                else if(choice == -2) return v.getFiled3_2();

                break;
            case 3:
                if(choice == 1)v.setFiled4(fieldName);
                else if(choice == 2)v.setFiled4_2(fieldName);
                else if(choice == -1) return v.getFiled4();
                else if(choice == -2) return v.getFiled4_2();

                break;
            case 4:
                if(choice == 1)v.setFiled5(fieldName);
                else if(choice == 2)v.setFiled5_2(fieldName);
                else if(choice == -1) return v.getFiled5();
                else if(choice == -2) return v.getFiled5_2();

                break;
            case 5:
                if(choice == 1)v.setFiled6(fieldName);
                else if(choice == 2)v.setFiled6_2(fieldName);
                else if(choice == -1) return v.getFiled6();
                else if(choice == -2) return v.getFiled6_2();

                break;
            case 6:
                if(choice == 1)v.setFiled7(fieldName);
                else if(choice == 2)v.setFiled7_2(fieldName);
                else if(choice == -1) return v.getFiled7();
                else if(choice == -2) return v.getFiled7_2();

                break;
            case 7:
                if(choice == 1)v.setFiled8(fieldName);
                else if(choice == 2)v.setFiled8_2(fieldName);
                else if(choice == -1) return v.getFiled8();
                else if(choice == -2) return v.getFiled8_2();

                break;

        }
        return null;
    }
    private int retreivePos(int field, Channel v){

        String arrayImg = null;
        arrayImg = retreiveImage(field, v);

        //recupero la posizione selezionata
        if(arrayImg == null) return -1;

        int pos = Integer.parseInt(arrayImg.substring(5));
        return pos;
    }

    /**
     * Recupero il valore dal canale correttamente
     * @param fields
     * @param valori
     * @param fieldsKey
     * @param field
     * @param ImgPosition
     * @param v
     * @return
     * @throws Exception
     */
    private String setDataFieldCorrect(ArrayList<String> fields, JSONObject valori, ArrayList<String> fieldsKey, String field, int ImgPosition, Channel v) throws Exception{

        String name = null, val = null, finalval = null;
        if((name = retreiveImage(ImgPosition, v)) != null){

            val = valori.getString(fieldsKey.get(fields.indexOf(name)));

            if(val != null && !val.isEmpty() && !val.equals("null")){
                finalval = valori.getString(fieldsKey.get(fields.indexOf(name)));
                checkFlags.getMap().put(field, true);
            }
        }
        else {
            val = valori.getString(fieldsKey.get(fields.indexOf(field)));
            if(val != null && !val.isEmpty() && !val.equals("null")){
                finalval = valori.getString(fieldsKey.get(fields.indexOf(field)));
                checkFlags.getMap().put(field, true);
            }
        }
        return finalval;
    }


    /**
     * Recupero l'ID corretto in base al field selezionato
     * @param channel
     * @param field
     * @return
     */
    private String retreiveCorrectID(Channel channel, String field){

        if(field == null) return null;

        String check = null;
        ArrayList<String> fieldID_1 = new ArrayList<>();
        ArrayList<String> fieldID_2 = new ArrayList<>();

        for(int i = 0; i<8; i++){
            check = select_check_Field(i, -1, null, channel);
            if(check != null )fieldID_1.add(check);
        }
        for(int i = 0; i<8; i++){
            check = select_check_Field(i, -2, null, channel);
            if(check != null )fieldID_2.add(check);
        }
        if(fieldID_1.contains(field))return channel.getLett_id();
        else if(fieldID_2.contains(field))return channel.getLett_id_2();

        return null;

    }

}


