package com.healthcare.repository;

import java.util.List;
import java.util.UUID;

public interface IAuditLogService {
    void log(String action, UUID userId);
    List<String> getLog(UUID userId);
}
