package com.healthcare.service;

import com.healthcare.model.Patient;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IPatientRepository;
import com.healthcare.util.PasswordUtil;

import java.util.UUID;

/**
 * Member: Kireeti Reddy P (PES1UG23CS307)
 */
public class PatientServiceImpl implements IPatientService {
    private final IPatientRepository repo;
    private final IAuditLogService auditLog;

    public PatientServiceImpl(IPatientRepository repo, IAuditLogService auditLog) {
        this.repo = repo;
        this.auditLog = auditLog;
    }

    @Override
    public Patient register(PatientDTO dto) {
        Patient p = new Patient.Builder()
                .email(dto.email)
                .passwordHash(PasswordUtil.hash(dto.password))
                .dob(dto.dob)
                .insuranceId(dto.insuranceId)
                .allergies(dto.allergies)
                .build();
        repo.saveJp(p);
        auditLog.log("REGISTER_PATIENT:" + p.getEmail(), p.getUserId());
        return p;
    }

    @Override
    public Patient findById(UUID id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Patient not found: " + id));
    }

    @Override
    public void updateProfile(Patient p) {
        repo.update(p);
        auditLog.log("UPDATE_PROFILE:" + p.getUserId(), p.getUserId());
    }
}
