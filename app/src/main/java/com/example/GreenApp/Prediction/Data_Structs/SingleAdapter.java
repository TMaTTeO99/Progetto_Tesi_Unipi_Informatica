package com.example.GreenApp.Prediction.Data_Structs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.GreenApp.Prediction.Utils.AdapterInterfaceCheck;
import com.example.GreenApp.Prediction.Utils.SelectionAdapter;
import com.example.firstapp.R;

import java.util.ArrayList;
import java.util.List;

public class SingleAdapter extends RecyclerView.Adapter<SingleAdapter.SingleViewHolder> implements AdapterInterfaceCheck {

    private List<EntryList> lista;
    private int lastCheckedPosition = -1;
    private SelectionAdapter listener;

    public class SingleViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public CheckBox checkBox;
        public SingleViewHolder(View v) {
            super(v);

            //faccio il binding con le componenti
            textView = v.findViewById(R.id.DataSelectable);
            checkBox = v.findViewById(R.id.my_checkBox);

            /*checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                EntryList myData = (EntryList) buttonView.getTag();
                if(myData != null){
                    myData.setChecked(isChecked);
                    if(isChecked) {
                        if(lastCheckedPosition != -1 && lastCheckedPosition != getAdapterPosition()){

                            lista.get(lastCheckedPosition).setChecked(false);
                            notifyItemChanged(lastCheckedPosition);
                        }
                        lastCheckedPosition = getAdapterPosition();
                    }
                }
            });

             */
        }
    }
    public SingleAdapter(List<EntryList> lista, SelectionAdapter listener){
        this.lista = lista;
        this.listener = listener;
    }
    @Override
    public List<String> getCheckedItems() {

        List<String> checkedItems = new ArrayList<>();
        for (EntryList myData : lista) {
            if (myData.isChecked()) {
                checkedItems.add(myData.getText());
            }
        }
        return checkedItems;
    }

    @NonNull
    @Override
    public SingleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.selectable_list, parent, false);
        return new SingleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleViewHolder holder, int position) {

        int idx = position;
        EntryList myData = lista.get(position);
        holder.textView.setText(myData.getText());
        holder.checkBox.setChecked(myData.isChecked());
        holder.checkBox.setTag(myData);
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.retreiveSelection(idx);
            }
        });
        /*holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.retreiveSelection(position);
        });*/
        /*
             checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                EntryList myData = (EntryList) buttonView.getTag();
                if(myData != null){
                    myData.setChecked(isChecked);
                    if(isChecked) {
                        if(lastCheckedPosition != -1 && lastCheckedPosition != getAdapterPosition()){

                            lista.get(lastCheckedPosition).setChecked(false);
                            notifyItemChanged(lastCheckedPosition);
                        }
                        lastCheckedPosition = getAdapterPosition();
                    }
                }
            });

        */

    }

    @Override
    public int getItemCount() {
        return lista.size();
    }
    
    public List<String> getList(){
        
        List<String> lst = new ArrayList<>();
        for (EntryList myData : lista) {
            lst.add(myData.getText());
        }
        return lst;
    }
}
