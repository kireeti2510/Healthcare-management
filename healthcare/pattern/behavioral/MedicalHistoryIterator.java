package com.healthcare.pattern.behavioral;

import com.healthcare.model.MedicalRecord;
import java.util.Iterator;
import java.util.List;

/**
 * ITERATOR PATTERN (Behavioral)
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 * Iterates over a patient's medical records without exposing the underlying list.
 */
public class MedicalHistoryIterator implements Iterator<MedicalRecord> {
    private final List<MedicalRecord> records;
    private int index = 0;

    public MedicalHistoryIterator(List<MedicalRecord> records) { this.records = records; }

    @Override
    public boolean hasNext() { return index < records.size(); }

    @Override
    public MedicalRecord next() {
        if (!hasNext()) throw new java.util.NoSuchElementException();
        return records.get(index++);
    }
}
