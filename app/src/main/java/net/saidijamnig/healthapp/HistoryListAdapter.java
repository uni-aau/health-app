package net.saidijamnig.healthapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.databinding.RecyclerViewHistoryBinding;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.CustomViewHolder> {
    private final ArrayList<History> list;
    private final Context context;

    public HistoryListAdapter(ArrayList<History> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public HistoryListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewHistoryBinding binding = RecyclerViewHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryListAdapter.CustomViewHolder holder, int position) {
        History history = list.get(position);

        TextView durationTv = holder.binding.recviewTextviewDuration;
        TextView typeTv = holder.binding.recviewTextviewType;
        TextView dateTv = holder.binding.recviewTextviewDate;
        TextView distanceTv = holder.binding.recviewTextviewDistance;
        ImageView activityTrack = holder.binding.recviewImageTrack;

        int durationInMilliSeconds = Integer.parseInt(history.durationInMilliSeconds);
        durationTv.setText(formatDuration(durationInMilliSeconds));

        dateTv.setText(history.activityDate);

        String unformattedTypeString = context.getString(R.string.text_history_type);
        String formattedTypeString = String.format(unformattedTypeString, "-/-"); // TODO
        typeTv.setText(formattedTypeString);

        String unformattedDistanceString = context.getString(R.string.text_history_distance);
        String formattedDistanceString = String.format(unformattedDistanceString, history.activityDistance);
        distanceTv.setText(formattedDistanceString);

        // TODO Bildsetzung
    }

    private String formatDuration(int durationInMilliSeconds) {
        int hours = (durationInMilliSeconds / (1000 * 60 * 60)) % 24;
        int minutes = (durationInMilliSeconds / (1000 * 60)) % 60;
        int seconds = (durationInMilliSeconds / 1000) % 60;


        String unformattedDurationString = context.getString(R.string.text_history_duration);
        return String.format(unformattedDurationString, formatTime(hours), formatTime(minutes), formatTime(seconds));
    }


    // TODO auslagern in timeformatter klasse (auch gps)
    private String formatTime(int value) {
        return String.format(Locale.getDefault(), "%02d", value); // two digits and the leading is a zero if necessary
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerViewHistoryBinding binding;

        public CustomViewHolder(RecyclerViewHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
