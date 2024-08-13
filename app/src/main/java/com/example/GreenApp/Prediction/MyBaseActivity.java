package com.example.GreenApp.Prediction;

import static com.example.GreenApp.Prediction.Utils.EMAlgorithm.init3DMatrix;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GreenApp.AppDatabase;
import com.example.GreenApp.Channel.Channel;
import com.example.GreenApp.Prediction.Data_Structs.AndroidVersionSingleton;
import com.example.GreenApp.Prediction.Data_Structs.DataContainer;
import com.example.GreenApp.Prediction.Data_Structs.DataSavedToPredict;
import com.example.GreenApp.Prediction.Data_Structs.EntryList;
import com.example.GreenApp.Prediction.Data_Structs.ListAdapter;
import com.example.GreenApp.Prediction.Data_Structs.Mean;
import com.example.GreenApp.Prediction.Data_Structs.Model;
import com.example.GreenApp.Prediction.Data_Structs.SingleAdapter;
import com.example.GreenApp.Prediction.Utils.AdapterInterfaceCheck;
import com.example.GreenApp.Prediction.Utils.DatePickerFragment;
import com.example.GreenApp.Prediction.Utils.EMAlgorithm;
import com.example.GreenApp.Prediction.Utils.GraphUtils.DateValueFormatter;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyFillFormatter;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyLineLegendRenderer;
import com.example.GreenApp.Prediction.Utils.GraphUtils.MyMarkerView;
import com.example.GreenApp.Prediction.Utils.SelectionAdapter;
import com.example.firstapp.R;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Function;

import Jama.Matrix;

@RequiresApi(api = Build.VERSION_CODES.O) //annotazione per asserire di star utilizzando la versione sufficiente di android
public class MyBaseActivity extends AppCompatActivity {


    /**
     * variabile usata per contenere i dati necessari a effettuare una previsione
     * con diversi parametri di controllo e confrontare tutto.
     */



    /**
     * Variabile temporanea che uso solo ora per settare un giorno e
     * considerarlo come presente nell range temporale dei dati che ho
     * a disposizione oggi "20/09/2022" <-- ultimmo giorno in cui ho dati
     */

    //TODO: da modificare alla fine settando la data di ieri reale

    protected String now = "21/09/2022";

    protected String yesterday = "20/09/2022";

    /**
     * Sezione variabili usate per il pop-up della scelta di data inizio e fine
     */
    protected AlertDialog alertBuilderDate = null; //builder per il pop up di selezione date

    protected DatePickerFragment fragmentEnd = null; //fragment per la selezione della data di fine
    protected DatePickerFragment fragmentStart = null; //fragment per la selezione della data di fine
    protected LinearLayout startEntry =  null;
    protected LinearLayout endEntry =  null;
    protected TextView dataStart = null;
    protected TextView dataEnd = null;

    /**
     * Fine Sezione
     */

    protected String startDateSelected = "";//data di inizio previsione
    protected String endDataSelected = "";//data di fine della previsione

    protected String startDataToServer = ""; //utilizzo un algtra variabile per recuperare i dati di un periodo precedente


    protected RecyclerView recyclerViewControllo;

    private LineDataSet lastNewDataSet = null;
    protected AlertDialog loadingBuilder = null;

    protected long DayInLong = 24L * 60 * 60 * 1000;

    protected final int NumChannel = 2;
    //array di booleani che uso per verificare che le richieste dei dati
    //siano tutte andate a buin fine
    protected boolean [] checkRetreiveChannel = new boolean[NumChannel];


    protected int [] usablePlotColor = new int[]{
            Color.GREEN, Color.BLUE, Color.BLACK, Color.YELLOW,
            Color.WHITE, Color.MAGENTA, Color.rgb(255, 165, 0), Color.rgb(247, 0, 255)
    };


    /**
     * Gestisco la grafica della toolbar
     * @return
     */
    protected void toolbarGrafic(MenuItem item, int time){

        item.setCheckable(true);
        item.setChecked(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                item.setCheckable(false);

            }
        }, time);
    }



    protected String getToday(){

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(today);

    }
    protected String getYesterday(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterdayTmp = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(yesterdayTmp);
    }

    /**
     * Costruisco la lista delle possibili previsioni
     * @param chaID1
     * @param chaID2
     * @return
     */
    protected ArrayList<EntryList> buildListAdapter(Channel chaID1, Channel chaID2) {

        ArrayList<EntryList> lstField = new ArrayList<>();

        //controllo dopo possono essere i field eventuali se corrispondono ai field
        //del channel 1 o in channel 2 o in entrambi

        int idx_1 = 0, idx_2 = 0;
        if(chaID1 != null && chaID2 == null){
            retreivePossibleChoice(-1, chaID1, lstField);
        }
        else if(chaID1 == null && chaID2 != null){
            retreivePossibleChoice(-1, chaID2, lstField);
        }
        else if(chaID1 != null && chaID2 != null){
            retreivePossibleChoice(-1, chaID1, lstField);
            retreivePossibleChoice(-2, chaID2, lstField);
        }

        return lstField;
    }

    /**
     * Metodo di supporto
     * @param idx
     * @param ch
     * @param lstField
     */
    private void retreivePossibleChoice(int idx, Channel ch, ArrayList<EntryList> lstField){

        for(int i = 0; i<8; i++){
            String field = select_check_Field(i, idx, null, ch);
            if(field != null && !field.equals("temperature") && !field.equals("irradiance")){
                lstField.add(new EntryList(field, "field" + (i+1), false));
            }
        }
    }
    protected ListAdapter buildControllAdapter() {
        return new ListAdapter(Arrays.asList(
                new EntryList("temperatura", "field1", false),
                new EntryList("Umidità", "field2", false),
                new EntryList("Ph", "field3", false),
                new EntryList("Ec", "field4", false),
                new EntryList("Umidita Suolo", "field5", false),
                new EntryList("Irradiazione", "field8", false),
                new EntryList("Kc", "field6", false)
        ));
    }
    protected String select_check_Field(int field, int choice, String fieldName, Channel v) {

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

    /**
     * Metodo per implementare la selezione dell' activity
     * @return ListAdapter
     */
    protected SingleAdapter buildActivitySelectAdapter(SelectionAdapter lst) {
        return new SingleAdapter(Arrays.asList(
                new EntryList("Storico", "", false),
                new EntryList("Previsione", "", false)
        ), lst);
    }


    protected void ShowLoading(Activity activity, boolean cancellable){

        if(loadingBuilder == null){
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            // layoutinflater object and use activity to get layout inflater
            LayoutInflater inflater = activity.getLayoutInflater();
            builder.setView(inflater.inflate(R.layout.layout_loading, null));
            builder.setCancelable(cancellable);
            loadingBuilder = builder.create();
            loadingBuilder.show();
        }
        else {
            loadingBuilder.show();
        }
    }
    protected void hideLoading() {
        if(loadingBuilder != null)loadingBuilder.dismiss();
    }
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

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                alertBuilder.setTitle(title);
                alertBuilder
                        .setMessage(mex)
                        .setCancelable(cancelable)
                        .setPositiveButton("Close",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(function_std != null)function_std.apply(d);
                                dialog.cancel();
                            }
                        });
                AlertDialog dialog = alertBuilder.create();
                dialog.show();
            }
        });
    }

    /**
     *
     *  Uso un TypeGerericFunction  per distinguere che tipo uso per la funzione egnerica passata
     *  al metodo in modo da arginare la type erasure
     *
     */
    protected <T,RR> void ShowCheckList(String title, String mex, boolean cancelable, Activity activity,
                                        AdapterInterfaceCheck adapter, Function<T, RR> function_std, T d, TypeGerericFunction type
                                        , String positiveText, String neagtiveText, Function<T, RR> function_std_neagtive, T d_negative){

        activity.runOnUiThread(new Runnable() {
            public void run() {

                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                LayoutInflater Inflater = activity.getLayoutInflater();

                View popUp = Inflater.inflate(R.layout.check_list, null);
                recyclerViewControllo = popUp.findViewById(R.id.idRecycleViewParameters);

                recyclerViewControllo.setAdapter((RecyclerView.Adapter) adapter);

                recyclerViewControllo.setLayoutManager(new LinearLayoutManager(activity));

                alertBuilder.setView(popUp);

                alertBuilder.setTitle(title);

                alertBuilder
                        .setMessage(mex)
                        .setCancelable(cancelable)
                        .setNegativeButton(neagtiveText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(function_std_neagtive != null){
                                    function_std_neagtive.apply(d_negative);
                                }
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if(adapter.getCheckedItems().size() <= 0){

                                    /*if(function_std_neagtive != null){
                                        function_std_neagtive.apply(d_negative);
                                    }*/
                                    dialog.cancel();
                                    ShowAlert("Errore", "Necessario Selezionare Previsione", false , activity, function_std_neagtive, null);
                                }
                                else {
                                    dialog.cancel();
                                    if(type == TypeGerericFunction.IntegerType) {
                                        ShowLoading(activity, false);
                                        function_std.apply(d);
                                    }
                                    else if(type == TypeGerericFunction.ViewType){
                                        ArrayList<String> choice = (ArrayList<String>)adapter.getCheckedItems();
                                        if(choice.size() != 1) {
                                            ShowAlert("Errore", "Selezionare Solo Una Opzione", false , activity, null, null);
                                        }
                                        else {
                                            function_std.apply(d);
                                        }
                                    }
                                    else if(type == TypeGerericFunction.OnlySelectionPrevition){
                                        ShowLoading(activity, false);
                                    }
                                }
                            }
                        });

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;

                AlertDialog dialog = alertBuilder.create();
                dialog.show();
                dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);

            }
        });
    }

    /**
     * Metodo aggiunto per verificare se fra i dati che ho nel database
     * ho buchi temporali
     */
    protected boolean isCompletedRange(List<Mean> dataFromChoice, String startData,  String endData){


        for(int i = 0; i<dataFromChoice.size()-1; i++){


            //controllo se il dato successivo sia distante da quello precedente
            //da piu di un giorno, in tal caso ho un buco


            if(getDayAhead(stringToLocalDate(changeFormatDate(dataFromChoice.get(i).getData(), "yyyy-MM-dd", "dd/MM/yyyy")), stringToLocalDate(changeFormatDate(dataFromChoice.get(i+1).getData(), "yyyy-MM-dd", "dd/MM/yyyy"))) > 1){
                return false;
            }
        }
        return true;
    }

    /**
     * Metodo per controllare se un field è presente nel canale
     * @return
     */
    protected boolean CheckExistField(List<EntryList> names_1, List<EntryList> names_2, String field){

        if(names_1 != null){
            for(EntryList e : names_1){
                if(e.getText().equals(field))return true;
            }
        }
        if(names_2 != null){
            for(EntryList e : names_2){
                if(e.getText().equals(field))return true;
            }
        }

        return false;
    }
    /**
     * Metodo usato per visualizzare pop-up date
     *
     */
    protected <T,RR> void ShowDateSelectable(String title, String mex, boolean cancelable, Activity activity,
                                         Function<T, RR> function_std, T d, TypeGerericFunction type){

        activity.runOnUiThread(new Runnable() {
            public void run() {

                if(alertBuilderDate == null){

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                    LayoutInflater Inflater = activity.getLayoutInflater();

                    View popUp = Inflater.inflate(R.layout.check_list_no_checkbox, null);

                    startEntry =  popUp.findViewById(R.id.EntryStart);
                    endEntry =  popUp.findViewById(R.id.EntryEnd);
                    dataStart = popUp.findViewById(R.id.DateStart);
                    dataEnd = popUp.findViewById(R.id.DateEnd);

                    if(startDateSelected != null && !startDateSelected.isEmpty()){
                        dataStart.setText(startDateSelected);
                    }
                    if(endDataSelected != null && !endDataSelected.isEmpty()){
                        dataEnd.setText(endDataSelected);
                    }

                    startEntry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fragmentStart.show(getSupportFragmentManager(), "datePikerStart");
                        }
                    });
                    endEntry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            fragmentEnd.show(getSupportFragmentManager(), "dataPickerEnd");
                        }
                    });
                    builder.setView(popUp);
                    builder.setTitle(title);
                    builder.setMessage(mex)
                            .setCancelable(cancelable)
                            .setPositiveButton("Finish",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {

                                    function_std.apply(d);
                                    alertBuilderDate.cancel();
                                }
                            })
                            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    //TODO: qui devo ripulire le date selezionate prima
                                    endDataSelected = "";
                                    startDateSelected = "";
                                    function_std.apply(d);

                                    alertBuilderDate.cancel();
                                    alertBuilderDate = null;

                                }
                            });;
                    alertBuilderDate = builder.create();
                }
                alertBuilderDate.show();
            }
        });
    }

    protected void addMargin(LinearLayout layout, int left, int top, int right, int bottom) {

        ConstraintLayout.LayoutParams par = new ConstraintLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        par.setMargins(0, 50, 0,0);
        layout.setLayoutParams(par);
    }
    //metodo per ripulire le strutture dati se necessario
    /*protected void cleanOldData() {
        myDataStructDataReal = SetmyDataStructDataReal();
    }
    */

    /**
     * Controllo se entrambe le richieste o soltanto una sono state soddisfatte
     * @param expectedRequest
     * @param ID_1
     * @param ID_2
     * @return
     */
    protected boolean allDataRetreive(int expectedRequest, String ID_1, String ID_2) {

        if(expectedRequest == 1){
            if(ID_1 != null){
                return checkRetreiveChannel[IdxChannel.IDX_channel1.ordinal()];
            }
            else if(ID_2 != null){
                return checkRetreiveChannel[IdxChannel.IDX_channel2.ordinal()];
            }
        }
        else if(expectedRequest == 2){
            return (checkRetreiveChannel[IdxChannel.IDX_channel1.ordinal()]
                    &&
                    checkRetreiveChannel[IdxChannel.IDX_channel2.ordinal()]);
        }
        return false;
    }

    /**
     * Resetto i flag dei dati ricevuti dal server dopo averli recuperati
     */
    protected void resetCheckAllDataRetreived(){
        checkRetreiveChannel[IdxChannel.IDX_channel1.ordinal()] = false;
        checkRetreiveChannel[IdxChannel.IDX_channel2.ordinal()] = false;
    }






    /**
     * Cambio il formato della stringa della data
     * @param data
     * @param inputFormat
     * @param outputFormat
     * @return
     */

    protected String changeFormatDate(String data, String inputFormat, String outputFormat) {

        String outputData = null;
        if (AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()) {

            DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern(outputFormat);
            DateTimeFormatter formatterInput = DateTimeFormatter.ofPattern(inputFormat);
            LocalDate dataIn = LocalDate.parse(data, formatterInput);
            outputData = dataIn.format(formatterOutput);
        }
        return outputData;
    }

    protected int addDateBetween(ArrayList <String> dates, String dCur, String dNext, int idxDate) {

        int dx = idxDate;
        if (AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()) {

            DateTimeFormatter formatterOutput = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate start = LocalDate.parse(dCur, formatter);
            LocalDate end = LocalDate.parse(dNext, formatter);

            for(LocalDate date = start.plusDays(1); !date.isAfter(end); date = date.plusDays(1)){
                dx++;
                dates.add(date.format(formatterOutput));
            }
        }
        return dx;
    }

    protected Object [] buildDataMatrix(List<String> selectedList, ArrayList<DataContainer> dataList) {

        int n = selectedList.size();
        int m = dataList.size();
        Object [] res = null;

        ArrayList<Double> [] summ = new ArrayList[n];
        ArrayList<String> allDate = new ArrayList<>();

        for(int i = 0; i<n; i++) {
            res = makeMean(m, selectedList.get(i), dataList);
            summ[i] = (ArrayList<Double>) res[0];
        }

        allDate = (ArrayList<String>) res[1];

        int m1 = summ[0].size();
        Matrix matrix = new Matrix(n, m1);

        for(int i = 0; i<n; i++) {
            for(int j = 0; j<m1; j++) {
                matrix.set(i, j, summ[i].get(j));
            }
        }

        return new Object[]{matrix, allDate};//matrix;
    }

    private Object [] makeMean(int m, String selected, ArrayList<DataContainer> dataList) {

        int idxDate = 1;
        int dataReaded = 0;
        int idxSum = 0;
        ArrayList<Double> summ = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();//strutta dati per recuperare le date delle medie

        dates.add(0, "");
        summ.add(idxSum, Double.NEGATIVE_INFINITY);
        for(int j = 0; j<m-1; j++) {

            char [] dCurArray = dataList.get(j).getData().toCharArray();
            char [] dNextArray = dataList.get(j+1).getData().toCharArray();

            //recupero solo anno mese e giorno dalla data delle mmisurazioni in esame
            String dCur = new String(dCurArray, 0, 10);
            String dNext = new String(dNextArray, 0, 10);



            //se sto selezionando i dati di oggi chiudo
            if(now.equals(changeFormatDate(dCur, "yyyy-MM-dd","dd/MM/yyyy"))){
                break;
            }

            if(!dates.get(idxDate-1).equals(changeFormatDate(dCur, "yyyy-MM-dd","dd/MM/yyyy"))){

                if(j != 0){
                    dates.set(idxDate, changeFormatDate(dCur,"yyyy-MM-dd", "dd/MM/yyyy"));
                    idxDate++;
                    dates.add(idxDate, "");
                }
                else {
                    dates.set(idxDate-1, changeFormatDate(dCur,"yyyy-MM-dd","dd/MM/yyyy"));
                    dates.add(idxDate, "");
                }
            }
            if(j == m-2){

                if(!dates.get(idxDate-1).equals(changeFormatDate(dNext, "yyyy-MM-dd", "dd/MM/yyyy"))){

                    dates.set(idxDate, changeFormatDate(dCur, "yyyy-MM-dd" ,"dd/MM/yyyy"));
                    idxDate++;
                }
            }
            //inserisco la prima misurazione che trovo
            if(dataReaded == 0){

                double tmp = dataList.get(j).getMapping(selected);
                if(tmp != Double.NEGATIVE_INFINITY && tmp != Double.POSITIVE_INFINITY) {
                    summ.set(idxSum, tmp);
                    dataReaded++;
                }

            }

            //controllo se le misurazioni prese in esame sono dello stesso giorno
            if(dCur.equals(dNext)){

                double tmp = dataList.get(j+1).getMapping(selected);
                if(tmp != Double.NEGATIVE_INFINITY && tmp != Double.POSITIVE_INFINITY){

                    //sommo la seconda misurazione
                    double oldValue = summ.get(idxSum);

                    if(oldValue == Double.NEGATIVE_INFINITY) summ.set(idxSum, tmp);
                    else summ.set(idxSum, summ.get(idxSum) + tmp);

                    dataReaded++;
                }
                if(j == m-2){
                    if(dataReaded == 0)dataReaded = 1;

                    summ.set(idxSum, summ.get(idxSum)/dataReaded);
                }
            }
            else {


                if(j == m-2) {
                    double tmp = dataList.get(j+1).getMapping(selected);
                    if(tmp != Double.NEGATIVE_INFINITY && tmp != Double.POSITIVE_INFINITY){
                        summ.set(idxSum + 1, tmp);
                    }
                    else {
                        summ.set(idxSum+1, Double.NEGATIVE_INFINITY);
                    }

                    summ.set(idxSum, summ.get(idxSum)/dataReaded);
                    dataReaded = 0;//per sicurezza ma non serve
                }
                else {

                    summ.set(idxSum, summ.get(idxSum)/dataReaded);
                    dataReaded = 0;


                    //se sto selezionando i dati di oggi chiudo
                    if(now.equals(changeFormatDate(dNext, "yyyy-MM-dd","dd/MM/yyyy"))){
                        break;
                    }

                    if (AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()) {

                        int days = 0;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                        LocalDate localDate1 = LocalDate.parse(new String(dCurArray), formatter);
                        LocalDate localDate2 = LocalDate.parse(new String(dNextArray), formatter);

                        days = (int) ChronoUnit.DAYS.between(localDate1, localDate2);
                        for(int i = 0; i<days; i++){
                            idxSum++;
                            summ.add(idxSum, Double.NEGATIVE_INFINITY);
                        }
                    }
                }
            }
        }

        dates.remove(""); //rimuovo l'ultima stringa vuota

        /*
        if(dates.size() < summ.size()){
            summ.remove(dates.size());
        }*/
        return new Object []{summ, dates};
    }







    protected long getReferceTime(String data) throws ParseException{

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date referenceDate = sdf.parse(data);

        //recupero il timestamp ottenuto dalla data
        long valueTime = referenceDate.getTime();
        Date dataProfTimeStamp = new Date(valueTime);

        //controllo che il timestamp restituisca la data corretta e non quella precedente
        String stringDataProfTimeStamp = sdf.format(dataProfTimeStamp);
        int checkDiff = getDayAhead(stringToLocalDate(stringDataProfTimeStamp), stringToLocalDate(data));

        //se il time stamp recuperato si riferisce alla data precedente
        if(checkDiff > 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(valueTime);
            int initialDay = calendar.get(Calendar.DAY_OF_YEAR);
            while (calendar.get(Calendar.DAY_OF_YEAR) == initialDay) {
                calendar.add(Calendar.HOUR_OF_DAY, 1);
            }
        }
        else if(checkDiff < 0){
            //se il time stamp recuperato si riferisce alla data successiva

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(valueTime);
            int initialDay = calendar.get(Calendar.DAY_OF_YEAR);
            while (calendar.get(Calendar.DAY_OF_YEAR) == initialDay) {
                calendar.add(Calendar.HOUR_OF_DAY, -1);
            }
            valueTime = calendar.getTimeInMillis();
        }
        System.out.println("la data from timestamp: " + stringDataProfTimeStamp);
        return valueTime;
    }

    protected ILineDataSet buildDataSeriesPrediction(ArrayList<Double> data, float lastTrackIdx, String endDataSelected, String name, int color, boolean enableT) throws ParseException {

        ArrayList<Entry> values = new ArrayList<>();


        long timeLasttrack = 0;
        long timePrediction = 0;
        try {
            timePrediction = getReferceTime(endDataSelected);
        }
        catch (ParseException e){e.printStackTrace();}


        double x0 = data.get(0);
        double x1 = data.get(1);
        values.add(new BarEntry(lastTrackIdx,  (float) x0 ));
        values.add(new BarEntry(timePrediction,  (float) x1 ));


        LineDataSet set = new LineDataSet(values, name);
        custimizeGraphicData(2, 4, enableT, 10, 10,color, set, "prediction");
        return set;

    }



    protected ArrayList<ArrayList<Entry>> buildDataSeries(ArrayList<Double> data, String startDate) throws ParseException{

        int idxFragment = 0;
        ArrayList<ArrayList<Entry>> fragmentedSeries = new ArrayList<>();

        long referenceTime = getReferceTime(startDate);


        fragmentedSeries.add(new ArrayList<>());
        for(int i = 0; i < data.size(); i++){

            //se il metodo è stato chiamato per settare i dati di previsione
            double d = data.get(i);

            if(d != Double.NEGATIVE_INFINITY){
                fragmentedSeries.get(idxFragment).add(new BarEntry(referenceTime + (i * DayInLong), (float) d));
            }
            else {
                fragmentedSeries.add(new ArrayList<>());
                idxFragment++;
            }

        }


        return fragmentedSeries;
    }


    /**
     * Per le misurazioni per visualizzare i singoli valori scollegati
     * creo una serie di dati diversa per ogni valore
     * @param data
     * @param startDate
     * @return
     * @throws ParseException
     */
    protected ArrayList<ArrayList<Entry>> buildDataSeriesMeasurement(ArrayList<Double> data, String startDate) throws ParseException{



        int idxFragment = 0;
        ArrayList<ArrayList<Entry>> fragmentedSeries = new ArrayList<>();

        long referenceTime = getReferceTime(startDate);


        fragmentedSeries.add(new ArrayList<>());
        for(int i = 0; i < data.size(); i++){

            //se il metodo è stato chiamato per settare i dati di previsione
            double d = data.get(i);

            if(d != Double.NEGATIVE_INFINITY){
                fragmentedSeries.get(idxFragment).add(new BarEntry(referenceTime + (i * DayInLong), (float) d));
                fragmentedSeries.add(new ArrayList<>());
                idxFragment++;
            }
            else {
                fragmentedSeries.add(new ArrayList<>());
                idxFragment++;
            }

        }


        return fragmentedSeries;

    }

    protected boolean checkDayAhead(LocalDate dataCur, LocalDate dataNext) throws Exception {

        if(AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()){
            Period period = Period.between(dataCur, dataNext);
            if(ChronoUnit.DAYS.between(dataCur, dataNext) > 0) return true;

            return false;
        }
        else throw new Exception();//caso in cui la versione android sia inferiore

    }

    protected int getDayAhead(LocalDate dataCur, LocalDate dataNext) {
        return (int) ChronoUnit.DAYS.between(dataCur, dataNext);
    }
    /**
     * TODO: Da sistemare i casi in cui potrebbe arrivare una exception
     *
     * @param d
     * @return
     */
    protected LocalDate stringToLocalDate(String d) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(d, formatter);

    }
    protected String LocalDateToString(LocalDate d) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return d.format(formatter);

    }
    protected static String getCurrentTimezoneOffset() {

        TimeZone tz = TimeZone.getDefault();
        int offsetInMillis = tz.getRawOffset();
        return String.valueOf( offsetInMillis/(1000*3600));
    }

    protected boolean checkDate(String start, String end, boolean flag, String current) {

        if (AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate S = null;
            LocalDate F = null;

            if(flag) {//sto settando la data di inizio

                S = LocalDate.parse(current, formatter);
                F = LocalDate.parse(end, formatter);
            }
            else {//sto settando la data di fine

                S = LocalDate.parse(start, formatter);
                F = LocalDate.parse(current, formatter);
            }
            try {
                return checkDayAhead(S, F);
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }


    /**
     * Metodo per aggiungere la leggenda al grafico
     */
    protected void setLegend(LineChart newGraph){

        int numeroDiSerie = 3;
        int maxLen = 0;
        Legend legend = newGraph.getLegend();
        LegendEntry[] legendEntries = new LegendEntry[numeroDiSerie];

        for(int i = 0; i<numeroDiSerie; i++) {

            LegendEntry entry = new LegendEntry();





            switch (i){
                case 0:
                    entry.formColor = getColor(R.color.predictionColor);
                    entry.label = "Previsione";
                    entry.form = Legend.LegendForm.LINE;
                    entry.formLineWidth = 2f;
                    entry.formSize = 20f;
                    break;
                case 1:

                    entry.formLineDashEffect = new DashPathEffect(new float[]{10f, 5f}, 0f);
                    entry.formColor = getColor(R.color.trackingColor);
                    entry.form = Legend.LegendForm.LINE;
                    entry.label = "Tracking";
                    entry.formLineWidth = 2f;
                    entry.formSize = 20f;
                    break;
                case 2:

                    entry.formColor = getColor(R.color.observationsColor);
                    entry.label = "Osservazioni";
                    entry.form = Legend.LegendForm.CIRCLE;
                    entry.formSize = 12f;
                    break;

            }
            maxLen = Math.max(maxLen + 8, entry.label.length());
            legendEntries[i] = entry;
        }

        legend.setCustom(legendEntries);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(true);
        legend.setYOffset(0f);
        legend.setXOffset(maxLen);//moltiplico per 5 per convertire in DP
        legend.setYEntrySpace(0f);
        legend.setTextSize(10f);

    }

    protected void custimizeGraphicData(float lineWidth, float circleRadius, boolean enable, float lineLength, float spaceLengthm, int color, LineDataSet set, String namedata) {

        switch (namedata){

            case "Tracking" :
                set.setColor(color);
                set.setLineWidth(lineWidth);
                set.setDrawCircles(false);
                set.setDrawValues(false);

                if(enable)set.enableDashedLine(lineLength, spaceLengthm, 0);
                break;
            case "prediction" :
                set.setColor(color);
                set.setCircleColor(color);
                set.setCircleHoleColor(color);
                set.setLineWidth(lineWidth);
                set.setCircleRadius(circleRadius);
                if(enable)set.enableDashedLine(lineLength, spaceLengthm, 0);
                break;
            case "Observations" :

                set.setDrawCircles(true);
                set.setColor(color);
                set.setCircleColor(color);
                set.setCircleHoleColor(color);
                set.setLineWidth(0f);
                set.setCircleRadius(circleRadius);

                break;
            case "LowerUpper" :
                set.setColor(color);
                set.setCircleColor(color);
                set.setCircleHoleColor(color);
                set.setLineWidth(lineWidth);
                set.setCircleRadius(circleRadius);
                if(enable)set.enableDashedLine(lineLength, spaceLengthm, 0);
                break;
        }



    }
    protected String fixData(int d){
        if(d < 10) return "0"+d;
        return String.valueOf(d);
    }

    /**
     * Recupero la data di inizio diminuita di 20 giorni per
     * effettuare le chiamate al server
     * @param day
     * @param month
     * @param year
     * @return
     */
    protected String buildDataForServer(int day, int month, int year){

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MONTH, month - 1);//sottraggo 1 al mese perche calendar conta i mesi a partire da 0
        calendar.set(Calendar.YEAR, year);

        calendar.add(Calendar.DAY_OF_MONTH, -20);//diminuisco la data di 20 giorni TODO: da verificare se basta anche meno


        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String tmp = format.format(calendar.getTime());
        return tmp;


    }
    protected int retreiveDayAhead(String endData) {

        //TODO da sistemare

        int dAhead = -1;
        if(AndroidVersionSingleton.getInstance().getAndroidVersioneFlag()){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localNow = LocalDate.parse(now, formatter);
            int tmp = getDayAhead(localNow.minusDays(1), LocalDate.parse(endData, formatter));

            dAhead = tmp <= 0 ? 1 : tmp;
        }
        return dAhead;
    }

    /**
     *  Metodo per effettuare realmente la predizione
     * @param A
     * @param B
     * @param u
     * @param valoreMedia
     * @param Σ_n
     * @param varianzaInit
     * @return
     */
    protected static Object [] prediction(Matrix A, Matrix B, Matrix u, Matrix valoreMedia, Matrix Σ_n, Matrix varianzaInit) {

        Matrix media = null;
        Matrix varianza = null;

        Matrix Bu = B.times(u);
        Matrix I = Matrix.identity(u.getRowDimension(), u.getRowDimension());
        Matrix tmp = EMAlgorithm.diag(u);
        Matrix A1 = I.minus(A.times(tmp));
        media = A1.times(valoreMedia).plus(Bu);
        varianza  = A1.times(varianzaInit).times(A1.transpose()).plus(Σ_n);

        return new Object[]{media, varianza};
    }

    /**
     * Metodo per avviare la predizione richiamabilie sia all interno di training che
     * al di fuori
     *
     * @param lambda
     * @param muHat
     * @param Vhat
     * @param availableT
     * @param U
     * @param Y
     * @return
     */
    protected Object [] doPrediction(int lambda, Matrix muHat, Matrix [] Vhat, int availableT, Matrix U, Matrix Y) {

        Model m = Model.getInstance();


        int q = m.getMu0().getRowDimension();

        Matrix my_muhat = new Matrix(q, lambda + 1);
        Matrix [] my_varhat = init3DMatrix(q, q, lambda+1);


        my_muhat.setMatrix(0, my_muhat.getRowDimension()-1, 0, 0, muHat.getMatrix(0, muHat.getRowDimension()-1, availableT, availableT));
        my_varhat[0] = Vhat[availableT];

        for(int i = 0; i<lambda; i++){
            Object [] ret = prediction(m.getA(), m.getB(), U.getMatrix(0, U.getRowDimension()-1, availableT, availableT),
                    my_muhat.getMatrix(0, my_muhat.getRowDimension()-1, i, i), m.getΣ_n(), my_varhat[i]);

            my_muhat.setMatrix(0, my_muhat.getRowDimension()-1, i+1, i+1, (Matrix) ret[0]);
            my_varhat[i+1] = (Matrix) ret[1];
        }

        my_muhat.setMatrix(0, my_muhat.getRowDimension()-1, 0, 0, new Matrix(my_muhat.getRowDimension(), my_muhat.getColumnDimension()));
        Matrix pred = m.getC().times(my_muhat);

        for(int i = 0; i<pred.getRowDimension(); i++){
            for(int s = 0; s<pred.getColumnDimension(); s++){
                if(pred.get(i, s) < 0) {pred.set(i, s, 0.0);}
            }
        }
        return new Object[] {my_varhat, pred, q};
    }
    protected void makeNewPoint(double temperature, double irradiance, LineData allListData,
                                LineChart newGraph, double lastTracking, Context activity, LineDataSet setPrediction, float idxLatTrack) throws ParseException{

        DataSavedToPredict data = DataSavedToPredict.getInstance();

        Matrix u = data.getU();
        u.set(0, data.getAvailableT(), temperature);
        u.set(1, data.getAvailableT(), irradiance);


        Object [] res = doPrediction(data.getLambda(), data.getMuHat() , data.getVhat(), data.getAvailableT(), u, data.getY());
        Matrix pred = (Matrix) res[1];

        ArrayList<Double> newPrediction = new ArrayList<>();
        newPrediction.add(lastTracking);
        newPrediction.add(pred.get(0, 1));

        if(lastNewDataSet != null) {
            allListData.removeDataSet(lastNewDataSet);
        }

        lastNewDataSet = (LineDataSet)buildDataSeriesPrediction(newPrediction, idxLatTrack, endDataSelected, "Prediction", Color.GREEN, true);


        lastNewDataSet.setFillFormatter(new MyFillFormatter(setPrediction));
        newGraph.setRenderer(new MyLineLegendRenderer(newGraph, newGraph.getAnimator(), newGraph.getViewPortHandler()));

        if(pred.get(0, 1) < data.getLastPrediction()){
            lastNewDataSet.setFillDrawable(ContextCompat.getDrawable(activity, R.drawable.my_graph_gradient_negative));
        }
        else if(pred.get(0, 1) > data.getLastPrediction()){
            lastNewDataSet.setFillDrawable(ContextCompat.getDrawable(activity, R.drawable.my_graph_gradient_positive));
        }
        else {
            lastNewDataSet.setFillDrawable(null);
        }

        allListData.addDataSet(lastNewDataSet);


        newGraph.setData(allListData);

        XAxis xAxis = newGraph.getXAxis();
        xAxis.setValueFormatter(new DateValueFormatter());
        xAxis.setDrawLabels(false);

        //newGraph.getLegend().setEnabled(false);
        newGraph.setMarker(new MyMarkerView(activity, R.layout.pointer_popup, allListData, newGraph));

        lastNewDataSet.setDrawFilled(true);
        newGraph.invalidate();


    }
    /**
     * Metodo usato per salvare i dati per effettuare previsione
     */
    protected void saveDataToPredict(int lambda, int availableT, Matrix muHat, Matrix [] Vhat, Matrix y, Matrix u) {

        DataSavedToPredict dataToPredict = DataSavedToPredict.getInstance();

        dataToPredict.setLambda(lambda);
        dataToPredict.setAvailableT(availableT);
        dataToPredict.setMuHat(muHat);
        dataToPredict.setVhat(Vhat);
        dataToPredict.setY(y);
        dataToPredict.setU(u);
        dataToPredict.setSaved(true);

    }

    /**
     * Controllo in base a quanti canali sono presenti se le risposte del server sono
     * state ricevute tutte
     * @param expectedRequest
     * @param idResponse
     * @param id1
     * @param id2
     */
    protected void checkResponseServer(int expectedRequest, String idResponse, String id1, String id2){

        switch (expectedRequest){

            case 1:
                if(id1 != null){
                    if(idResponse.equals(id1)){
                        checkRetreiveChannel[IdxChannel.IDX_channel1.ordinal()] = true;
                    }
                }
                else if(id2 != null){
                    if(idResponse.equals(id2)) {
                        checkRetreiveChannel[IdxChannel.IDX_channel2.ordinal()] = true;
                    }
                }
                break;
            case 2:
                if(idResponse.equals(id1)){
                    checkRetreiveChannel[IdxChannel.IDX_channel1.ordinal()] = true;
                }
                else if(idResponse.equals(id2)) {
                    checkRetreiveChannel[IdxChannel.IDX_channel2.ordinal()] = true;
                }
                break;
        }
    }

    /**
     * Implemento un metodo che mi consente di recuperare l'id del canale
     * che è stato usato per recuperare uno o piu fileds quando ho scaricato
     * dal server i dati
     */
    protected String retreiveIdFromField(String field, HashMap<String, List<EntryList>> myDataStructName ){


        Iterator<String> iterator =  myDataStructName.keySet().iterator();
        while(iterator.hasNext()){

            String ch = iterator.next();
            myDataStructName.get(ch);
            for(EntryList entry : myDataStructName.get(ch)) {
                if(entry.getText().equals(field)) return ch;
            }
        }

        return null;
    }

    protected void insertOnDatabase(int idxDate, ArrayList<String> dateData, HashMap<String, List<EntryList>> myDataStructName,
                                    HashMap<String, Matrix> fieldMeans, Context appContext, SingleAdapter adapterPrevisione){
        AppDatabase database = AppDatabase.getDataBase(appContext);
        for(int i = 0; i <= idxDate; i++){

            //non inserisco valori assenti
            /*if(fieldMeans.get("Previsione").get(0, i) == Double.NEGATIVE_INFINITY) {
                continue;
            }*/

            Mean meanTemperature = new Mean();
            Mean meanIrradiance = new Mean();
            Mean meanChoice = new Mean();

            //setto la data e la formatto per poterla usare nel db
            String dataFormatted = changeFormatDate(dateData.get(i), "dd/MM/yyyy", "yyyy-MM-dd");

            meanTemperature.setData(dataFormatted);
            meanIrradiance.setData(dataFormatted);
            meanChoice.setData(dataFormatted);

            //setto id canale
            meanTemperature.setIdChannel(retreiveIdFromField("temperature", myDataStructName));
            meanIrradiance.setIdChannel(retreiveIdFromField("irradiance", myDataStructName));
            meanChoice.setIdChannel(retreiveIdFromField(adapterPrevisione.getCheckedItems().get(0), myDataStructName));

            //setto il fieldName
            meanTemperature.setFieldName("temperature");
            meanIrradiance.setFieldName("irradiance");
            meanChoice.setFieldName(adapterPrevisione.getCheckedItems().get(0));

            //setto il valore della media
            meanTemperature.setField(String.valueOf(fieldMeans.get("Controllo").get(0, i)));
            meanIrradiance.setField(String.valueOf(fieldMeans.get("Controllo").get(1, i)));

            Double v = fieldMeans.get("Previsione").get(0, i);
            if(v == Double.NEGATIVE_INFINITY){
                meanChoice.setField("no");
            }
            else meanChoice.setField(String.valueOf(v));


            //inserisco nel db
            database.insertAllMean(meanTemperature);
            database.insertAllMean(meanIrradiance);
            database.insertAllMean(meanChoice);

        }

    }

    /**
     * Resetto la grafica della toolbar quando si torna
     * all'activity main
     */
    protected void resetToolBarButton(BottomNavigationView ToolBar_buttons){
        int size = ToolBar_buttons.getMenu().size();
        for (int i = 0; i < size; i++) {
            ToolBar_buttons.getMenu().getItem(i).setCheckable(false);
        }
    }

    /**
     * Inizializzo l'hashmap da usare per capire se recuperare i
     * nomi dei field da uno solo o entrambi i canali
     * @param ID_1
     * @param ID_2
     * @return
     */
    protected HashMap<Integer, Boolean> buildCheckID(String ID_1, String ID_2){

        HashMap<Integer, Boolean> flag = new HashMap<>();
        if(ID_1 != null) flag.put(0, true);
        else flag.put(0, false);

        if(ID_2 != null)flag.put(1, true);
        else flag.put(1, false);

        return flag;
    }

    public enum TypeGerericFunction{
        IntegerType, ViewType, OnlySelectionPrevition
    }
    enum IdxChannel {
        IDX_channel1, IDX_channel2
    }

}
