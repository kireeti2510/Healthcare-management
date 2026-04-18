package com.healthcare.model;

import com.healthcare.model.enums.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {
    private UUID appointmentId;
    private UUID patientId;
    private UUID clinicianId;
    private LocalDateTime scheduledAt;
    private AppointmentStatus status;
    private String reasonForVisit;
    private String roomId;
    private Boolean referralsMet;
    private Boolean priorVisitsMet;
    private String overrideReason;

    public Appointment(UUID appointmentId, UUID patientId, UUID clinicianId, LocalDateTime scheduledAt) {
        this(appointmentId, patientId, clinicianId, scheduledAt, null, null);
    }

    public Appointment(UUID appointmentId,
                       UUID patientId,
                       UUID clinicianId,
                       LocalDateTime scheduledAt,
                       String reasonForVisit,
                       String roomId) {
        this(appointmentId, patientId, clinicianId, scheduledAt, reasonForVisit, roomId, null, null, null);
    }

    public Appointment(UUID appointmentId,
                       UUID patientId,
                       UUID clinicianId,
                       LocalDateTime scheduledAt,
                       String reasonForVisit,
                       String roomId,
                       Boolean referralsMet,
                       Boolean priorVisitsMet,
                       String overrideReason) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.scheduledAt = scheduledAt;
        this.reasonForVisit = reasonForVisit;
        this.roomId = roomId;
        this.referralsMet = referralsMet;
        this.priorVisitsMet = priorVisitsMet;
        this.overrideReason = overrideReason;
        this.status = AppointmentStatus.PENDING;
    }

    // State machine transitions
    public void confirm() {
        if (status == AppointmentStatus.PENDING) status = AppointmentStatus.CONFIRMED;
        else throw new IllegalStateException("Can only confirm PENDING appointments");
    }

    public void cancel() {
        if (status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED)
            status = AppointmentStatus.CANCELLED;
        else throw new IllegalStateException("Cannot cancel appointment in state: " + status);
    }

    public void complete() {
        if (status == AppointmentStatus.CONFIRMED) status = AppointmentStatus.COMPLETED;
        else throw new IllegalStateException("Can only complete CONFIRMED appointments");
    }

    public void reschedule(LocalDateTime newTime) {
        if (status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED)
            this.scheduledAt = newTime;
        else throw new IllegalStateException("Cannot reschedule appointment in state: " + status);
    }

    public UUID getAppointmentId() { return appointmentId; }
    public UUID getPatientId() { return patientId; }
    public UUID getClinicianId() { return clinicianId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public AppointmentStatus getStatus() { return status; }
    public String getReasonForVisit() { return reasonForVisit; }
    public String getRoomId() { return roomId; }
    public Boolean getReferralsMet() { return referralsMet; }
    public Boolean getPriorVisitsMet() { return priorVisitsMet; }
    public String getOverrideReason() { return overrideReason; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setReasonForVisit(String reasonForVisit) { this.reasonForVisit = reasonForVisit; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public void setReferralsMet(Boolean referralsMet) { this.referralsMet = referralsMet; }
    public void setPriorVisitsMet(Boolean priorVisitsMet) { this.priorVisitsMet = priorVisitsMet; }
    public void setOverrideReason(String overrideReason) { this.overrideReason = overrideReason; }
}
