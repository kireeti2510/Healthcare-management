package com.healthcare.model;

import java.util.List;

public class ConflictResult {
    private boolean hasConflict;
    private List<String> conflicts;
    private String severity;

    public ConflictResult(boolean hasConflict, List<String> conflicts, String severity) {
        this.hasConflict = hasConflict;
        this.conflicts = conflicts;
        this.severity = severity;
    }

    public boolean isHasConflict() { return hasConflict; }
    public List<String> getConflicts() { return conflicts; }
    public String getSeverity() { return severity; }
}
