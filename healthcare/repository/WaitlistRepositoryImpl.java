package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.WaitlistEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaitlistRepositoryImpl implements IWaitlistRepository {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(WaitlistEntry entry) {
        String sql = """
                INSERT INTO appointment_waitlist(waitlist_id, patient_id, clinician_id, requested_at, requested_slot, room_id, reason_for_visit)
                VALUES(?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entry.getWaitlistId().toString());
            ps.setString(2, entry.getPatientId().toString());
            ps.setString(3, entry.getClinicianId().toString());
            ps.setString(4, entry.getRequestedAt().toString());
            ps.setString(5, entry.getRequestedSlot().toString());
            ps.setString(6, entry.getRoomId());
            ps.setString(7, entry.getReasonForVisit());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<WaitlistEntry> findByPatient(UUID patientId) {
        List<WaitlistEntry> entries = new ArrayList<>();
        String sql = "SELECT * FROM appointment_waitlist WHERE patient_id=? ORDER BY requested_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                entries.add(new WaitlistEntry(
                        UUID.fromString(rs.getString("waitlist_id")),
                        UUID.fromString(rs.getString("patient_id")),
                        UUID.fromString(rs.getString("clinician_id")),
                        LocalDateTime.parse(rs.getString("requested_at")),
                        LocalDateTime.parse(rs.getString("requested_slot")),
                        rs.getString("room_id"),
                        rs.getString("reason_for_visit")
                ));
            }
            return entries;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
