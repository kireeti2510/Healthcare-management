package com.healthcare.service;

import com.healthcare.model.Appointment;
import java.util.List;
import java.util.UUID;

public interface IAppointmentService {
    Appointment schedule(UUID patientId, UUID clinicianId, String dateTime);
    void cancel(UUID id);
    void cancelByPatient(UUID appointmentId, UUID patientId);
    List<Appointment> getAvailableSlots();
}
