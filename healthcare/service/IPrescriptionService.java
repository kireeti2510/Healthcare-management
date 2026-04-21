package com.healthcare.service;

import com.healthcare.model.*;

import java.util.List;
import java.util.UUID;

public interface IPrescriptionService {
    Prescription createRx(Appointment appointment);
    Prescription createRx(Appointment appointment, List<PrescriptionItem> items);
    void issueRx(UUID rxId);
    void issueRx(UUID rxId, String pharmacistContact, String overrideReason);
    void dispenseRx(UUID rxId);
    void voidRx(UUID rxId, String reason);
    ConflictResult checkConflicts(UUID rxId);
    ConflictResult reviewRx(UUID rxId, String overrideReason);
    ConflictResult checkConflicts(Prescription rx);
    void addRxItem(UUID rxId, PrescriptionItem item);
    void reviseRxItems(UUID rxId, List<PrescriptionItem> items);
    Prescription getRx(UUID rxId);
}
