package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.*;
import com.healthcare.model.enums.RxStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

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

        if (rx.getItems() != null && !rx.getItems().isEmpty()) {
            replaceItems(rx.getRxId(), rx.getItems());
        }
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
                rx.setItems(findItems(rxId));
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

    @Override
    public void addItem(UUID rxId, PrescriptionItem item) {
        upsertDrug(item.getDrug());
        String sql = "INSERT INTO prescription_items(rx_id,drug_code,dosage,frequency,duration_days) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rxId.toString());
            ps.setString(2, item.getDrug().getDrugCode());
            ps.setString(3, item.getDosage());
            ps.setString(4, item.getFrequency());
            ps.setInt(5, item.getDurationDays());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void replaceItems(UUID rxId, List<PrescriptionItem> items) {
        String deleteSql = "DELETE FROM prescription_items WHERE rx_id=?";
        try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setString(1, rxId.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (PrescriptionItem item : items) {
            addItem(rxId, item);
        }
    }

    @Override
    public List<PrescriptionItem> findItems(UUID rxId) {
        String sql = """
                SELECT pi.drug_code, pi.dosage, pi.frequency, pi.duration_days,
                       d.name AS drug_name, d.contraindications
                FROM prescription_items pi
                LEFT JOIN drugs d ON d.drug_code = pi.drug_code
                WHERE pi.rx_id = ?
                ORDER BY pi.id ASC
                """;

        List<PrescriptionItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rxId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String drugCode = rs.getString("drug_code");
                String drugName = rs.getString("drug_name");
                String contra = rs.getString("contraindications");

                Drug drug = new Drug(
                        drugCode,
                        (drugName == null || drugName.isBlank()) ? drugCode : drugName,
                        parseContraindications(contra)
                );
                PrescriptionItem item = new PrescriptionItem(
                        drug,
                        rs.getString("dosage"),
                        rs.getString("frequency"),
                        rs.getInt("duration_days")
                );
                items.add(item);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return items;
    }

    private void upsertDrug(Drug drug) {
        String sql = "MERGE INTO drugs(drug_code,name,contraindications) KEY(drug_code) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, drug.getDrugCode());
            ps.setString(2, drug.getName());
            ps.setString(3, serializeContraindications(drug.getContraindications()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeContraindications(List<String> contraindications) {
        if (contraindications == null || contraindications.isEmpty()) {
            return null;
        }
        return contraindications.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));
    }

    private List<String> parseContraindications(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
