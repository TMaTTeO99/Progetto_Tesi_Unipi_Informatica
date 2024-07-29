package com.example.GreenApp.Prediction.Data_Structs;

import Jama.Matrix;
/**
 * @author Matteo Torchia 599899
 */
public class Model {

    private static Model MyModelInstance;
    private Matrix A; //matrice di transizione
    private Matrix B; //matrice di input
    private Matrix Σ_n; //covarianza di transizione
    private Matrix C;//matrice di emissione
    private Matrix Σ_w; //covarianza emessa
    private Matrix Σ; //covarianza a priori
    private Matrix Mu0;
    private Model () {}


    /**
     * double-checked locking per non non ridurre troppo le prestazioni con synchronized
     * e non avere comunque problemi di concorrenza nel caso ne abbia necessità
     */
    public static Model getInstance() {

        if (MyModelInstance == null) {
            synchronized (Model.class) {
                if (MyModelInstance == null) {
                    MyModelInstance = new Model();
                }
            }
        }
        return MyModelInstance;
    }

    public Matrix getA() {
        return A.getMatrix(0,A.getRowDimension()-1, 0, A.getColumnDimension()-1);
    }

    public Matrix getB() {
        return B.getMatrix(0,B.getRowDimension()-1, 0, B.getColumnDimension()-1);
    }

    public Matrix getC() {
        return C.getMatrix(0,C.getRowDimension()-1, 0, C.getColumnDimension()-1);
    }


    public void setA(Matrix a) {
        A = a;
    }

    public void setB(Matrix b) {
        B = b;
    }

    public void setC(Matrix c) {
        C = c;
    }

    public Matrix getΣ_n() {
        return Σ_n.getMatrix(0,Σ_n.getRowDimension()-1, 0, Σ_n.getColumnDimension()-1);
    }

    public void setΣ_n(Matrix σ_n) {Σ_n = σ_n;}

    public Matrix getΣ_w() {
        return Σ_w.getMatrix(0,Σ_w.getRowDimension()-1, 0, Σ_w.getColumnDimension()-1);
    }

    public void setΣ_w(Matrix σ_w) {Σ_w = σ_w;}

    public Matrix getΣ() {
        return Σ.getMatrix(0,Σ.getRowDimension()-1, 0, Σ.getColumnDimension()-1);
    }

    public void setΣ(Matrix σ) {Σ = σ;}

    public Matrix getMu0() {
        return Mu0.getMatrix(0,Mu0.getRowDimension()-1, 0, Mu0.getColumnDimension()-1);
    }

    public void setMu0(Matrix mu0) {Mu0 = mu0;}

    /**
     * Metodo di test per stamapra le matrici
     * @param X
     */
    public void printTestMatrix(Matrix X, String name) {

        System.out.println("Stampa Matrice: " + name);

        for(int i = 0; i<X.getRowDimension(); i++) {
            for(int j = 0; j<X.getColumnDimension(); j++) {
                System.out.print(" " + X.get(i, j) + " ");
            }
            System.out.println();
        }

    }


}
