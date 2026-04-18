package com.healthcare.pattern.behavioral;

import com.healthcare.model.Patient;

public class AllergySearchExpression implements SearchExpression {
    private final String allergen;
    public AllergySearchExpression(String allergen) { this.allergen = allergen.toLowerCase(); }

    @Override
    public boolean interpret(Patient patient) {
        return patient.getAllergies().stream().anyMatch(a -> a.toLowerCase().contains(allergen));
    }
}
