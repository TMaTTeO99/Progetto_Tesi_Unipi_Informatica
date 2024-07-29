package com.example.GreenApp.Prediction.Utils;

import com.example.GreenApp.Prediction.Data_Structs.DataContainer;
import com.example.GreenApp.Prediction.Data_Structs.EntryList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface MyHttpCallBack {

    public void finishRequest(boolean value, String channel_ID, HashMap<String, ArrayList<DataContainer>> myDataStructDataReal, HashMap<String, List<EntryList>> myDataStructName);

}