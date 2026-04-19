package com.healthcare.service;

import com.healthcare.model.Appointment;
import com.healthcare.model.WaitlistEntry;
import com.healthcare.pattern.behavioral.CancelAppointmentCommand;
import com.healthcare.pattern.behavioral.NotifyCommand;
import com.healthcare.repository.IApptRepository;
import com.healthcare.repository.IAuditLogService;
import com.healthcare.repository.IWaitlistRepository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Member: Jayanth Reddy (PES1UG23CS264)
 * Uses: Command Pattern (behavioral) for audit/notify on schedule/cancel
 */
public class AppointmentServiceImpl implements IAppointmentService {
    private final IApptRepository repo;
    private final IAuditLogService auditLog;
    private final INotificationService notifSvc;
    private final IWaitlistRepository waitlistRepo;

    public AppointmentServiceImpl(IApptRepository repo, IAuditLogService auditLog, INotificationService notifSvc) {
        this(repo, auditLog, notifSvc, null);
    }

    public AppointmentServiceImpl(IApptRepository repo,
                                  IAuditLogService auditLog,
                                  INotificationService notifSvc,
                                  IWaitlistRepository waitlistRepo) {
        this.repo = repo;
        this.auditLog = auditLog;
        this.notifSvc = notifSvc;
        this.waitlistRepo = waitlistRepo;
    }

    @Override
    public Appointment schedule(UUID patientId, UUID clinicianId, String dateTime) {
        return schedule(
                patientId,
                clinicianId,
                dateTime,
                null,
                null,
                true,
                true,
                false,
                null,
                patientId,
                "PATIENT"
        );
    }

    @Override
    public Appointment schedule(UUID patientId,
                                UUID clinicianId,
                                String dateTime,
                                String reasonForVisit,
                                String roomId) {
        return schedule(
                patientId,
                clinicianId,
                dateTime,
                reasonForVisit,
                roomId,
                true,
                true,
                false,
                null,
                patientId,
                "PATIENT"
        );
    }

    @Override
    public Appointment schedule(UUID patientId,
                                UUID clinicianId,
                                String dateTime,
                                String reasonForVisit,
                                String roomId,
                                boolean referralsMet,
                                boolean priorVisitsMet,
                                boolean overrideMissingPrerequisites,
                                String overrideReason,
                                UUID actorUserId,
                                String actorRole) {
        authorizeScheduling(actorUserId, actorRole, patientId);

        LocalDateTime scheduledAt = LocalDateTime.parse(dateTime);
        if (!repo.isClinicianSlotAvailable(clinicianId, scheduledAt, roomId)) {
            throw new RuntimeException("Requested slot is unavailable. Choose alternative slot or join waitlist.");
        }
        if (repo.hasPatientConflict(patientId, scheduledAt)) {
            throw new RuntimeException("Patient already has an appointment at this time.");
        }

        boolean prerequisitesMet = referralsMet && priorVisitsMet;
        String normalizedOverrideReason = normalizeReason(overrideReason);
        if (!prerequisitesMet) {
            if (!overrideMissingPrerequisites) {
                throw new RuntimeException("Missing prerequisites: referral and/or prior-visit validation failed.");
            }
            if (normalizedOverrideReason == null) {
                throw new RuntimeException("Override reason is required when prerequisites are not met.");
            }
        }

        Appointment appt = new Appointment(UUID.randomUUID(), patientId, clinicianId,
                scheduledAt, reasonForVisit, roomId, referralsMet, priorVisitsMet, normalizedOverrideReason);
        repo.save(appt);
        auditLog.log("SCHEDULE_APPOINTMENT:" + appt.getAppointmentId(), actorUserId, appt.getAppointmentId());

        if (!prerequisitesMet && overrideMissingPrerequisites) {
            auditLog.log("SCHEDULE_OVERRIDE:" + appt.getAppointmentId() + ":" + normalizedOverrideReason,
                    actorUserId, appt.getAppointmentId());
        }

        // Command pattern: wrap notification as a command (email)
        NotifyCommand cmd = new NotifyCommand(notifSvc,
                "Appointment scheduled for " + dateTime + " (ID: " + appt.getAppointmentId() + ")", patientId.toString());
        cmd.execute();
        // SMS confirmation without PHI
        notifSvc.sendSMS(patientId.toString(), "Appointment confirmed for " + dateTime + ".");

        return appt;
    }

    @Override
    public void addToWaitlist(UUID patientId,
                              UUID clinicianId,
                              String dateTime,
                              String reasonForVisit,
                              String roomId) {
        if (waitlistRepo == null) {
            throw new RuntimeException("Waitlist repository is not configured.");
        }
        LocalDateTime requestedSlot = LocalDateTime.parse(dateTime);
        WaitlistEntry entry = new WaitlistEntry(
                UUID.randomUUID(),
                patientId,
                clinicianId,
                LocalDateTime.now(),
                requestedSlot,
                roomId,
                reasonForVisit
        );
        waitlistRepo.save(entry);
        auditLog.log("JOIN_WAITLIST:" + entry.getWaitlistId(), patientId, null);

        String msg = "Waitlist request received for " + dateTime + ". We'll notify when a slot opens.";
        notifSvc.sendEmail(patientId.toString(), msg);
        notifSvc.sendSMS(patientId.toString(), msg);
    }

    @Override
    public void cancel(UUID id) {
        Appointment appt = repo.findById(id)
            .orElseThrow(() -> new RuntimeException("Appointment not found: " + id));

        // Command pattern: wrap cancellation as a command
        CancelAppointmentCommand cancelCmd = new CancelAppointmentCommand(repo, id);
        cancelCmd.execute();

        auditLog.log("CANCEL_APPOINTMENT:" + id, appt.getPatientId(), id);

        NotifyCommand notifyCmd = new NotifyCommand(notifSvc,
            "Appointment canceled for " + appt.getScheduledAt(), appt.getPatientId().toString());
        notifyCmd.execute();
    }

    @Override
    public void cancelByPatient(UUID appointmentId, UUID patientId) {
        Appointment appt = repo.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found: " + appointmentId));
        if (!appt.getPatientId().equals(patientId)) {
            throw new RuntimeException("Patients can cancel only their own appointments.");
        }
        appt.cancel();
        repo.update(appt);
        auditLog.log("CANCEL_APPOINTMENT_BY_PATIENT:" + appointmentId, patientId, appointmentId);

        NotifyCommand notifyCmd = new NotifyCommand(notifSvc,
                "Appointment canceled for " + appt.getScheduledAt(), appt.getPatientId().toString());
        notifyCmd.execute();
    }

    @Override
    public List<Appointment> getAvailableSlots() {
        // Simplified: return empty list (real impl queries clinician schedules)
        return new ArrayList<>();
    }

    private void authorizeScheduling(UUID actorUserId, String actorRole, UUID patientId) {
        if (actorRole == null || actorRole.isBlank()) {
            return;
        }

        switch (actorRole) {
            case "CLINIC_ADMIN":
            case "RECEPTIONIST":
                return;
            case "PATIENT":
                throw new SecurityException("Patients are not authorized to schedule appointments.");
            default:
                throw new SecurityException("Access denied: role is not authorized for scheduling.");
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null) {
            return null;
        }
        String trimmed = reason.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
