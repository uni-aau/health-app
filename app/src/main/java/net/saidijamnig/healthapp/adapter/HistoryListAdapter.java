package net.saidijamnig.healthapp.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.saidijamnig.healthapp.Config;
import net.saidijamnig.healthapp.R;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.databinding.NoDataViewBinding;
import net.saidijamnig.healthapp.databinding.RecyclerViewHistoryBinding;
import net.saidijamnig.healthapp.util.TextFormatHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.CustomViewHolder> {
    private static final String TAG = "HA-main";
    private final ArrayList<History> list;
    private final Context context;
    private TextView durationTv;
    private TextView typeTv;
    private TextView dateTv;
    private TextView distanceTv;
    private ImageView activityTrack;
    private History history;
    private OnItemLongClickListener longClickListener;
    private int selectedPosition = RecyclerView.NO_POSITION; // Initialize with an invalid position
    private boolean isEmpty = false;

    public HistoryListAdapter(List<History> list, Context context) {
        this.list = (ArrayList<History>) list;
        this.context = context;

        if(list.isEmpty()) {
            isEmpty = true;
            list.add(new History());
        }
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public HistoryListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(!isEmpty) {
            RecyclerViewHistoryBinding binding = RecyclerViewHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CustomViewHolder(binding);
        } else {
            NoDataViewBinding binding = NoDataViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CustomViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryListAdapter.CustomViewHolder holder, int position) {
        if(holder.historyBinding != null) {
            history = list.get(position);

            durationTv = holder.historyBinding.recviewTextviewDuration;
            typeTv = holder.historyBinding.recviewTextviewType;
            dateTv = holder.historyBinding.recviewTextviewDate;
            distanceTv = holder.historyBinding.recviewTextviewDistance;
            activityTrack = holder.historyBinding.recviewImageTrack;

            addDataToRecyclerView();
            initializeImage();
        }
    }

    private void addDataToRecyclerView() {
        int durationInMilliSeconds = Integer.parseInt(history.durationInMilliSeconds);
        durationTv.setText(formatDuration(durationInMilliSeconds));

        dateTv.setText(history.activityDate);

        String unformattedTypeString = context.getString(R.string.text_history_type);
        String formattedTypeString = String.format(unformattedTypeString, history.activityType);
        typeTv.setText(formattedTypeString);

        String unformattedDistanceString = context.getString(R.string.text_history_distance);
        String formattedDistanceString = String.format(unformattedDistanceString, history.activityDistance);
        distanceTv.setText(formattedDistanceString);

        activityTrack.setContentDescription(Integer.toString(history.uid)); // used for unique id
    }

    private void initializeImage() {
        String fileName = history.imageTrackName;
        String fileNameNotNull = (fileName != null) ? fileName : "file";

        File internalDir = context.getApplicationContext().getFilesDir();
        File file = new File(internalDir, fileNameNotNull);

        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            activityTrack.setImageBitmap(bitmap);
            Log.i(TAG, "Successfully set image!");
        } else {
            Log.e(TAG, "File with name " + fileName + " does not exist. Using fallback image!");
            activityTrack.setImageResource(Config.FALLBACK_IMAGE_PATH);
        }
    }

    private String formatDuration(int durationInMilliSeconds) {
        String unformattedDurationString = context.getString(R.string.text_history_duration);
        return TextFormatHandler.getFormattedDurationTime(durationInMilliSeconds, unformattedDurationString);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerViewHistoryBinding historyBinding;
        private final NoDataViewBinding noDataViewBinding;

        public CustomViewHolder(RecyclerViewHistoryBinding binding) {
            super(binding.getRoot());
            this.noDataViewBinding = null;
            this.historyBinding = binding;

            binding.getRoot().setOnLongClickListener(view -> {
                if (longClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        longClickListener.onItemLongClick(binding.getRoot(), position);

                        int previousPosition = selectedPosition;
                        selectedPosition = position;

                        // Notifies the adapter of the item changes to trigger the animation and color change
                        notifyItemChanged(previousPosition);
                        notifyItemChanged(selectedPosition);
                        return true;
                    }
                }
                return false;
            });
        }

        public CustomViewHolder(NoDataViewBinding binding) {
            super(binding.getRoot());
            this.historyBinding = null;
            this.noDataViewBinding = binding;
        }
    }
}
