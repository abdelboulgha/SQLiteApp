package com.example.sqliteapp.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "etudiantDB";

    private static final int DATABASE_VERSION = 3; // Increment version to trigger onUpgrade

    private static final String TABLE_ETUDIANT = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance"; // New column
    private static final String KEY_PHOTO = "photo";

    private static final String CREATE_TABLE_ETUDIANT =
            "CREATE TABLE " + TABLE_ETUDIANT + "("
                    + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + KEY_NOM + " TEXT, "
                    + KEY_PRENOM + " TEXT, "
                    + KEY_DATE_NAISSANCE + " TEXT, "
                    + KEY_PHOTO + " BLOB)";// Add date of birth column

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Si on migre de la version 2 à 3, on ajoute juste la colonne photo
            db.execSQL("ALTER TABLE " + TABLE_ETUDIANT + " ADD COLUMN " + KEY_PHOTO + " BLOB");
        } else {
            // Sinon on recrée la table
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETUDIANT);
            onCreate(db);
        }
    }
}
