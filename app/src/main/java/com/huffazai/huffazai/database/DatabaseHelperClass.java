package com.huffazai.huffazai.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelperClass {
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    public DatabaseHelperClass() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    // Store a detected verse in Firebase Realtime Database
    public void storeDetectedVerse(String arabicText, boolean isCorrect) {
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            DatabaseReference userDetectedVersesRef = databaseReference.child("users").child(userId).child("detected_verses");
            String verseId = userDetectedVersesRef.push().getKey();

            if (verseId != null) {
                Map<String, Object> verseData = new HashMap<>();
                verseData.put("arabic_text", arabicText);
                verseData.put("is_correct", isCorrect);
                verseData.put("timestamp", ServerValue.TIMESTAMP);

                userDetectedVersesRef.child(verseId).setValue(verseData);
            }
        }
    }

    // Retrieve detected verses for the currently signed-in user
    public DatabaseReference getUserDetectedVersesRef() {
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            return databaseReference.child("users").child(userId).child("detected_verses");
        } else {
            return null;
        }
    }
}
