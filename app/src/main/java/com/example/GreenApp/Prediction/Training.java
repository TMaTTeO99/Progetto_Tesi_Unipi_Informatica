package com.example.GreenApp.Prediction;

import static com.example.GreenApp.Prediction.Utils.EMAlgorithm.diag;
import static com.example.GreenApp.Prediction.Utils.EMAlgorithm.init3DMatrix;
//import static com.example.GreenApp.Prediction.Utils.EMAlgorithm.prediction;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.GreenApp.Prediction.Data_Structs.AndroidVersionSingleton;
import com.example.GreenApp.Prediction.Data_Structs.DataContainer;
import com.example.GreenApp.Prediction.Data_Structs.Model;
import com.example.GreenApp.Prediction.Utils.EMAlgorithm;
import com.example.GreenApp.Prediction.Data_Structs.ListAdapter;
import com.example.GreenApp.Prediction.Utils.TrainingCallBack;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import Jama.Matrix;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Training extends MyBaseActivity implements Runnable{

    private int daysAhead;
    private int timeMin = 5;
    private String startDate;
    private String endData;
    private List<String> listaControllo;
    private List<String> listaPrevisione;
    protected HashMap<String, ArrayList<DataContainer>> myDataStructDataReal;
    private TrainingCallBack listener;
    private Matrix matrixControll = null;
    private Matrix matrixPrevisione = null;
    private ArrayList<String> measurementDate = null;

    public Training(TrainingCallBack listener) {
        this.listener = listener;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {

        //setto le variabili temporali
        now = getToday();
        yesterday = getYesterday();

        //costruisco la matrice di controllo
        int maxIter = 100;//iterazioni massima per EM_algoritm
        int lambda = 1;
        //int timeMin = 5;
        double tol = 1e-4;

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timeMin = measurementDate.indexOf(startDate) - 1; //-1 perche per predire dal giorno selezionato dall utente parto da quello prima
        }

        if((daysAhead = measurementDate.indexOf(endData)) == -1) {
            daysAhead = measurementDate.indexOf(yesterday);
        }
        else daysAhead -= 1; //devo sottrarre 1 perche devo predire un giorno che è
                             //presente fra i miei dati quindi mi fermo il giorno prima

         */
        /**
         * availableT : variabile usata per fermare i dati di controllo all'ultima data disponibile
         *              quando dovrò effettuare i calcoli nel futuro
         */
        int availableT = 3;
        int dAhead = retreiveDayAhead(endData);

        boolean init = true;//variabile flag per capire se devo ancora recuperare i dati per init


        Matrix support = new Matrix(1, 3);
        support.set(0, 0, 1);
        support.set(0, 1, 3);
        support.set(0, 2, 5);

        Double prediction = 0.0;
        ArrayList<Matrix> predictionUpper = new ArrayList<>();
        ArrayList<Matrix> predictionLower = new ArrayList<>();
        ArrayList<Double> track = new ArrayList<>();

        /**
         * Gestisco il caso in cui l'utente abbia inserito una data precedente a quella di ieri
         */


        //boolean checkOnlytraking = getDayAhead(stringToLocalDate(yesterday), stringToLocalDate(measurementDate.get(measurementDate.size() - 1))) < 0;

        //for(int T = timeMin; T <= daysAhead; T += lambda) {


            //if(T <= measurementDate.indexOf(yesterday) || checkOnlytraking){


        //availableT = measurementDate.indexOf(yesterday);
                //availableT = T;
        availableT = daysAhead;
        lambda = dAhead;

                //if(T == measurementDate.indexOf(yesterday))lambda = dAhead;
            //}
            /*if(T == measurementDate.indexOf(yesterday)) {
                System.out.println();
            }
            */
        Matrix U = matrixControll.getMatrix(0, matrixControll.getRowDimension()-1, 0, matrixPrevisione.getColumnDimension()-1);
        Matrix Y = matrixPrevisione.getMatrix(0, matrixPrevisione.getRowDimension()-1, 0, matrixPrevisione.getColumnDimension()-1);


        int dimY = Y.getColumnDimension();
            //if(init){

        if(dimY <= 3){
            support.set(0, 0, 1);
            support.set(0, 1, 2);
            support.set(0, 2, 3);
        }
        else {

            Matrix startSupp = new Matrix(1, 3); //una riga e 3 colonne
            Random rand = new Random();

            for(int safeIter = 0; safeIter < 20; safeIter++){

                int a = rand.nextInt(dimY / 3) + 1;
                int b = rand.nextInt(dimY / 3) + (dimY / 3) ;
                int c = rand.nextInt(dimY / 3) + ((2 * dimY) / 3) ;
                startSupp.set(0, 0, a);
                startSupp.set(0, 1, b);
                startSupp.set(0, 2, c);

                if(EMAlgorithm.retreiveFirstPoint(Y, U, startSupp)) {
                    support = startSupp.getMatrix(0,startSupp.getRowDimension()-1, 0, startSupp.getColumnDimension() - 1);
                    break;
                }

            }
        }
                //init = false;
            //}



        EMAlgorithm.InitEm(Y, U, support);
        Matrix llh = new Matrix(1, maxIter);
        for(int idxllh = 0; idxllh < maxIter; idxllh++){llh.set(0, idxllh, Double.NEGATIVE_INFINITY);}


        Matrix muHat = null;
        Matrix [] Vhat = null;
        Matrix [] Ezy = null;
        Matrix [] Ezz = null;
        Matrix [] Dzy = null;
        Matrix [] Dzz = null;

        for(int i = 0; i<maxIter; i++) {

            Object [] ret = EMAlgorithm.EStep(Y, U);
            muHat = (Matrix) ret[3];
            Vhat = (Matrix []) ret[4];
            Ezy = (Matrix []) ret[5];
            Ezz = (Matrix []) ret[6];
            Dzy = (Matrix []) ret[7];
            Dzz = (Matrix []) ret[8];
            llh.set(0, i, (double)ret[9]);

            if(i > 1 && llh.get(0,i) - llh.get(0, i-1) < tol*Math.abs(llh.get(0, i-1))) break;

            EMAlgorithm.MStep(Y, U, muHat, Ezy, Ezz, Dzy, Dzz);
        }

        /**
         * Salvo i valori necessari ad effettuare la predizione
         * del valore nel futuro, controllo se sono nel caso in cui
         * devo predirer il valore nel futuro, in tal caso i dati
         * parametri del modello sono pronti
         */
        //if(T == measurementDate.indexOf(yesterday)){
        saveDataToPredict(lambda, availableT, muHat, Vhat, Y, U);
        //}

        Model m = Model.getInstance();
        Matrix matrixTrak = m.getC().times(muHat);


        //recupero il traking
        for(int i = timeMin; i <= daysAhead; i++){
            track.add(matrixTrak.get(0,i));
        }


        Object [] res = doPrediction(lambda, muHat, Vhat, availableT, U, Y);

        //Matrix [] my_varhat = (Matrix []) res[0];
        Matrix pred = (Matrix)res[1];

            /*Model m = Model.getInstance();

            int d = Y.getRowDimension();
            Matrix pred_upper = new Matrix(d, lambda);
            Matrix pred_lower = new Matrix(d, lambda);

            for(int i = 0; i<lambda; i++){
                Matrix arg = m.getC().times(my_varhat[i]).times(m.getC().transpose());

                for(int k = 0; k<arg.getRowDimension(); k++){
                    for(int l = 0; l<arg.getColumnDimension(); l++){

                        if(arg.get(k,l) <= 0.0) arg.set(k, l, 0.0);
                        else arg.set(k, l, Math.sqrt(arg.get(k,l)));

                    }
                }
                pred_upper.setMatrix(0, pred_upper.getRowDimension()-1, i,i, pred.getMatrix(0, pred.getRowDimension()-1, i,i).plus(diag(arg)));
                pred_lower.setMatrix(0, pred_lower.getRowDimension()-1, i,i, pred.getMatrix(0, pred.getRowDimension()-1, i,i).minus(diag(arg)));
            }

            System.out.println("PREDICTION: " + pred.get(0, 1));*/


        prediction = pred.get(0, 1);
        //predictionUpper.add(pred_upper);
        //predictionLower.add(pred_lower);


        //}

        listener.finishTraining(false, prediction, track, matrixPrevisione, dAhead);

    }

    public void setAdapterControllo(List<String> adp) {
        this.listaControllo = adp;
    }
    public void setAdapterPrevisione(List<String> adp) {
        this.listaPrevisione = adp;
    }
    public void setMyDataStructDataReal(HashMap<String, ArrayList<DataContainer>> myDataStructDataReal){
        this.myDataStructDataReal = myDataStructDataReal;
    }
    public void setDaysAhead(int daysAhead) { this.daysAhead = daysAhead;}
    public void setTimeMin(int timeMin) {this.timeMin = timeMin;}
    public void setStartDate (String d) {this.startDate = d;}
    public void setEndData(String d) {this.endData = d;}
    public void setMatrixControll(Matrix matrixControll){this.matrixControll = matrixControll;}
    public void setMatrixPrevisione(Matrix matrixPrevisione){this.matrixPrevisione = matrixPrevisione;}
    public void setMeasurementDate(ArrayList<String> measurementDate){this.measurementDate = measurementDate;}
}
