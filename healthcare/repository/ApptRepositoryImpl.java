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
        String sql = "INSERT INTO appointments(appointment_id,patient_id,clinician_id,scheduled_at,status,reason_for_visit,room_id,referrals_met,prior_visits_met,override_reason) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getAppointmentId().toString());
            ps.setString(2, a.getPatientId().toString());
            ps.setString(3, a.getClinicianId().toString());
            ps.setString(4, a.getScheduledAt().toString());
            ps.setString(5, a.getStatus().name());
            ps.setString(6, a.getReasonForVisit());
            ps.setString(7, a.getRoomId());
            if (a.getReferralsMet() == null) ps.setNull(8, Types.BOOLEAN); else ps.setBoolean(8, a.getReferralsMet());
            if (a.getPriorVisitsMet() == null) ps.setNull(9, Types.BOOLEAN); else ps.setBoolean(9, a.getPriorVisitsMet());
            ps.setString(10, a.getOverrideReason());
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
    public boolean isClinicianSlotAvailable(UUID clinicianId, LocalDateTime scheduledAt, String roomId) {
        String sql = """
                SELECT COUNT(*)
                FROM appointments
                WHERE clinician_id=?
                  AND scheduled_at=?
                  AND status IN ('PENDING','CONFIRMED')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clinicianId.toString());
            ps.setString(2, scheduledAt.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (roomId == null || roomId.isBlank()) {
            return true;
        }

        String roomSql = """
                SELECT COUNT(*)
                FROM appointments
                WHERE room_id=?
                  AND scheduled_at=?
                  AND status IN ('PENDING','CONFIRMED')
                """;
        try (PreparedStatement ps = conn.prepareStatement(roomSql)) {
            ps.setString(1, roomId);
            ps.setString(2, scheduledAt.toString());
            ResultSet rs = ps.executeQuery();
            return !(rs.next() && rs.getInt(1) > 0);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasPatientConflict(UUID patientId, LocalDateTime scheduledAt) {
        String sql = """
                SELECT COUNT(*)
                FROM appointments
                WHERE patient_id=?
                  AND scheduled_at=?
                  AND status IN ('PENDING','CONFIRMED')
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.toString());
            ps.setString(2, scheduledAt.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Appointment a) {
        String sql = "UPDATE appointments SET status=?, scheduled_at=?, reason_for_visit=?, room_id=?, referrals_met=?, prior_visits_met=?, override_reason=? WHERE appointment_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getStatus().name());
            ps.setString(2, a.getScheduledAt().toString());
            ps.setString(3, a.getReasonForVisit());
            ps.setString(4, a.getRoomId());
            if (a.getReferralsMet() == null) ps.setNull(5, Types.BOOLEAN); else ps.setBoolean(5, a.getReferralsMet());
            if (a.getPriorVisitsMet() == null) ps.setNull(6, Types.BOOLEAN); else ps.setBoolean(6, a.getPriorVisitsMet());
            ps.setString(7, a.getOverrideReason());
            ps.setString(8, a.getAppointmentId().toString());
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
                LocalDateTime.parse(rs.getString("scheduled_at")),
                rs.getString("reason_for_visit"),
                rs.getString("room_id"),
                parseNullableBoolean(rs, "referrals_met"),
                parseNullableBoolean(rs, "prior_visits_met"),
                rs.getString("override_reason")
        );
        a.setStatus(AppointmentStatus.valueOf(rs.getString("status")));
        return a;
    }

    private Boolean parseNullableBoolean(ResultSet rs, String column) throws SQLException {
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? null : value;
    }
}
