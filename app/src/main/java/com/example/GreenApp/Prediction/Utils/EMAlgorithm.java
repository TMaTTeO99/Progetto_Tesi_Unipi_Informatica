package com.example.GreenApp.Prediction.Utils;

import static org.apache.commons.math4.core.jdkmath.AccurateMath.PI;
import static org.apache.commons.math4.core.jdkmath.AccurateMath.abs;
import static org.apache.commons.math4.core.jdkmath.AccurateMath.exp;
import static org.apache.commons.math4.core.jdkmath.AccurateMath.log;
import static org.apache.commons.math4.core.jdkmath.AccurateMath.pow;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.isFinite;


import android.view.Display;

import com.example.GreenApp.Prediction.Data_Structs.Model;
import com.example.GreenApp.Prediction.MyBaseActivity;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Objects;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * @author Matteo Torchia 599899
 */
public class EMAlgorithm {


    /**
     * metodo di supporto per la selezione corretta dei valori
     * per l'inizializzazione dell'algoritmo
     */

    /*
     *    Σ_n == G
     *    Σ == P0
     *    S == Σ_w
     * */

    ////////////****************************************************//////////////
    public static boolean retreiveFirstPoint(Matrix Y, Matrix U, Matrix s) {

        int k = U.getRowDimension();
        int d = 1; //dimensione delle osservazioni (U.getRowDimension() potrei usare <-- per essere generale)

        double epsilon = 1e-6; //10 ^ -6
        int nmax = 10;

        Matrix tmp_I = Matrix.identity(k, k).times(epsilon);
        Matrix Σ_n = tmp_I;
        Matrix Σ = tmp_I;
        Matrix Σ_w = Matrix.identity(d, d).times(epsilon);

        Matrix tmp_2 = new Matrix(d, k);
        for(int i = 0; i<d; i++) {
            for(int j = 0; j<k; j++) {
                tmp_2.set(i, j, (double) 1/k);
            }
        }
        Matrix C = tmp_2;

        Matrix Mu0 = C.transpose()
                        .times(C.times(C.transpose()).inverse())
                        .times(Y.get(0, (int)(s.get(0, 0) - 1.0)));


        Matrix A = null;
        Matrix B = null;
        Matrix D = null;
        Matrix meanRows = new Matrix(U.getRowDimension(), 1);

        for(int i =0 ; i<U.getRowDimension(); i++) {
            for(int j = 0; j<U.getColumnDimension(); j++) {
                meanRows.set(i, 0, meanRows.get(i, 0) + U.get(i, j));
            }
            meanRows.set(i, 0, meanRows.get(i, 0) / U.getColumnDimension());
        }
        D = diag(meanRows);

        double m = 0;
        double alpha = 0;
        double beta = 0;
        double t0 = 0;
        double kappa = getRealKappa(Y);
        int iter = 0;
        for(int i = 0; i<nmax; i++) {

            iter++;
            double t1 = (Y.get(0, (int) (s.get(0, 1) - 1.0)));
            double argA = 1.0 - (t1 / kappa);
            if(argA > 0) {
                alpha = log(argA);
            }
            double t2 = (Y.get(0, (int) (s.get(0, 2) - 1.0)));
            double argB = 1.0 - (t2 / kappa);
            if(argB > 0) {
                beta = log(argB);
            }
            m = (alpha - beta)/((int) (s.get(0, 2)) - (int) (s.get(0, 0)));
            t0 = (alpha / m) + (int) (s.get(0, 0));

            double den = (1.0 - exp(-m * ( (s.get(0, 0)) - t0)));
            double temp = Y.get(0, (int) (s.get(0, 0) - 1.0)) / den;
            if(temp > 2 || (abs(temp - kappa) / kappa) < 1e-3) {
                break;
            }
            else kappa = temp;
        }

        A = D.inverse().times(m);
        B = A.times(kappa);

        if(isFinite(kappa) && isFinite(m) && isFinite(t0))return true;

        return false;

    }
    private static double computeLLH(Matrix y, Matrix u, Matrix mu, Matrix v, Matrix A, Matrix B, Matrix Σ_n
            , Matrix C, Matrix Σ_w, Matrix I) {

        Matrix A1 = I.minus(A.times(diag(u)));


        Matrix P = A1.times(v).times(A1.transpose()).plus(Σ_n);
        Matrix PC = P.times(C.transpose());
        Matrix R = C.times(PC).plus(Σ_w);
        Matrix K = null;
        double llh = 0.0;

        boolean flagObservation = y.get(y.getRowDimension() - 1, 0) == NEGATIVE_INFINITY;
        if(flagObservation){
            K = C.transpose().times(0);
        }
        else {
            K = PC.times(R.inverse());
        }
        Matrix Amu = A1.times(mu).plus(B.times(u));
        Matrix CAmu = C.times(Amu);
        Matrix tmpmu = null;
        if(flagObservation) {
            tmpmu = Amu;
            llh = NEGATIVE_INFINITY;
        }
        else {
            tmpmu = Amu.plus(K.times(y.getMatrix(0, y.getRowDimension()-1, 0, 0).minus(CAmu)));
            llh =   log(multivarn(y.getMatrix(0, y.getRowDimension()-1, 0, 0), C.times(tmpmu), Σ_w)) +
                    log(multivarn(tmpmu, Amu, Σ_n));
        }
        return  llh;
    }

    //////////***************************************************///////////////////

    /**
     * init EM
     *
     * @param Y osservazioni
     * @param U dati di controllo
     * @param s vettore di supporto
     *          816869 canale nuovo
     */

    public static void InitEm(Matrix Y, Matrix U, Matrix s) {//s è un vettore


        int k = U.getRowDimension();
        int d = 1; //dimensione delle osservazioni (U.getRowDimension() potrei usare <-- per essere generale)

        double epsilon = 1e-6; //10 ^ -6
        int nmax = 10;

        Model model = Model.getInstance();//recupero il modello
        /*
        *    Σ_n == G
        *    Σ == P0
        *    S == Σ_w
        * */

        //matrici di covarianza
        Matrix tmp = Matrix.identity(k, k).times(epsilon);
        model.setΣ_n(tmp);
        model.setΣ(tmp);
        model.setΣ_w(Matrix.identity(d, d).times(epsilon));

        /**
         * Stampa di test
         */

        model.printTestMatrix(model.getΣ_n(), "Σ_n == G");
        model.printTestMatrix(model.getΣ_w(), "Σ_w == S");
        model.printTestMatrix(model.getΣ(), "Σ == P0");


        Matrix tmp_2 = new Matrix(d, k);
        for(int i = 0; i<d; i++) {
            for(int j = 0; j<k; j++) {
                tmp_2.set(i, j, (double) 1/k);
            }
        }
        model.setC(tmp_2);

        /**
         * Stampa di test
         */
        model.printTestMatrix(model.getC(), "C");


        model.setMu0(
                model.getC().transpose()
                        .times(model.getC().
                                times(model.getC().transpose()).inverse())
                        .times(Y.get(0, (int)(s.get(0, 0) - 1.0))));
        /*
        model.setMu0(model.getC().transpose().times(
                model.getC().times(model.getC().transpose()).inverse()
        ).times(Y.getMatrix(0, Y.getRowDimension() - 1, (int)(s.get(0,0) - 1), (int)(s.get(0,0) - 1))));
         */
        /**
         * Stampa di test
         */
        model.printTestMatrix(model.getMu0(), "MU0");


        Matrix D = null;
        Matrix meanRows = new Matrix(U.getRowDimension(), 1);

        for(int i =0 ; i<U.getRowDimension(); i++) {
            for(int j = 0; j<U.getColumnDimension(); j++) {
                meanRows.set(i, 0, meanRows.get(i, 0) + U.get(i, j));
            }
            meanRows.set(i, 0, meanRows.get(i, 0) / U.getColumnDimension());
        }
        D = diag(meanRows);

        double m = 0;
        double alpha = 0;
        double beta = 0;
        double t0 = 0;
        double kappa = getRealKappa(Y);
        int iter = 0;




        for(int i = 0; i<nmax; i++) {

            iter++;
            double t1 = (Y.get(0, (int) (s.get(0, 1) - 1.0)));
            double argA = 1.0 - (t1 / kappa);
            if(argA > 0) {
                alpha = log(argA);
            }
            double t2 = (Y.get(0, (int) (s.get(0, 2) - 1.0)));
            double argB = 1.0 - (t2 / kappa);
            if(argB > 0) {
                beta = log(argB);
            }
            m = (alpha - beta)/((int) (s.get(0, 2)) - (int) (s.get(0, 0)));
            t0 = (alpha / m) + (int) (s.get(0, 0));

            double den = (1.0 - exp(-m * ( (s.get(0, 0)) - t0)));
            double temp = Y.get(0, (int) (s.get(0, 0) - 1.0)) / den;
            if(temp > 2 || (abs(temp - kappa) / kappa) < 1e-3) {
                break;
            }
            else kappa = temp;
        }
        System.out.println("dopo ciclo: kappa: " + kappa + " m: " + m + " t0: " + t0 + " iter: " + iter);
        model.setA(D.inverse().times(m));
        model.setB(model.getA().times(kappa));


    }

    /**
     * Metodo di supporto per recuperare il valore ottimale del parametro kappa
     * nell'algoritmo di inizializzazione
     * @param Y
     * @return
     */
    private static double getRealKappa(Matrix Y){

        double max = Double.MIN_VALUE;

        for(int i = 0; i<Y.getColumnDimension(); i++){
            if(Y.get(0, i) > max)max = Y.get(0, i);
        }
        if(max < 1)return 1;

        return max;
    }


    public static Object [] EStep(Matrix Y, Matrix U) {

        /**
         * Sezione di init
         */

        /*Model model = Model.getInstance();
        Matrix A = model.getA();
        Matrix B = model.getB();
        Matrix Σ_n = model.getΣ_n();//Σ_n == G in mathlab
        Matrix C = model.getC();
        Matrix Σ_w = model.getΣ_w();//Σ_w == S in mathlab
        Matrix Mu0 = model.getMu0();
        Matrix Σ = model.getΣ(); // Σ == P0 in mathlab

        int n = Y.getColumnDimension();
        int q = Mu0.getRowDimension();
        Matrix mu = new Matrix(q, n);

         */

        /**
         * Per implementare le matrici tridimansionali uso array di matrici
         */

        /*
        Matrix [] V = init3DMatrix(q, q, n);

        Matrix [] P = init3DMatrix(q, q, n);
        Matrix llh = new Matrix(1, n);
        Matrix I = Matrix.identity(q, q);*/


        /**
         * Sezione forward
         */
        Object[] res = doForWardStep(Y, U);
        Matrix mu = (Matrix) res[0];
        double llhVal = (double) res[1];
        Matrix [] V = (Matrix []) res[2];
        Matrix [] P = (Matrix []) res[3];
        Matrix A = (Matrix) res[4];
        Matrix B = (Matrix) res[5];
        Matrix I = (Matrix) res[6];
        int n = (int) res[8];
        int q = (int) res[7];
        /*
        Matrix PC = Σ.times(C.transpose());
        Matrix R = C.times(PC).plus(Σ_w);
        Matrix K = PC.times(R.inverse());

        if(Y.get(Y.getRowDimension() - 1, 0) != NEGATIVE_INFINITY) {
            mu.setMatrix(0, mu.getRowDimension() - 1, 0, 0, Mu0.plus(
                    K.times(
                            Y.getMatrix(0, Y.getRowDimension()-1, 0, 0).minus(C.times(Mu0))
                    )
            ));
            llh.set(0,0, log(multivarn(mu.getMatrix(0, mu.getRowDimension()-1, 0, 0), Mu0, Σ)));
        }
        else {
            mu.setMatrix(0, mu.getRowDimension() - 1, 0, 0, Mu0);
            llh.set(0, 0, NEGATIVE_INFINITY);
        }
        V[0] = I.minus(K.times(C)).times(Σ);

        for(int i = 1; i<n; i++){

            Object [] ret = forwardStep(Y.getMatrix(0, Y.getRowDimension()-1, i, i),
                    U.getMatrix(0, U.getRowDimension()-1, i-1, i-1),
                    mu.getMatrix(0, mu.getRowDimension()-1, i-1, i-1),
                    V[i-1], A, B, Σ_n, C, Σ_w, I);

            mu.setMatrix(0, mu.getRowDimension()-1, i, i, (Matrix) ret[0]);
            V[i] = (Matrix) ret[1];
            P[i-1] = (Matrix) ret[2];
            llh.set(0, i, (double)ret[3]);

        }
        //recupero il valore in llh
        double llhVal = 0.0;
        for(int i = 0; i< llh.getColumnDimension(); i++) {
            double tmpllh = llh.get(0, i);
            if(tmpllh != NEGATIVE_INFINITY && tmpllh != POSITIVE_INFINITY){
                llhVal += tmpllh;
            }
        }
        */
        /**
         * Sezione backward
         */

        Matrix [] Ezz = init3DMatrix(q, q, n);
        Matrix [] Ezy = init3DMatrix(q, q, n);
        Matrix [] Dzz = init3DMatrix(q, q, n);
        Matrix [] Dzy = init3DMatrix(q, q, n);

        Matrix [] Vhat = new Matrix[n];
        Matrix muHat = mu.getMatrix(0, mu.getRowDimension()-1, 0, n-1);

        for(int i = 0; i<n; i++) {
            Vhat[i] = V[i];
        }
        for(int i = n-1; i>0; i--) {

            Object [] ret = backwardStep(muHat.getMatrix(0, muHat.getRowDimension() - 1, i,i),
                    Vhat[i], mu.getMatrix(0, mu.getRowDimension()-1, i-1, i-1),
                    V[i-1], P[i-1], A, B, U.getMatrix(0, U.getRowDimension()-1, i-1, i-1), I);

            muHat.setMatrix(0,muHat.getRowDimension()-1, i-1, i-1,  (Matrix)ret[0]);
            Vhat[i-1] = (Matrix)ret[1];

            Matrix A1 = I.minus(A.times(diag(U.getMatrix(0, U.getRowDimension()-1, i-1, i-1))));
            Matrix tmp = V[i-1].times(A1.transpose()).times(P[i-1].inverse());


            //qui nell ultimo trasponse c'è un errore, da vedere.
            Dzz[i] = Vhat[i].plus(
                    muHat.getMatrix(0, muHat.getRowDimension() -1, i,i).times(
                            muHat.getMatrix(0, muHat.getRowDimension() -1, i,i).transpose()
                    )
            ).times(diag(U.getMatrix(0, U.getRowDimension()-1, i,i)).transpose());

            Dzy[i] = Vhat[i].times(tmp.transpose()).plus(
                            muHat.getMatrix(0, muHat.getRowDimension()-1, i, i).times(
                                    muHat.getMatrix(0, muHat.getRowDimension()-1, i-1, i-1).transpose()
                            )).times(diag(U.getMatrix(0, U.getRowDimension()-1, i-1, i-1)));

            Ezy[i] = Vhat[i].times(tmp.transpose()).plus(
                            muHat.getMatrix(0, mu.getRowDimension()-1, i,i).times(
                                    muHat.getMatrix(0, mu.getRowDimension()-1, i-1,i-1).transpose()));

            Ezz[i] = Vhat[i].plus(
                    muHat.getMatrix(0, muHat.getRowDimension()-1, i,i).times(
                            muHat.getMatrix(0, muHat.getRowDimension()-1, i,i).transpose()));
        }

        Dzz[0] = Vhat[0].plus(
                muHat.getMatrix(0, muHat.getRowDimension()-1, 0, 0).times(
                        muHat.getMatrix(0, muHat.getRowDimension()-1, 0, 0).transpose())).times(
                                diag(U.getMatrix(0, U.getRowDimension()-1, 0,0)).transpose());

        Ezz[0] = Vhat[0].plus(
                muHat.getMatrix(0, muHat.getRowDimension()-1, 0,0).times(
                        muHat.getMatrix(0, muHat.getRowDimension()-1, 0,0).transpose()));


        return new Object[]{mu, V, P, muHat, Vhat, Ezy, Ezz, Dzy, Dzz, llhVal};
    }
    public static Object [] doForWardStep(Matrix Y, Matrix U) {

        Model model = Model.getInstance();
        Matrix A = model.getA();
        Matrix B = model.getB();
        Matrix Σ_n = model.getΣ_n();//Σ_n == G in mathlab
        Matrix C = model.getC();
        Matrix Σ_w = model.getΣ_w();//Σ_w == S in mathlab
        Matrix Mu0 = model.getMu0();
        Matrix Σ = model.getΣ(); // Σ == P0 in mathlab

        int n = Y.getColumnDimension();
        int q = Mu0.getRowDimension();
        Matrix mu = new Matrix(q, n);

        /**
         * Per implementare le matrici tridimansionali uso array di matrici
         */


        Matrix [] V = init3DMatrix(q, q, n);

        Matrix [] P = init3DMatrix(q, q, n);
        Matrix llh = new Matrix(1, n);
        Matrix I = Matrix.identity(q, q);



        Matrix PC = Σ.times(C.transpose());
        Matrix R = C.times(PC).plus(Σ_w);
        Matrix K = PC.times(R.inverse());

        if(Y.get(Y.getRowDimension() - 1, 0) != NEGATIVE_INFINITY) {
            mu.setMatrix(0, mu.getRowDimension() - 1, 0, 0, Mu0.plus(
                    K.times(
                            Y.getMatrix(0, Y.getRowDimension()-1, 0, 0).minus(C.times(Mu0))
                    )
            ));
            llh.set(0,0, log(multivarn(mu.getMatrix(0, mu.getRowDimension()-1, 0, 0), Mu0, Σ)));
        }
        else {
            mu.setMatrix(0, mu.getRowDimension() - 1, 0, 0, Mu0);
            llh.set(0, 0, NEGATIVE_INFINITY);
        }
        V[0] = I.minus(K.times(C)).times(Σ);

        for(int i = 1; i<n; i++){

            Object [] ret = forwardStep(Y.getMatrix(0, Y.getRowDimension()-1, i, i),
                    U.getMatrix(0, U.getRowDimension()-1, i-1, i-1),
                    mu.getMatrix(0, mu.getRowDimension()-1, i-1, i-1),
                    V[i-1], A, B, Σ_n, C, Σ_w, I);

            mu.setMatrix(0, mu.getRowDimension()-1, i, i, (Matrix) ret[0]);
            V[i] = (Matrix) ret[1];
            P[i-1] = (Matrix) ret[2];
            llh.set(0, i, (double)ret[3]);

        }
        //recupero il valore in llh
        double llhVal = 0.0;
        for(int i = 0; i< llh.getColumnDimension(); i++) {
            double tmpllh = llh.get(0, i);
            if(tmpllh != NEGATIVE_INFINITY && tmpllh != POSITIVE_INFINITY){
                llhVal += tmpllh;
            }
        }

        return new Object[]{mu, llhVal, V, P, A, B, I, q, n};
    }
    public static void MStep(Matrix Y, Matrix U, Matrix muHat, Matrix [] Ezy, Matrix [] Ezz, Matrix [] Dzy, Matrix [] Dzz) {

        int n = Y.getColumnDimension();
        int q = U.getRowDimension();

        //recupero il modello
        Model model = Model.getInstance();

        Matrix A = model.getA();
        Matrix B = model.getB();
        Matrix Σ_n = model.getΣ_n(); //G == Σ_n
        Matrix mu0 = muHat.getMatrix(0, muHat.getRowDimension()-1, 0,0);

        Matrix Σ = Ezz[0].minus(mu0.times(mu0.transpose())); //P0 == Σ

        Matrix lambda = U.getMatrix(0, U.getRowDimension()-1, 0, U.getColumnDimension()-2)
                .times(U.getMatrix(0, U.getRowDimension()-1, 0, U.getColumnDimension()-2).transpose());

        Matrix zeta = muHat.getMatrix(0, muHat.getRowDimension()-1, 1, muHat.getColumnDimension()-1).
                times(U.getMatrix(0, U.getRowDimension()-1, 0, U.getColumnDimension()-2).transpose());

        Matrix [] tmp = init3DMatrix(q, q, n);

        for(int i = 0; i<n-1; i++){
            tmp[i] = diag(U.getMatrix(0, U.getRowDimension()-1, i,i)).times(Ezz[i])
                    .times(diag(U.getMatrix(0, U.getRowDimension()-1, i,i)).transpose());
        }
        Matrix gamma = sumOn3DMatrix(tmp, 0, null);

        Matrix [] tmp2 = init3DMatrix(q, q, n);
        for(int i = 0; i<n-1; i++){
            tmp2[i] = dotProduct(U.getMatrix(0, U.getRowDimension()-1, i,i), muHat.getMatrix(0, muHat.getRowDimension()-1, i,i)).times(U.getMatrix(0, U.getRowDimension()-1, i,i).transpose());
        }


        Matrix kappa = sumOn3DMatrix(tmp2, 0, null);
        Matrix theta = muHat.getMatrix(0, muHat.getRowDimension()-1, 0, muHat.getColumnDimension()-2)
                .times(U.getMatrix(0, U.getRowDimension()-1, 0, U.getColumnDimension()-2).transpose());

        Matrix alpha = sumOn3DMatrix(Dzy, 0, null);
        Matrix beta = sumOn3DMatrix(redux3D(Dzz, 0, Dzz.length - 1), 0, null);

        Matrix [] tmp3 = init3DMatrix(q, q, n);

        for(int i = 0; i<n-1; i++){
            tmp3[i] = U.getMatrix(0, U.getRowDimension()-1, i,i)
                    .times(dotProduct(muHat.getMatrix(0, muHat.getRowDimension()-1, i,i), U.getMatrix(0, U.getRowDimension()-1, i,i))
                            .transpose());
        }
        Matrix epsilon = sumOn3DMatrix(tmp3, 0, null);

        Matrix [] tmp4 = redux3D(Ezz, 0, Ezz.length-1);
        Matrix Ezz1 = sumOn3DMatrix(tmp4, 0, null);
        Matrix [] tmp5 = redux3D(Ezz, 1, Ezz.length);
        Matrix Ezz2 = sumOn3DMatrix(tmp5, 0, null);
        Matrix Ezy2 = sumOn3DMatrix(Ezy, 0, null);

        if(!containsNaN(gamma)){
            if(conditionNumber(gamma) < 1e12){
                Matrix den = lambda.minus(epsilon.times(gamma.inverse()).times(kappa));
                if(conditionNumber(den) < 1e12){
                    B = zeta.minus(theta).plus(beta.minus(alpha)
                            .times(gamma.inverse()).times(kappa)).times(den.inverse());
                    A = beta.minus(alpha).plus(B.times(epsilon)).times(gamma.inverse());

                    Matrix firstTerm = Ezz2.minus(Ezy2.transpose()).plus(A.times(alpha.transpose())).minus(B.times(zeta.transpose()));
                    Matrix secondTerm = Ezy2.times(-1).plus(Ezz1).minus(A.times(beta.transpose())).plus(B.times(theta.transpose()));
                    Matrix thirdTerm = alpha.times(A.transpose()).minus(beta.times(A.transpose())).plus(A.times(gamma).times(A.transpose()))
                            .minus(B.times(epsilon).times(A.transpose()));
                    Matrix fourthTerm = zeta.times(-1).times(B.transpose()).plus(theta.times(B.transpose())).minus(A.times(kappa).times(B.transpose()))
                            .plus(B.times(lambda).times(B.transpose()));
                    Σ_n = firstTerm.plus(secondTerm).plus(thirdTerm).plus(fourthTerm).times((double) 1 / (n-1));

                }
            }
        }
        ArrayList<Integer> idx = new ArrayList<>();
        for(int i = 0; i<Y.getColumnDimension(); i++){
            if(Y.get(0,i) != NEGATIVE_INFINITY){
                idx.add(i);
            }
        }
        Matrix Ezzall = sumOn3DMatrix(Ezz, 1, idx);
        Matrix Y0 = Y.getMatrix(Y.getRowDimension()-1, Y.getRowDimension()-1, 0, Y.getColumnDimension()-1);
        for(int i = 0; i<Y.getColumnDimension(); i++){
            if(Y.get(0, i) == NEGATIVE_INFINITY) {
                Y0.set(0, i, 0);
            }
        }
        Matrix Ynu = Y0.times(muHat.transpose());
        int T = idx.size();
        Matrix C = Ynu.times(Ezzall.inverse());

        Matrix tmp6 = null;
        for(int i = 0; i<n; i++){
            if(tmp6 == null){
                tmp6 = C.times(muHat.getMatrix(0, muHat.getRowDimension()-1, i,i))
                        .times(Y0.getMatrix(0, Y0.getRowDimension()-1, i,i).transpose());
            }
            else {
                tmp6 = tmp6.plus(C.times(muHat.getMatrix(0, muHat.getRowDimension()-1, i,i))
                        .times(Y0.getMatrix(0, Y0.getRowDimension()-1, i,i).transpose()));
            }

        }

        Matrix Term1 = Y0.times(Y0.transpose());
        Matrix Term2 = tmp6.times(-1).minus(tmp6.transpose());
        Matrix Term3 = C.times(Ezzall).times(C.transpose());
        Matrix addTerm = Term1.plus(Term2).plus(Term3);

        Matrix Σ_w = addTerm.times((double) 1/T);

        /*
        Matrix Σ_w = Y0.times(Y0.transpose()).minus(tmp6.minus(tmp6.transpose()))
                .plus(C.times(Ezzall).times(C.transpose())).times((double) 1/T);
         */
        model.setA(A);
        model.setB(B);
        model.setC(C);
        model.setΣ_n(Σ_n);
        model.setΣ_w(Σ_w);
        model.setMu0(mu0);
        model.setΣ(Σ);
    }
    private static Object [] forwardStep(Matrix y, Matrix u, Matrix mu, Matrix v, Matrix A, Matrix B, Matrix Σ_n
                            , Matrix C, Matrix Σ_w, Matrix I) {

        Matrix Bu = B.times(u);

        Matrix A1 = I.minus(A.times(diag(u)));


        Matrix P = A1.times(v).times(A1.transpose()).plus(Σ_n);
        Matrix PC = P.times(C.transpose());
        Matrix R = C.times(PC).plus(Σ_w);
        Matrix K = null;
        double llh = 0.0;

        boolean flagObservation = y.get(y.getRowDimension() - 1, 0) == NEGATIVE_INFINITY;
        if(flagObservation){
            K = C.transpose().times(0);
        }
        else {
            K = PC.times(R.inverse());
        }
        Matrix Amu = A1.times(mu).plus(B.times(u));
        Matrix CAmu = C.times(Amu);
        Matrix tmpmu = null;
        if(flagObservation) {
            tmpmu = Amu;
            llh = NEGATIVE_INFINITY;
        }
        else {
            tmpmu = Amu.plus(K.times(y.getMatrix(0, y.getRowDimension()-1, 0, 0).minus(CAmu)));
            llh =   log(multivarn(y.getMatrix(0, y.getRowDimension()-1, 0, 0), C.times(tmpmu), Σ_w)) +
                    log(multivarn(tmpmu, Amu, Σ_n));
        }

        Matrix tmpV = I.minus(K.times(C)).times(P);
        return new Object[]{tmpmu, tmpV, P, llh};
    }
    private static Object [] backwardStep(Matrix muHat, Matrix Vhat, Matrix mu, Matrix V,
                                          Matrix P, Matrix A, Matrix B, Matrix U, Matrix I) {
        Matrix tmpmuHat = null;
        Matrix tmpVhat = null;

        Matrix A1 = I.minus(A.times(diag(U)));
        Matrix J = V.times(A1.transpose()).times(P.inverse());
        tmpmuHat = mu.plus(J.times(muHat.minus(A1.times(mu)).minus(B.times(U))));
        tmpVhat = V.plus(J.times(Vhat.minus(P).times(J.transpose())));


        return new Object[]{tmpmuHat, tmpVhat};
    }


    private static double multivarn(Matrix x, Matrix mu, Matrix sigma){

        int n = x.getRowDimension();
        Matrix P = sigma.inverse();

        double detSigma = sigma.det();

        Matrix diffXmu = x.minus(mu);
        Matrix exponent = diffXmu.transpose().times(P).times(diffXmu).times(-0.5);

        double tempNum = (1 / Math.sqrt(pow((2 * PI), n) * detSigma));
        double product = exp(exponent.get(0,0));

        return tempNum * product;
    }




    private static boolean containsNaN(Matrix m) {

        for(int i = 0; i<m.getRowDimension(); i++){
            for(int j = 0; j<m.getColumnDimension(); j++){
                if(Double.isNaN(m.get(i,j)))return true;
            }
        }
        return false;
    }




    /**
     * Metodo usato per recuperare il numero di condizionamento
     * di una matrice per valutare l'invertibilità della matrice
     * @param m
     * @return conditionNumber
     */
    private static double conditionNumber(Matrix m) {

        SingularValueDecomposition svd = m.svd();
        double [] singularValue = svd.getSingularValues();

        double max = -1;
        double min = Double.MAX_VALUE;
        for(double x : singularValue){
            if(x > max)max = x;
            if(x < min)min = x;
        }
        return max / min;

    }

    /**
     * TODO: metodo da inserire in utils
     * @param A
     * @param B
     * @return V T.C. foreach i in V : V(i) = A(i) * B(i)
     */
    private static Matrix dotProduct(Matrix A, Matrix B){

        int Ar = A.getRowDimension();
        int Ac = A.getColumnDimension();
        int Br = B.getRowDimension();
        int Bc = B.getColumnDimension();

        Matrix result = null;

        //caso in cui A è un vettore riga e B un vettore colonna e le dimensioni sono corrette
        if(Ar == 1 && Br > 1 && Ac == Br) {

            result = new Matrix(Br, Ac);
            for(int i = 0; i<Br; i++) {
                for(int j = 0; j<Ac; j++) {
                    result.set(i, j, B.get(i, 0) * A.get(0,j));
                }
            }
        }//caso in cui B è un vettore riga e A vettore colonna e le dim sono corrette
        else if(Br == 1 && Ar > 1 && Bc == Ar) {

            result = new Matrix(Ar, Bc);
            for(int i = 0; i<Ar; i++) {
                for(int j = 0; j<Bc; j++) {
                    result.set(i, j, A.get(i, 0) * B.get(0, j));
                }
            }

        }//caso in cui A e B siano due matrici di pari dimensioni
        else if(Ac == Bc && Ar == Br) {

            result = new Matrix(Ar, Bc);
            for(int i = 0; i<Ar; i++) {
                for(int j = 0; j<Ac; j++) {
                    result.set(i, j, A.get(i, j) * B.get(i, j));
                }
            }
        }
        else {
            /**
             * TODO: da sollevare eccezione
             */
            return null;
        }

        return result;
    }


    private static Matrix [] redux3D(Matrix [] M, int start, int end) {
        Matrix [] newM = new Matrix[end];
        for(int i = start; i<end; i++)newM[i] = M[i];

        return newM;
    }
    /**
     *
     * @return B T.C.  foreach i in B[i]:
     *                  foreach j in B[i][j]:
     *                     foreach k in size(_3DM):
     *                      B[i][j] == Sum(_3DM[i][j][K])
     *                      if flag == 0
     *                      else
     *         B T.C.      foreach i in B[i]:
     *                        foreach j in B[i][j]:
     *                           foreach k in size(_3DM) :
     *                            if idxs.contains(k) :
     *                              B[i][j] == Sum(_3DM[i][j][K])
     */
    private static Matrix sumOn3DMatrix(Matrix _3DM [], int flag, ArrayList<Integer> idxs) {

        int checkIdx = 0;
        while(checkIdx < _3DM.length && _3DM[checkIdx] == null){//_3DM potrebbe avere lungo la terza dimensione matrici == null
            checkIdx++;
        }
        if(checkIdx == _3DM.length) {
            //TODO: da sollevare eccezione personalizzata
            return null;
        }

        Matrix tmp = new Matrix(_3DM[checkIdx].getRowDimension(), _3DM[checkIdx].getColumnDimension());

        for(int i = 0; i<_3DM[checkIdx].getRowDimension(); i++){
            for(int j = 0; j<_3DM[checkIdx].getColumnDimension(); j++){
                for(int k = 0; k<_3DM.length; k++) {
                    if(flag == 1){
                        if(idxs.contains(k)){
                            if(_3DM[k] != null){
                                tmp.set(i, j, tmp.get(i,j) + _3DM[k].get(i,j));
                            }
                            else tmp.set(i, j, tmp.get(i,j) + 0.0);
                        }
                    }
                    else {
                        if(_3DM[k] != null){
                            tmp.set(i, j, tmp.get(i,j) + _3DM[k].get(i,j));
                        }
                        else tmp.set(i, j, tmp.get(i,j) + 0.0);
                    }
                }
            }
        }
        return tmp;
    }

     /** []
     *  TODO: metodo da inserire in utils
     * @param m Matrix || vector
     * @return Diag(m) if m == vector || column vector with m diagonal element if m == Matrix
     */
    public static Matrix diag(Matrix m) {

        if(m.getColumnDimension() == 1) {
            //caso in cui m è un vettore
            Matrix diagMatrix = new Matrix(m.getRowDimension(), m.getRowDimension());
            for(int i = 0; i<m.getRowDimension(); i++){
                diagMatrix.set(i, i, m.get(i, 0));
            }
            return diagMatrix;
        }
        else {

            //caso in cui la matrice è quadrata
            if(m.getColumnDimension() == m.getRowDimension()) {
                //caso in cui m è una matrice
                Matrix diagColumn = new Matrix(m.getRowDimension(), 1);

                for(int i = 0; i<m.getColumnDimension(); i++){
                    diagColumn.set(i, 0, m.get(i, i));
                }
                return diagColumn;
            }
            else {
                return null;
            }

        }
    }
    public static Matrix [] init3DMatrix(int x, int y, int z) {
        Matrix [] tmp = new Matrix[z];
        for(int i = 0; i<z; i++ ){tmp[i] = new Matrix(x, y);}

        return tmp;
    }

}
