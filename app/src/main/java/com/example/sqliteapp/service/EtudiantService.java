package com.example.sqliteapp.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sqliteapp.classes.Etudiant;
import com.example.sqliteapp.util.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;



public class EtudiantService {
    private static final String TABLE_NAME ="etudiant";

    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM ="prenom";
    private static final String KEY_DATE_NAISSANCE ="date_naissance"; // New column
    private static final String KEY_PHOTO = "photo";

    private static String [] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE_NAISSANCE, KEY_PHOTO};
    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public void create(Etudiant e){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());
        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto()); // Ajouter la photo
        }

        db.insert(TABLE_NAME, null, values);
        Log.d("insert", e.getNom());
        db.close();
    }

    public void update(Etudiant e){
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());
        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto()); // Mettre à jour la photo
        }

        db.update(TABLE_NAME,
                values,
                "id = ?",
                new String[]{String.valueOf(e.getId())});
        db.close();
    }

    public Etudiant findById(int id){
        Etudiant e = null;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c;
        c = db.query(TABLE_NAME,
                COLUMNS,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null);
        if(c.moveToFirst()){
            e = new Etudiant();
            e.setId(c.getInt(0));
            e.setNom(c.getString(1));
            e.setPrenom(c.getString(2));
            e.setDateNaissance(c.getString(3));
            e.setPhoto(c.getBlob(4)); // Récupérer la photo
        }
        db.close();
        return e;
    }


    public void delete(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        db.delete(TABLE_NAME,
                KEY_ID + " = ?",
                new String[] { String.valueOf(e.getId()) });
        db.close();
    }

    public List<Etudiant> findAll(){
        List<Etudiant> eds = new ArrayList<>();
        String req ="select * from "+TABLE_NAME;
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Cursor c = db.rawQuery(req, null);
        Etudiant e = null;
        if(c.moveToFirst()){
            do{
                e = new Etudiant();
                e.setId(c.getInt(0));
                e.setNom(c.getString(1));
                e.setPrenom(c.getString(2));
                e.setDateNaissance(c.getString(3));
                e.setPhoto(c.getBlob(4)); // Récupérer la photo
                eds.add(e);
                Log.d("id = ", e.getId()+"");
            }while(c.moveToNext());
        }
        return eds;
    }
}
