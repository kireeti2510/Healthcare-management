package com.healthcare.pattern.structural;

import com.healthcare.model.Patient;
import com.healthcare.model.User;
import com.healthcare.model.dto.PatientDTO;
import com.healthcare.model.enums.Role;
import com.healthcare.service.IPatientService;

import java.util.UUID;

/**
 * PROXY PATTERN (Structural)
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 * Access control proxy - checks permissions before delegating to real service.
 */
public class PatientServiceProxy implements IPatientService {
    private final IPatientService real;
    private final User currentUser;

    public PatientServiceProxy(IPatientService real, User currentUser) {
        this.real = real;
        this.currentUser = currentUser;
    }

    @Override
    public Patient register(PatientDTO dto) {
        if (currentUser.getRole() != Role.RECEPTIONIST && currentUser.getRole() != Role.CLINIC_ADMIN)
            throw new SecurityException("Only Receptionist or Admin can register patients");
        return real.register(dto);
    }

    @Override
    public Patient findById(UUID id) {
        return real.findById(id);
    }

    @Override
    public void updateProfile(Patient p) {
        if (!currentUser.hasPermission("REGISTER_PATIENT") && !currentUser.getRole().equals(Role.CLINIC_ADMIN))
            throw new SecurityException("No permission to update patient profile");
        real.updateProfile(p);
    }
}
