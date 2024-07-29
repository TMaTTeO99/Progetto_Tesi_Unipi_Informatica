package com.example.GreenApp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Alert.AlertActivity;
import com.example.GreenApp.Alert.ForegroundService;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Channel.ChannelActivity;
import com.example.GreenApp.Channel.savedValues;
import com.example.GreenApp.Irrigation.IrrigationActivity;
import com.example.GreenApp.Prediction.Data_Structs.AndroidVersionSingleton;
import com.example.GreenApp.Prediction.Data_Structs.DataSavedToPredict;
import com.example.GreenApp.Prediction.Data_Structs.EntryList;
import com.example.GreenApp.Prediction.Data_Structs.Mean;
import com.example.GreenApp.Prediction.Data_Structs.SingleAdapter;
import com.example.GreenApp.Prediction.MyBaseActivity;
import com.example.GreenApp.Prediction.Prediction_activity;
import com.example.GreenApp.Prediction.Utils.SelectionAdapter;
import com.example.firstapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONObject;

import Jama.Matrix;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 */

/**
 * Modifiche affettuate da Matteo Torchia 599899
 *
 * Tutte le activity estendono baseactivity, all'interno della BaseActivity
 * inserisco tutte le variabili necessarie alle activity nuove in modo da poter
 * accedere alle variabili di istanza necessarie all'esecuzione del codice
 * senza doverle passare da un activity all'altra
 *
 * Effettuo modifiche per permettere aggiunta di un secondo canale associato al primo
 *  TODO: ogni volta che cerco un canale nel database devo fare in modo
 *  TODO: di distinguere se sto cercando il canale per id1 o id2 per riuscire a trovare
 *  TODO: il canale
 *  TODO: Devo continuare dall' implementazione del settaggio dei dati manualmente
 *  TODO: per quanto riguarda il vento e il pesoPianta
 */

@RequiresApi(api = Build.VERSION_CODES.O) //annotazione per asserire di star utilizzando la versione sufficiente di android
public class MainActivity extends MyBaseActivity implements SelectionAdapter {

    private BottomNavigationView ToolBar_buttons; //tool bar


    /**
     * Aggiunta da Matteo Torchia 599899
     * adapterFoChoice: utilizzato per guidare il flusso
     * di esecuzione
     *
     */
    private SingleAdapter adapterForChoice = null;


    //TODO: DA CAMBIARE DOPO E FARLA PIU BELLINA
    private List<EntryList> listForChoiceMennu = Arrays.asList(
            new EntryList("Storico", "", false),
            new EntryList("Previsione", "", false)
    );

    /**
     * Modifiche aggiunte da Matteo Torchia
     * Componenti grafici per peso e vento
     *
     */


    public static TextView textPesoPianta;
    public static TextView textVento;

    //dichiaro componenti grafici
    public static TextView textTemp;
    public static ImageView image;
    public static TextView textUmidity;
    public static TextView textPh;
    public static TextView textConducibilita;
    public static TextView textIrradianza;
    public static TextView textPeso;
    public static TextView textStato;
    public static TextView testo1;
    public static TextView textSoil;

    //lista che conterrà il channel di default


    private static List<savedValues> channeldefault;
    private static List<Channel> channel;

    //id e chiave di lettura del channel utilizzato
    private static String channelID = null;
    private static String READ_KEY = null;

    /**
     * Variabili aggiunte da Matteo Torchia 599899
     * Utilizzate per recuperare i dati dell'eventuale
     * canale associato al primo
     */
    private static String channelID_2 = null;

    private static String READ_KEY_2 = null;

    private static String url_2 = null;
    /************************************************************/

    //url utilizzata per reperire le info del chaneel di default
    private static String url;

    private static AppDatabase database;
    private static TimerTask timerTask;
    public static Future<?> future;
    private static Context cont;

    //TODO: Variabile da eliminare, usata solo per caricare i dati su un canalee mio
    //TODO: di test

    private TextView MyUpLoadChannelData = null;

    //TODO: Variabile da eliminare, usata solo per caricare i dati su un canalee mio
    //TODO: di test



    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    /**
     * metodo principale eseguito all'avvio
     * @param savedInstanceState:insieme di parametri precedentemente salvati
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Aggiungo il layout personalizzato per l'actionbar
         */

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        View customView = LayoutInflater.from(this).inflate(R.layout.action_bar_layout, null);

        getSupportActionBar().setCustomView(customView);
        getSupportActionBar().setDisplayShowCustomEnabled(true);


        /**
         * Controllo aggiunto da Matteo Torchia 599899
         * Verifico la versione Android corrente
         */

        AndroidVersionSingleton version = AndroidVersionSingleton.getInstance();

        version.setAndroidVersioneFlag(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O);

        if(!version.getAndroidVersioneFlag()) {
            ShowAlert("", "Versione Android Non Supportata", false, this, this::closeActivity, 0);
        }

        //ripristino valori salvati precedentemente se ci sono
        BackupValues(savedInstanceState);

        //database.ChannelDao().deleteAll();
        //database.SavedDao().deleteAll();
        //associo i riferimenti alle varie componenti
        textTemp = findViewById(R.id.textTemp);
        textUmidity = findViewById(R.id.textUmidity);
        textPh = findViewById(R.id.textPh);
        textConducibilita = findViewById(R.id.textConducibility);
        textIrradianza = findViewById(R.id.textIrradiance);
        textPeso = findViewById(R.id.textEvap);
        textSoil = findViewById(R.id.textSoil);
        textStato = findViewById(R.id.textViewON);
        testo1 = findViewById(R.id.textView1);

        ToolBar_buttons = findViewById(R.id.bottom_navigation_main);

        image = findViewById(R.id.imagePianta);
        cont = getApplicationContext();

        Activity activity = this;




        /**
         * Faccio binding con componenti grafici peso e vento
         */

        textPesoPianta = findViewById(R.id.TextPesoPianta);
        textVento = findViewById(R.id.Textvento);


        //controllo se ho almeno un channel inserito
        if (url == null && url_2 == null) {

            textVento.setText("- -");
            textPesoPianta.setText("- -");

            textTemp.setText("- -");
            textUmidity.setText("- -");
            textPh.setText("- -");
            textConducibilita.setText("- -");
            textIrradianza.setText("- -");
            textPeso.setText("- -");
            textSoil.setText("- -");
            testo1.setText("INSERISCI UN NUOVO CHANNEL");
        }
        else {
            //se c'era almeno un channel avvio un nuovo task che si occuperà di gestire le richieste con il server ed avvio il service in background in caso di notifiche

            restartTimer(cont);
            //ForegroundService.stoptimer();
            /*Intent intentservices=new Intent(cont,ForegroundService.class);
            ContextCompat.startForegroundService(cont,intentservices);*/
        }

        ToolBar_buttons.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {

                    case  R.id.settings:
                        settingChannel();
                        return true;
                    case R.id.graphic :

                        doAdd();
                        return true;
                    case R.id.allarm:
                        notifiche();
                        return true;
                    case R.id.refresh:
                        refresh();
                        return true;
                }
                return false;

            }
        });






    }

    /* run notifications in the background */
    @Override
    protected void onStop () {
        super .onStop() ;
        //startService( new Intent( this, ForegroundService. class )) ;
    }

    /**
     * funzione per ripristinare i dati precedentemente impostati dal database
     * @param savedInstanceState:insieme di parametri precedentemente salvati
     */
    private void BackupValues(Bundle savedInstanceState) {
        //creo il database
        if (savedInstanceState == null) {

            database = AppDatabase.getDataBase(getApplicationContext());
            channeldefault = database.getChannelsSaved();//SavedDao().getAll();
            channel = database.getAllChannelStd();//ChannelDao().getAll();
            //controllo se ho almeno un elemento inserito
            if (channeldefault.size() > 0) {

                //se avevo un elmento inserito imposto quest'ultimo come default
                channelID = channeldefault.get(0).getId();
                READ_KEY = channeldefault.get(0).getRead_key();

                channelID_2 = channeldefault.get(0).getLett_id_2();
                READ_KEY_2 = channeldefault.get(0).getLett_read_key_2();

                if(channelID != null && !channelID.isEmpty()) url = "https://api.thingspeak.com/channels/" + channelID + "/feeds.json?api_key=" + READ_KEY + "&results=100";
                if(channelID_2 != null && !channelID_2.isEmpty()) url_2 = "https://api.thingspeak.com/channels/" + channelID_2 + "/feeds.json?api_key=" + READ_KEY_2 + "&results=100";

                ChannelActivity.setPosition(channeldefault.get(0).getPosition());
                /* start foreground service if notificationswitch set */
                if (channel.get(0).getNotification()) {
                    startService( new Intent( this, ForegroundService.class )) ;
                }
            }
            //se non ho nessun elemento inserito setto a null i valori dei channel e metto la posizione ad -1
            else {
                channelID = null;
                READ_KEY = null;
                url = null;
                url_2 = null;
                ChannelActivity.setPosition(-1);
            }
        }
    }


    /**
     * @author Matteo Torchia 599899
     *
     * @return x
     */

    private int activitySelect(){

        List<String> choice =  adapterForChoice.getCheckedItems();
        switch (choice.get(0)) {
            case "Previsione":

                if (channeldefault.size() > 0) {
                    openPredictView(channeldefault.get(0).getId(), channeldefault.get(0).getLett_id_2());
                }
                else Toast.makeText(cont, "INSERISCI UN CHANNEL!", Toast.LENGTH_SHORT).show();

                break;
            default:
                executingDoAdd();
                break;
        }

        return 1;
    }

    /**
     * Mertodo aggiunto da Matteo Torchia 599899
     * Aggiungo i field del canale selezionato alla lista
     * selezionabile dei dati per i grafici
     */
    private void addFieldsToSelect(Channel inUse, ArrayList<String> list, ArrayList<Integer> posField, List<Channel> selectedChannel){

        if (inUse.getFiled1() != null){
            list.add(inUse.getFiled1().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(1);
        }
        if (inUse.getFiled2() != null){
            list.add(inUse.getFiled2().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(2);
        }
        if (inUse.getFiled3() != null) {
            list.add(inUse.getFiled3().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(3);
        }
        if (inUse.getFiled4() != null){
            list.add(inUse.getFiled4().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(4);
        }
        if (inUse.getFiled5() != null){
            list.add(inUse.getFiled5().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(5);
        }
        if (inUse.getFiled6() != null){
            list.add(inUse.getFiled6().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(6);
        }
        if (inUse.getFiled7() != null){
            list.add(inUse.getFiled7().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(7);
        }
        if (inUse.getFiled8() != null){
            list.add(inUse.getFiled8().concat(" (id:").concat(inUse.getLett_id()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(8);
        }
        /*if(inUse.getFiled1_2() != null){
            list.add(inUse.getFiled1_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(9);
        }
        if(inUse.getFiled2_2() != null){
            list.add(inUse.getFiled2_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(10);
        }
        if(inUse.getFiled3_2() != null){
            list.add(inUse.getFiled3_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(11);
        }
        if(inUse.getFiled4_2() != null){
            list.add(inUse.getFiled4_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(12);
        }
        if(inUse.getFiled5_2() != null){
            list.add(inUse.getFiled5_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(13);
        }
        if(inUse.getFiled6_2() != null){
            list.add(inUse.getFiled6_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(14);
        }
        if(inUse.getFiled7_2() != null){
            list.add(inUse.getFiled7_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(15);
        }
        if(inUse.getFiled8_2() != null){
            list.add(inUse.getFiled8_2().concat(" (id:").concat(inUse.getLett_id_2()).concat(")"));
            selectedChannel.add(inUse);
            posField.add(16);
        }

         */
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Recupero i dati associati a due potenziali canali associati
     * alla singola istanza Channel e li inserisco in due instanze Channel
     * separate
     *
     * @param inUse
     * @return lista di channel
     */
    private List<Channel> buildSingleChannel(Channel inUse){

        List<Channel> splitted = new ArrayList<>();
        String id1 = inUse.getLett_id();
        String id2 = inUse.getLett_id_2();

        //distinguo 2 casi:
        //caso in cui entrambi i canali sono presenti
        //e il caso in cui solo uno dei due è presente

        if(id1 != null && id2 != null){
            splitted.add(setSplittedFields(inUse, true, inUse.getLett_id(), inUse.getScritt_id(), inUse.getLett_read_key(), inUse.getScritt_read_key(), inUse.getWrite_key()));
            splitted.add(setSplittedFields(inUse, false, inUse.getLett_id_2(), inUse.getScritt_id(), inUse.getLett_read_key_2(), inUse.getScritt_read_key(), inUse.getWrite_key()));
        }
        else if(id1 == null && id2 != null){
            splitted.add(setSplittedFields(inUse, true, inUse.getLett_id_2(), inUse.getScritt_id(), inUse.getLett_read_key_2(), inUse.getScritt_read_key(), inUse.getWrite_key()));
        }
        else if(id1 != null && id2 == null){
            splitted.add(setSplittedFields(inUse, true, inUse.getLett_id(), inUse.getScritt_id(), inUse.getLett_read_key(), inUse.getScritt_read_key(), inUse.getWrite_key()));
        }
        return splitted;
    }

    /**
     * Metodo aggiunto Da Matteo Torchia 599899
     * Metodo di supporto per il recupero dei file
     * quando viene splittato il canale
     * Flag == true drecupero i field da canale 1
     * Flag == false drecupero i field da canale 2
     *
     */

    //inUse.getLett_id(), inUse.getScritt_id(), null, inUse.getLett_read_key(), inUse.getScritt_read_key(), inUse.getWrite_key(), null
    private Channel setSplittedFields(Channel inUse, boolean flag, String idR, String idW, String keyRR, String keyWR, String keyWW){

        Channel ch = null;
        if(flag){

            ch = new Channel(idR, idW, null, keyRR, keyWR, keyWW, null);
            ch.setFiled1(inUse.getFiled1());
            ch.setFiled2(inUse.getFiled2());
            ch.setFiled3(inUse.getFiled3());
            ch.setFiled4(inUse.getFiled4());
            ch.setFiled5(inUse.getFiled5());
            ch.setFiled6(inUse.getFiled6());
            ch.setFiled7(inUse.getFiled7());
            ch.setFiled8(inUse.getFiled8());


        }
        else {
            ch = new Channel(idR, idW, null, keyRR, keyWR, keyWW, null);

            ch.setFiled1(inUse.getFiled1_2());
            ch.setFiled2(inUse.getFiled2_2());
            ch.setFiled3(inUse.getFiled3_2());
            ch.setFiled4(inUse.getFiled4_2());
            ch.setFiled5(inUse.getFiled5_2());
            ch.setFiled6(inUse.getFiled6_2());
            ch.setFiled7(inUse.getFiled7_2());
            ch.setFiled8(inUse.getFiled8_2());

        }

        return ch;
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Recupero il canale corrispondente a quello selezionato per
     * l'avvio corretto dello storico
     */
    private Channel retreiveDefoultChannel(List<Channel> allchannel, savedValues selected){

        for(int i = 0; i<allchannel.size(); i++){

            if(allchannel.get(i).getLett_id() != null && allchannel.get(i).getLett_id().equals(selected.getId())){
                return allchannel.get(i);
            }
            else if(allchannel.get(i).getLett_id_2() != null && allchannel.get(i).getLett_id_2().equals(selected.getLett_id_2())){
                return allchannel.get(i);
            }
        }

        return null;
    }
    /**
     * @author Matteo Torchia 599899
     * Metodo che esegue il codice di doAdd
     * Modifiche: faccio in modo che le possibile scelte siano
     * solo fra i fields del canale selezionato di default
     *
     */
    public Integer executingDoAdd() {

        //array che memorizza la posizione degli elementi selezionati e il nomed ell'elemento corrispondente
        final boolean[] checkedItems;
        final String[] listItems;

        //lista che mi setta la posizione degli elementi selezionati
        final ArrayList<Integer> mUserItems = new ArrayList<>();
        final ArrayList<String> list = new ArrayList<>();


        ///////////////////////////////////////////////////////



        //uso solo il canale selezionato come default per popolare le scelte

        //recupero tutti i canali
        final List<Channel> allchannel = database.getAllChannelStd();

        //recupero il canale selezionato come default
        savedValues selected =  channeldefault.get(0);
        Channel inUse = retreiveDefoultChannel(allchannel, selected);




        //memorizza il channel associato al field selezionato
        final List<Channel> selectedChannel=new ArrayList<>();

        //lista che mi salva il nome di tutti gli elementi selezionati e la posizione di essi
        final ArrayList<String> name = new ArrayList<>();
        final ArrayList<Integer> posField = new ArrayList<>();




        /**
         * In questo caso siccome ho i dati di due canali
         * su thingspeack in un solo channel, recupero i
         * dati e li inserisco in un singolo cnale in modo
         * da non dover modificare il codice esistente
         *
         */

        //posso avere potenzialmente 2 canali al piu
        List<Channel> retreived = buildSingleChannel(inUse);

        for(int i = 0; i<retreived.size(); i++){
            addFieldsToSelect(retreived.get(i), list, posField, selectedChannel);
        }

        

        //converto gli array in liste
        listItems = list.toArray(new String[list.size()]);
        checkedItems = new boolean[list.size()];

        //se non c'è nessun channel avviso l'utente
        if (list.size() == 0)
            Toast.makeText(cont, "INSERISCI UN CHANNEL!", Toast.LENGTH_SHORT).show();
        else {

            //se c'è almeno un channel presente apro la finestra di dialogo in cui l'utente può selezionare fino a 6 fields
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Seleziona i tipi di grafici(max 6)");
            final ArrayList<Integer> selectedPos=new ArrayList<>();
            final ArrayList<Channel> selChan=new ArrayList<>();
            mBuilder.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    //salvo la selezione
                    if (isChecked) {
                        name.add(list.get(position));
                        selectedPos.add(posField.get(position));
                        selChan.add(selectedChannel.get(position));
                    } else {
                        //se ho desezionato rimuovo l'elemento dalla lista
                        name.remove(list.get(position));
                        selectedPos.remove(posField.get(position));
                        selChan.remove(selectedChannel.get(position));
                    }

                    //se eccedo 6 elementi selezionati elimino l'ultimo
                    if(name.size()>6){
                        name.remove(list.get(position));
                        selectedPos.remove(posField.get(position));
                        selChan.remove(selectedChannel.get(position));

                        ((AlertDialog) dialogInterface).getListView().setItemChecked(position, false);
                        checkedItems[position]=false;
                        Toast.makeText(getApplicationContext(),"HAI SELEZIONATO PIU GRAFICI DEL PREVISTO, DESELEZIONA I PRECEDENTI",Toast.LENGTH_LONG).show();
                    }

                }
            });

            mBuilder.setCancelable(false);
            //azione da svolgere quando premo sul pulsante visualizza
            mBuilder.setPositiveButton("VISUALIZZA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //se non ho selezionato nessun grafico comunico un messaggio di errore
                    if (name.size() == 0)
                        Toast.makeText(cont, "NESSUN GRAFICO SELEZIONATO!", Toast.LENGTH_SHORT).show();
                    else {
                        Intent intent = com.example.GreenApp.Graphic.MainActivity.getActivityintent(MainActivity.this);
                        com.example.GreenApp.Graphic.MainActivity.setGrapView(name, selChan,selectedPos);
                        startActivity(intent);
                    }
                }
            });

            //azione da svolgere quando premo sul pulsante annulla
            mBuilder.setNeutralButton("ANNULLA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    //elimino tutte le selezioni
                    for (int i = 0; i < checkedItems.length; i++) {
                        checkedItems[i] = false;
                        mUserItems.clear();
                        name.clear();
                    }
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        }
        return 0;
    }

    /**
     * funzione eseguita quando vado a cliccare sul pulsate dei grafici
     */
    public void doAdd() {

        /**
         *
         * Modificche effettuata da Matteo Torchia 599899
         *
         *
         * Qui prima di far visualizzare all'utente i canali
         * da cui recuperare faccio scegliere se l'utente
         * vuole vedere lo storico => continuo esecuzione
         * originale, altrimeti se l'utente sceglie di
         * visualizzare la previsione cambio activity
         */
        adapterForChoice = new SingleAdapter(listForChoiceMennu, this);//buildActivitySelectAdapter((SelectionAdapter) this);

        ShowCheckList("Attività Da Svolgere", "Selezionare Una Sola Opzione", true, this, adapterForChoice,
                (x) -> activitySelect(), null, TypeGerericFunction.ViewType, "Confirm", "Close");

    }

    /**
     * azione che devo eseguire quando premo il pulsante impostazioni
     *
     */
    public void settingChannel() {
        //avvio la nuova activity channelactivity
        Intent intent = ChannelActivity.getActivityintent(MainActivity.this);
        startActivity(intent);
    }



    /**
     * Modifiche aggiunte da Matteo Torchia 599899
     * Cerco il canale nel db anche in base al secondo id che corrisponde al
     * secondo canale associato
     */

    /**
     * azione che devo eseguire quando premo il pulsante attenzione

     */

    public void notifiche() {

        Intent intent = AlertActivity.getActivityintent(MainActivity.this);

        List<Channel> channeList = database.getAllChannelStd();//ChannelDao().getAll();

        Channel trovato=null;

        //controllo che esiste almeno un channel
        for(int i=0;i<channeList.size();i++){
            if(channeldefault.get(0).getId() != null && channeList.get(i).getLett_id() != null && channeldefault.get(0).getId().equals(channeList.get(i).getLett_id())){
                trovato=channeList.get(i);
            }
            else if(channeldefault.get(0).getLett_id_2() != null && channeList.get(i).getLett_id_2() != null && channeldefault.get(0).getLett_id_2().equals(channeList.get(i).getLett_id_2())){
                trovato=channeList.get(i);
            }
        }
        //se esiste almeno un channel lo avvio altrimenti mando un messaggio di errore
        if(trovato==null){
          Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            AlertActivity.setChannel(trovato);
            startActivity(intent);
        }
    }

    /**
     * azione eseguita quando premo pulsante refresh
     */
    public void refresh(){
        //riavvio i il timer in modo da ri-scaricare gli ultimi valori dal server
        restartTimer(cont);
    }

    /**
     * mi imposta i nuovi vlori di default nel caso in cui l'utente abbia aggiornato il channel predefinito
     * @param id:nuovo id
     * @param key_read:chiave di lettura
     * @param pos:posizione associata
     */
    public static void setDefaultSetting(String id, String key_read, String id2, String key_read2, int pos) {

        //se non ho nessun canale (pos=-1) cancello tutto
        if (pos == -1) {
            channelID = null;
            READ_KEY = null;
            url = null;
            url_2 = null;
            ChannelActivity.setPosition(-1);

            if (channeldefault.size() != 0) channeldefault.clear();
            database.delateAllSaved();//SavedDao().deleteAll();

            textPesoPianta.setText("- -");
            textVento.setText("- -");
            textTemp.setText("- -");
            textUmidity.setText("- -");
            textPh.setText("- -");
            textConducibilita.setText("- -");
            textIrradianza.setText("- -");
            textPeso.setText("- -");
            textSoil.setText("- -");
            textStato.setText("OFFLINE");
            textStato.setTextColor(Color.RED);
            testo1.setText("INSERISCI UN NUOVO CHANNEL 1");

            try{
                future.cancel(true); }
            catch (Exception e) { e.printStackTrace(); }

        } else {

            //aggiungo alla lista channel default il nuovo solo se è diverso dal precedente
                if (channeldefault.size() != 0) channeldefault.clear();

                channeldefault.add(new savedValues(id, key_read, pos, id2, key_read2));
                database.delateAllSaved();//SavedDao().deleteAll();
                database.insertChannelSaved(new savedValues(id, key_read,pos, id2, key_read2));//database.SavedDao().insert(new savedValues(id, key_read,pos, id2, key_read2));

                // TODO: qui dovro farer in modo di chiamare i dati per entrambi i canali se necessario

                channelID_2 = id2;
                READ_KEY_2 = key_read2;

                channelID = id;
                READ_KEY = key_read;

                if(channelID_2 != null && !channelID_2.isEmpty()) url_2 = "https://api.thingspeak.com/channels/" + channelID_2 + "/feeds.json?api_key=" + READ_KEY_2 + "&results=100";
                else url_2 = null;

                if(channelID != null && !channelID.isEmpty()) url = "https://api.thingspeak.com/channels/" + channelID + "/feeds.json?api_key=" + READ_KEY + "&results=100";
                else url = null;

                restartTimer(cont);
            }
    }

    /**
     * funzione che mi permette di riavviare il timer e scaricare i nuovi valori dal server
     * @param cont:context associato
     */
    /**
     * Modifiche fatte da Matteo Torchia 599899
     * Inserisco in MyTimerTask anche : id ekey read del secondo canale e la rispettiva url
     */
    public static void restartTimer(Context cont) {


        Runnable timerTask = new MyTimerTask(channelID, READ_KEY, channelID_2, READ_KEY_2, url, url_2,
                textTemp, textUmidity, textPh, textConducibilita, textIrradianza, textSoil, textPeso,
                textStato, testo1, cont, database, image, textPesoPianta, textVento);
        try{
           future.cancel(true); }
        catch (Exception e) { e.printStackTrace(); }
        future =  scheduledExecutorService.scheduleAtFixedRate(timerTask,0 , 60, TimeUnit.SECONDS);
    }

    /**
     * avvio il timer che mi crea un nuovo task passando come parametro tutti i valori impostati di default
     * @param cont:context associato
     */

    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona temperatura)
     * @param v:puntatore al riferimento dell'icona della temperatura
     */
    /**
     * Modifiche effttuate da Matteo Torchia 599899
     * Devo farer in modo che venga considerato anche l'altro canale
     */

    /**
     * Metodo di supporto usato per recuperare il canale dalla lista di tutti
     * i canali
     * @param channeList
     * @return channel
     */
    private Channel searchChannel(List<Channel> channeList){

        for(int i=0;i<channeList.size();i++){

            if(channeldefault.get(0).getId() != null && channeList.get(i).getLett_id() != null && channeldefault.get(0).getId().equals(channeList.get(i).getLett_id())){
                return channeList.get(i);
            }
            else if(channeldefault.get(0).getLett_id_2() != null && channeList.get(i).getLett_id_2() != null && channeldefault.get(0).getLett_id_2().equals(channeList.get(i).getLett_id_2())){
                return channeList.get(i);
            }
        }
        return null;
    }
    public void tempSettings(View v) {

        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();

        Channel trovato = searchChannel(channeList);
        //controllo che esiste almeno un channel

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            //reperisco il channel utilizzato di default e faccio il parsing per scoprire quale fields è stato settato(posizione)
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();

            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);





            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(0, inUse.getImagetemp(), flag);

        }
    }

    /**
     * Metoto aggiunot da Matteo Torchia 599899
     * Necessario per poter settare il valore manualmente
     * per icona vento
     */
    public void windSettings(View v) {

        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {


            //reperisco il channel utilizzato di default e faccio il parsing per scoprire quale fields è stato settato(posizione)
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();

            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);



            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(8, inUse.getImageVento(), flag);
        }
    }

    /**
     * Metoto aggiunot da Matteo Torchia 599899
     * Necessario per poter settare il valore manualmente
     * per icona vento
     */
    public void scaleSettings(View v) {

        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {


            //reperisco il channel utilizzato di default e faccio il parsing per scoprire quale fields è stato settato(posizione)
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();

            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);


            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(7, inUse.getImagePesoPianta(), flag);
        }
    }
    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona ph)
     * @param v:puntatore al riferimento dell'icona del ph
     */
    public void phSettings(View v){
        List<Channel> channeList = database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {

            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);



            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(1,  inUse.getImageph(), flag);

        }
    }

    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona irradianza)
     * @param v:puntatore al riferimento dell'icona del irradianza
     */
    public void irraSettings(View v){

        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);



            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(2, inUse.getImageirra(), flag);
        }
    }

    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona conducibilità elettrica)
     * @param v:puntatore al riferimento dell'icona della conducibilità
     */
    public void condSettings(View v){
        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);
        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);

            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(3, inUse.getImagecond(), flag);
        }
    }

    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona evapotraspirazione)
     * @param v:puntatore al riferimento dell'icona del peso
     */
    public void pesoSettings(View v){
        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);


            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(4, inUse.getImagepeso(), flag);
        }
    }

    /**
     * funzione per settare il valore del singolo field non appena lo premo (icona umidità)
     * @param v:puntatore al riferimento dell'icona dell umidità
     */
    public void umidSettings(View v){
        
        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);


            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(5, inUse.getImageumid(), flag);
        }
    }

    public void soilSettings(View v) {
        List<Channel> channeList=database.getAllChannelStd();//ChannelDao().getAll();
        Channel trovato = searchChannel(channeList);

        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {
            //reperisco il channel utilizzato di default e faccio il parsing per scoprire quale fields è stato settato(posizione)
            final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();
            String id_1 = allchannel.get(0).getId();
            String key_1 =  allchannel.get(0).getRead_key();
            String id_2 = allchannel.get(0).getLett_id_2();
            String key_2 =  allchannel.get(0).getLett_read_key_2();

            Channel inUse = null;
            if(id_1 != null) inUse = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
            else if(id_2 != null) inUse = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);



            /**
             *  modifico il metodo fieldssettings perche dopo aver recuperato la prima volta
             *  il canale so gia se sono attivi entrambi i canali o uno dei due
             *  1 : attivo id1
             *  2 : attivo id2
             *  0: attivo id1 e id2
             *  -1: usato se e quando non mi serve tale info
             */
            int flag = -1;
            if(id_1 != null && id_2 != null) flag = 0;
            else if(id_1 != null && id_2 == null) flag = 1;
            else if(id_1 == null && id_2 != null) flag = 2;

            fieldssettings(6, inUse.getImagesoil(), flag);
        }
    }

    /**
     * mi permette di settare il field associato alla singola icona che è stata premuta
     * @param field:numero del field associato
     * @param selected: valore precedentemente selezionato
     */
    public void fieldssettings(final int field, String  selected, int flagChannel){


        final String[] listItems;
        final int[] pos = new int[1];

        //contiene i nomi dei fields
        final ArrayList<String> list = new ArrayList<>();

        //nome del fileds selezionato
        final String[] name = new String[1];

        //recupero il channel default con i relativi field
        final List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();

        String id_1 = allchannel.get(0).getId();
        String key_1 =  allchannel.get(0).getRead_key();
        String id_2 = allchannel.get(0).getLett_id_2();
        String key_2 =  allchannel.get(0).getLett_read_key_2();

        Object [] res = getChannelByRightKey(id_1, key_1, id_2, key_2, flagChannel);

        Channel inUse_1 = (Channel) res[0];
        Channel inUse_2 = (Channel) res[1];


        //Channel inUse = database.ChannelDao().findByName(allchannel.get(0).getId(), allchannel.get(0).getRead_key());

        setFieldName(list, inUse_1, inUse_2);

        //converte la lista contenete i nomi in un array
        listItems = list.toArray(new String[list.size()]);

        if (list.size() == 0)
            Toast.makeText(cont, "INSERISCI UN CHANNEL!", Toast.LENGTH_SHORT).show();
        else {
            //avvio la schermata per selezionare il rispettivo channel
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Seleziona il field da visualizzare");

            int position = selected != null ? list.indexOf(selected) : -1;

            mBuilder.setSingleChoiceItems(listItems, position, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                   name[0] = listItems[i];
                   pos[0]=i;
                }
            });


            mBuilder.setCancelable(false);
            //azione da svolgere quando premo sul pulsante visualizza
            mBuilder.setPositiveButton("VISUALIZZA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {

                    Object [] res = getChannelByRightKey(id_1, key_1, id_2, key_2, flagChannel);

                    Channel inUse_1 = (Channel) res[0];
                    Channel inUse_2 = (Channel) res[1];
                    Channel x = null;
                    if(inUse_1 != null)x = inUse_1;
                    else if(inUse_2 != null)x = inUse_2;


                    database.ChannelDao().delete(x);

                    if(field==0) x.setImagetemp(name[0] );
                    if(field==1) x.setImageph(name[0] );
                    if(field==2) x.setImageirra(name[0]);
                    if(field==3) x.setImagecond(name[0]);
                    if(field==4) x.setImagepeso(name[0]);
                    if(field==5) x.setImageumid(name[0]);
                    if(field==6) x.setImagesoil(name[0]);
                    if(field==7) x.setImagePesoPianta(name[0]);
                    if(field==8) x.setImageVento(name[0]);

                    database.insertChannelStd(x);
                    //database.ChannelDao().insert(x);


                    restartTimer(cont);
                }
            });

            //azione da svolgere quando premo sul pulsante cancella tutto
            mBuilder.setNegativeButton("RESET", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {


                    Object [] res = getChannelByRightKey(id_1, key_1, id_2, key_2, flagChannel);

                    Channel inUse_1 = (Channel) res[0];
                    Channel inUse_2 = (Channel) res[1];
                    Channel x = null;
                    if(inUse_1 != null)x = inUse_1;
                    else if(inUse_2 != null)x = inUse_2;

                    database.ChannelDao().delete(x);

                    if(field==0) x.setImagetemp(null);
                    if(field==1) x.setImageph(null);
                    if(field==2) x.setImageirra(null);
                    if(field==3) x.setImagecond(null);
                    if(field==4) x.setImagepeso(null);
                    if(field==5) x.setImageumid(null);
                    if(field==6) x.setImagesoil(null);
                    if(field==7) x.setImagePesoPianta(null);
                    if(field==8) x.setImageVento(null);

                    database.insertChannelStd(x);
                    //database.ChannelDao().insert(x);



                    restartTimer(cont);
                    dialogInterface.dismiss();
                }
            });

            //azione da svolgere quando premo sul pulsante cancella tutto
            mBuilder.setNeutralButton("ANNULLA", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dialogInterface.dismiss();
                }
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        }

    }

    /**
     * azione svolta quando premo sul pulsante irrigazione
     * @param v:puntatore al riferimento dell'icona dell'irrigazione
     */
    /**
     * Modifiche aggiunte da Matteo Torchia 599899
     * Controllo se devo recperare il canale usando id1 o id2
     */
    public void irrigation(View v){

        List<Channel> channeList = database.getAllChannelStd();
        Channel trovato=null;
        String id1 = channeldefault.get(0).getId();
        String id2 = channeldefault.get(0).getLett_id_2();

        //controllo che esiste almeno un channel
        for(int i=0; i<channeList.size(); i++){

            if(id1 != null && id1.equals(channeList.get(i).getLett_id()) || id1 != null && id1.equals(channeList.get(i).getLett_id_2())){

                trovato = channeList.get(i);
            }
            else if(id2 != null && id2.equals(channeList.get(i).getLett_id_2()) || id2 != null && id2.equals(channeList.get(i).getLett_id())) {

                trovato = channeList.get(i);
            }

        }
        if(trovato==null){
            Toast.makeText(cont,"INSERISCI UN CHANNEL",Toast.LENGTH_SHORT).show();
        }
        else {

            /**
             * Qui prima di effettuare operazioni controllo se la chiave di scrittura
             * è stata inserita
             */
            List<savedValues> allchannel = database.getChannelsSaved();//SavedDao().getAll();

            String id1_used = allchannel.get(0).getId();
            String id2_used = allchannel.get(0).getLett_id_2();

            Channel inUse = null;
            if(id1_used != null){
                inUse = database.getChannelStd(id1_used, allchannel.get(0).getRead_key(), 0);
            }
            else if(id2_used != null) inUse = database.getChannelStd(id2_used, allchannel.get(0).getLett_read_key_2(), 1);

            /**
             * Se il canale selezionato al momento non ha dati per effettuare le
             * Scritture non
             */
            /*
            if(inUse.getScritt_id() == null || inUse.getScritt_id().equals("") || inUse.getWrite_key() == null || inUse.getWrite_key().equals("")){
                Toast.makeText(cont,"IL CANALE NON CONTIENE DATI DI SCRITTURA",Toast.LENGTH_SHORT).show();
            }
            else {*/

                Intent intent = IrrigationActivity.getActivityintent(MainActivity.this);

                //cerco nel database il channel in uso e lo mando
                IrrigationActivity.setChannle(inUse);
                startActivity(intent);
            //}

        }
    }
    private void openPredictView(String idChannel_1, String idChannel_2) {

        Intent intent = new Intent(this, Prediction_activity.class);
        if(idChannel_1 != null)intent.putExtra("Channel_1", idChannel_1);
        if(idChannel_2 != null)intent.putExtra("Channel_2", idChannel_2);
        startActivity(intent);
    }

    /**
     * Metodo aggiunto da Matteo Torchia 599899
     * Utilizzato per recuperare i due channel
     * @return Object [] : [channel_1, channel_2]
     */
    public Object [] getChannelByRightKey(String id_1, String key_1, String id_2, String key_2, int flagChannel) {

        Channel inUse_1 = null;
        Channel inUse_2 = null;

        switch (flagChannel){
            case 0:
                inUse_1 = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
                inUse_2 = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);
                break;
            case 1:
                inUse_1 = database.getChannelStd(id_1, key_1, 0);//database.ChannelDao().findByName(id_1, key_1);
                break;
            case 2:
                inUse_2 = database.getChannelStd(id_2, key_2, 1);//database.ChannelDao().findBySecondName(id_2, key_2);
                break;
            default:
                break;
        }
        return new Object[]{inUse_1, inUse_2};
    }

    /**
     * Metodo inserito da Matteo Torchia 599899
     * Uso questo metodo di supporto per settare i fields lenna lista usata per
     * far visualizzare all'utente le scelte da fare quando tocca un icona
     */
    private void setFieldName(ArrayList<String> list, Channel inUse_1, Channel inUse_2) {

        /** Caso in cui è selezionato il canale 1*/
        if(inUse_1 != null) {
            if (inUse_1.getFiled1() != null) {
                list.add(inUse_1.getFiled1());
            }
            if (inUse_1.getFiled2() != null) {
                list.add(inUse_1.getFiled2());
            }
            if (inUse_1.getFiled3() != null) {
                list.add(inUse_1.getFiled3());
            }
            if (inUse_1.getFiled4() != null) {
                list.add(inUse_1.getFiled4());
            }
            if (inUse_1.getFiled5() != null) {
                list.add(inUse_1.getFiled5());
            }
            if (inUse_1.getFiled6() != null) {
                list.add(inUse_1.getFiled6());
            }
            if (inUse_1.getFiled7() != null) {
                list.add(inUse_1.getFiled7());
            }
            if (inUse_1.getFiled8() != null) {
                list.add(inUse_1.getFiled8());
            }
            if (inUse_1.getFiled1_2() != null) {
                list.add(inUse_1.getFiled1_2());
            }
            if (inUse_1.getFiled2_2() != null) {
                list.add(inUse_1.getFiled2_2());
            }
            if (inUse_1.getFiled3_2() != null) {
                list.add(inUse_1.getFiled3_2());
            }
            if (inUse_1.getFiled4_2() != null) {
                list.add(inUse_1.getFiled4_2());
            }
            if (inUse_1.getFiled5_2() != null) {
                list.add(inUse_1.getFiled5_2());
            }
            if (inUse_1.getFiled6_2() != null) {
                list.add(inUse_1.getFiled6_2());
            }
            if (inUse_1.getFiled7_2() != null) {
                list.add(inUse_1.getFiled7_2());
            }
            if (inUse_1.getFiled8_2() != null) {
                list.add(inUse_1.getFiled8_2());
            }
        }
        else if(inUse_2 != null) {

            if (inUse_2.getFiled1() != null) {
                list.add(inUse_2.getFiled1());
            }
            if (inUse_2.getFiled2() != null) {
                list.add(inUse_2.getFiled2());
            }
            if (inUse_2.getFiled3() != null) {
                list.add(inUse_2.getFiled3());
            }
            if (inUse_2.getFiled4() != null) {
                list.add(inUse_2.getFiled4());
            }
            if (inUse_2.getFiled5() != null) {
                list.add(inUse_2.getFiled5());
            }
            if (inUse_2.getFiled6() != null) {
                list.add(inUse_2.getFiled6());
            }
            if (inUse_2.getFiled7() != null) {
                list.add(inUse_2.getFiled7());
            }
            if (inUse_2.getFiled8() != null) {
                list.add(inUse_2.getFiled8());
            }
            if (inUse_2.getFiled1_2() != null) {
                list.add(inUse_2.getFiled1_2());
            }
            if (inUse_2.getFiled2_2() != null) {
                list.add(inUse_2.getFiled2_2());
            }
            if (inUse_2.getFiled3_2() != null) {
                list.add(inUse_2.getFiled3_2());
            }
            if (inUse_2.getFiled4_2() != null) {
                list.add(inUse_2.getFiled4_2());
            }
            if (inUse_2.getFiled5_2() != null) {
                list.add(inUse_2.getFiled5_2());
            }
            if (inUse_2.getFiled6_2() != null) {
                list.add(inUse_2.getFiled6_2());
            }
            if (inUse_2.getFiled7_2() != null) {
                list.add(inUse_2.getFiled7_2());
            }
            if (inUse_2.getFiled8_2() != null) {
                list.add(inUse_2.getFiled8_2());
            }

        }

    }

    /**
     * @author Matteo Torhcia 599899
     * Metodo utilizzato per il recupero dell'elemento selezionat
     * nella lista di scelte per i parametri da prevedere
     *
     * @param idx
     * @return
     */
    public void retreiveSelection(int idx){

        for(int i = 0; i<listForChoiceMennu.size(); i++){
            EntryList e = listForChoiceMennu.get(i);
            if(e.isChecked() && i != idx){
                e.setChecked(false);
            }
            if(i == idx)e.setChecked(true);
        }
        adapterForChoice.notifyDataSetChanged();
    }


    /**
     * @author Matteo Torhcia 599899
     *
     * metodo per la chiusura dell'activity se necessario
     * @param x
     * @return 0
     */
    private int closeActivity(int x){
        finish();
        return 0;
    }

}