package com.healthcare.view;

import com.healthcare.model.*;
import java.util.List;

/**
 * MVC View - Console output layer
 * All display logic lives here, not in controllers/services.
 */
public class ConsoleView {

    public void showPatient(Patient p) {
        System.out.println("=== Patient ===");
        System.out.println("ID       : " + p.getUserId());
        System.out.println("Email    : " + p.getEmail());
        System.out.println("DOB      : " + p.getDob());
        System.out.println("Allergies: " + p.getAllergies());
        System.out.println("Insurance: " + p.getInsuranceId());
    }

    public void showAppointment(Appointment a) {
        System.out.println("=== Appointment ===");
        System.out.println("ID       : " + a.getAppointmentId());
        System.out.println("Patient  : " + a.getPatientId());
        System.out.println("Clinician: " + a.getClinicianId());
        System.out.println("Time     : " + a.getScheduledAt());
        System.out.println("Status   : " + a.getStatus());
    }

    public void showPrescription(Prescription rx) {
        System.out.println("=== Prescription ===");
        System.out.println("RxID     : " + rx.getRxId());
        System.out.println("Status   : " + rx.getStatus());
        System.out.println("Items    : " + rx.getItems().size());
    }

    public void showConflict(ConflictResult cr) {
        if (cr.isHasConflict()) {
            System.out.println("[CONFLICT] Severity: " + cr.getSeverity());
            cr.getConflicts().forEach(c -> System.out.println("  - " + c));
        } else {
            System.out.println("[OK] No conflicts found.");
        }
    }

    public void showAuditLogs(List<String> logs) {
        System.out.println("=== Audit Logs ===");
        logs.forEach(System.out::println);
    }

    public void showMessage(String msg) { System.out.println("[INFO] " + msg); }
    public void showError(String msg) { System.err.println("[ERROR] " + msg); }
}
