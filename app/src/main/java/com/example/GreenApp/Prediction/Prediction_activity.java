package com.example.GreenApp.Prediction;
import android.app.Activity;

import androidx.activity.OnBackPressedCallback;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Prediction.Data_Structs.AndroidVersionSingleton;
import com.example.GreenApp.Prediction.Data_Structs.DataContainer;
import com.example.GreenApp.Prediction.Data_Structs.DataSavedToPredict;
import com.example.GreenApp.Prediction.Data_Structs.EntryList;
import com.example.GreenApp.Prediction.Data_Structs.Mean;
import com.example.GreenApp.Prediction.Data_Structs.SavedStatePrediction;
import com.example.GreenApp.Prediction.Utils.GraphUtils.DateValueFormatter;
import com.example.GreenApp.Prediction.Data_Structs.SingleAdapter;
import com.example.GreenApp.Prediction.Utils.DateCallBack;
import com.example.GreenApp.Prediction.Utils.DatePickerFragment;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyFillFormatter;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyLineLegendRenderer;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyMarkerView;
import com.example.GreenApp.Prediction.Utils.MyHttpCallBack;
import com.example.GreenApp.Prediction.Utils.RetreiveData;
import com.example.GreenApp.Prediction.Utils.SelectionAdapter;
import com.example.GreenApp.Prediction.Utils.TrainingCallBack;
import com.example.firstapp.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import Jama.Matrix;

/**
 * @author Matteo Torchia 599899
 */

@RequiresApi(api = Build.VERSION_CODES.O) //annotazione per asserire di star utilizzando la versione sufficiente di android
public class Prediction_activity extends MyBaseActivity implements MyHttpCallBack, TrainingCallBack, DateCallBack, SelectionAdapter {


    private HashMap<String, List<EntryList>> myDataStructName = null;
    private HashMap<String, ArrayList<DataContainer>> myDataStructDataReal = null;
    private List<EntryList> listForChoice = null;

    //variabile utilizzata per capire se i dati che recupero dal db nel caso in cui non siano completi
    //quando recupero i restanti dal DB in quale posizione vadano messi se prima o dopo
    private int flagPositionDataDB = -1;

    private Double lower = Double.NEGATIVE_INFINITY;
    private Double upper = Double.NEGATIVE_INFINITY;
    private Channel chaID1 = null;
    private Channel chaID2 = null;
    private static AppDatabase database; //variabile database
    private SeekBar seekBarTemperatura = null;
    private SeekBar seekBarIrradiazione = null;
    private TextView viewTemperatura = null;
    private TextView viewIrradiazione = null;

    private Button ButtonControlledPrediction = null;
    private GridLayout gridController = null;

    /**
     * Implemento una hashmap che conterrà le liste delle medie dei dati.
     */
    private HashMap<String, Matrix> fieldMeans;

    /**
     * Lista delle date
     */
    private ArrayList<String> dateData;

    private List<Mean> dataFromDbTemp; //variabile in cui inserisco i dati recuperati dal db
    private List<Mean> dataFromDbIrr; //variabile in cui inserisco i dati recuperati dal db
    private List<Mean> dataFromDbChoice; //variabile in cui inserisco i dati recuperati dal db

    //variabili per controllare se attive e settare i margini programmaticamente
    private LinearLayout layoutPrevisione = null;
    private LineChart newGraph = null;
    private LineData allListData = null;
    private LineDataSet setPrediction = null;

    private LineDataSet setLower = null;
    private LineDataSet setUpper = null;

    private TextView startDateView = null;
    private TextView endDateView = null;
    private GridLayout headerPrevisione = null;
    //private GridLayout GridViewExplanation = null;
    private Activity activity = this;//riferimento all'activity

    private double lastTraking = 0;

    private BottomNavigationView ToolBar_buttons;
    private SingleAdapter adapterPrevisione;

    private boolean dataRetreived = false;//variabile usata per sapere se i dati sono gia stati recuperati
    private boolean countFailed = false; //counter per tenere traccia di aquante richieste sono fallite
    private int countRequest = 0; //counter per tenere traccia di auqnte callback delle richieste ho ottenuto

    private int expectedRequest = 0;
    private RetreiveData servideRetreive = new RetreiveData();
    private double selectedTemperature = 0;
    private double selectedIrradiance = 0;

    private String ID_1 = null;
    private String ID_2 = null;

    private float idxLatTrackingPrediction = 0;

    private MenuItem itemData = null;
    /**
     * Utilizzo variabili globali per recuperare il valore massimo e il valore minimo
     * delle seekbar
     */
    private double minT = 0;
    private double maxT = 0;
    private double minI = 0;
    private double maxI = 0;

    int scale = 1000;//variabile di scala fra int e double per le seekbar

    private boolean flagPredictionDone = false; //flag per essere sicuro di aver previsto un dato del futuro
    //quando uso il controller
    private boolean flagControllerActive = false;//flag per controllare

    //variabile usata per aggiornare grafica del tasto per i grafici
    private static MenuItem itemGraph = null;
    private static MenuItem itemController = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        /**
         * Sezione di inizializzazione delle variabili indipendenti da landscape mode
         */
        activity = this;
        now = getToday();
        yesterday = getYesterday();

        //inizializzo il picker delle date
        fragmentEnd = new DatePickerFragment((DateCallBack) activity, false, null);
        fragmentStart = new DatePickerFragment((DateCallBack) activity, true, yesterday);

        //recupero il database
        database = AppDatabase.getDataBase(getApplicationContext());

        //recupero i canali impostati
        Intent intent = getIntent();
        try {
            ID_1 = intent.getExtras().get("Channel_1").toString();
            expectedRequest++;
        }
        catch (Exception e){}
        try {
            ID_2 = intent.getExtras().get("Channel_2").toString();
            expectedRequest++;
        }
        catch (Exception e){}

        //recuero i dati di stato
        SavedStatePrediction state = SavedStatePrediction.getInstance();

        //se i dati sono stati precedentemente salvati li recupero
        if(state.isSaved()){

            adapterPrevisione = state.getAdapterPrevisione();
            allListData = state.getAllListData();
            countFailed = state.isCountFailed();
            countRequest = state.getCountRequest();
            dataFromDbTemp = state.getDataFromDb();
            dataRetreived = state.isDataRetreived();
            dateData = state.getDateData();
            listForChoice = state.getListForChoice();


            fieldMeans = state.getFieldMeans();
            flagControllerActive = state.isFlagControllerActive();
            flagPredictionDone = state.isFlagPredictionDone();


            lastTraking = state.getLastTraking();
            maxI = state.getMaxI();
            maxT = state.getMaxT();
            minI = state.getMinI();
            minT = state.getMinT();
            selectedTemperature = state.getSelectedTemperature();

            setPrediction = state.getSetPrediction();
            idxLatTrackingPrediction = state.getIdxLatTrackingPrediction();



            setLower = state.getSetLower();
            setUpper = state.getSetUpper();




            selectedIrradiance = state.getSelectedIrradiance();
            selectedTemperature = state.getSelectedTemperature();
            servideRetreive = state.getServideRetreive();




            myDataStructDataReal = state.getMyDataStructDataReal();
            myDataStructName = state.getMyDataStructName();

            startDateSelected = state.getStartDateSelected();
            endDataSelected = state.getEndDataSelected();
            //startDataToServer = state.getStartDataToServer();
            recyclerViewControllo = state.getRecyclerViewControllo();
            loadingBuilder = state.getLoadingBuilder();


        }
        else {


            //variabile in cui memorizzare i dati che recupero dal db o dal server
            fieldMeans = new HashMap<>();
            dataRetreived = false;//variabile flag per capire se i dati sono stati recuperati correttamente dal server

            //costruisco l'adapter della lista dei dati che si possono prevedere
            chaID1 = database.getChannelStd(ID_1, "", 0);
            chaID2 = database.getChannelStd(ID_2, "", 1);

            listForChoice = buildListAdapter(chaID1, chaID2);//adapter per le scelte dei dati da prevedere;
            adapterPrevisione = new SingleAdapter(listForChoice, this);


        }





        layoutPrevisione = findViewById(R.id.linearLayoutPrevisione);
        startDateView = findViewById(R.id.startDateIdPreditction);
        endDateView = findViewById(R.id.endDateIdPreditction);
        headerPrevisione = findViewById(R.id.headerPrevisione);

        gridController = findViewById(R.id.GridController);
        ButtonControlledPrediction = findViewById(R.id.ButtonControlledPrediction);

        seekBarTemperatura = findViewById(R.id.SliderTemperature);
        seekBarIrradiazione = findViewById(R.id.SliderIrradiation);
        viewTemperatura = findViewById(R.id.TextTemperature);
        viewIrradiazione = findViewById(R.id.textIrradiation);

        ToolBar_buttons = findViewById(R.id.bottom_navigation);//toolbar per effettuare le operazioni



        //seekbar per la selezione dei dati di controllo per la previsione futura
        seekBarTemperatura.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                double prg = minT + progress / (double) scale;
                viewTemperatura.setText(String.format("%.4f", prg).concat("..."));
                selectedTemperature = prg;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        seekBarIrradiazione.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                double prg = minI + progress / (double)scale;
                viewIrradiazione.setText(String.format("%.4f", prg).concat("..."));
                selectedIrradiance = prg;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        ToolBar_buttons.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {


                switch (item.getItemId()) {

                    case  R.id.navigation_prediction:
                        itemGraph = item;
                        itemGraph.setCheckable(true);
                        itemGraph.setChecked(true);

                        if(!startDateSelected.isEmpty() && !endDataSelected.isEmpty()){

                            //setto i falg per il recupero dei nomi dei field
                            HashMap<Integer, Boolean> flag = buildCheckID(ID_1, ID_2);


                            ShowCheckList("Previsone", "", false, activity, adapterPrevisione,
                                    (x) -> retreiveFieldNameFromServer(flag), -1, TypeGerericFunction.IntegerType, "Predict", "Close", (x) -> doNeagtiveFunctionGraph(), null);


                        }
                        else ShowAlert("Errore", "Necessario Selezionare Data Inizio E Fine", false, activity, (x) -> doNeagtiveFunctionGraph(), null);

                        break;

                    case R.id.navigation_controllerPrediction :
                        itemController = item;
                        itemController.setCheckable(true);
                        itemController.setChecked(true);

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                DataSavedToPredict dataToPredict = DataSavedToPredict.getInstance();

                                if(dataToPredict.isSaved()) {

                                    if(flagPredictionDone) {

                                        if(!startDateSelected.isEmpty() && !endDataSelected.isEmpty()) {

                                            int diff = getDayAhead(stringToLocalDate(yesterday), stringToLocalDate(endDataSelected));
                                            if(diff >= 1) {

                                                if(gridController.getVisibility() == View.GONE){

                                                    Matrix current_U = dataToPredict.getCurrent_U();
                                                    viewTemperatura.setText(String.valueOf(current_U.get(0,0)));
                                                    viewIrradiazione.setText(String.valueOf(current_U.get(1,0)));


                                                    minT = current_U.get(0,0) / 2;
                                                    maxT = current_U.get(0,0) * 2;

                                                    seekBarTemperatura.setMax((int)((maxT - minT) * scale));
                                                    seekBarTemperatura.setProgress((int) ((current_U.get(0,0) - minT) * scale));
                                                    System.out.println("IL VALORE REALE DI TEMPERATURE:" + current_U.get(0,0));


                                                    minI = current_U.get(1,0) / 2;
                                                    maxI = current_U.get(1,0) * 2;

                                                    seekBarIrradiazione.setMax((int) ((maxI - minI) * scale));
                                                    seekBarIrradiazione.setProgress((int) ((current_U.get(1,0) - minI) * scale));
                                                    System.out.println("IL VALORE REALE DI IRRADIANCE:" + current_U.get(1,0));



                                                    if(itemController != null) toolbarGrafic(itemController, 120);
                                                    gridController.setVisibility(View.VISIBLE);
                                                    ButtonControlledPrediction.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            activity.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {

                                                                    try {
                                                                        makeNewPoint(selectedTemperature, selectedIrradiance, allListData, newGraph, lastTraking, activity, setPrediction, idxLatTrackingPrediction);
                                                                    }
                                                                    catch (Exception e) {
                                                                        ShowAlert("Errore", e.getMessage(), true, activity, null, null);
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                                else gridController.setVisibility(View.GONE);
                                            }
                                            else {
                                                ShowAlert("", "Necessario Selezionare Data Finale Superiore A: " + yesterday , true, activity, (x) -> doNeagtiveFunctionController(), null);
                                            }
                                        }
                                        else {
                                            ShowAlert("", "Necessario Selezionare Data Inizio E Fine", true, activity, (x) -> doNeagtiveFunctionController(), null);
                                        }
                                    }
                                    else {
                                        ShowAlert("", "Necessario Prevedere Dato Futuro", true, activity, (x) -> doNeagtiveFunctionController(), null);
                                    }
                                }
                                else {
                                    ShowAlert("", "Necessario Effettuare Predizione", true, activity, (x) -> doNeagtiveFunctionController(), null);
                                }
                            }
                        });
                        break;
                    case R.id.navigation_date:

                        gridController.setVisibility(View.GONE);
                        itemData = item;
                        itemData.setCheckable(true);
                        itemData.setChecked(true);
                        ShowDateSelectable("Data", "Selezionare Data Inizio e Fine", false, activity, (x) -> checkController(), 0, null);

                        break;
                }
                return false;

            }
        });

        //aggiungo listener per tornare all'activity precedente quando viene cliccato il tasto indietro
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeActivity(0);
            }
        });

        //abilito l'interfaccia se i dati sono presenti
        if(state.isSavedDataGraph()){

            buildGraph(state.getPrediction(), state.getTraking(), state.getObs(), state.getLower(), state.getUpper());
            enableGraphView();
        }

    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        SavedStatePrediction state = SavedStatePrediction.getInstance();

        state.setAdapterPrevisione(adapterPrevisione);
        state.setAllListData(allListData);
        state.setCountFailed(countFailed);
        state.setCountRequest(countRequest);
        state.setDataFromDb(dataFromDbTemp);
        state.setDataRetreived(dataRetreived);
        state.setDateData(dateData);

        state.setFieldMeans(fieldMeans);
        state.setFlagControllerActive(flagControllerActive);
        state.setFlagPredictionDone(flagPredictionDone);


        state.setIdxLatTrackingPrediction(idxLatTrackingPrediction);


        state.setListForChoice(listForChoice);
        state.setLastTraking(lastTraking);
        state.setMaxI(maxI);
        state.setMaxT(maxT);
        state.setMinI(minI);
        state.setMinT(minT);

        state.setSelectedTemperature(selectedTemperature);
        state.setSetPrediction(setPrediction);

        state.setSetLower(setLower);
        state.setSetUpper(setUpper);


        state.setSelectedIrradiance(selectedIrradiance);
        state.setSelectedTemperature(selectedTemperature);
        state.setServideRetreive(servideRetreive);



        state.setMyDataStructDataReal(myDataStructDataReal);
        state.setMyDataStructName(myDataStructName);


        state.setStartDateSelected(startDateSelected);
        state.setEndDataSelected(endDataSelected);
        //state.setStartDataToServer(startDataToServer);
        state.setRecyclerViewControllo(recyclerViewControllo);
        state.setLoadingBuilder(loadingBuilder);


        state.setSaved(true);

    }
    /**
     * Aggiunta da Matteo Torchia 599899
     * Funzione da eseguire quando utente seleziona close nel pop-up dei grafici
     */
    private int doNeagtiveFunctionGraph(){
        if(itemGraph != null) toolbarGrafic(itemGraph, 120);
        return 0;
    }
    private int doNeagtiveFunctionController(){
        if(itemController != null) toolbarGrafic(itemController, 120);
        return 0;
    }


    /**
     * Metodo per la getsione del flusso di recupero dati
     * Viene richiamato dall'ultima rispsota dal server dopo
     * aver recuperato i nomi dei field
     */

    private int handleFlow() {


        //solo dopo che l'utente ha effettuato veramente la scelta disabilito la grfica precedente
        //Elimino i dati contenuti nel grafico se utilizzato precedentemente

        if(newGraph != null){
            disableGraphView();
            newGraph = null;
            gridController.setVisibility(View.GONE);
        }



        flagPositionDataDB = -1;
        fieldMeans = new HashMap<>();
        dataFromDbTemp = null;
        dataFromDbIrr = null;
        dataFromDbChoice = null;
        dateData = null;

        //recupero la scelta fatta dall'utente
        String choice = adapterPrevisione.getCheckedItems().get(0);

        //recuper i dati nell'intevallo di tempo selezionato
        dataFromDbTemp = database.getDataFromBDTemperature(ID_1, ID_2, "temperature", changeFormatDate(startDateSelected, "dd/MM/yyyy", "yyyy-MM-dd"), changeFormatDate(endDataSelected, "dd/MM/yyyy", "yyyy-MM-dd"));
        dataFromDbIrr = database.getDataFromBDIrradiance(ID_1, ID_2, "irradiance", changeFormatDate(startDateSelected, "dd/MM/yyyy", "yyyy-MM-dd"), changeFormatDate(endDataSelected, "dd/MM/yyyy", "yyyy-MM-dd"));
        dataFromDbChoice = database.getDataFromBDChoice(ID_1, ID_2, choice, changeFormatDate(startDateSelected, "dd/MM/yyyy", "yyyy-MM-dd"), changeFormatDate(endDataSelected, "dd/MM/yyyy", "yyyy-MM-dd"));



        //se non ho dati
        if(dataFromDbChoice == null || dataFromDbChoice.isEmpty()){

            //recupero i dati dal server
            retreiveData(startDateSelected , endDataSelected, ID_1, ID_2);
        }
        else {


            //se sono presenti dati controllo se in quell'arco temporale sono presenti i dati scelti dall'utente
            boolean startDataFlag = dataFromDbChoice.contains(new Mean(changeFormatDate(startDateSelected, "dd/MM/yyyy", "yyyy-MM-dd")));
            boolean endDataFLag = dataFromDbChoice.contains(new Mean(changeFormatDate(endDataSelected, "dd/MM/yyyy", "yyyy-MM-dd")));

            //caso in cui ho i dati della scelta dell'utente
            if(startDataFlag && endDataFLag){

                if(!isCompletedRange(dataFromDbChoice, startDateSelected, endDataSelected))retreiveData(startDateSelected , endDataSelected, ID_1, ID_2);
                else {
                    buildDataFromDatabase(0);
                }

            }
            else {
                //caso in cui ho i dati sulla data di inizio ma non quella di fine
                if(startDataFlag && !endDataFLag){


                    if(!isCompletedRange(dataFromDbChoice, startDateSelected, endDataSelected))retreiveData(startDateSelected , endDataSelected, ID_1, ID_2);
                    else {


                        //devo controllare se la data finale scelta dall'utente è maggiore rispetto alla fine dei dati cho ho gia
                        //quindi rispetto alla data di ieri

                        int diff = getDayAhead(stringToLocalDate(changeFormatDate(dataFromDbChoice.get(dataFromDbChoice.size() - 1).getData(), "yyyy-MM-dd", "dd/MM/yyyy")),  stringToLocalDate(endDataSelected));

                        //recupero la differenza di giorni fra gli ultimi dati che ho e ieri
                        int diffFromYesterday = getDayAhead(stringToLocalDate(changeFormatDate(dataFromDbChoice.get(dataFromDbChoice.size() - 1).getData(), "yyyy-MM-dd", "dd/MM/yyyy")),  stringToLocalDate(yesterday));

                        //se la data che l'utente ha scelto rispetto ai dati che ho gia è maggiore
                        if(diff > 0){

                            //controlla se gli utlimi dati che ho sono di ieri o di giorni precedenti


                            if(diffFromYesterday == 0)buildDataFromDatabase(0);//se sono di ieri, effettuo la predizione
                            else {

                                flagPositionDataDB = 1;//segnalo che i dati nuovi vanno inseriti alla fine
                                buildDataFromDatabase(1);//altrimenti recupero i dati mancanti
                            }
                        }
                        else {
                            hideLoading();
                            ShowAlert("ERRORE -0000", "Ipossibile Effettiare Predizione", true, activity, null, null);
                        }

                    }

                }
                else if(!startDataFlag && endDataFLag){//caso in cui ho i dati sulla data di fine ma non quella di inizio

                    if(!isCompletedRange(dataFromDbChoice, startDateSelected, endDataSelected))retreiveData(startDateSelected , endDataSelected, ID_1, ID_2);
                    else {
                        flagPositionDataDB = 2;//segnalo che i dati nuovi vanno inseriti all'inizio
                        buildDataFromDatabase(2);
                    }
                }
                else if(!startDataFlag && !endDataFLag){

                    //caso in cui utente inserisce data precedente e successiva ai dati che ho
                    //per non effettuare troppe chiamate al server per recuperare i dati
                    //ricreo i dati dall'inizio
                    retreiveData(startDateSelected , endDataSelected, ID_1, ID_2);
                }
            }


        }

        return 0;
    }


    private int checkController() {


        if(startDateSelected.isEmpty() || endDataSelected.isEmpty()){
            gridController.setVisibility(View.GONE);
        }
        else {
            int diff = getDayAhead(stringToLocalDate(yesterday), stringToLocalDate(endDataSelected));
            if(diff < 1) {
                gridController.setVisibility(View.GONE);
            }
        }
        if(itemData != null) toolbarGrafic(itemData, 120);
        return 0;
    }



    public void finishRequest(boolean value, String channel_ID, HashMap<String, ArrayList<DataContainer>> DataReal, HashMap<String, List<EntryList>> DataName){


        countRequest++;
        if(!countFailed) {

            if(value) {//controllo se la callback è di richiesta soddisfatta o fallita

                //qui controllo in base al numero dei canali inseriri
                //se ho ottenuto tutte le risposte dal server o no
                checkResponseServer(expectedRequest, channel_ID, ID_1, ID_2);


            }
            else {//se la richiesta è fallita setto la var flag
                countFailed = true; //flaggo che anche solo una richiesta è fallita
            }
        }
        if(countFailed && countRequest == expectedRequest) {//se anche una sola richiesta è fallita e ho ricevuto tutte le callback

            countFailed = false;
            countRequest = 0;

            //istruzioni per invalidare i dati precedentementi acquisiti
            myDataStructName = null;
            myDataStructDataReal = null;

            hideLoading();
            ShowAlert("ERRORE", "Impossibile Recuperare I Dati. Errore Server", false, this, null, 0);
            //se fallisce la chiamata chiudo l'activity per ora.


        }
        else if(!countFailed){

            //quando tutti i canali che attualmente mi servono sono stati letti
            if(allDataRetreive(expectedRequest, ID_1, ID_2)){

                countRequest = 0;
                dataRetreived = true;
                myDataStructName = DataName;
                myDataStructDataReal = DataReal;

                //resetto i flag che indicano di aver ricevuto tutti i dati
                resetCheckAllDataRetreived();


                //prima di eseguire operazioni devo controllare se i dati
                //ho ricevuto dati dal server

                //TODO: Controllare se ho dati prima di eseguire il lavoro


                //devo lanciare un thread separato per elaborare i dati ricevuti dal server
                //per non bloccare il mainthread e quindi l'interfaccia per troppo tempo
                runProcessData();

            }
        }
    }

    /**
     * Recupero i field name dal server se necessario (quando ho i dati nel db ma non ho
     * i nomi dei field)
     */

    private int retreiveFieldNameFromServer(HashMap<Integer, Boolean> flag){

        String url_1 = null, url_2 = null;

        if(flag.get(0)){
            url_1 = "https://api.thingspeak.com/channels/" + ID_1 +"/feeds.json?results=0";
            dorequest(ID_1, url_1, flag);
        }
        if(flag.get(1)){
            url_2 = "https://api.thingspeak.com/channels/" + ID_2 +"/feeds.json?results=0";;
            dorequest(ID_2, url_2, flag);
        }

        return 0;
    }
    private void dorequest(String channelID, String url, HashMap<Integer, Boolean> flag) {

        final JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,

                new Response.Listener<JSONObject>() {
                    @Override
                    public synchronized void onResponse(JSONObject response) {
                        try {

                            //recupero i nomi dei field in un array
                            ArrayList<String> fields = new ArrayList<String>();
                            ArrayList<String> fieldsKey = new ArrayList<String>();
                            int dim = response.getJSONObject("channel").length();
                            try {
                                for (int i = 0; i < dim; i++) {
                                    fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i + 1))));
                                    fieldsKey.add("field" + (i + 1));
                                }
                            }
                            catch (Exception e) {}
                            SetMyDataStructName(channelID, fields, fieldsKey, false);

                            if(channelID == ID_1)flag.put(0, false);
                            else if(channelID == ID_2) flag.put(1, false);

                            //se ho ottenuto tutti i dati
                            if(!flag.get(0) && !flag.get(1)){

                                //controllo se temperatura e irradizaione esistono nel canale
                                boolean tempExist = CheckExistField(myDataStructName.get(ID_1), myDataStructName.get(ID_2), "temperature");
                                boolean irraExist = CheckExistField(myDataStructName.get(ID_1), myDataStructName.get(ID_2), "irradiance");


                                if(tempExist && irraExist)handleFlow();
                                else {
                                    hideLoading();
                                    ShowAlert("ERRORE", "Temperatura e/o Irrigazione Mancanti. Scegliere Canale Diverso", true, activity, null, null);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Errore nel download");

            }
        });
        Volley.newRequestQueue(activity).add(jsonObjectRequest);

    }
    private void SetMyDataStructName(String id, ArrayList<String> fields, ArrayList<String> fieldsKey, boolean resetFlag){

        if(!resetFlag){
            if(myDataStructName == null)myDataStructName = new HashMap<>();
            ArrayList<EntryList> data = new ArrayList<>();


            for(int i = 0; i<fields.size(); i++){
                data.add(new EntryList(fields.get(i), fieldsKey.get(i), false));
            }
            myDataStructName.put(id, data);
        }
        else myDataStructName = new HashMap<>();

    }


    /**
     * Ricreo le mie variabili con i dati dal database
     * e in base a behaviour eseguo operazioni diverse
     * @param behaviour if behaviour == 0 run runProcessData
     *                  else if behaviour == 1 recupero i dati finali
     *                  else if behaviour == 2 recupero i dati iniziali
     * @return
     */
    private int buildDataFromDatabase(int behaviour) {


        dateData = new ArrayList<>();

        ArrayList<Double> datatemp = new ArrayList<>();
        ArrayList<Double> datairr = new ArrayList<>();
        ArrayList<Double> datach = new ArrayList<>();

        for(int i = 0; i<dataFromDbChoice.size(); i++){

            datatemp.add(Double.parseDouble(dataFromDbTemp.get(i).getField()));
            datairr.add(Double.parseDouble(dataFromDbIrr.get(i).getField()));

            String v = dataFromDbChoice.get(i).getField();
            if(v.equals("no")){
                datach.add(Double.NEGATIVE_INFINITY);
            }
            else datach.add(Double.parseDouble(v));

            dateData.add(changeFormatDate(dataFromDbTemp.get(i).getData(), "yyyy-MM-dd", "dd/MM/yyyy"));

        }

        Matrix controllo = new Matrix(2, datatemp.size());
        Matrix pred = new Matrix(1, datach.size());

        for(int i = 0; i<datatemp.size(); i++){
            controllo.set(0, i, datatemp.get(i));
            controllo.set(1, i, datairr.get(i));
            pred.set(0, i, datach.get(i));
        }

        fieldMeans.put("Controllo", controllo);
        fieldMeans.put("Previsione", pred);
        LocalDate d = null;

        switch (behaviour){
            case 0:
                doTraining();
                break;
            case 1:

                d = stringToLocalDate(dateData.get(dateData.size() - 1));
                retreiveData(LocalDateToString(d.plusDays(1)) , endDataSelected, ID_1, ID_2);

                break;
            case 2:

                d = stringToLocalDate(dateData.get(0));
                retreiveData(startDateSelected, LocalDateToString(d.minusDays(1)), ID_1, ID_2);
                break;
        }

        return 0;

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void finishTraining(boolean errorFlag, Double pred, ArrayList<Double> Traking, Matrix observations, int days, Double upp, Double low) {



        if(!errorFlag) {
            /**
             * Se la previsione è stata completata con successo, controllo le date
             * che l'utente ha selezionato per costruire il grafico correttamente
             */
            if(AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()){


                //recupero il valore di lower e upper
                upper = upp;
                lower = low;

                ArrayList<Double> obs = new ArrayList<>();
                int firstObs = dateData.indexOf(startDateSelected);
                int lastObs = dateData.indexOf(endDataSelected);

                if(firstObs == -1) firstObs = 0;


                if(lastObs == -1) lastObs = dateData.indexOf(yesterday);
                if(lastObs == -1) lastObs = dateData.size() - 1;

                for(int i = firstObs; i <= lastObs; i++){
                    obs.add(observations.get(0, i));
                }


                //ArrayList<Double> traking = null;
                int diffDays = getDayAhead(stringToLocalDate(yesterday), stringToLocalDate(endDataSelected));

                //recupero l'istanza di salvataggio di stato
                SavedStatePrediction state = SavedStatePrediction.getInstance();

                if(diffDays > 0) {

                    //traking = new ArrayList<>(predictions.subList(0, predictions.size() - 1));
                    ArrayList<Double> prediction = new ArrayList<>();

                    ArrayList<Double> lowerrr = new ArrayList<>();
                    ArrayList<Double> upperrr = new ArrayList<>();

                    prediction.add(Traking.get(Traking.size() - 1));
                    prediction.add(pred);

                    lowerrr.add(Traking.get(Traking.size() - 1));
                    lowerrr.add(low);

                    upperrr.add(Traking.get(Traking.size() - 1));
                    upperrr.add(upp);


                    //salvo il valore dlla predizione scelta inizialmente dall'utente
                    //per poi recuperarla in caso l'utente scelga di predirre un nuovo dato
                    //modificando i dati di controllo
                    DataSavedToPredict dataToPredict = DataSavedToPredict.getInstance();
                    dataToPredict.setLastPrediction(pred);

                    //salvo i valori del grafico nell'istanza di salvataggio di stato dell activity per ricrearlo
                    //in caso di passaggio a landscape
                    state.setPrediction(prediction);
                    state.setTraking(Traking);
                    state.setObs(obs);

                    state.setLower(lowerrr);
                    state.setUpper(upperrr);

                    state.setSavedDataGraph(true);//setto il falg di salvataggio dei dati
                    buildGraph(prediction, Traking, obs, lowerrr, upperrr);
                }
                else {


                    //salvo i valori del grafico nell'istanza di salvataggio di stato dell activity per ricrearlo

                    //in caso di passaggio a landscape
                    state.setPrediction(null);
                    state.setTraking(Traking);
                    state.setObs(obs);
                    state.setLower(null);
                    state.setUpper(null);

                    state.setSavedDataGraph(true);//setto il falg di salvataggio dei dati
                    buildGraph(null, Traking, obs, null, null);
                }

            }
        }
        else {
            hideLoading();
            ShowAlert("Errore", "Necessario Selezionare Dati Controllo e Previsione", false , this, null, null);
        }
    }

    private void buildGraph(ArrayList<Double> prediction, ArrayList<Double> traking, ArrayList<Double> obs, ArrayList<Double> lower, ArrayList<Double> upper){


        SavedStatePrediction state = SavedStatePrediction.getInstance();

        newGraph = findViewById(R.id.Graph_NewType);
        allListData = new LineData();
        if(prediction != null){

            lastTraking = prediction.get(0); //tengo traccia dell'ultimo dato di tracking da usarer per plottare la predizione controllata
            flagPredictionDone = true;

        }
        else flagPredictionDone = false;

        try {

            ArrayList<ArrayList<Entry>> fragments = buildDataSeries(traking, dateData.get(0));
            addFragments(allListData, fragments, "Tracking", Color.BLACK, true);


            ArrayList<Entry> listWithLastTrackX = fragments.get(fragments.size() - 1);
            Entry lastTrackEntry = listWithLastTrackX.get(listWithLastTrackX.size() - 1);

            //recupero l'ultimo valore x dei dati di traking sul grafico
            idxLatTrackingPrediction = lastTrackEntry.getX();

            //lo salvo per il caso di landscape
            state.setIdxLatTrackingPrediction(idxLatTrackingPrediction);

            fragments = buildDataSeriesMeasurement(obs, dateData.get(0));
            addFragments(allListData, fragments, "Observations", getColor(R.color.observationsColor), false);

            if(prediction != null){

                setPrediction = (LineDataSet) buildDataSeriesPrediction(prediction, idxLatTrackingPrediction, endDataSelected, "Prediction", Color.BLUE, false);
                setPrediction.setDrawCircles(false);

                setLower = (LineDataSet) buildDataSeriesPrediction(lower, idxLatTrackingPrediction, endDataSelected, "Prediction", getColor(R.color.upperLower), false);
                setUpper = (LineDataSet) buildDataSeriesPrediction(upper, idxLatTrackingPrediction, endDataSelected, "Prediction", getColor(R.color.upperLower), false);

                setUpper.setFillFormatter(new MyFillFormatter(setLower));
                newGraph.setRenderer(new MyLineLegendRenderer(newGraph, newGraph.getAnimator(), newGraph.getViewPortHandler()));

                setUpper.setFillDrawable(ContextCompat.getDrawable(activity, R.drawable.my_graph_gradient_low_up));

                setLower.setDrawCircles(false);
                setUpper.setDrawCircles(false);
                allListData.addDataSet(setPrediction);
                allListData.addDataSet(setLower);
                allListData.addDataSet(setUpper);
            }

            newGraph.setData(allListData);

            XAxis xAxis = newGraph.getXAxis();
            xAxis.setValueFormatter(new DateValueFormatter());
            xAxis.setDrawLabels(false);

            newGraph.setMarker(new MyMarkerView(activity, R.layout.pointer_popup, allListData, newGraph));
            newGraph.invalidate();


            //resetto la posizione originale di zoom ogni volta
            //cosi fra un plot e un altro non zummi sempre di piu
            newGraph.fitScreen();

            /**
             * Imposto auto-zoom sull'ultimo dato
             */
            float lastPoint = allListData.getXMax();
            newGraph.moveViewToX(lastPoint);
            newGraph.zoom(4f, 1f, lastPoint, 0);

            //imposto la leggenda
            setLegend(newGraph);

            if(prediction != null)setUpper.setDrawFilled(true);

            newGraph.notifyDataSetChanged();
            hideLoading();
            enableGraphView();
        }
        catch (Exception e) {

            disableGraphView();

            hideLoading();
            e.printStackTrace();
            ShowAlert("ERRORE", "Impossibile Costruire Grafico", false, activity, null, null);

        }

    }


    private void addFragments(LineData allListData, ArrayList<ArrayList<Entry>> fragments, String name, int color, boolean flag) {

        for(ArrayList<Entry> fragment : fragments) {
            LineDataSet set = new LineDataSet(fragment, name);
            custimizeGraphicData(2, 4, flag, 10, 10,color, set, name);
            allListData.addDataSet(set);

        }

    }


    /**
     * Metodo per il recupero dei dati dal server
     * @param dataStart
     * @param dataEnd
     * @param ID_1
     * @param ID_2
     * @return
     */
    private int retreiveData(String dataStart, String dataEnd, String ID_1, String ID_2/*, boolean flagUseData*/) {


        int maxNumData = 8000;
        String start = "&start="+changeFormatDate(dataStart, "dd/MM/yyyy", "yyyy-MM-dd") + "%2000:00:00";
        String end = "&end="+changeFormatDate(dataEnd, "dd/MM/yyyy", "yyyy-MM-dd") + "%2023:59:59";
        String url_1 = null;
        String url_2 = null;
        if(ID_1 != null){
            url_1 = "https://api.thingspeak.com/channels/"+ID_1+ "/feeds.json?api_key=" + "YYY"+
                    "&results="+maxNumData+"&offset="+getCurrentTimezoneOffset() + start + end ;

            servideRetreive.DoRequest(ID_1, "", this, url_1, this);
        }
        if(ID_2 != null){
            url_2 = "https://api.thingspeak.com/channels/"+ID_2+ "/feeds.json?api_key=" + "YYY"+
                    "&results="+maxNumData+"&offset="+getCurrentTimezoneOffset() + start + end ;

            servideRetreive.DoRequest(ID_2, "", this, url_2, this);
        }
        return 0;
    }
    public void setDeta(int day, int month, int year, boolean flag){

        String data = fixData(day) +"/"+ fixData(month) +"/"+ fixData(year);
        boolean falgData = false;

        if(flag){//se il metodo è chiamato dal picker di start
            falgData = setDataCorrectly(data, endDataSelected, true);
            //if(falgData) startDataToServer = buildDataForServer(day, month, year);
        }
        else {
            setDataCorrectly(data, startDateSelected, false);
        }

    }

    private int doTraining() {


        if(!endDataSelected.isEmpty() && !startDateSelected.isEmpty()) {

            Training objTraining = new Training((TrainingCallBack) activity);
            Thread t = new Thread(objTraining);

            //prima di avviare il thread setto i dati necessari all'algoritmo per lavorare
            int timeMin = 3, daysAhead = 1;

            if (AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()) {

                //se timeMin non è presente => è stata selezionata una data precedente ai primi dati che ho
                if((timeMin = dateData.indexOf(startDateSelected)) == -1){
                    timeMin = 0; // => metto timeMin a 0
                }
            }

            //daysAhead rappresenta fin dove sono disponibili i dati e quindi fin dove fare traking
            //quindi daysAhead = dateData.indexOf(endDataSelected)) == -1 => potrebbe essere il caso in cui si fa una prediction
            //oppure il caso in cui sia stata selezionata una data precedente
            //=> daysAhead = dateData.indexOf(yesterday), se daysAhead = dateData.indexOf(yesterday) == -1 =>
            //caso in cui la data finale è stata selezionata in un range precedente => uso la fine dei dati che ho

            if((daysAhead = dateData.indexOf(endDataSelected)) == -1) {
                daysAhead = dateData.indexOf(yesterday);
                if(daysAhead == -1)daysAhead = dateData.size() - 1;
            }

            /*
             * Qui controllo se il range selezionato dall'utente contiene almeno
             * 3 misurazioni per poter effettuare le predizioni
             *
             * */

            Matrix Y = fieldMeans.get("Previsione");//recupero i dati delle misurazioni


            int count = 0;

            //controllo che siano presenti almeno 3 alementi
            for(int i = timeMin; i <= daysAhead; i++) {

                if(Y.get(0, i) != Double.NEGATIVE_INFINITY) count++;

                if(count == 3) break;
            }
            if(count >= 3){

                objTraining.setDaysAhead(daysAhead);
                objTraining.setTimeMin(timeMin);
                objTraining.setEndData(endDataSelected);

                objTraining.setMatrixControll(fieldMeans.get("Controllo"));
                objTraining.setMatrixPrevisione(fieldMeans.get("Previsione"));

                objTraining.setMeasurementDate(dateData);

                ShowLoading(activity, false);
                t.start();
            }
            else {

                hideLoading();
                ShowAlert("", "Dati Mancanti Nell'Intervallo Temporale Selezionato. Necessario Selezionare Intervallo Diverso",
                        false, activity, null, null);

            }




        }
        else {
            hideLoading();
            ShowAlert("Errore", "Selezionare Data Fine e Data Inizio", false, activity, null, null);
        }

        return 0;
    }
    private boolean setDataCorrectly(String current, String selected, Boolean flag) {

        //controllo che la data di inizio sia precedente a quella di fine se necessario
        if(!selected.isEmpty()){

            if(checkDate(startDateSelected, endDataSelected, flag, current)){

                if(flag) startDateSelected = current;
                else endDataSelected = current;

                updateDataLayout(flag, current);
                return true;
            }
            else {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if(flag) Toast.makeText(activity, "ERRORE Inserire Data Precedente", Toast.LENGTH_SHORT).show();
                        else Toast.makeText(activity, "ERRORE Inserire Data Successiva", Toast.LENGTH_SHORT).show();

                    }
                });
                return false;
            }
        }
        else {

            if(flag) startDateSelected = current;
            else endDataSelected = current;

            updateDataLayout(flag, current);
            return true;
        }
    }
    private void updateDataLayout(boolean flag, String current){

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(flag) dataStart.setText(current);
                else dataEnd.setText(current);
            }
        });
    }
    private void disableGraphView() {

        activity.runOnUiThread(new Runnable() {
            public void run() {

                startDateView.setText("Start:" + "\n" + startDateSelected);
                endDateView.setText("End:" + "\n" + endDataSelected);
                layoutPrevisione.setVisibility(View.GONE);
                newGraph.setVisibility(View.GONE);
                headerPrevisione.setVisibility(View.GONE);
                //GridViewExplanation.setVisibility(View.GONE);
                startDateView.setVisibility(View.GONE);
                endDateView.setVisibility(View.GONE);
                //resetto la grafica del testo prediction
                doNeagtiveFunctionGraph();
            }
        });

    }
    private void enableGraphView(){



        activity.runOnUiThread(new Runnable() {
            public void run() {
                startDateView.setText("Start:" + "\n" + startDateSelected);
                endDateView.setText("End:" + "\n" + endDataSelected);
                layoutPrevisione.setVisibility(View.VISIBLE);
                newGraph.setVisibility(View.VISIBLE);
                headerPrevisione.setVisibility(View.VISIBLE);
                //GridViewExplanation.setVisibility(View.VISIBLE);
                startDateView.setVisibility(View.VISIBLE);
                endDateView.setVisibility(View.VISIBLE);

                //resetto la grafica del testo prediction
                doNeagtiveFunctionGraph();
            }
        });

    }
    private int runProcessData() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Object [] res = null;

                res = buildDataMatrix(new ArrayList<>(
                                Arrays.asList("temperature", "irradiance"))
                        , Objects.requireNonNull(myDataStructDataReal.get(retreiveIdFromField("temperature", myDataStructName))));


                int lastData = -1;
                boolean flagOldData = dateData != null && !dateData.isEmpty();


                //qui unifico i dati di controllo e le date se erano presenti dati nel db
                if(flagOldData){

                    Matrix newControl = (Matrix)res[0];
                    ArrayList<String> newDate = (ArrayList<String>) res[1];

                    //qui devo controllare se devo inserire i dati dopo quelli che avevo
                    //nel db o prima
                    if(flagPositionDataDB == 1){

                        lastData = dateData.size();
                        Matrix control = new Matrix(2, lastData + newControl.getColumnDimension());

                        //inserisco i dati vecchi nella nuova struttura dati
                        for(int i = 0; i<lastData; i++){

                            control.set(0, i, fieldMeans.get("Controllo").get(0, i));
                            control.set(1, i, fieldMeans.get("Controllo").get(1, i));

                        }

                        //inserisco i dati nuovi
                        int j = 0;
                        for(int i = lastData; i < lastData + newControl.getColumnDimension(); i++){

                            control.set(0, i, newControl.get(0, j));
                            control.set(1, i, newControl.get(1, j));
                            if(j < newDate.size())dateData.add(i, newDate.get(j));
                            j++;
                        }
                        fieldMeans.put("Controllo", control);

                    }
                    else if(flagPositionDataDB == 2) {

                        lastData = dateData.size();
                        Matrix control = new Matrix(2, lastData + newControl.getColumnDimension() - 1);
                        ArrayList<String> d = new ArrayList<>();

                        //inserisco i dati nuovi nella nuova struttura dati
                        for(int i = 0; i<newControl.getColumnDimension(); i++){

                            control.set(0, i, newControl.get(0, i));
                            control.set(1, i, newControl.get(1, i));
                            d.add(newDate.get(i));
                        }

                        //inserisco i dati vecchi
                        int j = 1;
                        for(int i = newControl.getColumnDimension(); i < lastData + newControl.getColumnDimension(); i++){

                            if(j < fieldMeans.get("Controllo").getColumnDimension()) control.set(0, i, fieldMeans.get("Controllo").get(0, j));
                            if(j < fieldMeans.get("Controllo").getColumnDimension()) control.set(1, i, fieldMeans.get("Controllo").get(1, j));

                            if(j < dateData.size())d.add(dateData.get(j));
                            j++;
                        }

                        dateData = d;
                        fieldMeans.put("Controllo", control);
                    }
                }
                else {

                    fieldMeans.put("Controllo", (Matrix)res[0]);
                    dateData = (ArrayList<String>) res[1];
                }


                //qui devo controllare se la scelta dell'utente è l'evatraspirazione
                String choice = adapterPrevisione.getCheckedItems().get(0);

                /*if(choice.equals("evapotranspiration")){


                }
                else {
                    res = buildDataMatrix(new ArrayList<>(
                                    Arrays.asList(choice))
                            , Objects.requireNonNull(myDataStructDataReal.get(retreiveIdFromField(choice, myDataStructName))));
                }*/

                res = buildDataMatrix(new ArrayList<>(
                                Arrays.asList(choice))
                        , Objects.requireNonNull(myDataStructDataReal.get(retreiveIdFromField(choice, myDataStructName))));


                Matrix matrixPrevisione = (Matrix) res[0];
                Matrix previsione = null;

                //qui unifico le misurazioni
                if(flagOldData){


                    if(flagPositionDataDB == 1){

                        //lastData <-- indice dell'ultima data presente nel db
                        previsione = new Matrix(1, lastData + matrixPrevisione.getColumnDimension());

                        for(int i = 0; i<lastData; i++){
                            previsione.set(0, i, fieldMeans.get("Previsione").get(0, i));
                        }
                        int j = 0;
                        for(int i = lastData; i < lastData + matrixPrevisione.getColumnDimension(); i++){

                            previsione.set(0, i, matrixPrevisione.get(0, j));
                            j++;
                        }
                        fieldMeans.put("Previsione", previsione);

                    }
                    else if(flagPositionDataDB == 2){


                        previsione = new Matrix(1, lastData + matrixPrevisione.getColumnDimension() - 1);

                        for(int i = 0; i<matrixPrevisione.getColumnDimension(); i++){
                            previsione.set(0, i, matrixPrevisione.get(0, i));
                        }

                        int j = 1;
                        for(int i = matrixPrevisione.getColumnDimension(); i < lastData + matrixPrevisione.getColumnDimension(); i++){

                            if(j < fieldMeans.get("Previsione").getColumnDimension())previsione.set(0, i, fieldMeans.get("Previsione").get(0, j));
                            j++;
                        }
                        fieldMeans.put("Previsione", previsione);
                        int x = 0;
                    }


                }
                else {
                    previsione = matrixPrevisione;
                    fieldMeans.put("Previsione", matrixPrevisione);
                }
                System.out.println();

                /**
                 * controllare prima se endDataSelected è contenuta, se non
                 * c'è usare la fine di dateData
                 */
                int idxDate = 0;
                if((idxDate = dateData.indexOf(endDataSelected)) == -1){
                    idxDate = dateData.size() - 1;
                }

                //inserisco i dati nel db
                insertOnDatabase(idxDate, dateData, myDataStructName, fieldMeans, getApplicationContext(), adapterPrevisione);

                //Per poter settare il listener nella classe doTraining devo lanciare il thread
                //di doTraining dal thread main
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doTraining();
                    }
                });


            }
        }).start();
        return 0;
    }

    /**
     * Metodo per recuperare il field Name
     * @param id
     * @param field
     * @return
     */
    private String retreiveField(String id, String field){

        List<EntryList> lst = myDataStructName.get(id);
        String fld = null;
        for(int i = 0; i<lst.size(); i++){
            if(lst.get(i).getText().equals(field)){
                fld = lst.get(i).getNameFromServer();
                break;
            }
        }
        return fld;
    }

    /**
     * Metodo utilizzato per il recupero dell'elemento selezionat
     * nella lista di scelte per i parametri da prevedere
     *
     * @param idx
     * @return
     */
    public void retreiveSelection(int idx){

        for(int i = 0; i<listForChoice.size(); i++){
            EntryList e = listForChoice.get(i);
            if(e.isChecked() && i != idx){
                e.setChecked(false);
            }
            if(i == idx)e.setChecked(true);
        }
        adapterPrevisione.notifyDataSetChanged();

    }
    private int closeActivity(int x){

        //elimino lo sato precedente
        SavedStatePrediction.delateInstance();

        finish();
        return 0;
    }
}