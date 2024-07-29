package com.example.GreenApp;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Classe creata da Matteo Torchia 599899
 * Converter per il database room in modo da poter utilizzare
 * nelle query le date e ottenere i dati ordinati per data
 */
public class ConverterRoom {

    private static final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    @TypeConverter
    public static Date fromTimestamp(String value) {
        try {
            return value == null ? null : df.parse(value);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @TypeConverter
    public static String dateToTimestamp(Date date) {
        return date == null ? null : df.format(date);
    }


}
