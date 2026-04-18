package com.healthcare.repository;

import com.healthcare.model.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IApptRepository {
    void save(Appointment a);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByPatient(UUID patientId);
    boolean isClinicianSlotAvailable(UUID clinicianId, LocalDateTime scheduledAt, String roomId);
    boolean hasPatientConflict(UUID patientId, LocalDateTime scheduledAt);
    void update(Appointment a);
    void delete(UUID id);
}
