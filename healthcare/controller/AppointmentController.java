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

    public void cancelAppointment(UUID appointmentId) {
        service.cancel(appointmentId);
    }

    public List<Appointment> getAvailableSlots() {
        return service.getAvailableSlots();
    }
}
