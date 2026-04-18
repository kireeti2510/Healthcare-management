package com.healthcare.controller;

import com.healthcare.service.ClinicAdminServiceImpl;
import java.util.List;
import java.util.UUID;

/**
 * MVC Controller - Admin
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 */
public class AdminController {
    private final ClinicAdminServiceImpl adminService;

    public AdminController(ClinicAdminServiceImpl adminService) { this.adminService = adminService; }

    public List<String> getAuditLogs(UUID userId) { return adminService.reviewAuditLogs(userId); }

    public void manageUsers() { adminService.manageUsers(); }

    public void backupAndRestore() { adminService.backupAndRestore(); }
}
