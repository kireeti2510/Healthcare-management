package com.healthcare.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// State: CREATED -> UPDATED -> ARCHIVED
public class MedicalRecord {
    public enum State { CREATED, UPDATED, ARCHIVED }

    private UUID recordId;
    private UUID patientId;
    private LocalDateTime createdAt;
    private List<String> notes = new ArrayList<>();
    private State state = State.CREATED;

    public MedicalRecord(UUID recordId, UUID patientId) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.createdAt = LocalDateTime.now();
    }

    public void addNote(String note) {
        if (state == State.ARCHIVED) throw new IllegalStateException("Cannot modify archived record");
        notes.add(note);
        state = State.UPDATED;
    }

    public void archive() { state = State.ARCHIVED; }

    public List<String> getHistory() { return notes; }
    public UUID getRecordId() { return recordId; }
    public UUID getPatientId() { return patientId; }
    public State getState() { return state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
