package com.example.GreenApp.Prediction.Data_Structs;

import Jama.Matrix;

public class DataSavedToPredict {

    private static DataSavedToPredict instance = null;
    private boolean saved = false;
    private int lambda;
    private int availableT;
    private Matrix muHat;
    private Matrix Y;
    private Matrix U;
    private Matrix [] Vhat;
    private Double lastPrediction;


    private DataSavedToPredict() {

        /*
        int lambda, int availableT, Matrix muHat, Matrix [] Vhat, Matrix y, Matrix u
        this.lambda = lambda;
        this.availableT = availableT;
        this.muHat = muHat;
        this.Vhat = Vhat;
        Y = y;
        U = u;
        */
    }
    public static DataSavedToPredict getInstance() {

        if(instance == null) {
            synchronized (DataSavedToPredict.class) {
                if(instance == null){
                    instance = new DataSavedToPredict();
                }
            }
        }
        return instance;
    }
    public int getLambda() {
        return lambda;
    }

    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public int getAvailableT() {
        return availableT;
    }

    public void setAvailableT(int availableT) {
        this.availableT = availableT;
    }

    public Matrix getMuHat() {
        return muHat;
    }

    public void setMuHat(Matrix muHat) {
        this.muHat = muHat;
    }

    public Matrix getY() {
        return Y;
    }

    public void setY(Matrix y) {
        Y = y;
    }

    public Matrix getU() {
        return U;
    }

    public void setU(Matrix u) {
        U = u;
    }

    public Matrix[] getVhat() {
        return Vhat;
    }

    public void setVhat(Matrix[] vhat) {
        Vhat = vhat;
    }
    public Matrix getCurrent_U() {return U.getMatrix(0, U.getRowDimension()-1, availableT, availableT);}

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }

    public Double getLastPrediction() {return lastPrediction;}

    public void setLastPrediction(Double lastPrediction) {this.lastPrediction = lastPrediction;}
}
