package com.healthcare.service;

import com.healthcare.model.*;
import java.util.UUID;

public interface IPrescriptionService {
    Prescription createRx(Appointment appointment);
    void issueRx(UUID rxId);
    ConflictResult checkConflicts(Prescription rx);
}
