package com.healthcare.repository;

import com.healthcare.model.MedicalRecord;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IMedicalRecordRepository {
    void save(MedicalRecord record);
    Optional<MedicalRecord> findById(UUID recordId);
    List<MedicalRecord> findByPatient(UUID patientId);
    void update(MedicalRecord record);
    void addNote(UUID recordId, String note);
}
