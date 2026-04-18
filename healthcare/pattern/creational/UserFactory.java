package com.healthcare.pattern.creational;

import com.healthcare.model.*;
import com.healthcare.model.enums.Role;

import java.util.UUID;

/**
 * FACTORY PATTERN (Creational)
 * Member: Kireeti Reddy P (PES1UG23CS307)
 * Creates User subclass instances based on Role without exposing constructors to callers.
 */
public class UserFactory {
    public static User createUser(Role role, UUID id, String email, String passwordHash, String extra1, String extra2) {
        return switch (role) {
            case PATIENT      -> new Patient(id, email, passwordHash, null, null, extra1);
            case RECEPTIONIST -> new Receptionist(id, email, passwordHash, extra1, extra2);
            case CLINICIAN    -> new Clinician(id, email, passwordHash, extra1, extra2);
            case PHARMACIST   -> new Pharmacist(id, email, passwordHash, extra1, extra2);
            case CLINIC_ADMIN -> new ClinicAdmin(id, email, passwordHash, extra1);
        };
    }
}
