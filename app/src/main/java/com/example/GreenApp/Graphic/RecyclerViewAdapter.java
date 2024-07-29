package com.example.GreenApp.Graphic;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firstapp.R;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 *
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>
{

    private List<ModelData> data;
    private Context context;

    /**
     * metodo costruttore
     * @param data: insieme di oggetti ModelData contenenti tutti i dati da rappresentare
     * @param context: context associato
     */
    public RecyclerViewAdapter(List<ModelData> data, Context context) {
        this.data = data;
        this.context = context;
    }


    /**
     * faccio l'inflate (gonfiaggio) lo riportiamo sul ViewHolder -> grazie al quale andrà a richiamare i vari componenti
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.graphic_layout_row, parent, false);
        return new ViewHolder(v);
    }


    /**
     * imposta gli oggetti presi dalla lista popolata da classi "model"
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        ModelData user = data.get(position);
        holder.text.setText(user.getName());
        holder.series=user.getSeries();
        final LineGraphSeries<DataPoint> serie=holder.series;
        final GraphView graph= holder.graph;
        serie.setDrawDataPoints(true);
        serie.setDrawBackground(true);
        serie.setBackgroundColor(Color.LTGRAY);
        serie.setDataPointsRadius(8);
        final SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm");
        graph.addSeries(serie);
        //setto la data come valore da mostrare
        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
            public String formatLabel(double value,boolean isValueX){
                   if(isValueX){
                       return sdf.format(new Date((long) value));
                   }
                    else return super.formatLabel(value,isValueX);
                }
        });

        serie.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                Date dat=new Date((long) dataPoint.getX());
                Toast.makeText(context,"x: " + sdf.format(dat) +"\ny: "+dataPoint.getY(),Toast.LENGTH_LONG).show();

                //avvio un thread per inserire i valori
                MyThread myThread = new MyThread();
                myThread.settingsvalues(holder.series,graph,dataPoint);
                myThread.start();
            }
        });

        //setto tutti i parametri
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);
        Date xas=new Date((long) graph.getViewport().getMinX(true));
        Date yas=new Date((long) graph.getViewport().getMaxX(true));
        holder.start.setText(sdf.format(xas));
        holder.end.setText(sdf.format(yas));
        Double max=Math.round(serie.getHighestValueY()*100.0)/100.0;
        Double min=Math.round(serie.getLowestValueY()*100.0)/100.0;
        holder.max.setText(String.valueOf(max));
        holder.min.setText(String.valueOf(min));
        holder.avg.setText(String.valueOf(user.getMedia()));
        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        graph.getViewport().setOnXAxisBoundsChangedListener(new Viewport.OnXAxisBoundsChangedListener() {
            @Override
            //eseguita quando nel grafico vengono cambiate le coordinate (ad esempio quando effettuo uno zoom)
            public void onXAxisBoundsChanged(double minX, double maxX, Reason reason) {
                graph.getGridLabelRenderer().invalidate(false,false);
                Date xas=new Date((long) minX);
                Date yas=new Date((long) maxX);
                holder.start.setText(sdf.format(xas));
                holder.end.setText(sdf.format(yas));

                //faccio la scanzione di tutti i punti compresi tra minX e maxX per individuare minimo,massimo e media
                Iterator<DataPoint> massimo=serie.getValues(minX,maxX);
                Double val=massimo.next().getY();
                Double min=val;
                Double max=val;
                Double med=0.0;
                int conta=1;
                Double somma=0.0;
                while (massimo.hasNext()){
                    conta++;
                    val=massimo.next().getY();
                    if(val>max) max=val;
                    if(val<min) min=val;
                    somma=somma+val;
                }
                med=Math.round((somma/conta)*100.0)/100.0;
                max=Math.round(max*100.0)/100.0;
                min=Math.round(min*100.0)/100.0;
                holder.max.setText(String.valueOf(max));
                holder.min.setText(String.valueOf(min));
                holder.avg.setText(String.valueOf(med));
            }
        });
    }

    /**
     *
     * @retur la dimensione della lista
     */
    @Override
    public int getItemCount() {
        return data.size();
    }

    //definiamo il ViewHolder (si occuperà della gestione dei singoli view)
    public class ViewHolder extends RecyclerView.ViewHolder
    {

        private TextView text;
        private LineGraphSeries<DataPoint> series;
        private GraphView graph;
        private RelativeLayout touch_layout;
        private TextView start;
        private TextView min;
        private TextView max;
        private TextView avg;
        private TextView end;
        private PointsGraphSeries<DataPoint> last=null;

        /**
         * metodo costruttore
         * @param itemView puntatore al View
         */
        public ViewHolder(View itemView) {
            super(itemView);

            //creo le associazioni con il layout
            text = itemView.findViewById(R.id.titleText);
            graph = itemView.findViewById(R.id.graph);
            touch_layout = itemView.findViewById(R.id.touch_layout);
            start=itemView.findViewById(R.id.textViewstart);
            end=itemView.findViewById(R.id.textViewend);
            min=itemView.findViewById(R.id.textViewMin);
            max=itemView.findViewById(R.id.textViewMax);
            avg=itemView.findViewById(R.id.textViewMed);
        }
    }

    /**
     * thread che mi serve per settare i punti blu nel grafico quando ci clicco sopra
     */
    public class MyThread extends Thread {

        LineGraphSeries<DataPoint> serie=null;
        GraphView graph=null;
        DataPointInterface dataPoint;

        /**
         * metodo eseguito all'avvio del thread
         */
        public void run(){
            graph.removeAllSeries();
            DataPoint[] data = new DataPoint[]{new DataPoint(dataPoint.getX(), dataPoint.getY())};
            PointsGraphSeries<DataPoint> series1 = new PointsGraphSeries<>(data);
            series1.setColor(Color.BLUE);
            series1.setSize(8);
            graph.addSeries(serie);
            graph.addSeries(series1);
        }

        /**
         * funzione per impostare i valori del thread
         * @param serie1: serie di punti LineGraphSeries
         * @param graph1: grafico
         * @param dataPoint1: insieme di punti DataPoint
         */
        public void settingsvalues( LineGraphSeries<DataPoint> serie1, GraphView graph1, DataPointInterface dataPoint1){
            serie=serie1;
            graph=graph1;
            dataPoint=dataPoint1;
        }
    }
}

