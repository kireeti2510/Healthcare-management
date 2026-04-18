package com.healthcare.pattern.behavioral;

import com.healthcare.model.Patient;

public class EmailSearchExpression implements SearchExpression {
    private final String emailQuery;
    public EmailSearchExpression(String emailQuery) { this.emailQuery = emailQuery.toLowerCase(); }

    @Override
    public boolean interpret(Patient patient) {
        return patient.getEmail() != null && patient.getEmail().toLowerCase().contains(emailQuery);
    }
}
