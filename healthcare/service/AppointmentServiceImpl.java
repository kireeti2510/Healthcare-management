package com.healthcare.service;

import com.healthcare.model.Appointment;
import com.healthcare.pattern.behavioral.CancelAppointmentCommand;
import com.healthcare.pattern.behavioral.NotifyCommand;
import com.healthcare.repository.IApptRepository;
import com.healthcare.repository.IAuditLogService;

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

    public AppointmentServiceImpl(IApptRepository repo, IAuditLogService auditLog, INotificationService notifSvc) {
        this.repo = repo;
        this.auditLog = auditLog;
        this.notifSvc = notifSvc;
    }

    @Override
    public Appointment schedule(UUID patientId, UUID clinicianId, String dateTime) {
        Appointment appt = new Appointment(UUID.randomUUID(), patientId, clinicianId,
                LocalDateTime.parse(dateTime));
        repo.save(appt);
        auditLog.log("SCHEDULE_APPOINTMENT:" + appt.getAppointmentId(), patientId);

        // Command pattern: wrap notification as a command
        NotifyCommand cmd = new NotifyCommand(notifSvc,
                "Appointment scheduled for " + dateTime, patientId.toString());
        cmd.execute();

        return appt;
    }

    @Override
    public void cancel(UUID id) {
        Appointment appt = repo.findById(id);
        if (appt == null) throw new RuntimeException("Appointment not found: " + id);

        // Command pattern: wrap cancellation as a command
        CancelAppointmentCommand cancelCmd = new CancelAppointmentCommand(repo, id);
        cancelCmd.execute();

        auditLog.log("CANCEL_APPOINTMENT:" + id, appt.getPatientId());

        NotifyCommand notifyCmd = new NotifyCommand(notifSvc,
            "Appointment canceled for " + appt.getScheduledAt(), appt.getPatientId().toString());
        notifyCmd.execute();
    }

    @Override
    public List<Appointment> getAvailableSlots() {
        // Simplified: return empty list (real impl queries clinician schedules)
        return new ArrayList<>();
    }
}
