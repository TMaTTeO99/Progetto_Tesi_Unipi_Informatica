package com.example.GreenApp.Prediction.Data_Structs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GreenApp.Prediction.Utils.AdapterInterfaceCheck;
import com.example.firstapp.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> implements AdapterInterfaceCheck {

    private List<EntryList> lista;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CheckBox checkBox;
        public ViewHolder(View v) {
            super(v);

            //faccio il binding con le componenti
            textView = v.findViewById(R.id.DataSelectable);
            checkBox = v.findViewById(R.id.my_checkBox);


            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                EntryList myData = (EntryList) buttonView.getTag();
                if(myData != null){
                    myData.setChecked(isChecked);
                }
            });
        }

    }
    public ListAdapter(List<EntryList> lista){
        this.lista = lista;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EntryList myData = lista.get(position);
        holder.textView.setText(myData.getText());
        holder.checkBox.setChecked(myData.isChecked());
        holder.checkBox.setTag(myData);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public List<String> getCheckedItems() {
        List<String> checkedItems = new ArrayList<>();
        for (EntryList myData : lista) {
            if (myData.isChecked()) {
                checkedItems.add(myData.getText());
            }
        }
        return checkedItems;
    }
    public List<String> getList(){return null;}
}
