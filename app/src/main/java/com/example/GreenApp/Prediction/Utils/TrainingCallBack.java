package com.example.GreenApp.Prediction.Utils;

import java.util.ArrayList;

import Jama.Matrix;

public interface TrainingCallBack {
    public void finishTraining(boolean errorFlag, Double predictions, ArrayList<Double> Traking, Matrix observations, int days, Double upper, Double lower);
}
