package com.huffazai.huffazai.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.huffazai.huffazai.R;

import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.ViewHolder> {
    private List<String> recordingList;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(String recordingPath);
    }

    public RecordingAdapter(List<String> recordingList, OnItemClickListener onItemClickListener) {
        this.recordingList = recordingList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recording_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String recordingPath = recordingList.get(position);

        String recordingName = getRecordingName(recordingPath);

        holder.recordingNameTextView.setText(recordingName);

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(recordingPath);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView recordingNameTextView;
        ImageButton playButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recordingNameTextView = itemView.findViewById(R.id.recordingNameTextView);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }

    private String getRecordingName(String filePath) {
        String[] pathParts = filePath.split("/");
        return pathParts[pathParts.length - 1];
    }
}
