package com.example.GreenApp.Graphic;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.TypedArrayUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Channel.Channel;
import com.example.firstapp.R;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

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
 * Effettuo modifiche per consntire il recupero dei dati
 * dall'eventuale secondo canale associato.
 */

public class MainActivity extends AppCompatActivity {
    

    LineGraphSeries<DataPoint> series;
    private static String channelID=null;
    private static String READ_KEY=null;
    private static String url="https://api.thingspeak.com/channels/"+channelID+ "/feeds.json?api_key=" + READ_KEY;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private static ArrayList<String> nameFields;
    private static ArrayList<Integer> position;
    private static List<Channel> channelPos;


    //dichiaro le strutture
    private List<Double> fields1=new ArrayList<>();
    private List<Double> fields2=new ArrayList<>();
    private List<Double> fields3=new ArrayList<>();
    private List<Double> fields4=new ArrayList<>();
    private List<Double> fields5=new ArrayList<>();
    private List<Double> fields6=new ArrayList<>();
    private List<Double> fields7=new ArrayList<>();
    private List<Double> fields8=new ArrayList<>();

    private List<String> date_fields1=new ArrayList<>();
    private List<String> date_fields2=new ArrayList<>();
    private List<String> date_fields3=new ArrayList<>();
    private List<String> date_fields4=new ArrayList<>();
    private List<String> date_fields5=new ArrayList<>();
    private List<String> date_fields6=new ArrayList<>();
    private List<String> date_fields7=new ArrayList<>();
    private List<String> date_fields8=new ArrayList<>();
    private Context context=this;
    private static int i=0;
    private static TextView dataStart;
    private static TextView dataEnd;

    private List<ModelData> Insertdata=new ArrayList<>();

    /**
     * configuro i parametri
     * @param nameList: contiene la lista dei field
     * @param channel: contiene la lista dei channnel ssociati ad ogni elemento del field selezionato
     * @param pos: posizione nel grafico
     */
    public static void setGrapView(ArrayList<String> nameList, List<Channel> channel, ArrayList<Integer> pos){
        nameFields=nameList;
        position=pos;
        channelPos=channel;
        position=pos;
    }

    @Override
    /**
     * metodo eseguito alla creazione della classe
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graphic_activity_main);

        //creo le associazioni
        recyclerView = findViewById(R.id.recyclerview);
        dataStart=findViewById(R.id.textViewInizio);
        dataEnd=findViewById(R.id.textViewfine);

        //scandisco i channel e recupero i dati associati (finoa 8000 valori)
        for(int i=0;i<channelPos.size();i++){

            url="https://api.thingspeak.com/channels/"+channelPos.get(i).getLett_id()+"/feeds.json?api_key="+channelPos.get(i).getLett_read_key()+"&results=8000"+"&offset="+getCurrentTimezoneOffset();
            getJsonResponse(url,i);
        }
    }

    /**
     * azione che deve avvenire quando premo sul pulsante vai
     * @param v: puntatore di riferimento al pulsante "VAI"
     */
    public void visualizzaGrafici(View v){

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        Calendar dateObject = Calendar.getInstance();
        Calendar dateObject1 = Calendar.getInstance();
        int giornoStart=0;
        int meseStart=0;
        int annoStart=0;
        int giornoEnd=0;
        int meseEnd=0;
        int annoEnd=0;
        int stop=0;
        try{
            //parsing data inizio
            String dob_var=dataStart.getText().toString();
            dateObject.setTime(formatter.parse(dob_var));

            giornoStart=dateObject.get(Calendar.DAY_OF_MONTH);
            meseStart=dateObject.get(Calendar.MONTH)+1;
            annoStart=dateObject.get(Calendar.YEAR);

            //parsing data fine
            String dob_var1=(dataEnd.getText().toString());
            dateObject1.setTime(formatter.parse(dob_var1));

            giornoEnd=dateObject1.get(Calendar.DAY_OF_MONTH);
            meseEnd=dateObject1.get(Calendar.MONTH)+1;
            annoEnd=dateObject1.get(Calendar.YEAR);
        }

        //in caso di errore nell'inserimento mando errore
        catch (java.text.ParseException e)
        {
            stop=1;
            Toast.makeText(getApplicationContext(), "data inserita non corretta",Toast.LENGTH_SHORT).show();
            Log.i("Graphc/MainActivity", e.toString());
        }
        Log.i("Graphc/MainActivity","ho inserito:\n data inizio: " + giornoStart+"-"+meseStart+"-"+annoStart+"\n"+"data fine: "+ giornoEnd+"-"+meseEnd+"-"+annoEnd);

        //scarico i nuovi dati nel caso in cui la data è stata impostata correttamente
        if(stop==0) {
            Insertdata.clear();
            for (int i = 0; i < channelPos.size(); i++) {
                url = "https://api.thingspeak.com/channels/" + channelPos.get(i).getLett_id() + "/feeds.json?api_key=" + channelPos.get(i).getLett_read_key() +
                        "&start=" + annoStart + "-" + meseStart + "-" + giornoStart + "%2000:00:00&end=" + annoEnd + "-" + meseEnd + "-" + giornoEnd + "%2023:59:59&results=8000"+"&offset="+getCurrentTimezoneOffset();
                getJsonResponse(url, i);
            }
        }
    }

    /**
     * metodo per scaricare tutti i dati associati al channel
     * @param url: indirizzo utilizzato per scaricare i dati
     * @param index: posizione del channel
     */
    private void getJsonResponse(String url, final int index) {

        final JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Graphic/MainActivity", "download eseguito correttamente");
                        try {
                            //recupero l'array feeds
                            JSONArray jsonArray = response.getJSONArray("feeds");
                            String posfil="fields".concat(""+position.get(index));
                            //scorro tutto l'array e stampo a schermo il valore di field1
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //recupero l'oggetto dell'array di indice i
                                final JSONObject value = jsonArray.getJSONObject(i);

                                //salvo i valori contenuti nei field di tipo double
                                try {
                                    if (posfil.equals("fields1") && !value.getString("field1").equals("") && !value.getString("field1").equals("null")) {
                                        fields1.add(Double.parseDouble(value.getString("field1")));
                                        date_fields1.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try {
                                    if (posfil.equals("fields2") && !value.getString("field2").equals("") && !value.getString("field2").equals("null")) {
                                        fields2.add(Double.parseDouble(value.getString("field2")));
                                        date_fields2.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try {
                                    if (posfil.equals("fields3") && !value.getString("field3").equals("") && !value.getString("field3").equals("null")) {
                                        fields3.add(Double.parseDouble(value.getString("field3")));
                                        date_fields3.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try {
                                    if (posfil.equals("fields4") && !value.getString("field4").equals("") && !value.getString("field4").equals("null")) {
                                        fields4.add(Double.parseDouble(value.getString("field4")));
                                        date_fields4.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try{
                                    if(posfil.equals("fields5") && !value.getString("field5").equals("") && !value.getString("field5").equals("null")){
                                        fields5.add(Double.parseDouble(value.getString("field5")));
                                        date_fields5.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try {
                                    if (posfil.equals("fields6") && !value.getString("field6").equals("") && !value.getString("field6").equals("null")) {
                                        fields6.add(Double.parseDouble(value.getString("field6")));
                                        date_fields6.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }
                                try{
                                    if(posfil.equals("fields7") && !value.getString("field7").equals("") && !value.getString("field7").equals("null")){
                                        fields7.add(Double.parseDouble(value.getString("field7")));
                                        date_fields7.add(value.getString("created_at"));
                                    }
                                }catch (Exception e){ }

                                try{
                                      if(posfil.equals("fields8") && !value.getString("field8").equals("") && !value.getString("field8").equals("null")){
                                         fields8.add(Double.parseDouble(value.getString("field8")));
                                         date_fields8.add(value.getString("created_at"));
                                      }
                                }catch (Exception e){ }
                            }

                            //controllo che siano presenti dati nel periodo di tempo selezionato dall'utente
                            if(jsonArray.length() > 0){

                                //controllo se il field selelzionato era relativo al fieldx (in tal caso lo rappresento graficamente)
                                if(!fields1.isEmpty() && posfil.equals("fields1"))makegraph(nameFields.get(index),fields1,date_fields1);
                                if(!fields2.isEmpty() && posfil.equals("fields2"))makegraph(nameFields.get(index),fields2,date_fields2);
                                if(!fields3.isEmpty() && posfil.equals("fields3"))makegraph(nameFields.get(index),fields3,date_fields3);
                                if(!fields4.isEmpty() && posfil.equals("fields4"))makegraph(nameFields.get(index),fields4,date_fields4);
                                if(!fields5.isEmpty() && posfil.equals("fields5"))makegraph(nameFields.get(index),fields5,date_fields5);
                                if(!fields6.isEmpty() && posfil.equals("fields6"))makegraph(nameFields.get(index),fields6,date_fields6);
                                if(!fields7.isEmpty() && posfil.equals("fields7"))makegraph(nameFields.get(index),fields7,date_fields7);
                                if(!fields8.isEmpty() && posfil.equals("fields8"))makegraph(nameFields.get(index),fields8,date_fields8);

                                //imposto il recyclerview e l'adapter
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                                recyclerView.setLayoutManager(linearLayoutManager);
                                adapter=new RecyclerViewAdapter(Insertdata, context);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setHasFixedSize(true); //le cardView sono tutte delle stesse dimensioni
                                adapter.notifyDataSetChanged();

                            }
                            else {
                                //TODO devo trovare il modo di comunicare all'utente che per tale conale
                                //TODO nelle date scelte non ci sono dati
                                Toast.makeText(getApplicationContext(), "Channel: " + response.getJSONObject("channel").getString("id") + "\nDati assenti nel periodo selezionato" ,Toast.LENGTH_SHORT).show();
                            }

                            //libero tutta la memoria
                            fields1.clear();
                            date_fields1.clear();
                            fields2.clear();
                            date_fields2.clear();
                            fields3.clear();
                            date_fields3.clear();
                            fields4.clear();
                            date_fields4.clear();
                            fields5.clear();
                            date_fields5.clear();
                            fields7.clear();
                            date_fields6.clear();
                            fields6.clear();
                            date_fields7.clear();
                            fields8.clear();
                            date_fields8.clear();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Errore nel download");
                Toast x= Toast.makeText(getApplicationContext(),"Errore download",Toast.LENGTH_SHORT);
                x.show();
            }
        });
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);

    }

    /**
     * funzione che mi permette di creare la struttura ModelData che verrà poi passata al recyclerview per rappresentare i grafici
     * @param name: nome del field selezonato
     * @param list: insieme di valori scaricati ti tipo double
     * @param created: lista contennete le date di creazione di ogni field
     */
    private void makegraph(String name, List<Double> list,List<String> created) {

        DataPoint[] data = new DataPoint[created.size()];

        //detect time shift
        Calendar date_value_early = Calendar.getInstance();
        Calendar date_value_late = Calendar.getInstance();
        Double somma=0.0;

        //inizializzare all'istante 0
        int array_idx;
        String data0 = created.get(0);
        int giorno = Integer.valueOf(data0.substring(8, 10));
        int mese = Integer.valueOf(data0.substring(5, 7));
        int anno = Integer.valueOf(data0.substring(0, 4));
        int ore = Integer.valueOf(data0.substring(11, 13));
        int minuti = Integer.valueOf(data0.substring(14, 16));
        int secondi = Integer.valueOf(data0.substring(17, 19));
        date_value_late.set(Calendar.YEAR, anno);
        date_value_late.set(Calendar.MONTH, mese - 1);
        date_value_late.set(Calendar.DAY_OF_MONTH, giorno);
        date_value_late.set(Calendar.HOUR_OF_DAY, ore);
        date_value_late.set(Calendar.MINUTE, minuti);
        date_value_late.set(Calendar.SECOND, secondi);
        array_idx = 0;
        data[array_idx] = new DataPoint(date_value_late.getTime(), list.get(0));


        //scorro tutto l'array date e per ogni elemento memorizzo i valori associati
        for (int i = 1; i < created.size(); i++) {
            String data_creazione = created.get(i);

            giorno = Integer.valueOf(data_creazione.substring(8, 10));
            mese = Integer.valueOf(data_creazione.substring(5, 7));
            anno = Integer.valueOf(data_creazione.substring(0, 4));
            ore = Integer.valueOf(data_creazione.substring(11, 13));
            minuti = Integer.valueOf(data_creazione.substring(14, 16));
            secondi = Integer.valueOf(data_creazione.substring(17, 19));

            date_value_early.set(Calendar.YEAR, anno);
            date_value_early.set(Calendar.MONTH, mese - 1);
            date_value_early.set(Calendar.DAY_OF_MONTH, giorno);
            date_value_early.set(Calendar.HOUR_OF_DAY, ore);
            date_value_early.set(Calendar.MINUTE, minuti);
            date_value_early.set(Calendar.SECOND, secondi);

            if (date_value_early.after(date_value_late) ) { //check if daylight saving has changed
                array_idx = array_idx + 1;

                Date dat = date_value_early.getTime();
                data[array_idx] = new DataPoint(dat, list.get(i));
                date_value_late.setTime(dat);
                somma=somma+list.get(i);
            }

        }
        DataPoint[] data_shrinked = new DataPoint[array_idx+1];  //shrink data during change of daylight saving
        data_shrinked = Arrays.copyOf(data,array_idx+1);
        //creo la nuova serie, calcolo la media dei valori ed aggiungo la classe ModelData contenente tutte le info scaricate
        series = new LineGraphSeries<>(data_shrinked);
        series.setColor(Color.RED);
        Double media=Math.round((somma/data_shrinked.length) * 100.0) / 100.0;
        Insertdata.add(new ModelData(name, series,media));
        date_value_early.clear();
        date_value_late.clear();
    }

    /**
     *
     * @param context: il Contex della classe
     * @return Intent associato alla classe
     */
    public static Intent getActivityintent(Context context){
            Intent intent=new Intent(context, MainActivity.class);
            return intent;
        }

    /**
     * funzione che mi calcola l'offset in base all time-zone in cui mi trovo
     * @return il valore GTM+ relativo alla zona in numero intero (es 2= GTM+2)
     */
    public static String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        int offsetInMillis = tz.getRawOffset();
        /*int DSTSavInMillis = tz.getDSTSavings();
        if(tz.inDaylightTime(new Date())){
            offsetInMillis = offsetInMillis + DSTSavInMillis;
        }*/
        return String.valueOf( offsetInMillis/(1000*3600));
    }

    /**
     * funzione eseguita quando premo sul pulsante data inzio
     * @param v: puntatore al riferimento della TextView data-inizio
     */
    public void inizio(View v){
       final  TextView testo=findViewById(R.id.textViewInizio);
        Calendar current=Calendar.getInstance();
        int day=current.get(Calendar.DAY_OF_MONTH);
        int month= current.get(Calendar.MONTH);
        int year=current.get(Calendar.YEAR);

        //avvio un dialog attraverso cui l'utente potrà selezionare la data desiderata
        DatePickerDialog datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int mouthofyear, int dayofMonth) {
                //TODO: da inserire controllo data fine data inizio
                mouthofyear=mouthofyear+1;
                testo.setText(dayofMonth+"/"+mouthofyear+"/"+year);
            }
        }, year,month,day);
        datePickerDialog.show();
    }

    /**
     * funzione eseguita quando premo sul pulsante data fine
     * @param v: puntatore al riferimento della TextView data-fine
     */
    public void fine(View v){
        final TextView testo=findViewById(R.id.textViewfine);
        Calendar current=Calendar.getInstance();
        int day=current.get(Calendar.DAY_OF_MONTH);
        int month= current.get(Calendar.MONTH);
        int year=current.get(Calendar.YEAR);

        DatePickerDialog datePickerDialog=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int mouthofyear, int dayofMonth) {
                //TODO: da inserire controllo data fine data inizio
                mouthofyear=mouthofyear+1;
                testo.setText(dayofMonth+"/"+mouthofyear+"/"+year);
            }
        }, year,month,day);
        datePickerDialog.show();
    }
}
