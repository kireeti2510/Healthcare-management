package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.Appointment;
import com.healthcare.model.enums.AppointmentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class ApptRepositoryImpl implements IApptRepository {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Appointment a) {
        String sql = "INSERT INTO appointments(appointment_id,patient_id,clinician_id,scheduled_at,status) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAppointmentId().toString());
            ps.setString(2, a.getPatientId().toString());
            ps.setString(3, a.getClinicianId().toString());
            ps.setString(4, a.getScheduledAt().toString());
            ps.setString(5, a.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Appointment> findById(UUID id) {
        String sql = "SELECT * FROM appointments WHERE appointment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findByPatient(UUID patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    @Override
    public void update(Appointment a) {
        String sql = "UPDATE appointments SET status=?, scheduled_at=? WHERE appointment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getStatus().name());
            ps.setString(2, a.getScheduledAt().toString());
            ps.setString(3, a.getAppointmentId().toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void delete(UUID id) {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM appointments WHERE appointment_id=?")) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment(
                UUID.fromString(rs.getString("appointment_id")),
                UUID.fromString(rs.getString("patient_id")),
                UUID.fromString(rs.getString("clinician_id")),
                LocalDateTime.parse(rs.getString("scheduled_at"))
        );
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        return a;
    }
}
