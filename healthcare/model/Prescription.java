package com.healthcare.model;

import com.healthcare.model.enums.RxStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Prescription {
    private UUID rxId;
    private UUID appointmentId;
    private LocalDateTime issuedAt;
    private RxStatus status;
    private String overrideReason;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription(UUID rxId, UUID appointmentId) {
        this.rxId = rxId;
        this.appointmentId = appointmentId;
        this.status = RxStatus.DRAFT;
    }

    // State machine transitions
    public void issue() {
        if (status == RxStatus.DRAFT) { status = RxStatus.ISSUED; issuedAt = LocalDateTime.now(); }
        else throw new IllegalStateException("Can only issue DRAFT prescriptions");
    }

    public void dispense() {
        if (status == RxStatus.ISSUED) status = RxStatus.DISPENSED;
        else throw new IllegalStateException("Can only dispense ISSUED prescriptions");
    }

    public void voidPrescription() {
        if (status != RxStatus.DISPENSED) status = RxStatus.VOID;
        else throw new IllegalStateException("Cannot void a DISPENSED prescription");
    }

    public void addItem(PrescriptionItem item) { items.add(item); }
    public void printToEHR() { System.out.println("Exporting prescription " + rxId + " to EHR..."); }

    public UUID getRxId() { return rxId; }
    public UUID getAppointmentId() { return appointmentId; }
    public RxStatus getStatus() { return status; }
    public LocalDateTime getIssuedAt() { return issuedAt; }
    public List<PrescriptionItem> getItems() { return items; }
    public String getOverrideReason() { return overrideReason; }
    public void setStatus(RxStatus status) { this.status = status; }
    public void setIssuedAt(LocalDateTime issuedAt) { this.issuedAt = issuedAt; }
    public void setOverrideReason(String r) { this.overrideReason = r; }
}
