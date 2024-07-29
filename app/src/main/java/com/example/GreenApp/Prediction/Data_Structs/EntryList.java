package com.example.GreenApp.Prediction.Data_Structs;

public class EntryList {


    private String text;
    private String nameFromServer;
    private boolean checked;

    public EntryList(String text, String nameFromServer, boolean checked) {
        this.text = text;
        this.nameFromServer = nameFromServer;
        this.checked = checked;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getNameFromServer() { return nameFromServer; }

    public void setNameFromServer(String nameFromServer) { this.nameFromServer = nameFromServer; }



}
