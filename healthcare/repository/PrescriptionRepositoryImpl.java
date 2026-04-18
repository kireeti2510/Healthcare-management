package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.*;
import com.healthcare.model.enums.RxStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PrescriptionRepositoryImpl implements IPrescriptionRepository {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(Prescription rx) {
        String sql = "INSERT INTO prescriptions(rx_id,appointment_id,status,override_reason) VALUES(?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rx.getRxId().toString());
            ps.setString(2, rx.getAppointmentId().toString());
            ps.setString(3, rx.getStatus().name());
            ps.setString(4, rx.getOverrideReason());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Prescription> findById(UUID rxId) {
        String sql = "SELECT * FROM prescriptions WHERE rx_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rxId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Prescription rx = new Prescription(
                        UUID.fromString(rs.getString("rx_id")),
                        UUID.fromString(rs.getString("appointment_id"))
                );
                String status = rs.getString("status");
                if (status != null) {
                    rx.setStatus(RxStatus.valueOf(status));
                }
                String issuedAt = rs.getString("issued_at");
                if (issuedAt != null) {
                    rx.setIssuedAt(LocalDateTime.parse(issuedAt));
                }
                rx.setOverrideReason(rs.getString("override_reason"));
                return Optional.of(rx);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    @Override
    public void update(Prescription rx) {
        String sql = "UPDATE prescriptions SET status=?, issued_at=?, override_reason=? WHERE rx_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rx.getStatus().name());
            ps.setString(2, rx.getIssuedAt() != null ? rx.getIssuedAt().toString() : null);
            ps.setString(3, rx.getOverrideReason());
            ps.setString(4, rx.getRxId().toString());
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
