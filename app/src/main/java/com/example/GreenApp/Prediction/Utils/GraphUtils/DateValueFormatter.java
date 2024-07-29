package com.example.GreenApp.Prediction.Utils.GraphUtils;



import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class DateValueFormatter extends ValueFormatter {
    private final SimpleDateFormat mFormat;

    public DateValueFormatter() {
        mFormat = new SimpleDateFormat("dd-MM-yyyy");
    }

    @Override
    public String getFormattedValue(float value) {
        long millis = TimeUnit.SECONDS.toMillis((long) value);
        return mFormat.format(new Date(millis));
    }
}
