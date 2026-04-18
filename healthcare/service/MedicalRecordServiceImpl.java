package com.healthcare.service;

import com.healthcare.model.MedicalRecord;
import com.healthcare.model.User;
import com.healthcare.model.enums.Role;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IMedicalRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class MedicalRecordServiceImpl implements IMedicalRecordService {
    private final IMedicalRecordRepository repo;
    private final IAuditLogService auditLog;

    public MedicalRecordServiceImpl(IMedicalRecordRepository repo, IAuditLogService auditLog) {
        this.repo = repo;
        this.auditLog = auditLog;
    }

    @Override
    public MedicalRecord createRecord(UUID patientId, User actor) {
        requireRole(actor, Role.CLINICIAN);

        MedicalRecord record = new MedicalRecord(patientId);
        repo.save(record);
        auditLog.log("CREATE_MEDICAL_RECORD:" + record.getRecordId(), actor.getUserId());
        return record;
    }

    @Override
    public MedicalRecord addEncounterNote(UUID recordId, String note, User actor) {
        requireRole(actor, Role.CLINICIAN);
        MedicalRecord record = findRecordOrThrow(recordId);

        record.addNote(note);
        repo.addNote(record.getRecordId(), note);
        repo.update(record);
        auditLog.log("UPDATE_MEDICAL_RECORD:" + recordId, actor.getUserId());
        return record;
    }

    @Override
    public MedicalRecord archiveRecord(UUID recordId, String reason, User actor) {
        requireRole(actor, Role.CLINICIAN, Role.CLINIC_ADMIN);
        MedicalRecord record = findRecordOrThrow(recordId);

        if (record.getState() == MedicalRecord.State.ARCHIVED) {
            return record;
        }

        boolean retentionEligible = record.isRetentionEligible(LocalDateTime.now());
        boolean adminNoEncounterArchive = actor.getRole() == Role.CLINIC_ADMIN && record.getNotes().isEmpty();
        if (!retentionEligible && !adminNoEncounterArchive) {
            throw new IllegalStateException(
                    "Retention policy not met. Record can be archived on or after " + record.getRetentionUntil());
        }

        record.archive();
        repo.update(record);
        String normalizedReason = (reason == null || reason.isBlank()) ? "NO_REASON_PROVIDED" : reason;
        auditLog.log("ARCHIVE_MEDICAL_RECORD:" + recordId + ":" + normalizedReason, actor.getUserId());
        return record;
    }

    @Override
    public MedicalRecord getRecord(UUID recordId, User actor) {
        MedicalRecord record = findRecordOrThrow(recordId);
        ensureReadAccess(record, actor);
        return record;
    }

    @Override
    public List<MedicalRecord> getPatientRecords(UUID patientId, User actor) {
        ensurePatientOrPrivileged(actor, patientId);
        return repo.findByPatient(patientId);
    }

    private MedicalRecord findRecordOrThrow(UUID recordId) {
        return repo.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Medical record not found: " + recordId));
    }

    private void ensureReadAccess(MedicalRecord record, User actor) {
        ensurePatientOrPrivileged(actor, record.getPatientId());
    }

    private void ensurePatientOrPrivileged(User actor, UUID patientId) {
        Role role = actor.getRole();
        if (role == Role.CLINICIAN || role == Role.CLINIC_ADMIN || role == Role.RECEPTIONIST) {
            return;
        }
        if (role == Role.PATIENT && actor.getUserId().equals(patientId)) {
            return;
        }
        throw new SecurityException("No permission to access requested medical records.");
    }

    private void requireRole(User actor, Role... allowedRoles) {
        for (Role role : allowedRoles) {
            if (actor.getRole() == role) {
                return;
            }
        }
        throw new SecurityException("Role " + actor.getRole() + " is not allowed for this operation.");
    }
}
