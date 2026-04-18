package com.healthcare.model;

import com.healthcare.model.enums.Role;
import com.healthcare.util.PasswordUtil;
import java.util.List;
import java.util.UUID;

public class Receptionist extends User {
    private String staffId;
    private String shift;

    public Receptionist(UUID userId, String email, String passwordHash, String staffId, String shift) {
        super(userId, email, passwordHash, Role.RECEPTIONIST);
        this.staffId = staffId;
        this.shift = shift;
    }

    @Override
    public boolean authenticate(String password) { return PasswordUtil.matches(password, passwordHash); }

    @Override
    public boolean hasPermission(String action) {
        return List.of("REGISTER_PATIENT", "SCHEDULE_APPOINTMENT", "SEARCH_PATIENT",
                "RESCHEDULE_APPOINTMENT", "CANCEL_APPOINTMENT").contains(action);
    }

    public String getStaffId() { return staffId; }
    public String getShift() { return shift; }
}
