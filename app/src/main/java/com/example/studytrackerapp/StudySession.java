package com.example.studytrackerapp;

import com.google.firebase.Timestamp;

public class StudySession {
    private String userId;
    private String subject;
    private Timestamp startTime;
    private Timestamp endTime;
    private double focusTime;
    private int goal;
    private String notes;

    public StudySession() {
        // Needed for Firestore deserialization
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }

    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }

    public double getFocusTime() { return focusTime; }
    public void setFocusTime(double focusTime) { this.focusTime = focusTime; }

    public int getGoal() { return goal; }
    public void setGoal(int goal) { this.goal = goal; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}