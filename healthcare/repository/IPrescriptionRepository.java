package com.healthcare.repository;

import com.healthcare.model.Prescription;
import java.util.Optional;
import java.util.UUID;

public interface IPrescriptionRepository {
    void save(Prescription rx);
    Optional<Prescription> findById(UUID rxId);
    void update(Prescription rx);
}
