package com.healthcare.pattern.behavioral;

import com.healthcare.model.ConflictResult;
import com.healthcare.model.Prescription;

import java.util.List;

public class DrugInteractionHandler extends ConflictHandler {
    public DrugInteractionHandler(ConflictHandler next) { super(next); }

    @Override
    public ConflictResult check(Prescription rx) {
        // Simplified: flag if more than 5 items as potential interaction risk
        if (rx.getItems().size() > 5)
            return new ConflictResult(true, List.of("High polypharmacy risk"), "MEDIUM");
        return passToNext(rx);
    }
}
