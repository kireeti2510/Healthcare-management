package com.healthcare.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class WaitlistEntry {
    private final UUID waitlistId;
    private final UUID patientId;
    private final UUID clinicianId;
    private final LocalDateTime requestedAt;
    private final LocalDateTime requestedSlot;
    private final String roomId;
    private final String reasonForVisit;

    public WaitlistEntry(UUID waitlistId,
                         UUID patientId,
                         UUID clinicianId,
                         LocalDateTime requestedAt,
                         LocalDateTime requestedSlot,
                         String roomId,
                         String reasonForVisit) {
        this.waitlistId = waitlistId;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.requestedAt = requestedAt;
        this.requestedSlot = requestedSlot;
        this.roomId = roomId;
        this.reasonForVisit = reasonForVisit;
    }

    public UUID getWaitlistId() { return waitlistId; }
    public UUID getPatientId() { return patientId; }
    public UUID getClinicianId() { return clinicianId; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public LocalDateTime getRequestedSlot() { return requestedSlot; }
    public String getRoomId() { return roomId; }
    public String getReasonForVisit() { return reasonForVisit; }
}
