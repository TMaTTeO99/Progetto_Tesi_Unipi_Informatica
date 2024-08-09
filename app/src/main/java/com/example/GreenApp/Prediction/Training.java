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
    private String endData;
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
        double tol = 1e-4;


        /**
         * availableT : variabile usata per fermare i dati di controllo all'ultima data disponibile
         *              quando dovrò effettuare i calcoli nel futuro
         */
        int availableT = 3;
        int dAhead = retreiveDayAhead(endData);

        Matrix support = new Matrix(1, 3);
        support.set(0, 0, 1);
        support.set(0, 1, 3);
        support.set(0, 2, 5);

        Double prediction = 0.0;
        ArrayList<Matrix> predictionUpper = new ArrayList<>();
        ArrayList<Matrix> predictionLower = new ArrayList<>();
        ArrayList<Double> track = new ArrayList<>();


        availableT = daysAhead;
        lambda = dAhead;

        Matrix U = matrixControll.getMatrix(0, matrixControll.getRowDimension()-1, 0, matrixPrevisione.getColumnDimension()-1);
        Matrix Y = matrixPrevisione.getMatrix(0, matrixPrevisione.getRowDimension()-1, 0, matrixPrevisione.getColumnDimension()-1);


        int dimY = Y.getColumnDimension();
        int countPositiveValue = 0, idxtmp = 0;

        for(int i = 0; i<Y.getColumnDimension(); i++){

            if(Y.get(0, i) != Double.NEGATIVE_INFINITY){
                support.set(0, idxtmp, i+1);
                countPositiveValue++;
                idxtmp++;
            }
            if(idxtmp == 3)break;
        }

        if(dimY > 3 && countPositiveValue > 3) {

            //inizializzo le variabili necessarie
            ArrayList<Double> llh_new = new ArrayList<>();
            Matrix startSupp = new Matrix(1, 3); //una riga e 3 colonne
            Random rand = new Random();

            //provo 100 volte a calcolare la llh
            for(int i = 0; i<100; i++){

                //eseguo un ciclo che prova a trovare 3 punti iniziali che
                //rendono i parametri del modello dei parametri con valori finiti
                for(int safeIter = 0; safeIter < 20; safeIter++){

                    //calcolo casualmente 3 punti
                    int a = rand.nextInt(dimY / 3) + 1;
                    int b = rand.nextInt(dimY / 3) + (dimY / 3) ;
                    int c = rand.nextInt(dimY / 3) + ((2 * dimY) / 3) ;
                    startSupp.set(0, 0, a);
                    startSupp.set(0, 1, b);
                    startSupp.set(0, 2, c);

                    //controllo se i punti trovati rendono i parametri del modello
                    //dei parametri con valori finiti
                    if(EMAlgorithm.retreiveFirstPoint(Y, U, startSupp)) {
                        //se i valori sono finiti esco e salvo i 3 punti precedentemente trovati
                        support = startSupp.getMatrix(0,startSupp.getRowDimension()-1, 0, startSupp.getColumnDimension() - 1);
                        break;
                    }
                }

                //inizializzo i parametri del modello usando i 3 punti recuperati
                EMAlgorithm.InitEm(Y, U, support);

                //calcolo la llh
                Object[] res = EMAlgorithm.doForWardStep(Y, U);
                double llhVal = (double) res[1];

                //la salvo in una lista
                llh_new.add(llhVal);

                //se sono alla terza iterazione
                //posso controllare la condizione di llh massima
                if(i > 1 && llh_new.get(i) - llh_new.get(i-1) < tol*Math.abs(llh_new.get(i-1))) break;

                //ripeto
            }

        }

        EMAlgorithm.InitEm(Y, U, support);

        Matrix llh = new Matrix(1, maxIter);
        for(int idxllh = 0; idxllh < maxIter; idxllh++){llh.set(0, idxllh, Double.NEGATIVE_INFINITY);}


        Matrix muHat = null;
        Matrix [] Vhat = null;
        Matrix [] Ezy = null;
        Matrix [] Ezz = null;
        Matrix [] Dzy = null;
        Matrix [] Dzz = null;
        int maxitercount = 0;
        for(int i = 0; i<maxIter; i++) {

            maxitercount++;
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
        saveDataToPredict(lambda, availableT, muHat, Vhat, Y, U);

        Model m = Model.getInstance();
        Matrix matrixTrak = m.getC().times(muHat);


        //recupero il traking
        for(int i = timeMin; i <= daysAhead; i++){
            track.add(matrixTrak.get(0,i));
        }
        
        Object [] res = doPrediction(lambda, muHat, Vhat, availableT, U, Y);

        Matrix [] my_varhat = (Matrix []) res[0];
        Matrix pred = (Matrix)res[1];

        int d = Y.getRowDimension();

        ArrayList<Double> listPredUpper = new ArrayList<>();
        ArrayList<Double> listPredLower = new ArrayList<>();

        for(int i = 0; i<lambda; i++){
            Matrix arg = m.getC().times(my_varhat[i]).times(m.getC().transpose());

            for(int k = 0; k<arg.getRowDimension(); k++){
                for(int l = 0; l<arg.getColumnDimension(); l++){

                    if(arg.get(k,l) <= 0.0) arg.set(k, l, 0.0);
                    else arg.set(k, l, Math.sqrt(arg.get(k,l)));

                    listPredUpper.add(pred.get(0, pred.getColumnDimension()-1) + arg.get(k,l));
                    listPredLower.add(pred.get(0, pred.getColumnDimension()-1) - arg.get(k,l));
                }
            }
        }


        prediction = pred.get(0, pred.getColumnDimension()-1);
        listener.finishTraining(false, prediction, track, matrixPrevisione, dAhead, listPredUpper.get(listPredUpper.size()-1), listPredLower.get(listPredLower.size()-1));

    }


    public void setDaysAhead(int daysAhead) { this.daysAhead = daysAhead;}
    public void setTimeMin(int timeMin) {this.timeMin = timeMin;}
    public void setEndData(String d) {this.endData = d;}
    public void setMatrixControll(Matrix matrixControll){this.matrixControll = matrixControll;}
    public void setMatrixPrevisione(Matrix matrixPrevisione){this.matrixPrevisione = matrixPrevisione;}
    public void setMeasurementDate(ArrayList<String> measurementDate){this.measurementDate = measurementDate;}
}
