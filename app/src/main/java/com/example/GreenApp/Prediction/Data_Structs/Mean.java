package com.example.GreenApp.Prediction.Data_Structs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Entity(primaryKeys = {"data", "fieldName", "idChannel"})
public class Mean {

    @NonNull
    private String data;

    private String field; //salvo i valori come stringa per non avere problemi con lo storage per inserimento assenza valore


    @NonNull
    private String fieldName;

    @NonNull
    private String idChannel;

    public Mean(String data) {
        this.data = data;
    }
    public Mean(){}

    public String getData() {return data;}


    public void setData(String d) { this.data = d;}


    @Override
    public boolean equals(Object obj) {

        if(this == obj)return true;

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return Objects.equals(((Mean) obj).data, this.data);
    }
    @Override
    public int hashCode() {
        return Objects.hash(this.data);
    }

    public String getIdChannel() {
        return idChannel;
    }

    public void setIdChannel(String idChannel) {
        this.idChannel = idChannel;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }




    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
