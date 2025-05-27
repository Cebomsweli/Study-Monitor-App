package com.example.studytrackerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FragmentHistory extends Fragment {

    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<StudySession> studySessions = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        db = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        historyRecyclerView = view.findViewById(R.id.historyRecyclerView);

        // Initialize RecyclerView
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        historyAdapter = new HistoryAdapter(studySessions);
        historyRecyclerView.setAdapter(historyAdapter);

        loadHistoryData();

        return view;
    }

    private void loadHistoryData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        FirebaseFirestore.getInstance()
                .collection("Students")
                .whereEqualTo("userId", user.getUid())
//                .orderBy("dateSaved", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    showProgress(false);

                    if (task.isSuccessful()) {
                        studySessions.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            StudySession session = doc.toObject(StudySession.class);
                            if (session != null) {
                                session.setId(doc.getId());
                                studySessions.add(session);
                            }
                        }
                        historyAdapter.notifyDataSetChanged();

                        if (studySessions.isEmpty()) {
                            Toast.makeText(getContext(), "No sessions found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e instanceof FirebaseFirestoreException &&
                                ((FirebaseFirestoreException)e).getCode() == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                            // Show more helpful message about index
                            Toast.makeText(getContext(),
                                    "Please create the Firestore index for this query",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),
                                    "Error: " + (e != null ? e.getMessage() : "Unknown error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    // StudySession model class
    public static class StudySession {
        private String id;
        private String subject;
        private String notes;
        private Timestamp startTime;
        private Timestamp endTime;
        private Timestamp dateSaved;
        private double focusTime;
        private long goal;
        private String userId;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        public Timestamp getStartTime() { return startTime; }
        public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
        public Timestamp getEndTime() { return endTime; }
        public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
        public Timestamp getDateSaved() { return dateSaved; }
        public void setDateSaved(Timestamp dateSaved) { this.dateSaved = dateSaved; }
        public double getFocusTime() { return focusTime; }
        public void setFocusTime(double focusTime) { this.focusTime = focusTime; }
        public long getGoal() { return goal; }
        public void setGoal(long goal) { this.goal = goal; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    // RecyclerView Adapter
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

        private List<StudySession> sessions;

        public HistoryAdapter(List<StudySession> sessions) {
            this.sessions = sessions;
        }

        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new HistoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            StudySession session = sessions.get(position);

            // Format dates
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            holder.subjectTextView.setText(session.getSubject());
            holder.notesTextView.setText(session.getNotes());

            if (session.getStartTime() != null && session.getEndTime() != null) {
                String timeRange = timeFormat.format(session.getStartTime().toDate()) + " - " +
                        timeFormat.format(session.getEndTime().toDate());
                holder.timeRangeTextView.setText(timeRange);
            }

            if (session.getDateSaved() != null) {
                holder.dateTextView.setText(dateFormat.format(session.getDateSaved().toDate()));
            }

            holder.deleteButton.setOnClickListener(v -> deleteSession(session.getId()));
            holder.updateButton.setOnClickListener(v -> updateSession(session));
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        class HistoryViewHolder extends RecyclerView.ViewHolder {
            TextView subjectTextView, timeRangeTextView, dateTextView, notesTextView;
            MaterialButton updateButton, deleteButton;

            public HistoryViewHolder(@NonNull View itemView) {
                super(itemView);
                subjectTextView = itemView.findViewById(R.id.subjectTextView);
                timeRangeTextView = itemView.findViewById(R.id.timeRangeTextView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                notesTextView = itemView.findViewById(R.id.notesTextView);
                updateButton = itemView.findViewById(R.id.updateButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }

    private void deleteSession(String documentId) {
        showProgress(true);
        db.collection("Students").document(documentId)
                .delete()
                .addOnCompleteListener(task -> {
                    showProgress(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Session deleted", Toast.LENGTH_SHORT).show();
                        loadHistoryData(); // Refresh the list
                    } else {
                        Toast.makeText(getContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSession(StudySession session) {
        // Implement your update logic here
        Toast.makeText(getContext(), "Update session: " + session.getSubject(), Toast.LENGTH_SHORT).show();
    }
}