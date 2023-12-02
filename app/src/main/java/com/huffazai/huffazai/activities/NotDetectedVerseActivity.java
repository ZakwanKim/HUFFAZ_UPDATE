package com.huffazai.huffazai.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.huffazai.huffazai.R;
import com.huffazai.huffazai.adapter.NotDetectedVerseAdapter;
import com.huffazai.huffazai.model.Verse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotDetectedVerseActivity extends AppCompatActivity {

    private RecyclerView notDetectedVerseRecyclerView;
    private NotDetectedVerseAdapter adapter;
    private List<String> notDetectedVerses = new ArrayList<>();

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference notDetectedVersesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_detected_verse);

        notDetectedVerseRecyclerView = findViewById(R.id.not_detected_verse_recycler_view);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        notDetectedVersesRef = FirebaseDatabase.getInstance().getReference("notDetectedVerses");

        String notDetectedVerse = getIntent().getStringExtra("notDetectedVerse");

        if (notDetectedVerse != null) {
            // Add the not detected verse to the list
            notDetectedVerses.add(notDetectedVerse);

            // Store the not detected verse in Firebase
            storeNotDetectedVerseInFirebase(notDetectedVerse);
        }

        adapter = new NotDetectedVerseAdapter(notDetectedVerses);
        notDetectedVerseRecyclerView.setAdapter(adapter);
        notDetectedVerseRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load not detected verses from Firebase
        loadNotDetectedVersesFromFirebase();
    }

    private void storeNotDetectedVerseInFirebase(String verseText) {
        if (firebaseAuth.getCurrentUser() != null) {
            // Generate a unique key for the not detected verse
            String verseId = notDetectedVersesRef.push().getKey();

            // Store the not detected verse in Firebase with the user's unique ID
            notDetectedVersesRef.child(firebaseAuth.getCurrentUser().getUid()).child(verseId).setValue(verseText);
        }
    }

    private void loadNotDetectedVersesFromFirebase() {
        if (firebaseAuth.getCurrentUser() != null) {
            notDetectedVersesRef.child(firebaseAuth.getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            notDetectedVerses.clear();
                            for (DataSnapshot verseSnapshot : dataSnapshot.getChildren()) {
                                String verseText = verseSnapshot.getValue(String.class);
                                notDetectedVerses.add(verseText);
                            }

                            // Update the adapter with the new list of not detected verses
                            adapter.setNotDetectedVerses(notDetectedVerses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors or log the error message
                        }
                    });
        }
    }
}

