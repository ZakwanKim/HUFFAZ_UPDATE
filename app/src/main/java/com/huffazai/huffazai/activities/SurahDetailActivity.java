package com.huffazai.huffazai.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.huffazai.huffazai.R;
import com.huffazai.huffazai.adapter.SurahDetailAdapter;
import com.huffazai.huffazai.common.Common;
import com.huffazai.huffazai.database.DatabaseHelperClass;
import com.huffazai.huffazai.model.SurahDetail;
import com.huffazai.huffazai.viewmodel.SurahDetailViewModel;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SurahDetailActivity extends AppCompatActivity {

    private TextView surahName, surahType, surahTranslation, voicetext;
    private int no;

    private RecyclerView recyclerView;
    private List<SurahDetail> list;
    private SurahDetailAdapter adapter;
    private SurahDetailViewModel surahDetailViewModel;
    private String english = "english_hilali_khan";


    private ImageButton microphoneButton;

    // Firebase variables
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userDetectedVersesRef;
    private DatabaseReference userNotDetectedVersesRef;
    // DatabaseHelperClass instance
    private DatabaseHelperClass databaseHelper;

    private static final int REQUEST_SPEECH_RECOGNITION = 300;
    private static final int PERMISSION_REQUEST_CODE = 101;

    private boolean intentionallyStopped = false; // Flag to check if recognition was intentionally stopped
    private static final int MATCH_FOUND_DELAY_MS = 6000; // 5 seconds Set the duration for the background color to return to normal (in milliseconds)

    private SearchView searchView; // Added SearchView

    private List<String> matchingVerses = new ArrayList<>();
    private List<String> nonMatchingVerses = new ArrayList<>();

    private String qari = "mahmood_khaleel_al-husaree_iza3a";
    Handler handler = new Handler();
    SeekBar seekBar;
    TextView startTime, totalTime;
    ImageButton playButton;
    MediaPlayer mediaPlayer;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surah_detail);

        surahName = findViewById(R.id.surah_name);
        surahType = findViewById(R.id.type);
        surahTranslation = findViewById(R.id.translation);
        recyclerView = findViewById(R.id.surah_detail_rv);
        voicetext = findViewById(R.id.voicetext);

        no = getIntent().getIntExtra(Common.SURAH_NO, 0);
        surahName.setText(getIntent().getStringExtra(Common.SURAH_NAME));

        int totalAyah = getIntent().getIntExtra(Common.SURAH_TOTAL_AYA, 0);
        surahType.setText(getIntent().getStringExtra(Common.SURAH_TYPE) + " " + totalAyah + " Ayat");

        surahTranslation.setText(getIntent().getStringExtra(Common.SURAH_TRANSLATION));

        firebaseAuth = FirebaseAuth.getInstance();
        userDetectedVersesRef = FirebaseDatabase.getInstance().getReference("user_detected_verses");
        userNotDetectedVersesRef = FirebaseDatabase.getInstance().getReference().child("not_detected_verses");

        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();

        try {
            listenAudio(qari);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        microphoneButton = findViewById(R.id.microphone_button);

        // Initialize the SearchView
        searchView = findViewById(R.id.search_view);
        searchView.setQueryHint("Search Ayat Number");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the search query when the user submits it.
                adapter.filter(query); // Filter the RecyclerView with the search query.
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle the search query as the user types (optional).
                adapter.filter(newText); // Filter the RecyclerView with the search query.
                return true;
            }
        });


        microphoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechRecognition();
            }
        });

        surahTranslation(english, no);
    }

    private void surahTranslation(String lan, int id) {
        if (list.size() > 0) {
            list.clear();
        }

        surahDetailViewModel = new ViewModelProvider(this).get(SurahDetailViewModel.class);
        surahDetailViewModel.getSurahDetail(lan, id).observe(this, surahDetailResponse -> {

            for (int i = 0; i < surahDetailResponse.getList().size(); i++) {
                list.add(new SurahDetail(surahDetailResponse.getList().get(i).getId(),
                        surahDetailResponse.getList().get(i).getSura(),
                        surahDetailResponse.getList().get(i).getAya(),
                        surahDetailResponse.getList().get(i).getArabic_text(),
                        surahDetailResponse.getList().get(i).getTranslation(),
                        surahDetailResponse.getList().get(i).getFootnotes()));
            }

            if (list.size() != 0) {
                adapter = new SurahDetailAdapter(this, list);
                recyclerView.setAdapter(adapter);
            }
        });
    }

    private void startSpeechRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar");

        try {
            startActivityForResult(intent, REQUEST_SPEECH_RECOGNITION);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SPEECH_RECOGNITION) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    String voiceText = results.get(0);

                    voicetext.setText(voiceText);

                    // Handle the recognized text as needed.
                    // For example, you can use voiceText to identify Quranic verses.
                    matchVoiceTextToArabicText(voiceText);
                }
            }
        }
    }


    // Inside your SurahDetailActivity class

    private void matchVoiceTextToArabicText(String voiceText) {
        Log.d("Voice Text", "Voice Text: " + voiceText);

        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        boolean matchFound = false; // Flag to check if a match is found

        // Split the voice text using multiple pause marks: ۚ, ۘ, ۙ, ۗ, ۖ, and two consecutive ۛ (ۛۛ)
        String[] pauseMarks = {"ۚ", "ۘ", "ۙ", "ۗ", "ۖ", "ۛۛ"};
        String regex = String.join("|", pauseMarks);
        String[] verses = voiceText.split(regex);

        for (String verse : verses) {
            verse = verse.trim(); // Remove leading/trailing spaces
            boolean verseMatched = false; // Flag to check if the verse matches

            for (SurahDetail surahDetail : list) {
                String normalizedArabicText = surahDetail.getNormalizedArabicText();
                Log.d("Arabic Text", "Arabic Text: " + normalizedArabicText);

                if (normalizedArabicText != null) {
                    int distance = levenshteinDistance.apply(verse, normalizedArabicText);
                    int threshold = 2;

                    if (distance <= threshold) {
                        voicetext.setBackgroundColor(getResources().getColor(R.color.colorMatched));
                        Log.d("Matching verse found", "Matching verse found: " + normalizedArabicText);

                        // Display the matching verse in the TextView
                        voicetext.setText(verse);

                        // Add the detected verse to the matching list
                        matchingVerses.add(verse);

                        // For example, you can display a message with the matching verse:
                        Toast.makeText(this, "Matching verse found: " + normalizedArabicText, Toast.LENGTH_SHORT).show();

                        // Send the detected text to DetectedVerseActivity
                        handleDetectedVerse(normalizedArabicText);

                        matchFound = true;
                        verseMatched = true; // Set the verse as matched
                        intentionallyStopped = false; // Reset the flag
                        break;
                    }
                }
            }

            if (!verseMatched) {
                // If the verse didn't match, add it to the non-matching list
                nonMatchingVerses.add(verse);

                // For not detected verses, you can call the method to handle them
                handleNotDetectedVerse(verse);


            }
        }

        if (!matchFound) {
            // No matching verse found, display a toast to notify the user and set the background color to red
            Toast.makeText(this, "No matching verse found.", Toast.LENGTH_SHORT).show();
            voicetext.setBackgroundColor(getResources().getColor(R.color.colorNotMatched)); // Assuming you have a red color defined in your resources
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    voicetext.setBackgroundColor(getResources().getColor(android.R.color.transparent)); // Set the background color to transparent
                }
            }, MATCH_FOUND_DELAY_MS);
        }

        // Handle the specific cases
        handleSpecificCases(voiceText);
    }


    private void handleSpecificCases(String voiceText) {
        // Create a mapping of voice text patterns to their corresponding replacements
        Map<String, String> patternReplacements = new HashMap<>();
        patternReplacements.put("الف لام ميم", "الم");
        patternReplacements.put("الف لام ميم صاد", "المص");
        patternReplacements.put("الف لام را", "الر");
        patternReplacements.put("الف لام ميم ر", "المر");
        patternReplacements.put("ك ه ي ع ص", "كهيعص");
        patternReplacements.put("ط س م", "طسم");
        patternReplacements.put("ط س", "طس");
        patternReplacements.put("ياسين", "يس");
        patternReplacements.put("نون", "ن");

        for (String pattern : patternReplacements.keySet()) {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(voiceText);
            if (m.find()) {
                String replacement = patternReplacements.get(pattern);
                voicetext.setText(replacement);

                // Match found, you can handle it as needed.
                voicetext.setBackgroundColor(getResources().getColor(R.color.colorMatched));

                // Log the matching text to the console for debugging
                Log.d("Matching text found", "Matching text found: " + replacement);

                // For example, you can display a message with the matching text:
                Toast.makeText(this, "Matching text found: " + replacement, Toast.LENGTH_SHORT).show();

                // You can break the loop if you want to stop after the first match.
                break;
            }
        }
    }

    private boolean checkPermissions() {
        String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        List<String> permissionsToRequest = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    private void handleDetectedVerse(String detectedText) {
        // Check if the user is authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            // Store the detected verse in Firebase Realtime Database
            storeDetectedVerseInFirebase(detectedText); // Call the method here
        } else {
            // Handle the case when the user is not authenticated
            // For example, you can display an error message or prompt the user to sign in.
        }
    }


    private void storeDetectedVerseInFirebase(String verse) {
        // Check if the user is authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();

            // Generate a unique key for the detected verse
            String verseId = userDetectedVersesRef.child(userId).push().getKey();

            // Store the detected verse in Firebase Realtime Database
            userDetectedVersesRef.child(userId).child(verseId).setValue(verse);

            // Navigate to DetectedVerseActivity
            Intent detectedVerseIntent = new Intent(this, DetectedVerseActivity.class);
            detectedVerseIntent.putExtra("detectedVerse", verse);
            startActivity(detectedVerseIntent);
        } else {
            // Handle the case when the user is not authenticated
            // For example, you can display an error message or prompt the user to sign in.
        }
    }


    private void storeNotDetectedVerseInFirebase(String notDetectedText) {
        // Check if the user is authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();

            // Generate a unique key for the not detected verse
            String verseId = userNotDetectedVersesRef.child(userId).push().getKey();

            // Store the not detected verse in Firebase Realtime Database
            userNotDetectedVersesRef.child(userId).child(verseId).setValue(notDetectedText);

            // Navigate to NotDetectedVerseActivity
            Intent notDetectedVerseIntent = new Intent(this, NotDetectedVerseActivity.class);
            notDetectedVerseIntent.putExtra("notDetectedVerse", notDetectedText);
            startActivity(notDetectedVerseIntent);
        } else {
            // Handle the case when the user is not authenticated
            // For example, you can display an error message or prompt the user to sign in.
        }
    }


    private void handleNotDetectedVerse(String notDetectedText) {
        // Check if the user is authenticated
        if (firebaseAuth.getCurrentUser() != null) {
            // Store the detected verse in Firebase Realtime Database
            storeNotDetectedVerseInFirebase(notDetectedText); // Call the method here
        } else {
            // Handle the case when the user is not authenticated
            // For example, you can display an error message or prompt the user to sign in.
        }
    }


    private void listenAudio(String qari) throws IOException {
        playButton = findViewById(R.id.play_button);
        startTime = findViewById(R.id.start_time);
        totalTime = findViewById(R.id.total_time);
        seekBar = findViewById(R.id.seekBar);

        mediaPlayer = new MediaPlayer();
        seekBar.setMax(100);

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    handler.removeCallbacks(updater);
                    mediaPlayer.pause();
                    playButton.setImageResource(R.drawable.baseline_play_circle_24);
                } else {
                    mediaPlayer.start();
                    playButton.setImageResource(R.drawable.baseline_pause_circle_filled_24);
                    updateSeekBar();
                }
            }
        });

        preparedMediaPlayer(qari);
        seekBar.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                SeekBar seekBar = (SeekBar) v;
                int playPosition = (mediaPlayer.getDuration() / 100) * seekBar.getProgress();
                mediaPlayer.seekTo(playPosition);
                startTime.setText(timeToMilliSecond(mediaPlayer.getCurrentPosition()));
                return false;
            }
        });

        mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                seekBar.setSecondaryProgress(percent);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                seekBar.setProgress(0);
                playButton.setImageResource(R.drawable.baseline_play_circle_24);
                startTime.setText("0:00");
                totalTime.setText("0:00");
                mediaPlayer.reset();
                try {
                    preparedMediaPlayer(qari);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void preparedMediaPlayer(String qari) throws IOException {
        if (no < 10) {
            str = "00" + no;
        } else if (no < 100) {
            str = "0" + no;
        } else if (no >= 100) {
            str = String.valueOf(no);
        }

        mediaPlayer.setDataSource("https://download.quranicaudio.com/quran/" + qari + "/" + str.trim() + ".mp3");
        mediaPlayer.prepare();
        totalTime.setText(timeToMilliSecond(mediaPlayer.getDuration()));
    }


    private Runnable updater = new Runnable() {
        @Override
        public void run(){
                updateSeekBar();
                long currentDuration = mediaPlayer.getCurrentPosition();
                startTime.setText(timeToMilliSecond(currentDuration));
        }
    };

    private void updateSeekBar(){
        if(mediaPlayer.isPlaying()){
            seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration()) *100));
            handler.postDelayed(updater, 1000);
        }

    }

    private String timeToMilliSecond(long milliSecond){

        String timerString = "";
        String secondString = "";

        int hours = (int) (milliSecond/(1000 * 60 * 60));
        int minutes = (int) (milliSecond % (1000 * 60 * 60)) / (1000 * 60);
        int second = (int) ((milliSecond % (1000 * 60 * 60))% (1000 * 60) /1000);

        if (hours>0){
            timerString = hours + ":";
        }
        if(second < 10){
            secondString = "0" + second;
        }else {
            secondString = "" + second;
        }
        timerString = timerString + minutes + ":" + secondString;
        return timerString;
    }

    @Override
    protected void onDestroy() {
        if(mediaPlayer.isPlaying()) {
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            playButton.setImageResource(R.drawable.baseline_play_circle_24);
        }
        super.onDestroy();
    }


    @Override
    protected void onStop() {
        if(mediaPlayer.isPlaying()) {
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            playButton.setImageResource(R.drawable.baseline_play_circle_24);
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mediaPlayer.isPlaying()) {
            handler.removeCallbacks(updater);
            mediaPlayer.pause();
            playButton.setImageResource(R.drawable.baseline_play_circle_24);
        }
        super.onPause();
    }
}