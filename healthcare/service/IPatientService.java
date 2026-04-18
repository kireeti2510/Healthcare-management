package com.healthcare.service;

import com.healthcare.model.Patient;
import java.util.UUID;

public interface IPatientService {
    Patient register(com.healthcare.model.dto.PatientDTO dto);
    Patient findById(UUID id);
    void updateProfile(Patient p);
}
