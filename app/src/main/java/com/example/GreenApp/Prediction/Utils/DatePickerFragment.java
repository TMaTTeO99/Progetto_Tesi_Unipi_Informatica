package com.example.GreenApp.Prediction.Utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.fragment.app.DialogFragment;

import com.example.firstapp.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private DateCallBack objCallBack;//oggetto listener
    private boolean flag;//variabile usata per distinguere l'istanza del fragment

    //uso le date per impostare i bicker in modo da rendere possibile la selezione di date
    //in cui i dati sono presenti
    //private ArrayList<String> date;
    private String yesterday;
    public DatePickerFragment(DateCallBack objCallBack, boolean flag, String yesterday) {
        this.objCallBack = objCallBack;
        this.flag = flag;
        this.yesterday = yesterday;
        //this.date = date;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {



        //faccio in modo che la data di inizio non possa essere
        //superiore alla data di "ieri"




        //Calendar chosenDate = null;
        Calendar minData = null;
        Calendar maxData = null;
        SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy");

        minData = Calendar.getInstance();
        maxData = Calendar.getInstance();

        int year = 0;
        int month = 0;
        int day = 0;

        try {

            //TODO: per ora so che le date utili dal canale sono
            //TODO: da 0 a 83, lascio le prime 20 date per trovare i valori di init di EM
            //minData.setTime(Objects.requireNonNull(dataFormat.parse(date.get(19))));


            if(flag) {
                //maxData.setTime(Objects.requireNonNull(dataFormat.parse(yesterday)));

                //TODO: per ora so che le date utili dal canale sono
                //TODO: da 0 a 83, lascio le ultime 3/5 date per trovare i valori di init di EM

                year = minData.get(Calendar.YEAR);
                month = minData.get(Calendar.MONTH);
                day = minData.get(Calendar.DAY_OF_MONTH);
            }
            else {
                year = Calendar.getInstance().get(Calendar.YEAR);
                month = Calendar.getInstance().get(Calendar.MONTH);
                day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        DatePickerDialog pickerDialog = new DatePickerDialog(requireContext(), R.style.DialogTheme,this, year, month, day);
        DatePicker picker = pickerDialog.getDatePicker();

        //c.add(Calendar.DAY_OF_MONTH, -1);  TODO: <-- codice da usare alla fine


        if(flag){
            //picker.setMinDate(minData.getTimeInMillis());
            picker.setMaxDate(maxData.getTimeInMillis());
        }
        return pickerDialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {

        objCallBack.setDeta(day, month+1, year, flag);
    }
}