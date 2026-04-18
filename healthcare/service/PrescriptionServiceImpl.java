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
    private final INotificationService notifSvc;

    public PrescriptionServiceImpl(
            IPrescriptionRepository repo,
            IApptRepository apptRepo,
            IAuditLogService auditLog,
            INotificationService notifSvc
    ) {
        this.repo = repo;
        this.apptRepo = apptRepo;
        this.auditLog = auditLog;
        this.notifSvc = notifSvc;
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
        issueRx(rxId, null, null);
    }

    @Override
    public void issueRx(UUID rxId, String pharmacistContact, String overrideReason) {
        Prescription rx = getPrescriptionOrThrow(rxId);
        reviewRx(rxId, overrideReason);

        rx = getPrescriptionOrThrow(rxId);
        Appointment appt = getAppointmentOrThrow(rx.getAppointmentId());
        validateAppointmentForIssue(appt);

        rx.issue();
        advanceAppointmentToCompleted(appt);

        repo.update(rx);
        apptRepo.update(appt);
        auditLog.log("ISSUE_PRESCRIPTION:" + rxId, appt.getPatientId());
        auditLog.log("PRESCRIPTION_EVENT:ISSUED:" + rxId, appt.getPatientId());
        auditLog.log("COMPLETE_APPOINTMENT:" + appt.getAppointmentId(), appt.getPatientId());
        notifyPharmacist(rx, pharmacistContact);
    }

    @Override
    public ConflictResult checkConflicts(UUID rxId) {
        Prescription rx = getPrescriptionOrThrow(rxId);
        return checkConflicts(rx);
    }

    @Override
    public ConflictResult reviewRx(UUID rxId, String overrideReason) {
        Prescription rx = repo.findById(rxId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxId));
        if (rx.getStatus() != com.healthcare.model.enums.RxStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT prescriptions can be reviewed for issuing.");
        }

        ConflictResult result = checkConflicts(rx);
        if (result.isHasConflict()) {
            auditLog.log("CONFLICT_ALERT:" + rxId + ":" + result.getSeverity(), null);
            String reason = overrideReason != null ? overrideReason.trim() : "";
            if (reason.isEmpty()) {
                throw new RuntimeException("Conflict found. Provide override reason or revise prescription.");
            }
            rx.setOverrideReason(reason);
            auditLog.log("REVIEW_PRESCRIPTION:" + rxId + ":OVERRIDE", null);
        } else {
            auditLog.log("REVIEW_PRESCRIPTION:" + rxId + ":NO_CONFLICT", null);
        }

        repo.update(rx);
        return result;
    }

    private Prescription getPrescriptionOrThrow(UUID rxId) {
        return repo.findById(rxId)
                .orElseThrow(() -> new RuntimeException("Prescription not found: " + rxId));
    }

    private Appointment getAppointmentOrThrow(UUID appointmentId) {
        return apptRepo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Linked appointment not found: " + appointmentId));
    }

    private void validateAppointmentForIssue(Appointment appt) {
        if (appt.getStatus() == AppointmentStatus.CANCELLED) {
            throw new RuntimeException("Cannot issue prescription for cancelled appointment.");
        }
    }

    private void advanceAppointmentToCompleted(Appointment appt) {
        if (appt.getStatus() == AppointmentStatus.PENDING) {
            appt.confirm();
        }
        if (appt.getStatus() == AppointmentStatus.CONFIRMED) {
            appt.complete();
        }
    }

    private void notifyPharmacist(Prescription rx, String pharmacistContact) {
        String resolvedContact = pharmacistContact != null && !pharmacistContact.isBlank()
                ? pharmacistContact.trim()
                : "pharmacy@clinic.local";
        String event = "PRESCRIPTION_ISSUED:" + rx.getRxId() + ":" + resolvedContact;
        notifSvc.notify(event);
        if (resolvedContact.contains("@")) {
            notifSvc.sendEmail(resolvedContact, event);
        } else {
            notifSvc.sendSMS(resolvedContact, event);
        }
    }

    @Override
    public ConflictResult checkConflicts(Prescription rx) {
        // Chain of Responsibility: AllergyChecker -> DrugInteractionChecker
        ConflictHandler chain = new AllergyCheckerHandler(new DrugInteractionHandler(null));
        return chain.check(rx);
    }
}
