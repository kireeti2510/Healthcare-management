package com.healthcare.repository;

import com.healthcare.model.Appointment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IApptRepository {
    void save(Appointment a);
    Optional<Appointment> findById(UUID id);
    List<Appointment> findByPatient(UUID patientId);
    void update(Appointment a);
    void delete(UUID id);
}
