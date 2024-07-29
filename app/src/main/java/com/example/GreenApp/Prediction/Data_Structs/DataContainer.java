package com.example.GreenApp.Prediction.Data_Structs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 * @author Matteo Torchia 599899
 */
public class DataContainer implements Comparable<DataContainer>{

    private Date data = null;
    private int enrty_id;
    private HashMap<String, Double> mappedData = new HashMap<>();



    public String getData() {return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").format(data);}

    public void setData(String data) throws ParseException {
        this.data = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(data);
    }

    public void setmapping(String key, Double value) {
        mappedData.put(key, value);
    }
    public Double getMapping(String key) {
        return mappedData.get(key);
    }
    public int getEnrty_id() {
        return enrty_id;
    }

    public void setEnrty_id(int enrty_id) {
        this.enrty_id = enrty_id;
    }


    public static Comparator<DataContainer> DataComparetor = new Comparator<DataContainer>() {
        @Override
        public int compare(DataContainer o1, DataContainer o2) {
            return  o1.getData().compareTo(o2.getData());
        }
    };

    @Override
    public int compareTo(DataContainer o) {
        return DataComparetor.compare(this, o);
    }



}
