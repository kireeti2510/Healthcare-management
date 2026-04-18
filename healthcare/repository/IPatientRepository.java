package com.healthcare.repository;

import com.healthcare.model.Patient;
import java.util.Optional;
import java.util.UUID;

public interface IPatientRepository {
    void saveJp(Patient p);
    Optional<Patient> findById(UUID id);
    Optional<Patient> findByEmail(String email);
    void update(Patient p);
}
