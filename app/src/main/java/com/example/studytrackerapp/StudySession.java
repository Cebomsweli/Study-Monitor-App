package com.example.studytrackerapp;

import com.google.firebase.Timestamp;

public class StudySession {
    private String id;
    private String userId;
    private String subject;
    private String notes;
    private Timestamp startTime;
    private Timestamp endTime;
    private Timestamp dateSaved;
    private double focusTime;
    private long goal;

    // Empty constructor required for Firestore
    public StudySession() {}

    // Full constructor
    public StudySession(String id, String userId, String subject, String notes,
                        Timestamp startTime, Timestamp endTime, Timestamp dateSaved,
                        double focusTime, long goal) {
        this.id = id;
        this.userId = userId;
        this.subject = subject;
        this.notes = notes;
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateSaved = dateSaved;
        this.focusTime = focusTime;
        this.goal = goal;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getDateSaved() {
        return dateSaved;
    }

    public void setDateSaved(Timestamp dateSaved) {
        this.dateSaved = dateSaved;
    }

    public double getFocusTime() {
        return focusTime;
    }

    public void setFocusTime(double focusTime) {
        this.focusTime = focusTime;
    }

    public long getGoal() {
        return goal;
    }

    public void setGoal(long goal) {
        this.goal = goal;
    }

    // Helper method to calculate duration in minutes
    public long getDurationMinutes() {
        if (startTime != null && endTime != null) {
            return (endTime.toDate().getTime() - startTime.toDate().getTime()) / (60 * 1000);
        }
        return 0;
    }
}