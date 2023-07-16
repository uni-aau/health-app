package net.saidijamnig.healthapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import net.saidijamnig.healthapp.adapter.HistoryListAdapter;
import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.database.HistoryDao;
import net.saidijamnig.healthapp.databinding.FragmentHistoryBinding;

import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {

    private HistoryDao historyDao;
    private FragmentHistoryBinding binding;
    private ArrayList<History> historyElements = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);

        initializeDatabase();
        processDataFromDatabase();

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    private void initializeDatabase() {
        AppDatabase db;
        db = Room.databaseBuilder(requireContext(), AppDatabase.class, "history").build();
        historyDao = db.historyDao();
    }

    private void processDataFromDatabase() {
        Thread thread = new Thread(() -> {
            historyElements = (ArrayList<History>) historyDao.getWholeHistoryEntries();
            Collections.reverse(historyElements);
            requireActivity().runOnUiThread(this::sendDataToRecyclerView); // wait to finish before executing sendData method
        });
        thread.start();
    }

    // TODO
/*    public void setActivityEntryAmount(int size) {
        String activityTitleEntryAmount = getResources().getQuantityString(R.plurals.text_history_activity_title, size, size);
        binding.textviewHistoryTitle.setText(activityTitleEntryAmount);
    }*/


    private void sendDataToRecyclerView() {
        HistoryListAdapter viewAdapter = new HistoryListAdapter(historyElements, requireContext());
        viewAdapter.setOnItemLongClickListener((view, position) -> {
            // TODO
            System.out.println("Works");
        });


        RecyclerView.LayoutManager viewManager = new LinearLayoutManager(requireContext());

        RecyclerView recyclerView = binding.historyRecyclerView;
        recyclerView.setLayoutManager(viewManager);
        recyclerView.setAdapter(viewAdapter);
    }
}