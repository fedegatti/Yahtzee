package it.gliandroidi.yahtzee;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Crea (ed istanzia) un database per i risultati (tipi di dato Result) di ogni partita giocata e portata
// correttamente a termine; si fa uso di Room
@Database(entities = Result.class, exportSchema = false, version = 1)
public abstract class AppResultDatabase extends RoomDatabase {
    private static final String DB_NAME = "resultDB";
    private static AppResultDatabase instance;

    public static synchronized AppResultDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, AppResultDatabase.class, DB_NAME).fallbackToDestructiveMigration().build();
        }
        return instance;
    }

    public abstract ResultDAO resultDAO();

}
