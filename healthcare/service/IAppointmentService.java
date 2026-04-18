package com.healthcare.service;

import com.healthcare.model.Appointment;
import java.util.List;
import java.util.UUID;

public interface IAppointmentService {
    Appointment schedule(UUID patientId, UUID clinicianId, String dateTime);
    Appointment schedule(UUID patientId, UUID clinicianId, String dateTime, String reasonForVisit, String roomId);
    Appointment schedule(UUID patientId,
                         UUID clinicianId,
                         String dateTime,
                         String reasonForVisit,
                         String roomId,
                         boolean referralsMet,
                         boolean priorVisitsMet,
                         boolean overrideMissingPrerequisites,
                         String overrideReason,
                         UUID actorUserId,
                         String actorRole);
    void addToWaitlist(UUID patientId, UUID clinicianId, String dateTime, String reasonForVisit, String roomId);
    void cancel(UUID id);
    void cancelByPatient(UUID appointmentId, UUID patientId);
    List<Appointment> getAvailableSlots();
}
