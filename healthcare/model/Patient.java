package com.healthcare.model;

import com.healthcare.model.enums.Role;
import com.healthcare.util.PasswordUtil;
import java.time.LocalDate;
import java.util.*;

public class Patient extends User {
    private LocalDate dob;
    private List<String> allergies;
    private String insuranceId;
    private List<MedicalRecord> history = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();

    // Private constructor - use Builder
    private Patient() { super(UUID.randomUUID(), null, null, Role.PATIENT); }

    public Patient(UUID userId, String email, String passwordHash,
                   LocalDate dob, List<String> allergies, String insuranceId) {
        super(userId, email, passwordHash, Role.PATIENT);
        this.dob = dob;
        this.allergies = allergies != null ? new ArrayList<>(allergies) : new ArrayList<>();
        this.insuranceId = insuranceId;
    }

    @Override
    public boolean authenticate(String password) {
        return PasswordUtil.matches(password, passwordHash);
    }

    @Override
    public boolean hasPermission(String action) {
        return List.of("VIEW_OWN_APPOINTMENTS", "VIEW_MEDICAL_HISTORY", "MANAGE_DOCUMENTS", "CANCEL_APPOINTMENT")
                .contains(action);
    }

    public List<MedicalRecord> viewHistory() { return Collections.unmodifiableList(history); }
    public List<Appointment> viewAppointments() { return Collections.unmodifiableList(appointments); }
    public void addMedicalRecord(MedicalRecord r) { history.add(r); }
    public void addAppointment(Appointment a) { appointments.add(a); }

    public LocalDate getDob() { return dob; }
    public List<String> getAllergies() { return allergies; }
    public String getInsuranceId() { return insuranceId; }

    // ---- Builder (Creational - Builder Pattern) ----
    public static class Builder {
        private UUID userId = UUID.randomUUID();
        private String email, passwordHash, insuranceId;
        private LocalDate dob;
        private List<String> allergies = new ArrayList<>();

        public Builder userId(UUID id) { this.userId = id; return this; }
        public Builder email(String e) { this.email = e; return this; }
        public Builder passwordHash(String p) { this.passwordHash = p; return this; }
        public Builder dob(LocalDate d) { this.dob = d; return this; }
        public Builder allergies(List<String> a) {
            this.allergies = a != null ? new ArrayList<>(a) : new ArrayList<>();
            return this;
        }
        public Builder insuranceId(String i) { this.insuranceId = i; return this; }
        public Patient build() {
            return new Patient(userId, email, passwordHash, dob, allergies, insuranceId);
        }
    }
}
