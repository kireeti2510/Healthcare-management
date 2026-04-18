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

    public Appointment(UUID appointmentId, UUID patientId, UUID clinicianId, LocalDateTime scheduledAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.clinicianId = clinicianId;
        this.scheduledAt = scheduledAt;
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
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
