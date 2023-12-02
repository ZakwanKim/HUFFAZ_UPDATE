package com.huffazai.huffazai.database;

public class DetectedVerse {
    private String arabicText;
    private boolean isCorrect;
    private long timestamp;

    // Default constructor (required for Firebase)
    public DetectedVerse() {
    }

    public DetectedVerse(String arabicText, boolean isCorrect, long timestamp) {
        this.arabicText = arabicText;
        this.isCorrect = isCorrect;
        this.timestamp = timestamp;
    }

    public String getArabicText() {
        return arabicText;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
