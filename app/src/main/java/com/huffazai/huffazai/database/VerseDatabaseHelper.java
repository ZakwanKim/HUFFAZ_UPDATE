package com.huffazai.huffazai.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class VerseDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "verse_database";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_VERSES = "verses";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_VERSE_TEXT = "verse_text";
    public static final String COLUMN_VERSE_TYPE = "verse_type";

    public VerseDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_VERSES_TABLE = "CREATE TABLE " + TABLE_VERSES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_VERSE_TEXT + " TEXT,"
                + COLUMN_VERSE_TYPE + " TEXT)";
        db.execSQL(CREATE_VERSES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERSES);
            onCreate(db);
        }
    }
}
