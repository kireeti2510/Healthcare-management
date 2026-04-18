package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.Patient;
import com.healthcare.model.enums.Role;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class PatientRepositoryImpl implements IPatientRepository {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void saveJp(Patient p) {
        try {
            String userSql = "MERGE INTO users(user_id,email,password_hash,role) KEY(user_id) VALUES(?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(userSql)) {
                ps.setString(1, p.getUserId().toString());
                ps.setString(2, p.getEmail());
                ps.setString(3, p.getPasswordHash());
                ps.setString(4, Role.PATIENT.name());
                ps.executeUpdate();
            }
            String patientSql = "MERGE INTO patients(user_id,dob,insurance_id,allergies) KEY(user_id) VALUES(?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(patientSql)) {
                ps.setString(1, p.getUserId().toString());
                ps.setString(2, p.getDob() != null ? p.getDob().toString() : null);
                ps.setString(3, p.getInsuranceId());
                ps.setString(4, String.join(",", p.getAllergies()));
                ps.executeUpdate();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Patient> findById(UUID id) {
        String sql = "SELECT u.*, p.dob, p.insurance_id, p.allergies FROM users u JOIN patients p ON u.user_id=p.user_id WHERE u.user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByEmail(String email) {
        String sql = "SELECT u.*, p.dob, p.insurance_id, p.allergies FROM users u JOIN patients p ON u.user_id=p.user_id WHERE u.email=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public void update(Patient p) {
        String sql = "UPDATE patients SET dob=?, insurance_id=?, allergies=? WHERE user_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getDob() != null ? p.getDob().toString() : null);
            ps.setString(2, p.getInsuranceId());
            ps.setString(3, String.join(",", p.getAllergies()));
            ps.setString(4, p.getUserId().toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Patient mapRow(ResultSet rs) throws SQLException {
        String allergiesStr = rs.getString("allergies");
        List<String> allergies = (allergiesStr != null && !allergiesStr.isEmpty())
                ? Arrays.asList(allergiesStr.split(",")) : new ArrayList<>();
        return new Patient.Builder()
            .userId(UUID.fromString(rs.getString("user_id")))
                .email(rs.getString("email"))
                .passwordHash(rs.getString("password_hash"))
                .dob(rs.getString("dob") != null ? LocalDate.parse(rs.getString("dob")) : null)
                .insuranceId(rs.getString("insurance_id"))
                .allergies(allergies)
                .build();
    }
}
