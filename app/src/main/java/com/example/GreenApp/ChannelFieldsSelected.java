package com.example.GreenApp;

import java.util.HashMap;

/**
 * Classe aggiunta da Matteo Torchia 599899
 * Classe utilizzata per controllare fra due risposte successive del server
 * i dati sono gia stati recuperati e le variabili del layout sono state settate
 * o meno
 */
public class ChannelFieldsSelected {

    private boolean flag_ctime1 = false;
   /* private boolean flag_ctime2 = false;
    private boolean flag_field1 = false;
    private boolean flag_field2 = false;
    private boolean flag_field3 = false;
    private boolean flag_field4 = false;
    private boolean flag_field5 = false;
    private boolean flag_field6 = false;
    private boolean flag_field7 = false;
    private boolean flag_field8 = false;
    private boolean flag_field1_2 = false;
    private boolean flag_field2_2 = false;*/
    private boolean flag_EVT = false;


    private HashMap<String, Boolean> map = new HashMap<>();
    public void setMap(HashMap<String, Boolean> map){ this.map = map;}
    public HashMap<String, Boolean> getMap(){ return map;}


    public ChannelFieldsSelected(){}
    /*public boolean isFlag_field1() { return flag_field1; }

    public void setFlag_field1(boolean flag_field1) {this.flag_field1 = flag_field1;}

    public boolean isFlag_field3() {
        return flag_field3;
    }

    public void setFlag_field3(boolean flag_field3) {
        this.flag_field3 = flag_field3;
    }

    public boolean isFlag_field2() {
        return flag_field2;
    }

    public void setFlag_field2(boolean flag_field2) {
        this.flag_field2 = flag_field2;
    }

    public boolean isFlag_field4() {
        return flag_field4;
    }

    public void setFlag_field4(boolean flag_field4) {
        this.flag_field4 = flag_field4;
    }

    public boolean isFlag_field5() {return flag_field5;}

    public void setFlag_field5(boolean flag_field5) {
        this.flag_field5 = flag_field5;
    }

    public boolean isFlag_field6() {
        return flag_field6;
    }

    public void setFlag_field6(boolean flag_field6) {
        this.flag_field6 = flag_field6;
    }

    public boolean isFlag_field7() {
        return flag_field7;
    }

    public void setFlag_field7(boolean flag_field7) {
        this.flag_field7 = flag_field7;
    }

    public boolean isFlag_field8() {
        return flag_field8;
    }

    public void setFlag_field8(boolean flag_field8) {
        this.flag_field8 = flag_field8;
    }
    public boolean isFlag_field1_2() {
        return flag_field1_2;
    }

    public void setFlag_field1_2(boolean flag_field1_2) {
        this.flag_field1_2 = flag_field1_2;
    }

    public boolean isFlag_field2_2() {
        return flag_field2_2;
    }

    public void setFlag_field2_2(boolean flag_field2_2) {this.flag_field2_2 = flag_field2_2;}*/

    public boolean isFlag_ctime1() {return flag_ctime1;}

    public void setFlag_ctime1(boolean flag_ctime1) {this.flag_ctime1 = flag_ctime1;}
/*
    public boolean isFlag_ctime2() {return flag_ctime2;}

    public void setFlag_ctime2(boolean flag_ctime2) {this.flag_ctime2 = flag_ctime2;}*/
    /*public void resetFlag(boolean set) {

        setFlag_field1(set);
        setFlag_field2(set);
        setFlag_field3(set);
        setFlag_field4(set);
        setFlag_field5(set);
        setFlag_field6(set);
        setFlag_field7(set);
        setFlag_field8(set);
        setFlag_field1_2(set);
        setFlag_field2_2(set);

        setFlag_EVT(set);

        setFlag_ctime1(set);
        setFlag_ctime2(set);
    }

     */


    public boolean isFlag_EVT() {
        return flag_EVT;
    }

    public void setFlag_EVT(boolean flag_EVT) {
        this.flag_EVT = flag_EVT;
    }
}
