package net.saidijamnig.healthapp;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import net.saidijamnig.healthapp.adapter.HistoryListAdapter;
import net.saidijamnig.healthapp.database.AppDatabase;
import net.saidijamnig.healthapp.database.History;
import net.saidijamnig.healthapp.database.HistoryDao;
import net.saidijamnig.healthapp.databinding.EntryDeletePopupBinding;
import net.saidijamnig.healthapp.databinding.FragmentHistoryBinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

public class HistoryFragment extends Fragment {

    private HistoryDao historyDao;
    private FragmentHistoryBinding binding;
    private ArrayList<History> historyElements = new ArrayList<>();
    HistoryListAdapter viewAdapter;
    private int position;

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
            requireActivity().runOnUiThread(() -> { // wait to finish before executing sendData method
                sendDataToRecyclerView();
                createLongPressListener();
            });
        });
        thread.start();
    }

    // TODO
/*    public void setActivityEntryAmount(int size) {
        String activityTitleEntryAmount = getResources().getQuantityString(R.plurals.text_history_activity_title, size, size);
        binding.textviewHistoryTitle.setText(activityTitleEntryAmount);
    }*/


    private void sendDataToRecyclerView() {
        viewAdapter = new HistoryListAdapter(historyElements, requireContext());

        RecyclerView.LayoutManager viewManager = new LinearLayoutManager(requireContext());

        RecyclerView recyclerView = binding.historyRecyclerView;
        recyclerView.setLayoutManager(viewManager);
        recyclerView.setAdapter(viewAdapter);
    }

    private void createLongPressListener() {
        viewAdapter.setOnItemLongClickListener((view, position) -> {
            this.position = position;
            openDeleteRequestPopUp();
        });
    }

    private void openDeleteRequestPopUp() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(requireContext());
        EntryDeletePopupBinding popupBinding = EntryDeletePopupBinding.inflate(LayoutInflater.from(requireContext()));
        TextView entryDeleteText = popupBinding.textViewDeleteEntry;
        alertDialogBuilder.setView(popupBinding.getRoot());
        alertDialogBuilder.setCancelable(false);

        String formattedEntryDeleteString = getResources().getString(R.string.text_popup_delete_question, String.valueOf(historyElements.get(position).uid));
        entryDeleteText.setText(formattedEntryDeleteString);

        AlertDialog alertDialog = alertDialogBuilder.create();

        popupBinding.buttonDeleteEntryNo.setOnClickListener(view1 -> alertDialog.dismiss());
        popupBinding.buttonDeleteEntryYes.setOnClickListener(view1 -> {
            deleteHistoryEntry();
            alertDialog.dismiss();
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void deleteHistoryEntry() {
        History historyElement = historyElements.get(position);
        int uid = historyElement.uid;

        String directory = requireActivity().getApplicationContext().getFilesDir().getAbsolutePath();
        String imageName = historyElement.imageTrackName;

        if (imageName != null && !imageName.isEmpty()) {
            Path imagePath = Paths.get(directory, imageName);
            try {
                Files.deleteIfExists(imagePath);
                Log.i("TAG", "Successfully deleted image " + imageName);
            } catch (IOException e) {
                Log.e("TAG", "Error deleting image file " + imageName + ": " + e);
            }
        } else {
            Log.w("TAG", "Image name with id" + uid + " is null - Moving forward to removal of element deletion!");
        }

        historyElements.remove(position);
        viewAdapter.notifyItemRemoved(position);
        deleteHistoryEntryFromDatabase(uid);
    }

    private void deleteHistoryEntryFromDatabase(int uid) {
        Thread thread = new Thread(() -> {
            historyDao.deleteHistoryEntryById(uid);
        });
        thread.start();
        Log.i("TAG", "Successfully deleted history entry " + uid);
    }
}