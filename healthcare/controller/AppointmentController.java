package com.healthcare.controller;

import com.healthcare.model.Appointment;
import com.healthcare.service.IAppointmentService;

import java.util.List;
import java.util.UUID;

/**
 * MVC Controller - Appointments
 * Member: Jayanth Reddy (PES1UG23CS264)
 */
public class AppointmentController {
    private final IAppointmentService service;

    public AppointmentController(IAppointmentService service) { this.service = service; }

    public Appointment scheduleAppointment(UUID patientId, UUID clinicianId, String dateTime) {
        return service.schedule(patientId, clinicianId, dateTime);
    }

    public Appointment scheduleAppointment(UUID patientId,
                                           UUID clinicianId,
                                           String dateTime,
                                           String reasonForVisit,
                                           String roomId) {
        return service.schedule(patientId, clinicianId, dateTime, reasonForVisit, roomId);
    }

    public Appointment scheduleAppointment(UUID patientId,
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
        return service.schedule(
                patientId,
                clinicianId,
                dateTime,
                reasonForVisit,
                roomId,
                referralsMet,
                priorVisitsMet,
                overrideMissingPrerequisites,
                overrideReason,
                actorUserId,
                actorRole
        );
    }

    public void joinWaitlist(UUID patientId,
                             UUID clinicianId,
                             String dateTime,
                             String reasonForVisit,
                             String roomId) {
        service.addToWaitlist(patientId, clinicianId, dateTime, reasonForVisit, roomId);
    }

    public void cancelAppointment(UUID appointmentId) {
        service.cancel(appointmentId);
    }

    public void cancelAppointmentByPatient(UUID patientId, UUID appointmentId) {
        service.cancelByPatient(appointmentId, patientId);
    }

    public List<Appointment> getAvailableSlots() {
        return service.getAvailableSlots();
    }
}
