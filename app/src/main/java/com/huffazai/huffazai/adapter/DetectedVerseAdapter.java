package com.huffazai.huffazai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huffazai.huffazai.R;

import java.util.List;

public class DetectedVerseAdapter extends RecyclerView.Adapter<DetectedVerseAdapter.ViewHolder> {
    private List<String> detectedVerses;

    public DetectedVerseAdapter(List<String> detectedVerses) {
        this.detectedVerses = detectedVerses;
    }

    public void setDetectedVerses(List<String> detectedVerses) {
        this.detectedVerses = detectedVerses;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.verse_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String verse = detectedVerses.get(position);
        holder.verseTextView.setText(verse);
    }

    @Override
    public int getItemCount() {
        return detectedVerses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView verseTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            verseTextView = itemView.findViewById(R.id.verse_textview);
        }
    }
}
