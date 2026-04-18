package com.healthcare.pattern.behavioral;

import com.healthcare.model.ConflictResult;
import com.healthcare.model.Prescription;

/**
 * CHAIN OF RESPONSIBILITY (Behavioral)
 * Member: K Sailakshmi Srinivas (PES1UG23CS271)
 */
public abstract class ConflictHandler {
    protected ConflictHandler next;

    public ConflictHandler(ConflictHandler next) { this.next = next; }

    public abstract ConflictResult check(Prescription rx);

    protected ConflictResult passToNext(Prescription rx) {
        if (next != null) return next.check(rx);
        return new ConflictResult(false, java.util.List.of(), "NONE");
    }
}
