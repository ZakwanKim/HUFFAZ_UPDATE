package com.huffazai.huffazai.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.huffazai.huffazai.R;
import com.huffazai.huffazai.adapter.DetectedVerseAdapter;
import com.huffazai.huffazai.model.Verse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DetectedVerseActivity extends AppCompatActivity {

    private RecyclerView detectedVerseRecyclerView;
    private DetectedVerseAdapter adapter;
    private List<String> detectedVerses = new ArrayList();

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference detectedVersesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detected_verse);

        detectedVerseRecyclerView = findViewById(R.id.detected_verse_recycler_view);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        detectedVersesRef = FirebaseDatabase.getInstance().getReference("detectedVerses");

        String detectedVerse = getIntent().getStringExtra("detectedVerse");

        if (detectedVerse != null) {
            // Add the detected verse to the list
            detectedVerses.add(detectedVerse);

            // Store the detected verse in Firebase
            storeDetectedVerseInFirebase(detectedVerse);
        }

        adapter = new DetectedVerseAdapter(detectedVerses);
        detectedVerseRecyclerView.setAdapter(adapter);
        detectedVerseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load detected verses from Firebase
        loadDetectedVersesFromFirebase();
    }

    private void storeDetectedVerseInFirebase(String verseText) {
        if (firebaseAuth.getCurrentUser() != null) {
            // Generate a unique key for the detected verse
            String verseId = detectedVersesRef.push().getKey();

            // Store the detected verse in Firebase with the user's unique ID
            detectedVersesRef.child(firebaseAuth.getCurrentUser().getUid()).child(verseId).setValue(verseText);
        }
    }

    private void loadDetectedVersesFromFirebase() {
        if (firebaseAuth.getCurrentUser() != null) {
            detectedVersesRef.child(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            detectedVerses.clear();
                            for (DataSnapshot verseSnapshot : dataSnapshot.getChildren()) {
                                String verseText = verseSnapshot.getValue(String.class);
                                detectedVerses.add(verseText);
                            }

                            // Update the adapter with the new list of detected verses
                            adapter.setDetectedVerses(detectedVerses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors or log the error message
                        }
                    });
        }
    }
}
