package com.healthcare.service;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.User;

import java.util.List;
import java.util.UUID;

public interface IMedicalRecordService {
    MedicalRecord createRecord(UUID patientId, User actor);
    MedicalRecord addEncounterNote(UUID recordId, String note, User actor);
    MedicalRecord archiveRecord(UUID recordId, String reason, User actor);
    MedicalRecord getRecord(UUID recordId, User actor);
    List<MedicalRecord> getPatientRecords(UUID patientId, User actor);
}
