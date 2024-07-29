package com.example.GreenApp.Prediction.Utils;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.GreenApp.Prediction.Data_Structs.DataContainer;
import com.example.GreenApp.Prediction.Data_Structs.EntryList;
import com.example.GreenApp.Prediction.MyBaseActivity;
import com.example.GreenApp.Prediction.Prediction_activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Matteo Torchia 599899
 */
public class RetreiveData extends MyBaseActivity {

    private MyHttpCallBack listener;
    private static String channelID=null;
    private static String READ_KEY=null;
    private HashMap<String, List<EntryList>> myDataStructName = null;
    private HashMap<String, ArrayList<DataContainer>> myDataStructDataReal = null;

    public void DoRequest(String channelID, String READ_KEY, MyHttpCallBack listener, String url,
                          Prediction_activity context) {






        final JsonObjectRequest jsonObjectRequest;
        jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            //recupero i nomi dei field in un array
                            ArrayList<String> fields = new ArrayList<String>();
                            ArrayList<String> fieldsKey = new ArrayList<String>();
                            int dim = response.getJSONObject("channel").length();

                            for (int i = 0; i < dim; i++) {

                                try {
                                    fields.add(String.valueOf(response.getJSONObject("channel").get("field" + (i + 1))));
                                    fieldsKey.add("field" + (i + 1));
                                }
                                catch (Exception e) {}
                            }

                            SetmyDataStructDataReal(channelID, false);
                            SetMyDataStructName(channelID, fields, fieldsKey, false);

                            //recupero l'array feeds
                            JSONArray jsonArray = response.getJSONArray("feeds");

                            String x = null;
                            for(int i = 0; i<jsonArray.length(); i++) {

                                JSONObject obj = (JSONObject) jsonArray.get(i);

                                DataContainer dataObj = new DataContainer();
                                try {
                                    dataObj.setData(obj.getString("created_at"));
                                }
                                catch (ParseException e) {
                                    Log.d("ParseException", "Formato data non aspettato");
                                }

                                dataObj.setEnrty_id(Integer.parseInt(obj.getString("entry_id")));

                                for(int j = 0; j<myDataStructName.get(channelID).size(); j++) {

                                    String checkValue = null;
                                    //se non c'è mapping con il valore (valore null) setto la stringa null a checkvalue
                                    try{
                                        checkValue = obj.getString(myDataStructName.get(channelID).get(j).getNameFromServer());
                                    }
                                    catch (Exception e) {
                                        checkValue  = "null";
                                    }

                                    JSONObject valori = jsonArray.getJSONObject(i);
                                    int test = Integer.parseInt(valori.getString("entry_id"));
                                    if(test == 875){
                                        System.out.println("CI VADO: " + test);
                                        int h = 0;
                                    }


                                    //se ho settato la stringa null a checkvalue viene sollevata un eccezione e
                                    //setto un valore significativo (Double.NEGATIVE_INFINITY) che verra poi usato dopo per i controlli
                                    try {
                                        dataObj.setmapping(myDataStructName.get(channelID).get(j).getText(), Double.parseDouble(checkValue));
                                    }
                                    catch (Exception e) {
                                        //caso in cui i dati sono sporchi => setto - infinito
                                        dataObj.setmapping(myDataStructName.get(channelID).get(j).getText(), Double.NEGATIVE_INFINITY);
                                    }

                                }
                                myDataStructDataReal.get(channelID).add(dataObj);

                            }
                            //ordino la lista di dati in base al ordine temporale
                            Collections.sort(myDataStructDataReal.get(channelID), DataContainer.DataComparetor);
                            listener.finishRequest(true, channelID, myDataStructDataReal, myDataStructName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("MainActivity", "Errore nel download");

                //notifico la PredictionActivity che la richiesta è fallita
                listener.finishRequest(false, channelID, null, null);
            }
        });
        Volley.newRequestQueue(context).add(jsonObjectRequest);
    }
    private void SetmyDataStructDataReal(String id, boolean resetFlag) {

        if(!resetFlag){
            if(myDataStructDataReal == null)myDataStructDataReal = new HashMap<>();

            myDataStructDataReal.put(id, new ArrayList<DataContainer>());

        }
        else myDataStructDataReal = new HashMap<>();

    }
    private void SetMyDataStructName(String id, ArrayList<String> fields, ArrayList<String> fieldKey, boolean resetFlag){

        if(!resetFlag){
            if(myDataStructName == null)myDataStructName = new HashMap<>();
            ArrayList<EntryList> data = new ArrayList<>();


            for(int i = 0; i<fields.size(); i++){
                data.add(new EntryList(fields.get(i), fieldKey.get(i), false));
            }
            myDataStructName.put(id, data);
        }
        else myDataStructName = new HashMap<>();

    }
}
