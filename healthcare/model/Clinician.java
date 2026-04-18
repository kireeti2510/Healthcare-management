package com.healthcare.model;

import com.healthcare.model.enums.Role;
import com.healthcare.util.PasswordUtil;
import java.util.List;
import java.util.UUID;

public class Clinician extends User {
    private String licenceNo;
    private String speciality;

    public Clinician(UUID userId, String email, String passwordHash, String licenceNo, String speciality) {
        super(userId, email, passwordHash, Role.CLINICIAN);
        this.licenceNo = licenceNo;
        this.speciality = speciality;
    }

    @Override
    public boolean authenticate(String password) { return PasswordUtil.matches(password, passwordHash); }

    @Override
    public boolean hasPermission(String action) {
        return List.of("RECORD_ENCOUNTER", "CREATE_PRESCRIPTION", "ISSUE_PRESCRIPTION",
                "VIEW_PATIENT_HISTORY", "GENERATE_REPORTS").contains(action);
    }

    public String getLicenceNo() { return licenceNo; }
    public String getSpeciality() { return speciality; }
}
