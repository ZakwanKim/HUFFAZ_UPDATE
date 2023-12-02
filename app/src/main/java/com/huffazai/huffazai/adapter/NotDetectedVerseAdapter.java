package com.huffazai.huffazai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huffazai.huffazai.R;

import java.util.List;

public class NotDetectedVerseAdapter extends RecyclerView.Adapter<NotDetectedVerseAdapter.ViewHolder> {

    private List<String> notDetectedVerses;

    public NotDetectedVerseAdapter(List<String> notDetectedVerses) {
        this.notDetectedVerses = notDetectedVerses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.verse_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String verse = notDetectedVerses.get(position);
        holder.verseTextView.setText(verse);

    }

    @Override
    public int getItemCount() {
        return notDetectedVerses.size();
    }

    // Method to update the list of not detected verses
    public void setNotDetectedVerses(List<String> notDetectedVerses) {
        this.notDetectedVerses = notDetectedVerses;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView verseTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            verseTextView = itemView.findViewById(R.id.verse_textview);
        }
    }
}

