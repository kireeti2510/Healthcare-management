package com.healthcare.controller;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.User;
import com.healthcare.service.IMedicalRecordService;

import java.util.List;
import java.util.UUID;

public class MedicalRecordController {
    private final IMedicalRecordService service;

    public MedicalRecordController(IMedicalRecordService service) {
        this.service = service;
    }

    public MedicalRecord createRecord(UUID patientId, User actor) {
        return service.createRecord(patientId, actor);
    }

    public MedicalRecord addEncounterNote(UUID recordId, String note, User actor) {
        return service.addEncounterNote(recordId, note, actor);
    }

    public MedicalRecord archiveRecord(UUID recordId, String reason, User actor) {
        return service.archiveRecord(recordId, reason, actor);
    }

    public MedicalRecord getRecord(UUID recordId, User actor) {
        return service.getRecord(recordId, actor);
    }

    public List<MedicalRecord> getPatientRecords(UUID patientId, User actor) {
        return service.getPatientRecords(patientId, actor);
    }
}
