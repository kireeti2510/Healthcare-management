package com.healthcare.controller;

import com.healthcare.model.Patient;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.service.IPatientService;

import java.util.UUID;

/**
 * MVC Controller - Patients
 * Member: Kireeti Reddy P (PES1UG23CS307)
 */
public class PatientController {
    private final IPatientService service;

    public PatientController(IPatientService service) { this.service = service; }

    public Patient register(PatientDTO dto) { return service.register(dto); }

    public Patient getPatient(UUID id) { return service.findById(id); }

    public void updateProfile(Patient p) { service.updateProfile(p); }
}
