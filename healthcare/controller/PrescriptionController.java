package com.healthcare.controller;

import com.healthcare.model.*;
import com.healthcare.service.IPrescriptionService;

import java.util.UUID;

/**
 * MVC Controller - Prescriptions
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 */
public class PrescriptionController {
    private final IPrescriptionService service;

    public PrescriptionController(IPrescriptionService service) { this.service = service; }

    public Prescription createPrescription(Appointment appt) { return service.createRx(appt); }

    public void issuePrescription(UUID rxId) { service.issueRx(rxId); }

    public ConflictResult checkConflicts(Prescription rx) { return service.checkConflicts(rx); }
}
