package com.healthcare.repository;

import com.healthcare.db.DatabaseConnection;
import com.healthcare.model.MedicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MedicalRecordRepositoryImpl implements IMedicalRecordRepository {
    private final Connection conn = DatabaseConnection.getInstance().getConnection();

    @Override
    public void save(MedicalRecord record) {
        String sql = """
                INSERT INTO medical_records(record_id, patient_id, created_at, updated_at, archived_at, state)
                VALUES(?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getRecordId().toString());
            ps.setString(2, record.getPatientId().toString());
            ps.setString(3, record.getCreatedAt().toString());
            ps.setString(4, record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
            ps.setString(5, record.getArchivedAt() != null ? record.getArchivedAt().toString() : null);
            ps.setString(6, record.getState().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (String note : record.getNotes()) {
            addNote(record.getRecordId(), note);
        }
    }

    @Override
    public Optional<MedicalRecord> findById(UUID recordId) {
        String sql = "SELECT * FROM medical_records WHERE record_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recordId.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            MedicalRecord record = MedicalRecord.fromPersistence(
                    UUID.fromString(rs.getString("record_id")),
                    UUID.fromString(rs.getString("patient_id")),
                    LocalDateTime.parse(rs.getString("created_at")),
                    parseNullableDateTime(rs.getString("updated_at")),
                    parseNullableDateTime(rs.getString("archived_at")),
                    MedicalRecord.State.valueOf(rs.getString("state")),
                    getNotes(recordId)
            );
            return Optional.of(record);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<MedicalRecord> findByPatient(UUID patientId) {
        List<MedicalRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM medical_records WHERE patient_id=? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, patientId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID recordId = UUID.fromString(rs.getString("record_id"));
                records.add(MedicalRecord.fromPersistence(
                        recordId,
                        UUID.fromString(rs.getString("patient_id")),
                        LocalDateTime.parse(rs.getString("created_at")),
                        parseNullableDateTime(rs.getString("updated_at")),
                        parseNullableDateTime(rs.getString("archived_at")),
                        MedicalRecord.State.valueOf(rs.getString("state")),
                        getNotes(recordId)
                ));
            }
            return records;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(MedicalRecord record) {
        String sql = "UPDATE medical_records SET updated_at=?, archived_at=?, state=? WHERE record_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, record.getUpdatedAt() != null ? record.getUpdatedAt().toString() : null);
            ps.setString(2, record.getArchivedAt() != null ? record.getArchivedAt().toString() : null);
            ps.setString(3, record.getState().name());
            ps.setString(4, record.getRecordId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addNote(UUID recordId, String note) {
        String sql = "INSERT INTO medical_record_notes(record_id, note) VALUES(?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recordId.toString());
            ps.setString(2, note);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getNotes(UUID recordId) throws SQLException {
        List<String> notes = new ArrayList<>();
        String sql = "SELECT note FROM medical_record_notes WHERE record_id=? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, recordId.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                notes.add(rs.getString("note"));
            }
        }
        return notes;
    }

    private LocalDateTime parseNullableDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value);
    }
}
