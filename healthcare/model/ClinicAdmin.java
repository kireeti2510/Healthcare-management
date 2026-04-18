package com.healthcare.model;

import com.healthcare.model.enums.Role;
import com.healthcare.util.PasswordUtil;
import java.util.List;
import java.util.UUID;

public class ClinicAdmin extends User {
    private String adminLevel;

    public ClinicAdmin(UUID userId, String email, String passwordHash, String adminLevel) {
        super(userId, email, passwordHash, Role.CLINIC_ADMIN);
        this.adminLevel = adminLevel;
    }

    @Override
    public boolean authenticate(String password) { return PasswordUtil.matches(password, passwordHash); }

    @Override
    public boolean hasPermission(String action) {
        return List.of("MANAGE_USERS", "CONFIGURE_SETTINGS", "REVIEW_AUDIT_LOGS", "BACKUP_RESTORE").contains(action);
    }

    public String getAdminLevel() { return adminLevel; }
}
