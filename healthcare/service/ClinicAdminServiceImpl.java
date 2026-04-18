package com.healthcare.service;

import com.healthcare.repository.IAuditLogService;
import java.util.List;
import java.util.UUID;

/**
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 */
public class ClinicAdminServiceImpl {
    private final IAuditLogService auditLog;
    private final INotificationService notifSvc;

    public ClinicAdminServiceImpl(IAuditLogService auditLog, INotificationService notifSvc) {
        this.auditLog = auditLog;
        this.notifSvc = notifSvc;
    }

    public void manageUsers() { System.out.println("[Admin] Managing users..."); }

    public void configureSettings() { System.out.println("[Admin] Configuring settings..."); }

    public List<String> reviewAuditLogs(UUID userId) { return auditLog.getLog(userId); }

    public void backupAndRestore() { System.out.println("[Admin] Backup and restore initiated."); }
}
