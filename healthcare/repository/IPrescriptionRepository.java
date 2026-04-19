package com.healthcare.repository;

import com.healthcare.model.Prescription;
import com.healthcare.model.PrescriptionItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPrescriptionRepository {
    void save(Prescription rx);
    Optional<Prescription> findById(UUID rxId);
    void update(Prescription rx);
    void addItem(UUID rxId, PrescriptionItem item);
    void replaceItems(UUID rxId, List<PrescriptionItem> items);
    List<PrescriptionItem> findItems(UUID rxId);
}
