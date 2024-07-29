package com.example.GreenApp.Prediction.Data_Structs;

/**
 * @Author Matteo TOrchia 599899
 *
 * Utilizzo una calsse singleton per tenere traccia della varsione Android del
 * dispositivo su cui è istallate l'app
 * Nella MainActivity quando viene avviata l'app controllo
 * la versione android e modifico tale varibile flag.
 */
public class AndroidVersionSingleton {

    private boolean androidVersioneFlag = false;
    private static AndroidVersionSingleton instance = null;

    private AndroidVersionSingleton(){}

    public static AndroidVersionSingleton getInstance(){

        if(instance == null){
            synchronized (AndroidVersionSingleton.class){
                if(instance == null){
                    instance = new AndroidVersionSingleton();
                }
            }
        }
        return instance;
    }

    public synchronized void setAndroidVersioneFlag(boolean androidVersionCorrect) {this.androidVersioneFlag = androidVersionCorrect;}
    public synchronized boolean getAndroidVersioneFlag(){return this.androidVersioneFlag;}

}