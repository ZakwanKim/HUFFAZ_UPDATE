package com.huffazai.huffazai.database;

public class Verse {
    private int id;
    private String verseText;
    private String verseType;

    public Verse() {
        // Default constructor required for SQLite
    }

    public Verse(String verseText, String verseType) {
        this.verseText = verseText;
        this.verseType = verseType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVerseText() {
        return verseText;
    }

    public void setVerseText(String verseText) {
        this.verseText = verseText;
    }

    public String getVerseType() {
        return verseType;
    }

    public void setVerseType(String verseType) {
        this.verseType = verseType;
    }
}
