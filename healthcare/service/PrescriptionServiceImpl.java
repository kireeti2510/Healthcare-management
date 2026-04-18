package com.healthcare.service;

import com.healthcare.model.*;
import com.healthcare.pattern.behavioral.AllergyCheckerHandler;
import com.healthcare.pattern.behavioral.DrugInteractionHandler;
import com.healthcare.pattern.behavioral.ConflictHandler;
import com.healthcare.model.enums.AppointmentStatus;
import com.healthcare.repository.IApptRepository;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IPrescriptionRepository;

import java.util.UUID;

/**
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 * Uses: Chain of Responsibility (behavioral) for conflict checking
 */
public class PrescriptionServiceImpl implements IPrescriptionService {
    private final IPrescriptionRepository repo;
    private final IApptRepository apptRepo;
    private final IAuditLogService auditLog;

    public PrescriptionServiceImpl(IPrescriptionRepository repo, IApptRepository apptRepo, IAuditLogService auditLog) {
        this.repo = repo;
        this.apptRepo = apptRepo;
        this.auditLog = auditLog;
    }

    @Override
    public Prescription createRx(Appointment appointment) {
        Prescription rx = new Prescription(UUID.randomUUID(), appointment.getAppointmentId());
        repo.save(rx);
        auditLog.log("CREATE_PRESCRIPTION:" + rx.getRxId(), appointment.getPatientId());
        return rx;
    }

    @Override
    public void issueRx(UUID rxId) {
        Prescription rx = repo.findById(rxId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxId));
        rx.issue();

        Appointment appt = apptRepo.findById(rx.getAppointmentId())
                .orElseThrow(() -> new RuntimeException("Linked appointment not found: " + rx.getAppointmentId()));
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot issue prescription for cancelled appointment.");
        }
        if (appt.getStatus() == AppointmentStatus.PENDING) {
            appt.confirm();
        }
        if (appt.getStatus() == AppointmentStatus.CONFIRMED) {
            appt.complete();
        }

        repo.update(rx);
        apptRepo.update(appt);
        auditLog.log("ISSUE_PRESCRIPTION:" + rxId, null);
        auditLog.log("COMPLETE_APPOINTMENT:" + appt.getAppointmentId(), appt.getPatientId());
    }

    @Override
    public ConflictResult checkConflicts(Prescription rx) {
        // Chain of Responsibility: AllergyChecker -> DrugInteractionChecker
        ConflictHandler chain = new AllergyCheckerHandler(new DrugInteractionHandler(null));
        return chain.check(rx);
    }
}
