package com.example.GreenApp.Alert;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.ChannelFieldsSelected;
import com.example.GreenApp.Graphic.MainActivity;
import com.example.GreenApp.AppDatabase;
import com.example.firstapp.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

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
 * Modifiche effettuate da Matteo Torchia 599899
 * Aggiunte componenti grafiche per i nuovi valori
 */

public class AlertActivity extends AppCompatActivity {


    /**
     * Variabili aggiunte da Matteo Torchia 599899
     */

    //TextView dei valori correnti
    private TextView textViewPeso1;
    private TextView textViewvento;

    //edit text di massimo e minimo dei nuovi valori
    private EditText pesoPiantamin;
    private EditText pesoPiantamax;
    private EditText ventoMin;
    private EditText ventomax;

    /** FINE SEZIONE **/


    private static Context cont;
    private EditText tempMin;
    private EditText tempMax;
    private EditText umidMin;
    private EditText umidMax;
    private EditText condMin;
    private EditText condMax;
    private EditText phMin;
    private EditText phMax;
    private EditText irraMin;
    private EditText irraMax;
    private EditText pesMin;
    private EditText pesMax;
    private EditText soilMin;
    private EditText soilMax;
    private EditText tempomax;
    private static TextView temp;
    private static TextView umid;
    private static TextView ph;
    private static TextView cond;
    private static TextView irra;
    private static TextView peso;
    private static TextView soil;
    private static EditText minutes;
    private static Channel channel;             //channel usato
    private static AppDatabase database;
    private static Switch aSwitch;
    private static int minuti=0;
    private static Intent serviceIntent;

    private Activity activity = this;//riferimento all'activity per popup
    private Channel x = null; //riferimento all channel per popup

    private ChannelFieldsSelected checkFlags = new ChannelFieldsSelected();

    /**
     * metodo eseguito alla creazione
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_settings);

        //inizializzo l'intent per il service da lanciare in background
        serviceIntent = new Intent(this, ForegroundService.class);

        //inizializzo i valori
        cont=getApplication();
        tempMin=findViewById(R.id.Tempmin);
        tempMax=findViewById(R.id.tempmax);
        umidMin=findViewById(R.id.umidmin);
        umidMax=findViewById(R.id.umidmax);
        condMin=findViewById(R.id.condmin);
        condMax=findViewById(R.id.condmax);
        phMin=findViewById(R.id.phmin);
        phMax=findViewById(R.id.phmax);
        irraMin=findViewById(R.id.irramin);
        irraMax=findViewById(R.id.irramax);
        pesMin=findViewById(R.id.pesomin);
        pesMax=findViewById(R.id.pesomax);
        soilMin=findViewById(R.id.soilmin);
        soilMax=findViewById(R.id.soilmax);
        temp=findViewById(R.id.textViewtemp);
        umid=findViewById(R.id.textViewUmid);
        ph=findViewById(R.id.textViewPh);
        cond=findViewById(R.id.textViewcond);
        irra=findViewById(R.id.textViewirra);
        peso=findViewById(R.id.textViewPes);
        soil=findViewById(R.id.textViewSoil);
        tempomax=findViewById(R.id.Edittempomax);
        //minutes=findViewById(R.id.editTextMinuti);
        aSwitch=findViewById(R.id.switch2);


        /**
         * Sezione Aggiunta da Matteo Torchia 599899
         */
        textViewPeso1 = findViewById(R.id.textViewPeso1);
        textViewvento = findViewById(R.id.textViewvento);

        pesoPiantamin = findViewById(R.id.pesoPiantamin);
        pesoPiantamax = findViewById(R.id.pesoPiantamax);
        ventoMin = findViewById(R.id.ventoMin);
        ventomax = findViewById(R.id.ventomax);


        //creo l'associazione con il database 
        database = AppDatabase.getDataBase(getApplicationContext());


        //ripristino i valori relativi al channel precedentemente salvati
        if (channel.getTempMin()!= null ) tempMin.setText(String.format(channel.getTempMin().toString()));
        if (channel.getTempMax()!=null) tempMax.setText(String.format(channel.getTempMax().toString()));
        if (channel.getUmidMin()!=null) umidMin.setText(String.format(channel.getUmidMin().toString()));
        if (channel.getUmidMax()!=null) umidMax.setText(String.format(channel.getUmidMax().toString()));
        if (channel.getCondMin()!=null) condMin.setText(String.format(channel.getCondMin().toString()));
        if (channel.getCondMax()!=null) condMax.setText(String.format(channel.getCondMax().toString()));
        if (channel.getPhMin()!=null) phMin.setText(String.format(channel.getPhMin().toString()));
        if (channel.getPhMax()!=null) phMax.setText(String.format(channel.getPhMax().toString()));
        if (channel.getIrraMin()!=null) irraMin.setText(String.format(channel.getIrraMin().toString()));
        if (channel.getIrraMax()!=null) irraMax.setText(String.format(channel.getIrraMax().toString()));
        if (channel.getPesMin()!=null) pesMin.setText(String.format(channel.getPesMin().toString()));
        if (channel.getPesMax()!=null) pesMax.setText(String.format(channel.getPesMax().toString()));
        if (channel.getSoilMin()!=null) soilMin.setText(String.format(channel.getSoilMin().toString()));
        if (channel.getSoilMax()!=null) soilMax.setText(String.format(channel.getSoilMax().toString()));
        if (channel.getPesPiantaMax() != null)pesoPiantamax.setText(String.format(channel.getPesPiantaMax().toString()));
        if (channel.getPesPiantaMin() != null)pesoPiantamin.setText(String.format(channel.getPesPiantaMin().toString()));
        if (channel.getVentoMin() != null)ventoMin.setText(String.format(channel.getVentoMin().toString()));
        if (channel.getVentoMax() != null)ventomax.setText(String.format(channel.getVentoMax().toString()));
        //if (channel.getLastimevalues()!=0) minutes.setText(String.valueOf(channel.getLastimevalues()));

        if (channel.getTempomax()!=0) tempomax.setText(String.valueOf(channel.getTempomax()));

        //se le notifiche erano attive avvio il servizio notifiche
        if (channel.getNotification()){
            aSwitch.setChecked(true);
        }
        else{
            aSwitch.setChecked(false);
        }

        //scarico la media dei valori e la rappresento a schermo
        downloadMedia();
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //appena l'irrigazione è attiva
                if (isChecked){
                    Log.d("AlertActivity","attivo notifiche");

                    //abilito le notifiche
                    ShowAlert("Notifiche", "Vuoi Attivare Notifiche ?", false, activity, (y) -> startNotify(), 0);

                    /*
                    String id1 = channel.getLett_id();
                    String id2 = channel.getLett_id_2();

                    if(id1 != null)x = database.getChannelStd(id1, channel.getLett_read_key(), 0);//database.ChannelDao().findByName(id1, channel.getLett_read_key());
                    else if(id2 != null)x = database.getChannelStd(id2, channel.getLett_read_key_2(), 1);//database.ChannelDao().findBySecondName(id2, channel.getLett_read_key_2());
                    */
                    /*
                    database.delateChannelStd(x);
                    x.setNotification(true);
                    database.insertChannelStd(x);

                    //comunico al service che devo attivare le notifiche
                    channel=x;
                    startService();*/
                }
                else{
                    Log.d("AlertActivity","fermo notifiche");
                    //disabilito le notifiche


                    String id1 = channel.getLett_id();
                    String id2 = channel.getLett_id_2();

                    if(id1 != null) x = database.getChannelStd(id1, channel.getLett_read_key(), 0);//database.ChannelDao().findByName(id1, channel.getLett_read_key());
                    else x = database.getChannelStd(id2, channel.getLett_read_key_2(), 1);//database.ChannelDao().findByName(id2, channel.getLett_read_key_2());

                    database.delateChannelStd(x);//database.ChannelDao().delete(x);
                    x.setNotification(false);
                    database.insertChannelStd(x);//database.ChannelDao().insert(x);

                    //devo interrompere il servizio delle notifiche
                    MyTimerTask.remove(x);
                    NotificationManager notificationManager = (NotificationManager) cont.getSystemService(Context.NOTIFICATION_SERVICE);  /* delete all notifications from  statusbar */
                    notificationManager.cancelAll();
                    stopService();
                }

            }
        });

    }

    /**
     * Funzione di supporto per l'avvio effettivo del servizio di notifiche
     */

    private int startNotify(){

        String id1 = channel.getLett_id();
        String id2 = channel.getLett_id_2();

        if(id1 != null)x = database.getChannelStd(id1, channel.getLett_read_key(), 0);
        else if(id2 != null)x = database.getChannelStd(id2, channel.getLett_read_key_2(), 1);

        database.delateChannelStd(x);
        x.setNotification(true);
        database.insertChannelStd(x);

        //comunico al service che devo attivare le notifiche
        channel=x;
        startService();

        return 0;
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Visualizzo popup la conferma dell'attivazione delle notifiche
     *
     * @param title
     * @param mex
     * @param cancelable
     * @param activity
     * @param function_std
     * @param d
     * @param <T>
     * @param <RR>
     */
    protected  <T,RR> void ShowAlert(String title, String mex, boolean cancelable, Activity activity, Function<T, RR> function_std, T d){
        /**
         *  necessario eseguire il codice nel medodo runOnUiThread di Activity
         *  in quanto ShowAlert potrebbe essere richiamato a seguito
         *  dell'esecuzione di un thread separato, non dal thread principale
         *  come succede per esempio se l'utente cerca di allenare
         *  il modello senza selezionarer i dati da usare nel training
         */
        activity.runOnUiThread(new Runnable() {
            public void run() {
                androidx.appcompat.app.AlertDialog.Builder alertBuilder = new androidx.appcompat.app.AlertDialog.Builder(activity);
                alertBuilder.setTitle(title);

                alertBuilder
                        .setMessage(mex)
                        .setCancelable(cancelable)
                        .setPositiveButton("Avvia",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(function_std != null){
                                    function_std.apply(d);
                                }
                                dialog.cancel();
                            }
                        }).setNegativeButton("Chiudi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                aSwitch.setChecked(false);
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        });
    }





    /**
     * Modifica effettuata da Matteo Torchia 599899
     * Devo controllare il secondo ID per recuperare il canale
     * dal database
     */
    /**
     * scarica l'ultimo dato inserito per capire la distanza in minuti
     */
    private void downloadMedia() {

        String id1 = channel.getLett_id();
        String id2 = channel.getLett_id_2();

        Channel actualchannel = null;

        if(id1 != null){
            actualchannel = database.getChannelStd(id1, channel.getLett_read_key(), 0);//database.ChannelDao().findByName(id1, channel.getLett_read_key());
        }
        else if(id2 != null){
            actualchannel = database.getChannelStd(id2, channel.getLett_read_key_2(), 1);//database.ChannelDao().findBySecondName(id2, channel.getLett_read_key_2());
        }

        //uso le due url per recuperare i dati dai due canali eventuali
        String url_1 = null;
        String url_2 = null;


        //con questa chiamata posso recuperare dalla risposta sia last_data_age che id del canale e poter effettuare la chiamata corretta successivamente
        if(id1 != null) url_1 = "https://api.thingspeak.com/channels/" + id1 + "/feeds.json/?api_key=" + actualchannel.getLett_read_key() + "&results=1";
        if(id2 != null) url_2 = "https://api.thingspeak.com/channels/" + id2 + "/feeds.json/?api_key=" + actualchannel.getLett_read_key_2() + "&results=1";

        //recupero il time dell ultimo inserimento per entrambi i canali
        getlasttime(url_1, url_2, actualchannel, id1, id2);
    }

    /**
     * scarica tutti gli ultimi valori
     * @param url_1:indirizzo utilizzato
     * @param channel:channel utilizzato
     */
    private void getlasttime(String url_1, String url_2, final Channel channel, String id1, String id2){

        //variabile per sapere dentro il codice di risposta a quale chiamata mi trovo
        //necessario usare un array final per avere visibilità dentro il codice del JsonObjectRequest

        final int[] idxToCall = {0};
        HashMap<Integer, Boolean> flag = new HashMap<>();
        String urlT0Call = null;

        /**
         * Controllo se verranno effettuate due chiamate o una sola per decidere quando eliminare il channel dal DB
         */
        if(url_1 != null) flag.put(0, true);
        else flag.put(0, false);

        if(url_2 != null) flag.put(1, true);
        else flag.put(1, false);

        for(int i = 0; i<2; i++){

            if(i == 0) urlT0Call = url_1;
            else urlT0Call = url_2;

            if(urlT0Call != null) {

                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlT0Call, null,

                        new Response.Listener<JSONObject>() {
                             int minuti=0;

                            /**
                             * metodo eseguito alla risposta del database
                             * @param response: messaggio di rispetto
                             */
                            @Override
                            public synchronized void onResponse(JSONObject response) {

                                try {

                                    idxToCall[0]++;//incremento la variabile per capire a quale risposta mi trovo

                                    //recupero l'id del canale che mi ha dato la risposta
                                    String idFromServer = response.getJSONObject("channel").get("id").toString();

                                    //recupero il valore ottenuto
                                    JSONArray jsonArray = response.getJSONArray("feeds");

                                    //effettuo il calcolo del last_data_age
                                    String cretime = retreiveLastDataAge((JSONObject) jsonArray.get(0));

                                    minuti= Integer.parseInt(cretime);
                                    minuti = (minuti /60) + 1;

                                    //controllo se devo eliminare dal DB il canale
                                    if(idxToCall[0] == 1){
                                        if(!flag.get(1))database.delateChannelStd(channel);//database.ChannelDao().delete(channel);
                                    }
                                    else database.delateChannelStd(channel);//database.ChannelDao().delete(channel);

                                    //se sono alla prima risposta allora setto il minutaggio della prima chiamata
                                    if(idFromServer.equals(id1))channel.setMinutes((double)minuti);
                                    else channel.setMinutes_cannel_2((double)minuti);//altrimenti il minutaggio della seconda (secondo canale)

                                    //Controllo se va inserito in questa chiamata o nell'eventuale chiamata successiva nel DB
                                    if(idxToCall[0] == 1){
                                        if(!flag.get(1))database.insertChannelStd(channel);//database.ChannelDao().insert(channel);
                                    }
                                    else database.insertChannelStd(channel);//database.ChannelDao().insert(channel);

                                    int dist = 0;

                                    if(idFromServer.equals(id1))dist = channel.getLastimevalues() + minuti;
                                    else dist = channel.getLastimevalues_channel_2() + minuti;

                                    String url = null;

                                    if(idFromServer.equals(id1)) url = "https://api.thingspeak.com/channels/" + channel.getLett_id() + "/feeds.json?api_key=" + channel.getLett_read_key()
                                            + "&minutes=" + dist + "&offset=" + MainActivity.getCurrentTimezoneOffset();
                                    else url = "https://api.thingspeak.com/channels/" + channel.getLett_id_2() + "/feeds.json?api_key=" + channel.getLett_read_key_2()
                                            + "&minutes=" + dist + "&offset=" + MainActivity.getCurrentTimezoneOffset();


                                    //Log.d("ALERTACTIVITY",urlString);
                                    getJsonResponse(url, flag);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    //in caso di errore
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Thread background", "errore download");
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
     * Modifiche effttuate da Matteo Torchia 599899
     * Codice modificato per consentire di recuperare i dati anche dal secondo canale
     * Necessario usare ulteriori parametri per poter effettuare le modifiche in base
     * a quante chiamate al server bisogna fare.
     */
    /**
     * metodo che reperisce tutti i valori
     * @param url:indirizzo utilizzato
     * @param flag: parametro per capire se successivamente ferrà effettuata una seconda chiamata
     */
    private void getJsonResponse (final String url, HashMap<Integer, Boolean> flag){


            final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,

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
                                Log.d("Thread background", "download eseguito JsonObjectRequest");
                                //salvo tutti i field nell'array

                                for (int i = 0; i < dim; i++) {

                                    try {
                                        fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i + 1))));
                                        fieldsKey.add("field" + (i + 1));
                                    }
                                    catch (Exception e){}
                                }

                                Double t = 0.0;
                                Double somt=0.0;
                                Double u = 0.0;
                                Double somu=0.0;
                                Double p = 0.0;
                                Double somp=0.0;
                                Double c = 0.0;
                                Double somc=0.0;
                                Double so = 0.0;
                                Double somso=0.0;
                                Double ir = 0.0;
                                Double somir=0.0;
                                Double pe=0.0;
                                Double sompe=0.0;

                                //variabili aggiunte da Matteo Torchia 599899
                                Double pesoPianta = 0.0;
                                Double sPesoPianta = 0.0;

                                Double vento = 0.0;
                                Double sVento = 0.0;

                                //stringa che mi salva l'ultimo data di aggiornamento dei valori
                                String cretime=null;
                                Channel v = channel;


                                //scorro tutto l'array
                                for (int i = 0; i < jsonArray.length(); i++) {


                                    //recupero il primo oggetto dell'array
                                    final JSONObject value = jsonArray.getJSONObject(i);

                                    //se sono nel caso in cui sto recuperando i valori presi dal canale
                                    //utilizzato per i vecchi velori lascio il codice invariato

                                    String field = null;

                                    try{
                                        field = setDataFieldCorrect(fields, value, fieldsKey, "temperature", 1, v);
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

                                        u=u+(Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                        somu++;
                                    }
                                    catch (Exception e){}

                                    try {

                                        field = setDataFieldCorrect(fields, value, fieldsKey, "windspeed", 8, v);

                                        vento = vento + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                        sVento++;
                                    }
                                    catch (Exception e){}


                                    try {
                                        field = setDataFieldCorrect(fields, value, fieldsKey, "pH_value", 4, v);

                                        p = p + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                        somp++;
                                    }
                                    catch (Exception e){
                                        e.printStackTrace();
                                    }

                                    try {
                                        field = setDataFieldCorrect(fields, value, fieldsKey, "electric_conductivity", 5, v);

                                        c = c + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                        somc++;
                                    }
                                    catch (Exception e){}

                                    try {
                                        field = setDataFieldCorrect(fields, value, fieldsKey, "irradiance", 6, v);
                                        ir = ir + (Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                        somir++;

                                    }
                                    catch (Exception e){}

                                    try {


                                        //se ho impostato un valore, inserisci quello,altrimenti non scrivo nulla
                                        String name = null, val = null;
                                        if((name = v.getImagepeso()) != null){


                                            val = value.getString(fieldsKey.get(fields.indexOf(name)));

                                            if(val != null && !val.isEmpty() && !val.equals("null")){
                                                field = val;
                                                pe=pe+(Math.round(Double.parseDouble(String.format(field)) * 100.0) / 100.0);
                                                sompe++;
                                                checkFlags.getMap().put("evapotraspirazione", true);
                                            }

                                        }
                                        else {

                                            if(fields.contains("irrigation") && fields.contains("drainage")){

                                                String idCur = response.getJSONObject("channel").get("id").toString();
                                                String url = null;

                                                if(idCur.equals(channel.getLett_id())){
                                                    url = "https://api.thingspeak.com/channels/"+channel.getLett_id()+"/fields/7-8.json?api_key=" + channel.getLett_read_key();
                                                }
                                                else {
                                                    url = "https://api.thingspeak.com/channels/"+channel.getLett_id_2()+"/fields/7-8.json?api_key=" + channel.getLett_read_key_2();
                                                }
                                                downloadEvapotraspirazione(url);
                                            }

                                        }

                                    }
                                    catch (Exception e){}
                                    try {

                                        field = setDataFieldCorrect(fields, value, fieldsKey, "moisture", 7, v);
                                        so=so+Math.round(Double.parseDouble(String.format(field)));
                                        somso++;

                                    }
                                    catch (Exception e){}
                                }

                                //calcolo la media di tutti i valori e la confronto con i miei valori,se la supera invio la notifica
                                t=Math.round(t/somt * 100.0) / 100.0;
                                u=Math.round(u/somu * 100.0) / 100.0;
                                p=Math.round(p/somp * 100.0) / 100.0;
                                c=Math.round(c/somc * 100.0) / 100.0;
                                ir=Math.round(ir/somir * 100.0) / 100.0;
                                pe=Math.round(pe/sompe * 100.0) / 100.0;
                                so=Math.round(so/somso * 100.0) / 100.0;


                                vento = Math.round(vento/sVento * 100.0) / 100.0;
                                pesoPianta = Math.round(pesoPianta/sPesoPianta * 100.0) / 100.0;


                                Log.d("SOMMA VALORI: ","t:"+somt+" u:"+ somu +" ph:"+ somp +" c:"+ somc +" ir:"+ somir+" pe:"+ sompe +" so:"+ somso);
                                Log.d("MEDIA VALORI: ","t:"+t+" u:"+ u +" ph:"+ p +" c:"+ c +" ir:"+ ir +" pe:"+ pe + " so:"+ so);

                                if(channel.getNotification())Log.d("NOTIFICHE", "ATTIVE");
                                else Log.d("NOTIFICHE", "NON ATTIVE");

                                    //invio le notifiche se i valori non rispettano le soglie imposte
                                    try {
                                        if (temp != null){
                                            if(!checkFlags.getMap().get("temperature"))temp.setText("- -");
                                            else if(somt != 0)temp.setText(String.valueOf(t));
                                        }
                                    } catch (Exception e) {
                                       temp.setText("- -");
                                    }
                                    try {
                                        if (umid != null){
                                            //se non ho scaricato valori
                                            if(!checkFlags.getMap().get("humidity"))  umid.setText("- -");
                                            else if(somu != 0) umid.setText(String.valueOf(u));
                                        }
                                    } catch (Exception e) {
                                        umid.setText("- -");
                                    }
                                    try {
                                        if (ph != null){
                                            //se non ho scaricato valori
                                            if(!checkFlags.getMap().get("pH_value"))  ph.setText("- -");
                                            else if(somp != 0)  ph.setText(String.valueOf(p));
                                        }
                                    } catch (Exception e) {
                                       ph.setText("- -");
                                    }
                                    try {
                                        if (cond != null){
                                            //se non ho scaricato valori
                                            if(!checkFlags.getMap().get("electric_conductivity"))  cond.setText("- -");
                                            else if(somc != 0) cond.setText(String.valueOf(c));
                                        }
                                    } catch (Exception e) {
                                       cond.setText("- -");
                                    }
                                    try {
                                        if (irra != null){
                                            //se non ho scaricato valori
                                            if(!checkFlags.getMap().get("irradiance")) irra.setText("- -");
                                            else if(somir!=0)irra.setText(String.valueOf(ir));
                                        }
                                    } catch (Exception e) {
                                      irra.setText("- -");
                                    }
                                     try {
                                         if (peso != null && sompe!=0) peso.setText(String.valueOf(pe));
                                    } catch (Exception e) {
                                        peso.setText("- -");
                                    }
                                    try {
                                        if(soil != null){
                                            if(!checkFlags.getMap().get("moisture"))soil.setText("- -");
                                            else if(somso != 0)soil.setText(String.valueOf(so));
                                        }
                                    } catch (Exception e) {
                                        soil.setText("- -");
                                    }
                                    try {
                                        if (textViewPeso1 != null){
                                            if(!checkFlags.getMap().get("P0"))  textViewPeso1.setText("- -");
                                            else if(sPesoPianta != 0) textViewPeso1.setText(String.valueOf(pesoPianta));
                                        }
                                    }
                                    catch (Exception e) {
                                        textViewPeso1.setText("- -");
                                    }
                                    try {
                                        if (textViewvento != null){
                                            if(!checkFlags.getMap().get("windspeed"))  textViewvento.setText("- -");
                                            else if(sVento != 0) textViewvento.setText(String.valueOf(vento));
                                        }
                                    }
                                    catch (Exception e) {
                                        textViewvento.setText("- -");
                                    }

                                    checkResetFlags(flag, checkFlags);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            Log.d("AlertActivity", "download eseguito correttamente");
                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("AlertActivity", "errore donwload");
                }
            });
            Volley.newRequestQueue(getContext()).add(jsonObjectRequest);

    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Controllo e modifico la variabile flag per verificare se è
     * arrivata l'ultima risposta dal server, in tal caso resetto
     * le var flag necessarie per il recupero dei valori.
     * @param flag
     * @param checkFlags
     */
    private void checkResetFlags(HashMap<Integer, Boolean>  flag, ChannelFieldsSelected checkFlags){

        if(flag.get(0) != null){

            flag.put(0, null);
            if(!flag.get(1)){
                flag.put(1, null);
                checkFlags.setMap(new HashMap<>());
            }
        }
        else if(flag.get(1) != null){
            flag.put(1, null);
            checkFlags.setMap(new HashMap<>());
        }
    }
    /**
     * scarica i dati riguardante gli ultimi valori dell'evapotraspirazione
     * @param urlString:
     */
    private void downloadEvapotraspirazione(String urlString) {
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

                            Boolean ok=false;
                            Boolean ok1=false;
                            Double irrigazione =0.0;
                            Double drainaggio = 0.0;

                            //scandisco tutti i 100 valori per trovare i valori di irrigazione e drenaggio
                            for (int k = 0; k < jsonArray.length(); k++) {
                                JSONObject valori = jsonArray.getJSONObject(k);
                                try {
                                    if (!valori.getString("field7").equals("") && !valori.getString("field7").equals("null")) {
                                        ok=true;
                                        irrigazione=Double.parseDouble(valori.getString("field7"));
                                    }
                                }catch (Exception e){ }

                                try {
                                    if (!valori.getString("field8").equals("") && !valori.getString("field8").equals("null")) {
                                        ok1=true;
                                        drainaggio=Double.parseDouble(valori.getString("field8"));
                                    }
                                }catch (Exception e){ }

                                if(ok && ok1) {
                                    Double ev=Math.round((irrigazione - drainaggio) * 100.0) / 100.0;
                                    peso.setText(String.valueOf(ev));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("AlertActivity", "errore donwload");
            }
        });
        Volley.newRequestQueue(cont).add(jsonObjectRequest);
    }

    /**
     * restituisce l'intent
     * @param context: context
     * @return intent associato dell'activity
     */
    public static Intent getActivityintent(Context context){
        Intent intent=new Intent(context, AlertActivity.class);
        return intent;
    }

    /**
     * se premo il pulsante save
     * @param v puntatore al pulsante save
     */
    public void saveButton(View v) {

        Channel x = null;
        String id1 = channel.getLett_id();
        String id2 = channel.getLett_id_2();

        if(id1 != null)x = database.getChannelStd(id1, channel.getLett_read_key(), 0);//database.ChannelDao().findByName(id1, channel.getLett_read_key());
        else x = database.getChannelStd(id2, channel.getLett_read_key(), 1);//database.ChannelDao().findBySecondName(id2, channel.getLett_read_key());

        if (x != null) {

            database.delateChannelStd(x);
            //database.ChannelDao().delete(x);

            try {
                x.setPesPiantaMin(Double.valueOf(pesoPiantamin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setPesPiantaMax(Double.valueOf(pesoPiantamax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setVentoMin(Double.valueOf(ventoMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setVentoMax(Double.valueOf(ventomax.getText().toString()));
            } catch (NumberFormatException e) {
            }

            try {
                x.setTempMin(Double.valueOf(tempMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setTempMax(Double.valueOf(tempMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setUmidMin(Double.valueOf(umidMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setUmidMax(Double.valueOf(umidMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setCondMin(Double.valueOf(condMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setCondMax(Double.valueOf(condMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setPhMin(Double.valueOf(phMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setPhMax(Double.valueOf(phMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                String vv = irraMin.getText().toString();
                Double value = Double.valueOf(irraMin.getText().toString());
                x.setIrraMin(Double.valueOf(irraMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setIrraMax(Double.valueOf(irraMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setPesMin(Double.valueOf(pesMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setPesMax(Double.valueOf(pesMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setSoilMin(Double.valueOf(soilMin.getText().toString()));
            } catch (NumberFormatException e) {
            }
            try {
                x.setSoilMax(Double.valueOf(soilMax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            /*try {
                x.setLastimevalues(Integer.valueOf(minutes.getText().toString()));
            } catch (NumberFormatException e) {
            }*/
            try {
                x.setTempomax(Integer.valueOf(tempomax.getText().toString()));
            } catch (NumberFormatException e) {
            }
            database.insertChannelStd(x);
            channel=x;
            Toast.makeText(cont,"VALORI SALVATI CORRETTAMENTE!",Toast.LENGTH_SHORT).show();
        }

        //riscarico i dati e faccio un reset di tutto
        downloadMedia();
    }

    /**
     * se premo il pulsante reset
     * @param v puntatore al pulsante reset
     */
    public void resetButton (View v) {

            Channel x = null;
            String id1 = channel.getLett_id();
            String id2 = channel.getLett_id_2();

            if(id1 != null)x = database.getChannelStd(id1, channel.getLett_read_key(), 0);//database.ChannelDao().findByName(id1, channel.getLett_read_key());
            else x = database.getChannelStd(id2, channel.getLett_read_key_2(), 1);//database.ChannelDao().findBySecondName(id2, channel.getLett_read_key());

            database.delateChannelStd(x);
            //database.ChannelDao().delete(x);
            x.setTempMin(null);
            x.setTempMax(null);
            x.setUmidMin(null);
            x.setUmidMax(null);
            x.setCondMin(null);
            x.setCondMax(null);
            x.setPhMin(null);
            x.setPhMax(null);
            x.setIrraMin(null);
            x.setIrraMax(null);
            x.setPesMin(null);
            x.setPesMax(null);
            x.setVentoMin(null);
            x.setVentoMax(null);
            x.setPesPiantaMin(null);
            x.setPesPiantaMax(null);
            x.setLastimevalues(0);
            x.setTempomax(0);
            x.setSoilMax(null);
            x.setSoilMin(null);
            database.insertChannelStd(x);

            //resetto i valori anche nei text
            tempMin.setText(" ");
            tempMax.setText(" ");
            umidMin.setText(" ");
            umidMax.setText(" ");
            condMin.setText(" ");
            condMax.setText(" ");
            phMin.setText(" ");
            phMax.setText(" ");
            irraMin.setText(" ");
            irraMax.setText(" ");
            pesMin.setText(" ");
            pesMax.setText(" ");
            pesoPiantamin.setText(" ");
            pesoPiantamax.setText(" ");
            ventoMin.setText(" ");
            ventomax.setText(" ");
           // minutes.setText(" ");
            tempomax.setText(" ");
            soilMin.setText(" ");
            soilMax.setText(" ");
            Toast.makeText(cont,"VALORI RESETTATI CORRETTAMENTE",Toast.LENGTH_SHORT).show();
            downloadMedia();
    }

    /**
     * inizializzo il channel all'apertura iniziale
     * @param chan: channel da impostare
     */
    public static void setChannel(Channel chan){
        channel=chan;
    }

    /**
     *
     * @return restituisce il context associato al channel
     */
    public static Context getContext(){
        return cont;
    }

    /**
     * per avviare ForegroundServices
     */
    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Start Foreground Service");
        ContextCompat.startForegroundService(this, serviceIntent);
        Log.d("MAINACTIVITY","STARTSERVICE");
    }

    /**
     * per fermare ForegroundServices
     */
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        //ForegroundService.stoptimer();
        stopService(serviceIntent);
        Log.d("MAINACTIVITY","STOPSERVICE");
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Metodo usato per recuperare la posizione da cui viene selezionato il field nella lista
     * di selezione nella schermata principale
     */
    private int retreivePos(int field, Channel v){

        String arrayImg = null;
        arrayImg = retreiveImage(field, v);

        //recupero la posizione selezionata
        if(arrayImg == null) return -1;

        int pos = Integer.parseInt(arrayImg.substring(5));
        return pos;
    }
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
     * Recupero il field corretto dal canale
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
     * Metodo Aggiunto Da Matteo Torchia 599899
     * Recupera il valore corretto dalla risposta
     * @param fieldInResponse
     * @param fieldToSearch
     * @param v
     * @param imageIDX
     * @param value
     * @return
     * @throws Exception
     */
    /*private String retreiveValue(String fieldInResponse, String fieldToSearch, Channel v, JSONObject value,
                                 int imageIDX, int numField, ArrayList<String> fields, int flagPosition) throws Exception{

        String field = null;

        if(retreiveImage(imageIDX, v) != null) {
            field = supportForPosition(flagPosition, imageIDX, v, value, fields);
        }
        else {
            if(fieldInResponse.equals(fieldToSearch)){
                selectFlag(flagPosition, 1, true, v);
                field = value.getString("field" + numField);
            }
        }
        return field;
    }

     */
    /*private String supportForPosition(int flagposition, int imageIDX, Channel v, JSONObject value, ArrayList<String> fields) throws Exception{

        String field = null;

        int tmpPos = retreivePos(imageIDX, v);
        int idxInFields = -1;
        int realPos = -1;

        if(tmpPos > 8) {

            realPos = ((tmpPos - 1) % 8) + 1;
            String nameFIeld = select_check_Field(realPos - 1, -2, "", v);
            idxInFields = fields.indexOf(nameFIeld);

            if(idxInFields != -1){
                selectFlag(flagposition, 1, true, v);
                field = value.getString("field" + (idxInFields + 1));
            }

        }
        else {

            String nameFIeld = select_check_Field(tmpPos - 1, -1, "", v);
            idxInFields = fields.indexOf(nameFIeld);

            if(idxInFields != -1){
                selectFlag(flagposition, 1, true, v);
                field = value.getString("field" + (idxInFields + 1));
            }
        }
        return field;
    }

     */
    /**
     * metodo aggiunto da Matteo Torchia 599899
     * Recupero il valore settato nel field quando devo settare il valore
     */
   /* private String select_check_Field(int field, int choice, String fieldName, Channel v) {

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

    */

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Controllo o setto se un dato è stato trovato da un canale o meno
     *
     * @param position
     * @param choice
     * @param value
     * @param v
     * @return
     */
   /* private boolean selectFlag(int position, int choice, boolean value, Channel v){

        boolean flag = false;
        switch (position) {
            case 0:

                if(choice == 0)flag = checkFlags.isFlag_field1();
                else checkFlags.setFlag_field1(value);

                break;
            case 1:

                if(choice == 0)flag = checkFlags.isFlag_field2();
                else checkFlags.setFlag_field2(value);

                break;
            case 2:

                if(choice == 0)flag = checkFlags.isFlag_field3();
                else checkFlags.setFlag_field3(value);
                break;
            case 3:

                if(choice == 0)flag = checkFlags.isFlag_field4();
                else checkFlags.setFlag_field4(value);


                break;
            case 4:
                if(choice == 0)flag = checkFlags.isFlag_field5();
                else checkFlags.setFlag_field5(value);
                break;
            case 5:

                if(choice == 0)flag = checkFlags.isFlag_field6();
                else checkFlags.setFlag_field6(value);
                break;
            case 6:
                //utilizzato in questo caso per controllare il valore del'evotraspirazione
                if(choice == 0)flag = checkFlags.isFlag_field7();
                else checkFlags.setFlag_field7(value);
                break;
            case 7:
                flag = checkFlags.isFlag_field8();
                break;
            case 8:

                if(choice == 0)flag = checkFlags.isFlag_field1_2();
                else checkFlags.setFlag_field1_2(value);

                break;
            case 9:

                if(choice == 0)flag = checkFlags.isFlag_field2_2();
                else checkFlags.setFlag_field2_2(value);
                break;
        }
        return flag;
    }*/


}
