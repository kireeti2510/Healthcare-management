package com.healthcare.pattern.creational;

import com.healthcare.model.Prescription;
import com.healthcare.model.PrescriptionItem;

import java.util.UUID;

/**
 * PROTOTYPE PATTERN (Creational)
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 * Clone a prescription template (e.g. repeat prescription) instead of building from scratch.
 */
public class PrescriptionPrototype {
    private Prescription template;

    public PrescriptionPrototype(Prescription template) {
        this.template = template;
    }

    /** Creates a fresh DRAFT prescription copying items from the template */
    public Prescription cloneForAppointment(UUID newAppointmentId) {
        Prescription clone = new Prescription(UUID.randomUUID(), newAppointmentId);
        for (PrescriptionItem item : template.getItems()) {
            clone.addItem(new PrescriptionItem(item.getDrug(), item.getDosage(),
                    item.getFrequency(), item.getDurationDays()));
        }
        return clone;
    }
}
