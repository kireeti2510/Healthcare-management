package com.healthcare.pattern.behavioral;

import com.healthcare.model.Patient;

/**
 * INTERPRETER PATTERN (Behavioral)
 * Member: Karthikeya Thotamsetty (PES1UG23CS287)
 * Simple search DSL: "email:john@x.com" or "allergy:penicillin"
 */
public interface SearchExpression {
    boolean interpret(Patient patient);
}
