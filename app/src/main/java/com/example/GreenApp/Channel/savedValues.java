package com.example.GreenApp.Channel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

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
 * modifiche aggiunte da Matteo Torchia 599899
 * Devo modificare la classe per consentire di aggiungere i dati dell'ID2 e della sua
 * chiave di lettura.
 */

@Entity
public class savedValues {
        @PrimaryKey(autoGenerate = true)
        private int uid;

        @ColumnInfo(name = "id_key")
        private String id;

        @ColumnInfo(name = "read_key")
        private String read_key;


        @ColumnInfo(name = "position_key")
        private int position;

        @ColumnInfo(name = "id_key_2")
        private String lett_id_2;//variabile e relativi metodi aggiunti da Matteo Torchia 599899

        @ColumnInfo(name = "read_key_2")
        private String lett_read_key_2; //variabile e relativi metodi aggiunti da Matteo Torchia 599899

     /**
      * metodo costruttore
      *
      * @param id           : id associato
      * @param read_key     : chiave di lettura
      * @param position     : posizione
      * @param lett_read_key_2
      * @param lett_read_key_2
      */
        public savedValues(String id, String read_key, int position, String lett_id_2, String lett_read_key_2) {
            this.id = id;
            this.read_key=read_key;
            this.position=position;
            this.lett_id_2 = lett_id_2;
            this.lett_read_key_2 = lett_read_key_2;
        }

    /**
     *
     * @return restuisce uid associato
     */
    public int getUid() {
            return uid;
        }

    /**
     *
     * @param uid: imposta uid
     */
        public void setUid(int uid) {
            this.uid = uid;
        }

    /**
     *
     * @return restuisce id associato
     */
        public String getId() {
            return id;
        }

    /**
     *
     * @param id: id del channel
     */
        public void setId(String id) {
            this.id = id;
        }

    /**
     *
     * @return restituisce la chiave di lettura
     */
        public String getRead_key() {
        return read_key;
        }

    /**
     *
     * @return restitusce la posizione
     */
        public int getPosition() {
            return position;
        }

    /**
     *
     * @param pos: imposta la posizione
     */
        public void setPosition(int pos) {
            this.position = pos;
        }

    /**
     * Metodi aggiunti da Matteo Torchia 599899
     *
     */
        public String getLett_id_2() {
            return lett_id_2;
        }

        public void setLett_id_2(String lett_id_2) {
            this.lett_id_2 = lett_id_2;
        }

        public String getLett_read_key_2() {
            return lett_read_key_2;
        }

        public void setLett_read_key_2(String lett_read_key_2) {this.lett_read_key_2 = lett_read_key_2;}
}

