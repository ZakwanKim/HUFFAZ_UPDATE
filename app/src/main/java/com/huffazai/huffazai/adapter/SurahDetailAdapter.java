package com.huffazai.huffazai.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView; // Import this

import com.huffazai.huffazai.R;
import com.huffazai.huffazai.model.SurahDetail;

import java.util.ArrayList;
import java.util.List;

public class SurahDetailAdapter extends RecyclerView.Adapter<SurahDetailAdapter.ViewHolder> {

    private Context context;
    private List<SurahDetail> originalList;
    private List<SurahDetail> filteredList;

    public SurahDetailAdapter(Context context, List<SurahDetail> list) {
        this.context = context;
        this.originalList = list;
        this.filteredList = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.surah_detail_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ayaNo.setText(String.valueOf(filteredList.get(position).getAya()));
        holder.arabicText.setText(filteredList.get(position).getArabic_text());
        holder.translation.setText(filteredList.get(position).getTranslation());
    }

    @Override
    public int getItemCount() {
        return filteredList.size(); // Use filtered list size
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            query = query.toLowerCase();
            for (SurahDetail surahDetail : originalList) {
                if (String.valueOf(surahDetail.getAya()).contains(query)) {
                    filteredList.add(surahDetail);
                }
            }
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView ayaNo, arabicText, translation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ayaNo = itemView.findViewById(R.id.aya_no);
            arabicText = itemView.findViewById(R.id.arabic_text);
            translation = itemView.findViewById(R.id.translation);
        }
    }
}
