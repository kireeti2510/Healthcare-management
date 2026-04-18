package com.healthcare.model;

public class PrescriptionItem {
    private Drug drug;
    private String dosage;
    private String frequency;
    private int durationDays;

    public PrescriptionItem(Drug drug, String dosage, String frequency, int durationDays) {
        this.drug = drug;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
    }

    public boolean validate() {
        return drug != null && dosage != null && !dosage.isEmpty()
                && frequency != null && durationDays > 0;
    }

    public Drug getDrug() { return drug; }
    public String getDosage() { return dosage; }
    public String getFrequency() { return frequency; }
    public int getDurationDays() { return durationDays; }
}
