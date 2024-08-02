package com.example.GreenApp.Channel;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.GreenApp.AppDatabase;
import com.example.firstapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

/**
 * Modifiche fatte da Matteo Torchia 599899
 *
 * Devo visualizzare Entrambi gli id dei canali
 *
 */


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyVieworder> {
    private static int pos=0;
    private List<Channel> channel;
    private Context context;
    private AppDatabase db;

    /**
     * metodo costruttore
     * @param channel: channel da inserire
     * @param context: contesto
     * @param db: database utilizzato
     */
    public RecyclerViewAdapter(List<Channel> channel, Context context,AppDatabase db) {
        this.context=context;
        this.channel=channel;
        this.db=db;
    }

    /**
     * faccio l'inflate (gonfiaggio) lo riportiamo sul ViewHolder -> grazie al quale andrà a richiamare i vari componenti
     */
    @Override
    public MyVieworder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_row,parent,false);
        return new MyVieworder(v);
    }

    /**
     * imposta gli oggetti presi dalla lista popolata da classi "model"
     */
    @Override
    public void onBindViewHolder(final MyVieworder holder, final int position) {
        final Channel chan=channel.get(position);

        /**
         * Modifiche fatte da Matteo Torchia 599899
         *
         */

        String id1 = chan.getLett_id();
        String id2 = chan.getLett_id_2();
        String idw = chan.getScritt_id();

        String Rch_1 = "";
        String Rch_2 = "";
        String Wch_1 = "";

        Rch_1 += (id1 != null) ? id1 : "";
        Rch_2 += (id2 != null) ? id2 : "";
        Wch_1 += (idw != null) ? idw : "";

        holder.readChannel_1.setText(Rch_1);
        holder.readChannel_2.setText(Rch_2);
        holder.writeChannel_1.setText(Wch_1);

        holder.titleRead.setText("Lettura:");
        holder.titleWrite.setText("Scrittura:");
        holder.name.setText(chan.getNameChannel());

        if(channel.get(position).getNotification()) holder.notifiche.setText("NOTIFICHE ON");
        else holder.notifiche.setText("NOTIFICHE OFF");

        pos=ChannelActivity.getposition();
        if(pos==position) ChannelActivity.sendPrefer(holder.star,context,pos);

        holder.bottone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelActivity.sendObjcet(v,context,position);
            }
        });
        //funzione eseguita guando premo sul pulsante "stella"
        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChannelActivity.sendPrefer(v,context,position);

            }
        });

    }

    /**
     *
     * @return numero di elementi presenti
     */
    @Override
    public int getItemCount() {
        return channel.size();
    }

    /**
     *  definiamo il ViewHolder (si occuperà della gestione dei singoli view)
     */
    public static class MyVieworder extends RecyclerView.ViewHolder{


        private TextView titleRead;
        private TextView titleWrite;

        private TextView readChannel_1;
        private TextView readChannel_2;

        private TextView writeChannel_1;

        private TextView notifiche;
        private FloatingActionButton bottone;
        private RelativeLayout touch_layout;
        private ImageButton star;
        private TextView name;

        /**
         * metodo costruttore
         * @param itemView puntatore al View
         */
        public MyVieworder(View itemView) {
            super(itemView);
            titleRead=itemView.findViewById(R.id.titleRead);
            titleWrite=itemView.findViewById(R.id.titleWrite);
            readChannel_1=itemView.findViewById(R.id.titleText);
            readChannel_2=itemView.findViewById(R.id.titleText_2);
            writeChannel_1=itemView.findViewById(R.id.titleText_3);
            bottone=itemView.findViewById(R.id.Button2);
            star=itemView.findViewById(R.id.favorite);
            name = itemView.findViewById(R.id.NameChannel);
            touch_layout=itemView.findViewById(R.id.touch_layout);
            notifiche=itemView.findViewById(R.id.textNotification);
        }
    }
}