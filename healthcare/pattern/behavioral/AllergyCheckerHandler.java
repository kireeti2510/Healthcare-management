package com.healthcare.pattern.behavioral;

import com.healthcare.model.ConflictResult;
import com.healthcare.model.PrescriptionItem;
import com.healthcare.model.Prescription;

import java.util.ArrayList;
import java.util.List;

public class AllergyCheckerHandler extends ConflictHandler {
    // Simulated patient allergies lookup — in real use, inject PatientService
    private final List<String> patientAllergies = List.of("penicillin", "sulfa");

    public AllergyCheckerHandler(ConflictHandler next) { super(next); }

    @Override
    public ConflictResult check(Prescription rx) {
        List<String> conflicts = new ArrayList<>();
        for (PrescriptionItem item : rx.getItems()) {
            for (String ci : item.getDrug().getContraindications()) {
                if (patientAllergies.stream().anyMatch(a -> a.equalsIgnoreCase(ci))) {
                    conflicts.add("Allergy conflict: " + item.getDrug().getName() + " contains " + ci);
                }
            }
        }
        if (!conflicts.isEmpty()) return new ConflictResult(true, conflicts, "HIGH");
        return passToNext(rx);
    }
}
