package com.healthcare.model.dto;

import java.time.LocalDate;
import java.util.List;

public class PatientDTO {
    public String email;
    public String password;
    public LocalDate dob;
    public String insuranceId;
    public List<String> allergies;

    public PatientDTO(String email, String password, LocalDate dob, String insuranceId, List<String> allergies) {
        this.email = email; this.password = password;
        this.dob = dob; this.insuranceId = insuranceId; this.allergies = allergies;
    }
}
