package com.huffazai.huffazai;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.huffazai.huffazai.activities.AccountActivity;
import com.huffazai.huffazai.activities.DetectedVerseActivity;
import com.huffazai.huffazai.activities.NotDetectedVerseActivity;
import com.huffazai.huffazai.activities.SurahDetailActivity;
import com.huffazai.huffazai.activities.SurahHistoryActivity;
import com.huffazai.huffazai.adapter.SurahAdapter;
import com.huffazai.huffazai.common.Common;
import com.huffazai.huffazai.listener.SurahListener;
import com.huffazai.huffazai.model.Surah;
import com.huffazai.huffazai.viewmodel.SurahViewModel;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurahListener {

    private RecyclerView recyclerView;
    private SurahAdapter surahAdapter;
    private SurahViewModel surahViewModel;
    private List<Surah> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        if(getSupportActionBar()!= null){
            getSupportActionBar().show();
        }


        list = new ArrayList<>();

        recyclerView = findViewById(R.id.surahRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        surahAdapter = new SurahAdapter(this, list, this);
        recyclerView.setAdapter(surahAdapter);

        surahViewModel = new ViewModelProvider(this).get(SurahViewModel.class);
        surahViewModel.getSurah().observe(this, surahResponse -> {
            Log.d("iii", "onCreate: " + surahResponse.getList().size());

            list.clear(); // Clear the list before adding new data

            for (int i = 0; i < surahResponse.getList().size(); i++) {
                list.add(new Surah(
                        surahResponse.getList().get(i).getNumber(),
                        String.valueOf(surahResponse.getList().get(i).getName()),
                        String.valueOf(surahResponse.getList().get(i).getEnglishName()),
                        String.valueOf(surahResponse.getList().get(i).getEnglishNameTranslation()),
                        surahResponse.getList().get(i).getNumberofAyahs(),
                        String.valueOf(surahResponse.getList().get(i).getRevelationType())
                ));
            }

            surahAdapter.notifyDataSetChanged(); // Notify the adapter of data changes
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_setting) {
            // Handle the "Settings" menu item click
            Intent settingsIntent = new Intent(this, AccountActivity.class);
            startActivity(settingsIntent);
            return true;
        } else if (itemId == R.id.DetectedVerse) {
            // Handle the "Detected Verse History" menu item click
            Intent detectedVerseIntent = new Intent(this, DetectedVerseActivity.class);
            startActivity(detectedVerseIntent);
            return true;
        } else if (itemId == R.id.NotDetectedVerse) {
            // Handle the "Not Detected Verse History" menu item click
            Intent notDetectedVerseIntent = new Intent(this, NotDetectedVerseActivity.class);
            startActivity(notDetectedVerseIntent);
            return true;
        } else {
            // Handle other menu items if needed
            return super.onOptionsItemSelected(item);
        }
    }




    @Override
    public void onSurahListener(int position) {
        Intent intent = new Intent(MainActivity.this, SurahDetailActivity.class);
        intent.putExtra(Common.SURAH_NO,list.get(position).getNumber());
        intent.putExtra(Common.SURAH_NAME,list.get(position).getName());
        intent.putExtra(Common.SURAH_TOTAL_AYA,list.get(position).getNumberofAyahs());
        intent.putExtra(Common.SURAH_TYPE,list.get(position).getRevelationType());
        intent.putExtra(Common.SURAH_TRANSLATION, list.get(position).getEnglishNameTranslation());
        startActivity(intent);
    }
}
