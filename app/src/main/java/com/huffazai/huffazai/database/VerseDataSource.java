package com.huffazai.huffazai.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.huffazai.huffazai.model.Verse;

import java.util.ArrayList;
import java.util.List;

public class VerseDataSource {
    private SQLiteDatabase database;
    private VerseDatabaseHelper dbHelper;

    public VerseDataSource(Context context) {
        dbHelper = new VerseDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertVerse(Verse verse) {
        ContentValues values = new ContentValues();
        values.put(VerseDatabaseHelper.COLUMN_VERSE_TEXT, verse.getVerseText());
        values.put(VerseDatabaseHelper.COLUMN_VERSE_TYPE, verse.getVerseType());

        return database.insert(VerseDatabaseHelper.TABLE_VERSES, null, values);
    }

    public List<Verse> getAllVerses(String verseType) {
        List<Verse> verses = new ArrayList<>();
        Cursor cursor = database.query(
                VerseDatabaseHelper.TABLE_VERSES,
                null,
                VerseDatabaseHelper.COLUMN_VERSE_TYPE + " = ?",
                new String[]{verseType},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Verse verse = cursorToVerse(cursor);
                    verses.add(verse);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return verses;
    }

    private Verse cursorToVerse(Cursor cursor) {
        Verse verse = new Verse();
        verse.setId(cursor.getInt(cursor.getColumnIndex(VerseDatabaseHelper.COLUMN_ID)));
        verse.setVerseText(cursor.getString(cursor.getColumnIndex(VerseDatabaseHelper.COLUMN_VERSE_TEXT)));
        verse.setVerseType(cursor.getString(cursor.getColumnIndex(VerseDatabaseHelper.COLUMN_VERSE_TYPE)));
        return verse;
    }
}
