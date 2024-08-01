package com.example.GreenApp.Channel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.GreenApp.Alert.MyTimerTask;
import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.MainActivity;
import com.example.firstapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

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
 * Modifiche affettuate da Matteo Torchia 599899
 * Devo aggiungere la possibilità di aggiungere due canali di lettura
 *
 */

public class ChannelActivity  extends AppCompatActivity {

    private static List<Channel> channel;
    private static AppDatabase db;
    private static RecyclerView recycleView;
    private static RecyclerViewAdapter adapter;
    private static Context BasicContext;
    private static View viewlayout;
    private static View defaultStar = null;
    private static int pos=0;
    private static String DEFAULT_ID =null;

    /**
     * Variabili aggiunte da Matteo Torchia 599899
     */
    private static String DEFAULT_ID_2 = null;
    private static String DEFAULT_READ_KEY_2 = null;

    /*********************************************/
    private static String DEFAULT_READ_KEY = null;

    private static String name = null;


    public static void setPosition(int position) {
        pos=position;
    }

    @Override
    /**
     * metodo eseguito alla creazione
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_main);

        //creo le associazioni con gli elementi grafici
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewlayout=findViewById(R.id.view_layout);
        BasicContext=ChannelActivity.this;

        //creo il database
        if(savedInstanceState==null) {

            db = AppDatabase.getDataBase(getApplicationContext());
            getValue();
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        //azione quando premo il pulsante "+"
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = Channelinsert.getActivityintent(ChannelActivity.this);
                startActivity(intent);
            }
        });

        recycleView=findViewById(R.id.recyclerview);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        recycleView.setLayoutManager(linearLayoutManager);
        adapter=new RecyclerViewAdapter(channel,BasicContext,db);
        recycleView.setAdapter(adapter);
        recycleView.setHasFixedSize(true);
    }

    /**
     *  metodo eseguito quando vado ad inserire un nuovo channel, effettua un controllo sull'esistenza del channel su thinkspeak
     *
     * @param id: id associato al channel
     * @param key: api lettura chiave lettura
     * @param id_scritt: id chiave di scrittura
     * @param read_scritt: api lettura chiave scrittura
     * @param write_scritt: api scrittura chiave scrittura
     */
    public static void Execute(String id, String key, String id_scritt, String read_scritt, String write_scritt,
                                String id_2, String key_2, String nameChannel){

        //controllo se i dati inseriti corrispondono ad un channel esistente
        Channel t1 = db.getChannelStd(id, key, 0);
        Channel t2 = db.getChannelStd(id_2, key_2, 1);

        if (
                db.getChannelStd(id, key, 0) != null ||
                db.getChannelStd(id, key, 1) != null ||
                db.getChannelStd(id_2, key_2, 0) != null ||
                db.getChannelStd(id_2, key_2 ,1) != null
            )
        {
            Toast.makeText(BasicContext, "channel già esistente " + id + "!", Toast.LENGTH_SHORT).show();
        }
        else {

            // controllo se i parametri inseriti sono corretti
            DEFAULT_ID = id;
            DEFAULT_READ_KEY = key;

            DEFAULT_ID_2 = id_2;
            DEFAULT_READ_KEY_2 = key_2;

            name = nameChannel;
            Boolean [] resultChannels = testData(DEFAULT_ID, DEFAULT_READ_KEY, DEFAULT_ID_2, DEFAULT_READ_KEY_2, id_scritt,read_scritt,write_scritt);

            if(!resultChannels[0] && !resultChannels[1]) {
                Toast.makeText(BasicContext, "operazione ERRATA!", Toast.LENGTH_SHORT).show();
            }
            else {

                //comunico il database aggiornato al thread
                com.example.GreenApp.MyTimerTask.updateDatabase(db);
                Toast.makeText(BasicContext, "operazione eseguita correttamente!", Toast.LENGTH_SHORT).show();

                //segnalo al thread principale i nuovi id,key
                if (pos == -1) pos = 0;

                MainActivity.setDefaultSetting(DEFAULT_ID, DEFAULT_READ_KEY, DEFAULT_ID_2, DEFAULT_READ_KEY_2, pos);


            }
            //segnalo eventuali modifiche
            adapter.notifyDataSetChanged();

        }
    }

    /**
     * metodo impostare i valori
     */
    private void getValue() {

        channel=db.getAllChannelStd();//ChannelDao().getAll();
        recycleView=viewlayout.findViewById(R.id.recyclerview);
        final LinearLayoutManager linearLayoutManager= new LinearLayoutManager(BasicContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recycleView.setLayoutManager(linearLayoutManager);
        adapter=new RecyclerViewAdapter(channel,BasicContext,db);
        recycleView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /**
     * azione da svolgere dopo che ho ciccato sul pulsante "delete"
     * @param v: puntatore al pulsante delete
     * @param context: context associato alla classe
     * @param position: poszione del channel nel recyclerview
     */
    public static void sendObjcet(View v, Context context, final int position) {

        //avviso di cancellazione
        AlertDialog.Builder builder=new AlertDialog.Builder(BasicContext);
        builder.setTitle("Sei sicuro di voler eliminare il canale?");
        builder.setCancelable(true);
        builder.setPositiveButton("si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //devo interrompere il servizio delle notifiche
                 MyTimerTask.remove(channel.get(position));
                //se non ci sono più canali cancello tutto
                if(channel.size()<2){
                    Toast.makeText(BasicContext,"CANCELLO TUTTO",Toast.LENGTH_SHORT).show();

                    //elimino tutti i dati delle medie quando non ho piu canali
                    db.deleteAllMean();

                    //cancello i database
                    db.delateAllSaved();
                    //cancello lista channel
                    channel.clear();
                    //cancello il database dei canali
                    db.delateAllStd();//db.ChannelDao().deleteAll();
                    //invio agli altri che adesso è tutto null!

                    MainActivity.setDefaultSetting(null, null,null, null,-1);
                }
                else {
                    //lo elimino dal database
                    db.delateChannelStd(channel.get(position));

                    //elimino i dati delle medie associati a quel canale
                    db.deleteMeanFromID(channel.get(position).getLett_id(), channel.get(position).getLett_id_2());

                    channel.remove(position);
                    if(position-1<0){
                        setPosition(0);
                    }
                    else{
                        //se c'è almeno un canale rimasto metto quello come default
                        if(channel.size()<2) setPosition(0);
                        else pos=position-1;
                    }
                    Channel nuovo=channel.get(pos);

                    MainActivity.setDefaultSetting(nuovo.getLett_id(), nuovo.getLett_read_key(), nuovo.getLett_id_2(), nuovo.getLett_read_key_2(),position-1);
                    Toast.makeText(BasicContext, "canale " + position + " cancellato!", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();
            }
        });
        //azione da fare quando premo il pulsante negativo
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(BasicContext,"operazione annullata!",Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog allert=builder.create();
        allert.show();
    }

    /**
     * eseguito quando premo sul pulsante per i preferiti
     *
     * @param v: puntatore al pulsante preferiti
     * @param context: context associato alla classe
     * @param position: poszione del channel nel recyclerview
     */
    public static void sendPrefer(View v, Context context, final int position) {

        System.out.println("aumenta: " + position);

        //setto il nuovo channel come quello di default
        Channel chan = channel.get(position);

        DEFAULT_ID = chan.getLett_id();
        DEFAULT_READ_KEY = chan.getLett_read_key();

        DEFAULT_READ_KEY_2 = chan.getLett_read_key_2();
        DEFAULT_ID_2 = chan.getLett_id_2();

        chan.setPosition(1);

        if(DEFAULT_ID != null){
            db.getChannelStd(DEFAULT_ID, DEFAULT_READ_KEY, 0).setPosition(1);
        }
        else if(DEFAULT_ID_2 != null){
            db.getChannelStd(DEFAULT_ID_2, DEFAULT_READ_KEY_2, 1).setPosition(1);
        }


        if(pos==-1) pos=0;

        //cancello il channel precedente come default se è diverso dal precedente
        Channel prec = channel.get(pos);

        String idToSearch = DEFAULT_ID == null ? DEFAULT_ID_2 : DEFAULT_ID;
        String readKeyToSearch = DEFAULT_READ_KEY == null ? DEFAULT_READ_KEY_2 : DEFAULT_READ_KEY;

        String precId = prec.getLett_id();
        String precId2 = prec.getLett_id_2();
        String precKey = prec.getLett_read_key();
        String precKey2 = prec.getLett_read_key_2();

        if((precId != null && idToSearch != precId) || (precId2 != null && idToSearch != precId2)) {

            prec.setPosition(0);
            idToSearch = precId == null ? precId2 : precId;
            readKeyToSearch = precKey == null ? precKey2 : precKey;

            if(precId != null){
                db.getChannelStd(precId, precKey, 0).setPosition(0);
            }
            else {
                db.getChannelStd(precId2, precKey2, 1).setPosition(0);
            }

            //setto la nuova posizione
            pos = position;
        }
        if(position==pos) {
            if (defaultStar == null) {
                //salvo il bottone corrente e il background
                defaultStar = v;
                //cambio la disposizione
                v.setBackgroundResource(R.drawable.ic_star);
            } else {

                //salvo il bottone corrente e il background
                defaultStar.setBackground(v.getBackground());
                defaultStar = v;

                //cambio la disposizione
                v.setBackgroundResource(R.drawable.ic_star);
            }
        }

        //invio i nuovi dati di default
        MainActivity.setDefaultSetting(DEFAULT_ID, DEFAULT_READ_KEY, DEFAULT_ID_2, DEFAULT_READ_KEY_2, pos);
    }

    /**
     *
     * @return la posizione associata
     */
    public static int getposition(){
        return pos;
    }

    /**
     * restituisce l'intent associato
     * @param context: context associato alla classe
     * @return l'intent legato all'activity
     */
    public static Intent getActivityintent(Context context){
        Intent intent=new Intent(context,ChannelActivity.class);
        return intent;
    }

    /**
     * funzione che mi controlla l'esistenza del channel su thinkspeak
     *
     * @param valueID: id della chiave di lettura
     * @param valueREADKEY: api lettura chiave lettura
     * @param id_scritt: id chiave di scrittura
     * @param read_scritt: api lettura chiave scrittura
     * @param write_scritt: api scrittura chiave scrittura
     * @return
     */
    /**
     * Modifica aggiunta da Matteo Torchia 599899
     * Modifico la coda per far si che possa controllare se entrambi i
     * canali non esistono o solo uno o se entrambi esistono
     *
     */
    public static Boolean [] testData(String valueID, String valueREADKEY, String valueID_2, String valueREADKEY_2,
                                   String id_scritt,String read_scritt,String write_scritt) {

        BlockingQueue<Boolean []> esito = new LinkedBlockingQueue<Boolean [] >();

        ExecutorService pes = Executors.newFixedThreadPool(1);
        pes.submit(new Task(esito, valueID, valueREADKEY, valueID_2, valueREADKEY_2, id_scritt, read_scritt, write_scritt));
        pes.shutdown();
        Boolean [] esit = new Boolean[]{false, false};
        try {
            esit = esito.take();
        }catch (Exception e){
            e.printStackTrace();
        }
        return esit;
    }

    /**
     * funzione che crea il thread che gestirà le richieste
     */
    /**
     * Modifica aggiunta da Matteo Torchia 599899
     * Devo aggiungere i campi necessari ed effettuare le richieste al server
     *
     */
    static class Task implements Runnable {
        private static String lett_id;
        private static String lett_read_key;

        /**
         * variabili aggiunte da Matteo Torchia 599899
         */
        private static String lettReadKey2;

        private static String lettId2;

        /**********************************/

        private static String scritt_read_key;
        private static String scritt_id;
        private static String write_key;
        private final BlockingQueue<Boolean []> sharedQueue ;

        /**
         * metodo costruttore
         * @param esito:
         * @param id: id chiave di lettura
         * @param read_key: api lettura chiave lettura
         * @param id_scritt: id chiave di scrittura
         * @param read_scritt: api lettura chiave scrittura
         * @param write_scritt: api scrittura chiave scrittura
         */
        public Task(BlockingQueue<Boolean []> esito, String id, String read_key, String valueID_2, String valueREADKEY_2, String id_scritt, String read_scritt, String write_scritt) {

            lett_id = id;
            scritt_id=id_scritt;
            lett_read_key=read_key;
            scritt_read_key=read_scritt;
            write_key=write_scritt;
            this.sharedQueue = esito;
            lettId2 = valueID_2;
            lettReadKey2 = valueREADKEY_2;

        }

        /**
         * funzione eseguita all'avvio del thread
         */
        /**
         * Modifica aggiunta da Matteo Torchia 599899
         * Devo chiamare entrambi i canali e recuperare i dati
         *
         */
        @Override
        public void run() {

            try {

                /**
                 * Devo controllare entrambi i canali, modifico il codice in modo da poter controllare
                 * che field ho nel canale, il suo nome
                 */

                //int iter = checkID(lett_id, lettId2);

                //creo la variabile channel
                Channel add = new Channel(lett_id, scritt_id, lettId2, lett_read_key, scritt_read_key, write_key, lettReadKey2);

                ArrayList<String[]> ids_keys = new ArrayList<>();
                ids_keys.add(new String[]{lett_id, lett_read_key});
                ids_keys.add(new String[]{lettId2, lettReadKey2});

                Boolean [] result = new Boolean []{false, false};

                for(int i = 0; i<2; i++){

                    URL url = new URL("https://api.thingspeak.com/channels/" + ids_keys.get(i)[0] + "/feeds.json?api_key=" + ids_keys.get(i)[1]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == 200) {


                        //settare i fields qui appena inserisco il mio channel
                        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

                        String JSON_DATA = br.readLine();
                        JSONObject obj = new JSONObject(JSON_DATA);

                        JSONObject metaData = obj.getJSONObject("channel");


                        result[i] = true;
                        int id = -1;

                        //prendo uid dell'ultimo elemento inserito
                        if (channel.size() - 1 >= 0)
                            id = channel.get(channel.size() - 1).getUid();
                        else  id = 0;

                        //cambiare getUid con READ_KEY
                        add.setUid(id + 1);



                        //aggiungo tutti i field rilevati al channel e li inserisco nel database
                        try {
                            setNameFields(metaData, "field1", add);
                        } catch (Exception e){
                            //add.setFiled1(null);
                            //add.setFiled1_2(null);
                        }

                        try {
                            setNameFields(metaData, "field2", add);

                        } catch (Exception e){
                            //add.setFiled2(null);
                            //add.setFiled2_2(null);
                        }

                        try {
                            setNameFields(metaData, "field3", add);

                        } catch (Exception e){
                           // add.setFiled3(null);
                            //add.setFiled3_2(null);
                        }

                        try {
                            setNameFields(metaData, "field4", add);
                        } catch (Exception e){
                           // add.setFiled4(null);
                            //add.setFiled4_2(null);

                        }
                        try {
                            setNameFields(metaData, "field5", add);

                        } catch (Exception e){
                            //add.setFiled5(null);
                            //add.setFiled5_2(null);

                        }
                        try {
                            setNameFields(metaData, "field6", add);

                        } catch (Exception e){
                            //add.setFiled6(null);
                            //add.setFiled6_2(null);

                        }
                        try {
                            setNameFields(metaData, "field7", add);

                        } catch (Exception e){
                            //add.setFiled7(null);
                            //add.setFiled7_2(null);

                        }
                        try {
                            setNameFields(metaData, "field8", add);
                        } catch (Exception e){
                            //add.setFiled8(null);
                            //add.setFiled8_2(null);

                        }
                    } else{
                        result[i] = false;
                    }
                }

                if(result[0] || result[1]){

                    add.setNameChannel(name);//inserisco il nome del canale
                    db.insertChannelStd(add);
                    channel.add(add);
                }
                sharedQueue.put(result);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        private void setNameFields(JSONObject metaData, String field, Channel add) throws JSONException {

            String nameField = String.valueOf(metaData.get(field));

            switch (field) {
                case "field1":
                    if(add.getFiled1() == null) {
                        add.setFiled1(nameField);
                    }
                    else if(add.getFiled1_2() == null) add.setFiled1_2(nameField);

                    break;
                case "field2":
                    if(add.getFiled2() == null) {
                        add.setFiled2(nameField);
                    }
                    else if(add.getFiled2_2() == null)add.setFiled2_2(nameField);

                    break;
                case "field3":
                    if(add.getFiled3() == null) {
                        add.setFiled3(nameField);
                    }
                    else if(add.getFiled3_2() == null)add.setFiled3_2(nameField);

                    break;
                case "field4":
                    if(add.getFiled4() == null) {
                        add.setFiled4(nameField);
                    }
                    else if(add.getFiled4_2() == null)add.setFiled4_2(nameField);

                    break;
                case "field5":
                    if(add.getFiled5() == null) {
                        add.setFiled5(nameField);
                    }
                    else if(add.getFiled5_2() == null)add.setFiled5_2(nameField);

                    break;
                case "field6":
                    if(add.getFiled6() == null) {
                        add.setFiled6(nameField);
                    }
                    else if(add.getFiled6_2() == null)add.setFiled6_2(nameField);

                    break;
                case "field7":
                    if(add.getFiled7() == null) {
                        add.setFiled7(nameField);
                    }
                    else if(add.getFiled7_2() == null)add.setFiled7_2(nameField);

                    break;
                case "field8":
                    if(add.getFiled8() == null) {
                        add.setFiled8(nameField);
                    }
                    else if(add.getFiled8_2() == null)add.setFiled8_2(nameField);

                    break;
            }

        }
    }

    /**
     * Controllo se il canale inserito ha almeno 8 field
     * @return
     */
    private static boolean  checkNumField(JSONObject metaData){

        int dimCheck = metaData.length();
        int Flag = 0;

        for(int j = 0; j<dimCheck; j++){
            try {
                metaData.get("field" + (j + 1));
                Flag++;
            }
            catch (Exception e){}
        }
        if(Flag < 8){
            return false;
        }
        return true;
    }
}