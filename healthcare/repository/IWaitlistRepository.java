package com.healthcare.repository;

import com.healthcare.model.WaitlistEntry;

import java.util.List;
import java.util.UUID;

public interface IWaitlistRepository {
    void save(WaitlistEntry entry);
    List<WaitlistEntry> findByPatient(UUID patientId);
}
