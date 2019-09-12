package com.example.android.pets.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import com.example.android.pets.data.PetsContract.PetsEntry;

public class PetDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Pets.db";
    public static final int DATABASE_VERSION = 1;

    public static final String SQL_CREATE_ENTERIES = "CREATE TABLE " + PetsContract.PetsEntry.TABLE_NAME + "("
            + PetsEntry.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PetsEntry.COLUMN_NAME + " TEXT NOT NULL, "
            + PetsEntry.COLUMN_BREED + " TEXT, "
            + PetsEntry.COLUMN_GENDER + " INTEGER NOT NULL, "
            + PetsEntry.COLUMN_WEIGHT + " INTEGER NOT NULL DEFAULT 0);";

    public static final String SQL_DELETE_ENTERIES = "DROP TABLE " + "pets";

    public PetDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTERIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTERIES);

    }
}
