package com.healthcare.model;

import com.healthcare.model.enums.Role;
import com.healthcare.util.PasswordUtil;
import java.util.List;
import java.util.UUID;

public class Pharmacist extends User {
    private String regNo;
    private String pharmacy;

    public Pharmacist(UUID userId, String email, String passwordHash, String regNo, String pharmacy) {
        super(userId, email, passwordHash, Role.PHARMACIST);
        this.regNo = regNo;
        this.pharmacy = pharmacy;
    }

    @Override
    public boolean authenticate(String password) { return PasswordUtil.matches(password, passwordHash); }

    @Override
    public boolean hasPermission(String action) {
        return List.of("VIEW_PRESCRIPTION", "DISPENSE_MEDICATION").contains(action);
    }

    public String getRegNo() { return regNo; }
    public String getPharmacy() { return pharmacy; }
}
