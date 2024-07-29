package com.example.GreenApp.Prediction.Utils.GraphUtils;

import com.example.firstapp.R;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class MyMarkerView extends MarkerView {

    private TextView popup;
    private LinearLayout boxColor;
    private LineData data;

    public MyMarkerView(Context context, int layoutResource, LineData data, Chart graph) {
        super(context, layoutResource);
        this.popup = (TextView) findViewById(R.id.pop_up_pointer);
        this.boxColor = (LinearLayout) findViewById(R.id.boxColor);
        this.data = data;
        setChartView(graph);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        long d = (long) e.getX() + (60000 * 60);//incremento di un ora il timestamp per non vedere data precedente
        String dateString = formatter.format(new Date(d));
        popup.setText("Data: " + dateString + "\n" + "Valore: " + e.getY());

        for(ILineDataSet set : data.getDataSets()) {
            if(set.contains(e)) {
                boxColor.setBackgroundColor(set.getCircleColor(0));
            }
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffsetForDrawingAtPoint(float posX, float posY){


        MPPointF offset = getOffset();
        float width = getWidth();
        float height = getHeight();

        Chart tmp = getChartView();
        float chartWidth = tmp.getWidth();
        float chartHeight = tmp.getHeight();

        if (posX < chartWidth * 0.25) {
            offset.x = 0;
        } else if (posX > chartWidth * 0.75) {
            offset.x = -width;
        } else {
            offset.x = -(width / 2);
        }

        if (posY < chartHeight * 0.25) {
            offset.y = 0;
        } else if (posY > chartHeight * 0.75) {
            offset.y = -height;
        } else {
            offset.y = -(height / 2);
        }

        return offset;
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -getHeight());
    }
}