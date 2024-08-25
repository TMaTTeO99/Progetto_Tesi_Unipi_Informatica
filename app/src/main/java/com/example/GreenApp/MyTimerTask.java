package com.example.GreenApp;


import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Channel.savedValues;
import com.example.firstapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.TimerTask;

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
 * Modifiche effttuate da Matteo Torchia 599899
 * Aggiungo le variabili e il meccanismo necessario a fare 2 chiamate
 * al server per recuperare i dati se necessario
 *
 */
public class MyTimerTask extends TimerTask {

    //dichiaro i riferiemnti agli elementi grafici

    /**
     * Modifiche aggiunte da Matteo Torchia
     * Inserite le varibili che fanno riferimento alla schermata principale
     * riguardo peso e vente
     */
    /**
     * //TODO: VA MODIFICATO PER FAR SI CHE L AGGIORNAMENTO DELLE TTEXTVIEW DEI VALORI
     * //TODO: VENGA FATTO DOPO L EVENTUALE SECONDA CHIAMATA E NON AD OGNI CHIAMATA PER EVITARE EFFETTO CATTIVO
     *
     */
    TextView textTemp;
    TextView textUmidity;
    TextView textPh;
    TextView textConducibilita;
    TextView textIrradianza;
    TextView textSoil;
    TextView textevap;
    TextView text1;
    TextView stato;

    TextView textPesoPianta;
    TextView textVento;
    ImageView image;

    MenuItem itemRefresh;

    String url;
    String url_2; //variabile aggiunta da Matteo Torchia 599899
    Context context;

    //variabili per memorizzare il channel di default
    private static String channelID=null;
    private static String READ_KEY=null;
    private static AppDatabase database;

    /**
     * variabili aggiunte da Matteo Torchia 599899
     *
     */
    private HashMap<Integer, Boolean> flag = null;
    private Channel v = null;
    private int idxInFor = 0;
    private static String channelID_2 = null;
    private static String READ_KEY_2 = null;

    private ChannelFieldsSelected checkFlags = new ChannelFieldsSelected();


    /**
     * funzione costruttore
     * @param id:id del channel
     * @param key:chiave di lettura associata (può anche non esserci)
     * @param url:indirizzo utilizzato
     * @param textTemp1:riferiemnto all'icona testuale della temperatura
     * @param textUmidity1:riferiemnto all'icona testuale dell'umidità
     * @param textPh1:riferiemnto all'icona testuale del ph
     * @param textConducibilita1:riferiemnto all'icona testuale della conducibilità elettrica
     * @param textIrradianza1:riferiemnto all'icona testuale dell'irradianza
     * @param textSoil:riferiemnto all'icona testuale del suolo
     * @param textPO1:riferiemnto all'icona testuale dell evapotraspirazione
     * @param stato:riferiemnto all'icona testuale dello stato
     * @param testo1:riferiemnto all'icona testuale dell ultimo aggiornamento
     * @param cont:context associato
     * @param database:database utilizzato
     * @param imm:riferiemnto all'icona dell'innaffiatoio
     */
    public MyTimerTask(String id, String key, String ID2, String keyread2, String url, String url_2, TextView textTemp1,TextView textUmidity1, TextView textPh1, TextView textConducibilita1,
                       TextView textIrradianza1,TextView textSoil, TextView textPO1,TextView stato,TextView testo1, Context cont,AppDatabase database,ImageView imm,
                       TextView textPesoPianta, TextView textVento, MenuItem itemRefresh) {
        textTemp=textTemp1;
        textUmidity=textUmidity1;
        textPh=textPh1;
        textConducibilita=textConducibilita1;
        textIrradianza=textIrradianza1;
        this.textSoil=textSoil;
        textevap=textPO1;
        this.stato=stato;
        text1=testo1;
        channelID=id;
        READ_KEY=key;
        this.url=url;
        this.database=database;
        context=cont;
        image=imm;
        this.textPesoPianta = textPesoPianta;
        this.textVento = textVento;
        this.itemRefresh = itemRefresh;

        this.channelID_2 = ID2;
        this.url_2 = url_2;
        this.READ_KEY_2 = keyread2;


    }


    /**
     * funzione che viene svolta all'avvio del task
     */
    @Override
    public void run() {
        //reperisco i valori channel lettura
        getJsonResponse(url, url_2);

        //reperisco valori channel scrittura
        donwload();
    }

    /**
     * metodo per reperire le risposte json
     * @param url:indirizzo utilizzato per reprire i parametri
     */

    /**
     * Modifiche fatte da MAtteo Torchia 599899
     * Devo fare in modo che le chiamate al server vengano fatte per entrambi i canali
     * associati al channel se necessario
     */
    private void getJsonResponse (final String url, final String url_2){
        //se non ho nessun url inserita setto i valori a 0


        /**
         * Qui devo controllarer quali canali interrogare
         */


        //caso in cui entrambe le url non sono impostate, non dovrebbe succedere mai
        if(url == null && url_2 == null){
            stato.setText("OFFLINE");
            stato.setTextColor(Color.RED);
        }
        else {

            flag = new HashMap<>();
            idxInFor = 0;

            if(url != null) flag.put(0, true);
            else flag.put(0, false);

            if(url_2 != null)flag.put(1, true);
            else flag.put(1, false);


            //per entrambi i canali
            for(int i = 0; i<2; i++) {


                String urlToCall = null;
                if(i == 0){
                    urlToCall = url;
                }
                else {
                    urlToCall = url_2;
                }

                //se il canale va controllato
                if(urlToCall != null){

                    //TODO: C'è da controllare bene il setting di peso e vento perche cosi
                    //TODO: quando li trova li inserisce per entrambi i canali
                    final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlToCall, null,

                            new Response.Listener<JSONObject>() {

                                @Override
                                public synchronized void onResponse(JSONObject response) {

                                    if(v == null){

                                        if(flag.get(0)) {
                                            v = database.getChannelStd(channelID, READ_KEY, 0);
                                        }
                                        else if(flag.get(1)) v = database.getChannelStd(channelID_2, READ_KEY_2, 1);//v = database.ChannelDao().findBySecondName(channelID_2, READ_KEY_2);

                                    }
                                    try {

                                        idxInFor++;

                                        //recupero l'array feeds
                                        JSONArray jsonArray = response.getJSONArray("feeds");

                                        //recupero i fields associati al channel
                                        ArrayList<String> fields = new ArrayList<String>();
                                        ArrayList<String> fieldsKey = new ArrayList<String>();

                                        int dim = response.getJSONObject("channel").length();



                                        for (int i = 0; i < dim; i++) {

                                            try {
                                                fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i + 1))));
                                                fieldsKey.add("field" + (i + 1));
                                            }
                                            catch (Exception e){}

                                        }

                                        int x = 0;
                                        //recupero il canale e lo cancello, dopo aver settato i valori lo reinserisco

                                        //elimino il channel dal db se necessario
                                        /*checkAndDelateChannel();

                                        //Aggiorno i nomi dei fields
                                        if(fields.size() >= 1)setFieldName(fields, 0);
                                        if(fields.size() >= 2)setFieldName(fields, 1);
                                        if(fields.size() >= 3)setFieldName(fields, 2);
                                        if(fields.size() >= 4)setFieldName(fields, 3);
                                        if(fields.size() >= 5)setFieldName(fields, 4);
                                        if(fields.size() >= 6)setFieldName(fields, 5);
                                        if(fields.size() >= 7)setFieldName(fields, 6);
                                        if(fields.size() >= 8)setFieldName(fields, 7);

                                        chechAndInsertChannel();*/



                                        //variabili che memorizzano tutti gli ultimi valori nonNULL
                                        Boolean flag_irrigation = false;
                                        Boolean flag_drainage = false;
                                        Double irrigazione = 0.0;
                                        Double drainaggio = 0.0;
                                        String temperature = null;
                                        String umidity = null;
                                        String ph = null;
                                        String conducibilita = null;
                                        String irradianza = null;
                                        String soil = null;
                                        String cretime=null;
                                        String evapotraspirazione=null;
                                        String leachingfraction = null;
                                        String peso = null;
                                        String vento = null;

                                        HashMap<String, String> newData = new HashMap<>();


                                        //scandisco tutti i 100 valori per trovare gli ultimi valori
                                        for (int i = 0; i < jsonArray.length(); i++) {

                                            JSONObject valori = jsonArray.getJSONObject(i);
                                            int test = Integer.parseInt(valori.getString("entry_id"));
                                            if(test == 444){
                                                System.out.println("CI VADO: " + test);
                                                int h = 0;
                                            }
                                            try {

                                                if (!valori.getString("field7").equals("") && !valori.getString("field7").equals("null") && fields.get(6).equals("irrigation")) {
                                                    flag_irrigation = true;
                                                    irrigazione = Double.parseDouble(valori.getString("field7"));
                                                    checkFlags.getMap().put("irrigation", true);
                                                }

                                            } catch (Exception e) {
                                            }

                                            try {

                                                if (!valori.getString("field8").equals("") && !valori.getString("field8").equals("null") && fields.get(7).equals("drainage")) {
                                                    flag_drainage = true;
                                                    drainaggio = Double.parseDouble(valori.getString("field8"));
                                                    checkFlags.getMap().put("drainage", true);
                                                }

                                            } catch (Exception e) {
                                            }
                                            try {
                                                String name = null, val = null, finalval = null;

                                                if((name = v.getImagepeso()) != null ){

                                                    val = valori.getString(fieldsKey.get(fields.indexOf(name)));
                                                    if(val != null && !val.isEmpty() && !val.equals("null")){

                                                        evapotraspirazione = valori.getString(fieldsKey.get(fields.indexOf(name)));
                                                        v.setEvapotraspirazione(Double.parseDouble(evapotraspirazione));
                                                        checkFlags.getMap().put("evapotraspirazione", true);
                                                    }

                                                }
                                                else if(flag_irrigation && flag_drainage){

                                                    evapotraspirazione = String.valueOf(Math.round((irrigazione - drainaggio) * 100.0) / 100.0);
                                                    leachingfraction = String.valueOf(Math.round((drainaggio/irrigazione) * 100.0));
                                                    v.setEvapotraspirazione(Double.parseDouble(evapotraspirazione));
                                                }
                                            } catch (Exception e) {
                                            }

                                            try {temperature = setDataFieldCorrect(fields, valori, fieldsKey, "temperature", 1);}
                                            catch (Exception e) {e.printStackTrace();}

                                            try {peso = setDataFieldCorrect(fields, valori, fieldsKey, "P0", 2);}
                                            catch (Exception e) {e.printStackTrace();}

                                            try {vento = setDataFieldCorrect(fields, valori, fieldsKey, "windspeed", 8);}
                                            catch (Exception e) {e.printStackTrace();}

                                            try {umidity = setDataFieldCorrect(fields, valori, fieldsKey, "humidity", 3);}
                                            catch (Exception e){e.printStackTrace();}

                                            try {ph = setDataFieldCorrect(fields, valori, fieldsKey, "pH_value", 4);}
                                            catch (Exception e){e.printStackTrace();}

                                            try {conducibilita = setDataFieldCorrect(fields, valori, fieldsKey, "electric_conductivity", 5);}
                                            catch (Exception e){e.printStackTrace();}

                                            try {irradianza =  setDataFieldCorrect(fields, valori, fieldsKey, "irradiance", 6);}
                                            catch (Exception e){e.printStackTrace();}

                                            try {soil = setDataFieldCorrect(fields, valori, fieldsKey, "moisture", 7);}
                                            catch (Exception e){}


                                            cretime = valori.getString("created_at");

                                            //formattatore di stringa per far visualizzare x.y
                                            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
                                            otherSymbols.setDecimalSeparator('.');
                                            DecimalFormat df = new DecimalFormat("0.0", otherSymbols);

                                            //mostro a schermo gli ultimi valori
                                            try{
                                                if (temperature != null){


                                                    if (v.getImagetemp() != null) {

                                                        textTemp.setText(df.format(Math.round(Double.parseDouble(String.format(temperature)) * 100.0) / 100.0));
                                                    } else if (fields.get(0).equals("temperature")) {
                                                        textTemp.setText(df.format(Math.round(Double.parseDouble(String.format(temperature)) * 100.0) / 100.0).concat(" °C"));
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("temperature")){
                                                    textTemp.setText("- -");
                                                }

                                            }catch (Exception e){
                                                textTemp.setText("- -");
                                            }
                                            try {
                                                if (umidity != null) {
                                                    if (v.getImageumid() != null) {
                                                        textUmidity.setText(df.format(Math.round(Double.parseDouble(String.format(umidity)) * 100.0) / 100.0));
                                                    } else if (fields.get(1).equals("humidity")) {
                                                        textUmidity.setText(df.format(Math.round(Double.parseDouble(String.format(umidity)) * 100.0) / 100.0).concat(" %"));
                                                    }
                                                } else if(!checkFlags.getMap().get("humidity"))textUmidity.setText("- -");
                                            }catch (Exception e){
                                                textUmidity.setText("- -");
                                            }
                                            try {
                                                if (ph != null) {
                                                    if (v.getImageph() != null) {
                                                        textPh.setText(df.format(Math.round(Double.parseDouble(String.format(ph)) * 100.0) / 100.0));
                                                    } else if (fields.get(2).equals("pH_value")) {
                                                        textPh.setText(df.format(Math.round(Double.parseDouble(String.format(ph)) * 100.0) / 100.0));
                                                    }
                                                } else if(!checkFlags.getMap().get("pH_value"))textPh.setText("- -");
                                            }catch (Exception e){
                                                textPh.setText("- -");
                                            }
                                            try{
                                                if (conducibilita != null){
                                                    if (v.getImagecond() != null) {
                                                        textConducibilita.setText(df.format(Math.round(Double.parseDouble(String.format(conducibilita)) * 100.0) / 100.0));
                                                    } else if (fields.get(3).equals("electric_conductivity")) {
                                                        textConducibilita.setText(df.format(Math.round(Double.parseDouble(String.format(conducibilita)) * 100.0) / 100.0).concat(" dS·m⁻¹"));
                                                    }
                                                }
                                                else  if(!checkFlags.getMap().get("electric_conductivity"))textConducibilita.setText("- -");
                                            }catch (Exception e){
                                                textConducibilita.setText("- -");
                                            }
                                            try{
                                                if (irradianza != null){
                                                    if (v.getImageirra() != null) {
                                                        textIrradianza.setText(df.format(Math.round(Double.parseDouble(String.format(irradianza)) * 100.0) / 100.0));
                                                    } else if (fields.get(4).equals("irradiance")) {
                                                        textIrradianza.setText(df.format(Math.round(Double.parseDouble(String.format(irradianza)) * 100.0) / 100.0).concat(" W·m⁻²"));
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("irradiance"))textIrradianza.setText("- -");
                                            }catch (Exception e){
                                                textIrradianza.setText("- -");
                                            }


                                            try{
                                                if (soil != null){
                                                    if (v.getImagesoil() != null) {
                                                        textSoil.setText(df.format(Math.round(Double.parseDouble(String.format(soil)))));
                                                    } else if (fields.get(5).equals("moisture")) {
                                                        textSoil.setText(df.format(Math.round(Double.parseDouble(String.format(soil)))).concat(" %"));
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("moisture"))textSoil.setText("- -");
                                            }catch (Exception e){
                                                textSoil.setText("- -");
                                            }


                                            try{
                                                if (evapotraspirazione != null){
                                                    if (v.getImagepeso() != null) {
                                                        textevap.setText(df.format(Math.round(Double.parseDouble(String.format(evapotraspirazione)) * 100.0) / 100.0));
                                                    } else if (flag_irrigation && flag_drainage) {

                                                        String segno = (Float.valueOf(evapotraspirazione) < 0) ? "-" : "";
                                                        String plot = evapotraspirazione + " g / " + segno + leachingfraction + " %";

                                                        textevap.setText(plot) ;
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("irrigation") && !checkFlags.getMap().get("drainage") && !checkFlags.getMap().get("evapotraspirazione")) textevap.setText("- -");



                                            }catch (Exception e){
                                                textevap.setText("- -");
                                            }
                                            try {
                                                if(peso != null){
                                                    if(v.getImagePesoPianta() != null){

                                                        textPesoPianta.setText(df.format(Math.round(Double.parseDouble(String.format(peso)))));
                                                    }
                                                    else if(fields.get(0).equals("P0")) {
                                                        textPesoPianta.setText(df.format(Math.round(Double.parseDouble(String.format(peso)))).concat(" g"));
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("P0")) textPesoPianta.setText("- -");
                                            }
                                            catch (Exception e) {
                                                textPesoPianta.setText("- -");
                                            }
                                            try {
                                                if(vento != null){
                                                    if(v.getImageVento() != null){
                                                        textVento.setText(df.format(Math.round(Double.parseDouble(String.format(vento)))));
                                                    }
                                                    else if(fields.get(1).equals("windspeed")){
                                                        textVento.setText(df.format(Math.round(Double.parseDouble(String.format(vento)))).concat(" km·h⁻¹"));
                                                    }
                                                }
                                                else if(!checkFlags.getMap().get("windspeed"))textVento.setText("- -");
                                            }
                                            catch (Exception e){
                                                textVento.setText("- -");
                                            }

                                        }

                                        String idFromServer = response.getJSONObject("channel").get("id").toString();
                                        //stampo a schermo la distanza dall'ultimo valore inserito
                                        distanza(cretime, idFromServer);

                                        //setto lo stato ONLINE se la richiesta è andata a buon fine
                                        stato.setText("ONLINE");
                                        stato.setTextColor(Color.GREEN);

                                        if(idxInFor == 2)v = null;

                                    } catch (JSONException e) {

                                        e.printStackTrace();
                                        stato.setText("OFFLINE");
                                        stato.setTextColor(Color.RED);
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            stato.setText("OFFLINE");
                            stato.setTextColor(Color.RED);
                        }
                    });

                    Volley.newRequestQueue(context).add(jsonObjectRequest);
                }
            }
        }
    }

    /**
     * funzione che scarica i dati dal server riguardanti la configurazione dell'irrigazione
     */
    private void donwload() {

        List<savedValues> lista=database.getChannelsSaved();

        Channel list = null;
        String id1 = lista.get(0).getId();
        String id2 = lista.get(0).getLett_id_2();

        if(id1 != null)list = database.getChannelStd(id1 , lista.get(0).getRead_key(), 0);
        else if(id2 != null)list = database.getChannelStd(id2 , lista.get(0).getLett_read_key_2(), 1);


        String url="https://api.thingspeak.com/channels/"+list.getScritt_id()+"/fields/7.json?api_key="+list.getScritt_read_key()+"&results=1";

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            int field7=0;

                            //scandisco tutti i valori e ne determino l'ultimo associato al field7
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject valori = jsonArray.getJSONObject(i);
                                if (!valori.getString("field7").equals("null")) {
                                    if (Double.parseDouble(valori.getString("field7")) == 1) {
                                        field7=1;
                                    } else {
                                        field7=0;
                                    }
                                } else {
                                    field7=0;
                                }
                            }
                            //se l'irrigazione è attiva cambio il simbolo, ltrimenti niente
                            if (field7 == 1) image.setImageResource(R.drawable.innaffia_attiva_new);
                            else  image.setImageResource(R.drawable.innaffia);

                        } catch (Exception e) {
                            if(image!=null)image.setImageResource(R.drawable.innaffia);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(image!=null) image.setImageResource(R.drawable.innaffia);
            }
        });
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }

    /**
     * funzione che calcola la distanza dal momento in cui faccio richiesta all'ultima data reperita dal server
     * @param data:stringa contenente la data
     */
    private void distanza(String data, String channelID) {

        Calendar date_now= Calendar.getInstance ();
        date_now.setTimeZone(TimeZone.getTimeZone("GMT"));
        Calendar date_value = Calendar.getInstance ();

        //parsing della data
        int giorno=0, mese=0, anno=0, ore=0, minuti=0, secondi=0;

        if (data!=null) {
            giorno=Integer.valueOf(data.substring(8, 10));
            mese=Integer.valueOf(data.substring(5, 7));
            anno=Integer.valueOf(data.substring(0, 4));
            ore=Integer.valueOf(data.substring(11, 13));
            minuti=Integer.valueOf(data.substring(14, 16));
            secondi=Integer.valueOf(data.substring(17, 19));
        }

        //setto le impostazioni relative alla data
        date_value.set (Calendar.YEAR,anno);
        date_value.set (Calendar.MONTH,mese-1);
        date_value.set (Calendar.DAY_OF_MONTH,giorno);
        date_value.set (Calendar.HOUR_OF_DAY,ore);
        date_value.set (Calendar.MINUTE,minuti);
        date_value.set (Calendar.SECOND, secondi);

        //converto la data del cloud alla mia zona gmt
        date_value.setTimeZone(TimeZone.getTimeZone("GMT"));

        //durata in secondi
        long durata= (date_now.getTimeInMillis()/1000 - date_value.getTimeInMillis()/1000);
        long giorni1=(durata/86400);
        long temp=giorni1*86400;
        long ore1=(durata-temp)/3600;
        long minuti1=((durata-temp)-3600*ore1)/60;
        temp=(durata-temp)-3600*ore1;
        long secondi1=temp-(minuti1*60);

        String toSet = giorno == 0 ? "Nessun aggiornamento" : "Ultimo aggiornamento: " + giorni1 + " giorni " + ore1 + " ore " + minuti1 + " minuti " + secondi1+ " secondi ";
        text1.setText(toSet);

        //itemRefresh == null quando viene fatto il refresh automatico
        //altrimenti è stato toccato dall'utente => aggiorno colore del tasto
        if(itemRefresh != null)toolbarGraficRefresh(itemRefresh);

    }
    protected void toolbarGraficRefresh(MenuItem item){

        item.setCheckable(true);
        item.setChecked(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setCheckable(false);
            }
        }, 120);
    }


    private String setDataFieldCorrect(ArrayList<String> fields, JSONObject valori, ArrayList<String> fieldsKey, String field, int ImgPosition) throws Exception{

        String name = null, val = null, finalval = null;
        if((name = selectImage(ImgPosition)) != null){

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


    private String selectImage(int position){

        String image = null;
        switch (position) {

            case 1:
                image = v.getImagetemp();
                break;
            case 2:
                image = v.getImagePesoPianta();
                break;
            case 3:
                image = v.getImageumid();
                break;
            case 4:
                image = v.getImageph();
                break;
            case 5:
                image = v.getImagecond();
                break;
            case 6:
                image = v.getImageirra();
                break;
            case 7:
                image = v.getImagesoil();
                break;
            case 8:
                image = v.getImageVento();
                break;
            case 9:
                image = v.getImagepeso();
                break;
        }
        return image;
    }


    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Usato per reinserire il channel eliminato dal DB
     */

    /*private void chechAndInsertChannel(){

        if(idxInFor == 1){
            if(!flag.get(1)){
                database.insertChannelStd(v);
            }
        }
        else database.insertChannelStd(v);
    }

     */

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Usato per eliminare il channel recuperato dal DB
     */

   /* private void checkAndDelateChannel(){

        if (v != null) {

            if(idxInFor == 1 && !flag.get(1)) {
                database.delateChannelStd(v);
            }
            else if(idxInFor == 2) {
                database.delateChannelStd(v);
            }

        }
    }


    */

    /**
     * aggiorna il nuovo riferimento al database
     * @param db:nuovo database di riferimento
     */
    public static void updateDatabase(AppDatabase db){
        database=db;
    }

}


