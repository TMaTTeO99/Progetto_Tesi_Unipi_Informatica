package com.example.GreenApp.Graphic;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

/*
 * Progetto: svilluppo App Android per Tirocinio interno
 *
 * Dipartimento di Informatica Università di Pisa
 *
 * Autore:Domenico Profumo matricola 533695
 * Si dichiara che il programma è in ogni sua parte, opera originale dell'autore
 *
 */

public class ModelData
{
    //lista dei parametri utilizzati
    private String name;
    private LineGraphSeries<DataPoint> series;
    private Double media;

    public ModelData() {}

    /**
     * costruttore
     * @param name: contiene il nome del field
     * @param series: contiene la serie dei punti che sarà poi utilizzata dal recyclerView
     * @param media valore della media di tutti i punti
     */
    public ModelData(String name, LineGraphSeries<DataPoint> series, Double media) {
        this.name = name;
        this.series = series;
        this.media=media;
    }

    /**
     * utilizzata per recuperare il nome del field
     * @return nome del field
     */
    public String getName() {
        return name;
    }

    /**
     * utilizzata per impostare il nome del field
     * @param name nome del field
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * resituisce la media di tutti i valori
     * @return media del valori in Double
     */
    public Double getMedia() {
        return media;
    }

    /**
     * per impostare la media
     * @return media del valori in Double
     */
    public void setMedia(Double media) {
        this.media = media;
    }

    /**
     *
     * @return la serie di punti
     */
    public LineGraphSeries<DataPoint> getSeries() {
        return series;
    }

    /**
     *
     * @param series: l'insieme di punti
     */
    public void setSeries(LineGraphSeries<DataPoint> series) {
        this.series = series;
    }
}
