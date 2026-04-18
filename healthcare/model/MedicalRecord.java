package com.healthcare.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// State: CREATED -> UPDATED -> ARCHIVED
public class MedicalRecord {
    public static final int RETENTION_YEARS = 7;

    public enum State { CREATED, UPDATED, ARCHIVED }

    private final UUID recordId;
    private final UUID patientId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime archivedAt;
    private final List<String> notes;
    private State state = State.CREATED;

    public MedicalRecord(UUID patientId) {
        this.recordId = UUID.randomUUID();
        this.patientId = patientId;
        this.createdAt = LocalDateTime.now();
        this.notes = new ArrayList<>();
    }

    private MedicalRecord(UUID recordId,
                          UUID patientId,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          LocalDateTime archivedAt,
                          State state,
                          List<String> notes) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
        this.state = state;
        this.notes = new ArrayList<>(notes != null ? notes : List.of());
    }

    public static MedicalRecord fromPersistence(UUID recordId,
                                                UUID patientId,
                                                LocalDateTime createdAt,
                                                LocalDateTime updatedAt,
                                                LocalDateTime archivedAt,
                                                State state,
                                                List<String> notes) {
        return new MedicalRecord(recordId, patientId, createdAt, updatedAt, archivedAt, state, notes);
    }

    public void addNote(String note) {
        if (state == State.ARCHIVED) throw new IllegalStateException("Cannot modify archived record");
        if (note == null || note.isBlank()) throw new IllegalArgumentException("Note cannot be empty");
        notes.add(note);
        updatedAt = LocalDateTime.now();
        state = State.UPDATED;
    }

    public void archive() {
        if (state == State.ARCHIVED) return;
        state = State.ARCHIVED;
        archivedAt = LocalDateTime.now();
    }

    public boolean isRetentionEligible(LocalDateTime now) {
        return !createdAt.isAfter(now.minusYears(RETENTION_YEARS));
    }

    public LocalDateTime getRetentionUntil() {
        return createdAt.plusYears(RETENTION_YEARS);
    }

    public List<String> getHistory() { return Collections.unmodifiableList(notes); }
    public List<String> getNotes() { return Collections.unmodifiableList(notes); }
    public UUID getRecordId() { return recordId; }
    public UUID getPatientId() { return patientId; }
    public State getState() { return state; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getArchivedAt() { return archivedAt; }
}
