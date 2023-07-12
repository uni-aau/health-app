package net.saidijamnig.healthapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.CustomViewHolder> {
    private final ArrayList<HistoryListElement> list;
    private final Context context;

    public HistoryListAdapter(ArrayList<HistoryListElement> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @NonNull
    @Override
    public HistoryListAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_history, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryListAdapter.CustomViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        private final View layoutView;

        public CustomViewHolder(View layoutView) {
            super(layoutView);
            this.layoutView = layoutView;
        }
    }
}
