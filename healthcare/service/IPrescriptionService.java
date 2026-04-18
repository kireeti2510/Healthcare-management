package com.healthcare.service;

import com.healthcare.model.*;
import java.util.UUID;

public interface IPrescriptionService {
    Prescription createRx(Appointment appointment);
    void issueRx(UUID rxId);
    void issueRx(UUID rxId, String pharmacistContact, String overrideReason);
    ConflictResult checkConflicts(UUID rxId);
    ConflictResult reviewRx(UUID rxId, String overrideReason);
    ConflictResult checkConflicts(Prescription rx);
}
