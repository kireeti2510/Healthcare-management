package com.healthcare.service;

import com.healthcare.model.*;
import com.healthcare.model.NotificationDelivery;
import com.healthcare.model.enums.RxStatus;
import com.healthcare.pattern.behavioral.AllergyCheckerHandler;
import com.healthcare.pattern.behavioral.DrugInteractionHandler;
import com.healthcare.pattern.behavioral.ConflictHandler;
import com.healthcare.model.enums.AppointmentStatus;
import com.healthcare.repository.IApptRepository;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IPrescriptionRepository;

import java.util.ArrayList;
import java.util.List;
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
        return createRx(appointment, List.of());
    }

    @Override
    public Prescription createRx(Appointment appointment, List<PrescriptionItem> items) {
        Prescription rx = new Prescription(UUID.randomUUID(), appointment.getAppointmentId());
        if (items != null && !items.isEmpty()) {
            validateItems(items);
            rx.setItems(items);
        }
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
        ensureHasItems(rx);
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
        Prescription rx = getPrescriptionOrThrow(rxId);
        if (rx.getStatus() != RxStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT prescriptions can be reviewed for issuing.");
        }
        ensureHasItems(rx);

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

    @Override
    public void addRxItem(UUID rxId, PrescriptionItem item) {
        if (item == null || !item.validate()) {
            throw new RuntimeException("Invalid prescription item. Drug, dosage, frequency, and duration are required.");
        }

        Prescription rx = getPrescriptionOrThrow(rxId);
        ensureDraft(rx);

        repo.addItem(rxId, item);
        auditLog.log("REVISE_PRESCRIPTION:" + rxId + ":ADD_ITEM", null);
    }

    @Override
    public void reviseRxItems(UUID rxId, List<PrescriptionItem> items) {
        Prescription rx = getPrescriptionOrThrow(rxId);
        ensureDraft(rx);
        validateItems(items);

        repo.replaceItems(rxId, new ArrayList<>(items));
        auditLog.log("REVISE_PRESCRIPTION:" + rxId + ":REPLACE_ITEMS", null);
    }

    @Override
    public Prescription getRx(UUID rxId) {
        return getPrescriptionOrThrow(rxId);
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

    private void ensureDraft(Prescription rx) {
        if (rx.getStatus() != RxStatus.DRAFT) {
            throw new RuntimeException("Only DRAFT prescriptions can be revised.");
        }
    }

    private void validateItems(List<PrescriptionItem> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("At least one prescription item is required.");
        }
        for (PrescriptionItem item : items) {
            if (item == null || !item.validate()) {
                throw new RuntimeException("One or more prescription items are invalid.");
            }
            if (item.getDrug() == null || item.getDrug().getDrugCode() == null || item.getDrug().getDrugCode().isBlank()) {
                throw new RuntimeException("Prescription item drug code is required.");
            }
        }
    }

    private void ensureHasItems(Prescription rx) {
        if (rx.getItems() == null || rx.getItems().isEmpty()) {
            throw new RuntimeException("Prescription must contain at least one item before review/issue.");
        }
    }

    private void notifyPharmacist(Prescription rx, String pharmacistContact) {
        String resolvedContact = pharmacistContact != null && !pharmacistContact.isBlank()
                ? pharmacistContact.trim()
                : "pharmacy@clinic.local";
        String event = "PRESCRIPTION_ISSUED:" + rx.getRxId() + ":" + resolvedContact;
        NotificationDelivery delivery = notifSvc.notify(event, resolvedContact);
        auditLog.log("NOTIFY_PHARMACIST:" + rx.getRxId() + ":" + delivery.getStatus(), null);
    }

    @Override
    public ConflictResult checkConflicts(Prescription rx) {
        // Chain of Responsibility: AllergyChecker -> DrugInteractionChecker
        ConflictHandler chain = new AllergyCheckerHandler(new DrugInteractionHandler(null));
        return chain.check(rx);
    }
}
