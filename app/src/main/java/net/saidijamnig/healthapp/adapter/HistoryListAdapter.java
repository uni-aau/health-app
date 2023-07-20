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
import net.saidijamnig.healthapp.databinding.RecyclerViewHistoryBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

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

    public HistoryListAdapter(ArrayList<History> list, Context context) {
        this.list = list;
        this.context = context;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    @NonNull
    @Override
    public HistoryListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerViewHistoryBinding binding = RecyclerViewHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CustomViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryListAdapter.CustomViewHolder holder, int position) {
        // Sets the animation for selected item
        /*if (selectedPosition == holder.getAdapterPosition()) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.selection_animation);
            holder.itemView.startAnimation(animation);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_color));
        } else {
            holder.itemView.clearAnimation();
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        }*/

        history = list.get(position);

        durationTv = holder.binding.recviewTextviewDuration;
        typeTv = holder.binding.recviewTextviewType;
        dateTv = holder.binding.recviewTextviewDate;
        distanceTv = holder.binding.recviewTextviewDistance;
        activityTrack = holder.binding.recviewImageTrack;

        addDataToRecyclerView();
        initializeImage();
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
        int hours = (durationInMilliSeconds / (1000 * 60 * 60)) % 24;
        int minutes = (durationInMilliSeconds / (1000 * 60)) % 60;
        int seconds = (durationInMilliSeconds / 1000) % 60;


        String unformattedDurationString = context.getString(R.string.text_history_duration);
        return String.format(unformattedDurationString, formatTime(hours), formatTime(minutes), formatTime(seconds));
    }

    private String formatTime(int value) {
        return String.format(Locale.getDefault(), Config.DURATION_FORMAT, value); // two digits and the leading is a zero if necessary
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerViewHistoryBinding binding;

        public CustomViewHolder(RecyclerViewHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

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
    }
}
