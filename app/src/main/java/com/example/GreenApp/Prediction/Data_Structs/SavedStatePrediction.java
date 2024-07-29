package com.example.GreenApp.Prediction.Data_Structs;


import android.app.Activity;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Prediction.Utils.DatePickerFragment;
import com.example.GreenApp.Prediction.Utils.RetreiveData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import Jama.Matrix;

/**
 * Utilizzo una classe singleton per salvare lo stato
 * dell'activity di predizione prima che l'activity vemga
 * distrutta in caso di rotazione dello schermo e recuperarli
 * subito dopo in modo da poter mantenere l'interfaccia attiva
 * anche dopo la rotazione
 */
public class SavedStatePrediction {



    private boolean isSaved = false;//variabile per controllare se i dati generali sono stati salvati
    private boolean isSavedDataGraph = false; ////variabile per controllare se i dati del grafico sono stati salvati

    //istanza della classe stato
    private static SavedStatePrediction instance = null;

    private List<EntryList> listForChoice = null;

    //variabili per il salvataggio dello stato

    private HashMap<String, List<EntryList>> myDataStructName = null;
    private HashMap<String, ArrayList<DataContainer>> myDataStructDataReal = null;


    private static AppDatabase database;
    private SeekBar seekBarTemperatura = null;
    private SeekBar seekBarIrradiazione = null;
    private TextView viewTemperatura = null;
    private TextView viewIrradiazione = null;

    private Button ButtonControlledPrediction = null;
    private GridLayout gridController = null;

    private HashMap<String, Matrix> fieldMeans;

    private ArrayList<String> dateData;

    private List<Mean> dataFromDb;

    private LinearLayout layoutPrevisione = null;
    private LineChart newGraph = null;
    private LineData allListData = null;
    private LineDataSet setPrediction = null;
    private TextView startDateView = null;
    private TextView endDateView = null;
    private GridLayout headerPrevisione = null;
    private Activity activity = null;

    private AlertDialog alertBuilderDate = null;
    private double lastTraking = 0;

    private BottomNavigationView ToolBar_buttons;
    private SingleAdapter adapterPrevisione;

    private boolean dataRetreived = false;
    private boolean countFailed = false;
    private int countRequest = 0;

    private int expectedRequest = 0;
    private RetreiveData servideRetreive = new RetreiveData();
    private double selectedTemperature = 0;
    private double selectedIrradiance = 0;

    private String ID_1 = null;
    private String ID_2 = null;
    private double minT = 0;
    private double maxT = 0;
    private double minI = 0;
    private double maxI = 0;

    private boolean flagPredictionDone = false;
    private boolean flagControllerActive = false;

    //////////////////////////////////

    private DatePickerFragment fragmentEnd = null;
    private DatePickerFragment fragmentStart = null;
    private LinearLayout startEntry =  null;
    private LinearLayout endEntry =  null;
    private TextView dataStart = null;
    private TextView dataEnd = null;
    private String startDateSelected = "";
    private String endDataSelected = "";
    private String startDataToServer = "";
    private RecyclerView recyclerViewControllo;

    private AlertDialog loadingBuilder = null;

    private ArrayList<Double> prediction = null;
    private ArrayList<Double> traking = null;
    private ArrayList<Double> obs = null;

    /////////////////////////////////////////
    private SavedStatePrediction(){}

    public static SavedStatePrediction getInstance(){

        if(instance == null){
            synchronized(SavedStatePrediction.class){
                if(instance == null){
                    instance = new SavedStatePrediction();
                }
            }
        }
        return instance;
    }
    public static void delateInstance(){
        synchronized(SavedStatePrediction.class){
            instance = null;
        }
    }
    public static AppDatabase getDatabase() {
        return database;
    }

    public static void setDatabase(AppDatabase database) {
        SavedStatePrediction.database = database;
    }


    //metodi per controllare se i dati sono presenti
    public boolean isSaved() {return isSaved;}

    public void setSaved(boolean saved) {isSaved = saved;}

    public HashMap<String, List<EntryList>> getMyDataStructName() {
        return myDataStructName;
    }

    public void setMyDataStructName(HashMap<String, List<EntryList>> myDataStructName) {
        this.myDataStructName = myDataStructName;
    }

    public HashMap<String, ArrayList<DataContainer>> getMyDataStructDataReal() {
        return myDataStructDataReal;
    }

    public void setMyDataStructDataReal(HashMap<String, ArrayList<DataContainer>> myDataStructDataReal) {
        this.myDataStructDataReal = myDataStructDataReal;
    }

    public SeekBar getSeekBarTemperatura() {
        return seekBarTemperatura;
    }

    public void setSeekBarTemperatura(SeekBar seekBarTemperatura) {
        this.seekBarTemperatura = seekBarTemperatura;
    }

    public SeekBar getSeekBarIrradiazione() {
        return seekBarIrradiazione;
    }

    public void setSeekBarIrradiazione(SeekBar seekBarIrradiazione) {
        this.seekBarIrradiazione = seekBarIrradiazione;
    }

    public TextView getViewTemperatura() {
        return viewTemperatura;
    }

    public void setViewTemperatura(TextView viewTemperatura) {
        this.viewTemperatura = viewTemperatura;
    }

    public TextView getViewIrradiazione() {
        return viewIrradiazione;
    }

    public void setViewIrradiazione(TextView viewIrradiazione) {
        this.viewIrradiazione = viewIrradiazione;
    }

    public Button getButtonControlledPrediction() {
        return ButtonControlledPrediction;
    }

    public void setButtonControlledPrediction(Button buttonControlledPrediction) {
        ButtonControlledPrediction = buttonControlledPrediction;
    }

    public GridLayout getGridController() {
        return gridController;
    }

    public void setGridController(GridLayout gridController) {
        this.gridController = gridController;
    }

    public HashMap<String, Matrix> getFieldMeans() {
        return fieldMeans;
    }

    public void setFieldMeans(HashMap<String, Matrix> fieldMeans) {
        this.fieldMeans = fieldMeans;
    }

    public ArrayList<String> getDateData() {
        return dateData;
    }

    public void setDateData(ArrayList<String> dateData) {
        this.dateData = dateData;
    }

    public List<Mean> getDataFromDb() {
        return dataFromDb;
    }

    public void setDataFromDb(List<Mean> dataFromDb) {
        this.dataFromDb = dataFromDb;
    }

    public LinearLayout getLayoutPrevisione() {
        return layoutPrevisione;
    }

    public void setLayoutPrevisione(LinearLayout layoutPrevisione) {
        this.layoutPrevisione = layoutPrevisione;
    }

    public LineChart getNewGraph() {
        return newGraph;
    }

    public void setNewGraph(LineChart newGraph) {
        this.newGraph = newGraph;
    }

    public LineData getAllListData() {
        return allListData;
    }

    public void setAllListData(LineData allListData) {
        this.allListData = allListData;
    }

    public LineDataSet getSetPrediction() {
        return setPrediction;
    }

    public void setSetPrediction(LineDataSet setPrediction) {
        this.setPrediction = setPrediction;
    }

    public TextView getStartDateView() {
        return startDateView;
    }

    public void setStartDateView(TextView startDateView) {
        this.startDateView = startDateView;
    }

    public TextView getEndDateView() {
        return endDateView;
    }

    public void setEndDateView(TextView endDateView) {
        this.endDateView = endDateView;
    }

    public GridLayout getHeaderPrevisione() {
        return headerPrevisione;
    }

    public void setHeaderPrevisione(GridLayout headerPrevisione) {
        this.headerPrevisione = headerPrevisione;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public double getLastTraking() {
        return lastTraking;
    }

    public void setLastTraking(double lastTraking) {
        this.lastTraking = lastTraking;
    }

    public BottomNavigationView getToolBar_buttons() {
        return ToolBar_buttons;
    }

    public void setToolBar_buttons(BottomNavigationView toolBar_buttons) {
        ToolBar_buttons = toolBar_buttons;
    }

    public SingleAdapter getAdapterPrevisione() {
        return adapterPrevisione;
    }

    public void setAdapterPrevisione(SingleAdapter adapterPrevisione) {
        this.adapterPrevisione = adapterPrevisione;
    }

    public boolean isDataRetreived() {
        return dataRetreived;
    }

    public void setDataRetreived(boolean dataRetreived) {
        this.dataRetreived = dataRetreived;
    }

    public boolean isCountFailed() {
        return countFailed;
    }

    public void setCountFailed(boolean countFailed) {
        this.countFailed = countFailed;
    }

    public int getCountRequest() {
        return countRequest;
    }

    public void setCountRequest(int countRequest) {
        this.countRequest = countRequest;
    }

    public int getExpectedRequest() {
        return expectedRequest;
    }

    public void setExpectedRequest(int expectedRequest) {
        this.expectedRequest = expectedRequest;
    }

    public RetreiveData getServideRetreive() {
        return servideRetreive;
    }

    public void setServideRetreive(RetreiveData servideRetreive) {
        this.servideRetreive = servideRetreive;
    }

    public double getSelectedTemperature() {
        return selectedTemperature;
    }

    public void setSelectedTemperature(double selectedTemperature) {
        this.selectedTemperature = selectedTemperature;
    }

    public double getSelectedIrradiance() {
        return selectedIrradiance;
    }

    public void setSelectedIrradiance(double selectedIrradiance) {
        this.selectedIrradiance = selectedIrradiance;
    }

    public String getID_1() {
        return ID_1;
    }

    public void setID_1(String ID_1) {
        this.ID_1 = ID_1;
    }

    public String getID_2() {
        return ID_2;
    }

    public void setID_2(String ID_2) {
        this.ID_2 = ID_2;
    }

    public double getMinT() {
        return minT;
    }

    public void setMinT(double minT) {
        this.minT = minT;
    }

    public double getMaxT() {
        return maxT;
    }

    public void setMaxT(double maxT) {
        this.maxT = maxT;
    }

    public double getMinI() {
        return minI;
    }

    public void setMinI(double minI) {
        this.minI = minI;
    }

    public double getMaxI() {
        return maxI;
    }

    public void setMaxI(double maxI) {
        this.maxI = maxI;
    }

    public boolean isFlagPredictionDone() {
        return flagPredictionDone;
    }

    public void setFlagPredictionDone(boolean flagPredictionDone) {
        this.flagPredictionDone = flagPredictionDone;
    }

    public boolean isFlagControllerActive() {
        return flagControllerActive;
    }

    public void setFlagControllerActive(boolean flagControllerActive) {
        this.flagControllerActive = flagControllerActive;
    }

    public DatePickerFragment getFragmentEnd() {
        return fragmentEnd;
    }

    public void setFragmentEnd(DatePickerFragment fragmentEnd) {
        this.fragmentEnd = fragmentEnd;
    }

    public DatePickerFragment getFragmentStart() {
        return fragmentStart;
    }

    public void setFragmentStart(DatePickerFragment fragmentStart) {
        this.fragmentStart = fragmentStart;
    }

    public LinearLayout getStartEntry() {
        return startEntry;
    }

    public void setStartEntry(LinearLayout startEntry) {
        this.startEntry = startEntry;
    }

    public LinearLayout getEndEntry() {
        return endEntry;
    }

    public void setEndEntry(LinearLayout endEntry) {
        this.endEntry = endEntry;
    }

    public TextView getDataStart() {
        return dataStart;
    }

    public void setDataStart(TextView dataStart) {
        this.dataStart = dataStart;
    }

    public TextView getDataEnd() {
        return dataEnd;
    }

    public void setDataEnd(TextView dataEnd) {
        this.dataEnd = dataEnd;
    }

    public String getStartDateSelected() {
        return startDateSelected;
    }

    public void setStartDateSelected(String startDateSelected) {
        this.startDateSelected = startDateSelected;
    }

    public String getEndDataSelected() {
        return endDataSelected;
    }

    public void setEndDataSelected(String endDataSelected) {
        this.endDataSelected = endDataSelected;
    }

    public String getStartDataToServer() {
        return startDataToServer;
    }

    public void setStartDataToServer(String startDataToServer) {
        this.startDataToServer = startDataToServer;
    }

    public RecyclerView getRecyclerViewControllo() {
        return recyclerViewControllo;
    }

    public void setRecyclerViewControllo(RecyclerView recyclerViewControllo) {
        this.recyclerViewControllo = recyclerViewControllo;
    }


    public AlertDialog getLoadingBuilder() {
        return loadingBuilder;
    }

    public void setLoadingBuilder(AlertDialog loadingBuilder) {
        this.loadingBuilder = loadingBuilder;
    }

    public AlertDialog getAlertBuilderDate() {
        return alertBuilderDate;
    }

    public void setAlertBuilderDate(AlertDialog alertBuilderDate) {
        this.alertBuilderDate = alertBuilderDate;
    }

    public ArrayList<Double> getPrediction() {
        return prediction;
    }

    public void setPrediction(ArrayList<Double> prediction) {
        this.prediction = prediction;
    }

    public ArrayList<Double> getTraking() {
        return traking;
    }

    public void setTraking(ArrayList<Double> traking) {
        this.traking = traking;
    }

    public ArrayList<Double> getObs() {
        return obs;
    }

    public void setObs(ArrayList<Double> obs) {
        this.obs = obs;
    }

    public boolean isSavedDataGraph() {
        return isSavedDataGraph;
    }

    public void setSavedDataGraph(boolean savedDataGraph) {
        isSavedDataGraph = savedDataGraph;
    }

    public List<EntryList> getListForChoice() {
        return listForChoice;
    }

    public void setListForChoice(List<EntryList> listForChoice) {
        this.listForChoice = listForChoice;
    }
}
