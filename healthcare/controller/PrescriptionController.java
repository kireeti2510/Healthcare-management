package com.healthcare.controller;

import com.healthcare.model.*;
import com.healthcare.service.IPrescriptionService;

import java.util.List;
import java.util.UUID;

/**
 * MVC Controller - Prescriptions
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 */
public class PrescriptionController {
    private final IPrescriptionService service;

    public PrescriptionController(IPrescriptionService service) { this.service = service; }

    public Prescription createPrescription(Appointment appt) { return service.createRx(appt); }

    public Prescription createPrescription(Appointment appt, List<PrescriptionItem> items) {
        return service.createRx(appt, items);
    }

    public void issuePrescription(UUID rxId) { service.issueRx(rxId); }

    public void issuePrescription(UUID rxId, String pharmacistContact, String overrideReason) {
        service.issueRx(rxId, pharmacistContact, overrideReason);
    }

    public void dispensePrescription(UUID rxId) {
        service.dispenseRx(rxId);
    }

    public void voidPrescription(UUID rxId, String reason) {
        service.voidRx(rxId, reason);
    }

    public ConflictResult reviewPrescription(UUID rxId, String overrideReason) {
        return service.reviewRx(rxId, overrideReason);
    }

    public ConflictResult checkConflicts(Prescription rx) { return service.checkConflicts(rx); }

    public ConflictResult checkConflicts(UUID rxId) { return service.checkConflicts(rxId); }

    public void addPrescriptionItem(UUID rxId, PrescriptionItem item) {
        service.addRxItem(rxId, item);
    }

    public void revisePrescriptionItems(UUID rxId, List<PrescriptionItem> items) {
        service.reviseRxItems(rxId, items);
    }

    public Prescription getPrescription(UUID rxId) {
        return service.getRx(rxId);
    }
}
